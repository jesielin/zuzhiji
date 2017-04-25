package com.zzj.zuzhiji.util;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.IBinder;
import android.text.TextUtils;
import android.widget.Toast;

import com.yayandroid.theactivitymanager.TheActivityManager;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.UpdateInfo;
import com.zzj.zuzhiji.service.DownloadService;

import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by shawn on 2017-04-25.
 */

public class UpdateHelper implements ServiceConnection {

    private Context mContext;

    private Dialog loadingDialog;
    private Dialog wifiDialog;
    private Dialog infoDialog;
    private ProgressDialog downloadDialog;

    private boolean isManual;

    private UpdateInfo info;

    private DownloadService mService;
    private DownloadService.DownloadListener downloadListener = new DownloadService.DownloadListener() {
        @Override
        public void onStart() {
            showDownloadDialog();
        }

        @Override
        public void onProgress(int progress) {


            if (downloadDialog != null) {

                downloadDialog.setProgress(progress);
            }
        }

        @Override
        public void onComplete() {

            DialogUtils.dismissDialog(downloadDialog);
            unbindService();
        }
    };

    public UpdateHelper(Context context, boolean isManual) {
        mContext = context;
        this.isManual = isManual;
    }

    public void check() {
        Network.getInstance().update()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        loadingDialog = DialogUtils.showProgressDialog(mContext, "正在获取版本信息..."); // 需要在主线程执行
                    }
                })
                .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<UpdateInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Toast.makeText(mContext, e.getMessage(), Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissDialog(loadingDialog);
                    }

                    @Override
                    public void onNext(final UpdateInfo updateInfo) {
                        DialogUtils.dismissDialog(loadingDialog);
                        DebugLog.d("version code:" + getVersionCode());
                        DebugLog.d("remote version code:" + updateInfo.version_code);

                        info = updateInfo;

                        resolveUpdateInfo();

                    }
                });
    }

    private void resolveUpdateInfo() {

        switch (resolveVersionCode()) {
            case Constant.UPDATE.NO_UPDATE:
                showNoUpdate();
                break;
            case Constant.UPDATE.NEED_UPDATE:
                showNeedUpdate();
                break;
            case Constant.UPDATE.MUST_UPDATE:
                showMustUpdate();
                break;
            case Constant.UPDATE.HAS_IGNORE_THIS_VERSION:
                showIgnoreUpdate();
                break;
        }

    }

    private void showIgnoreUpdate() {
        Toast.makeText(mContext, "忽略此版本", Toast.LENGTH_SHORT).show();
    }

    private void showMustUpdate() {
        infoDialog = DialogUtils.showAlterDialog(mContext, "发现重要版本", info.description, "退出应用", "立即更新",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        DialogUtils.dismissDialog(infoDialog);
                        TheActivityManager.getInstance().finishAll();
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DialogUtils.dismissDialog(infoDialog);
                        startDownload(true);
                    }
                }, false);
    }

    private void showNeedUpdate() {
        infoDialog = DialogUtils.showAlterDialog(mContext, "发现新版本", info.description, "以后再说", "立即更新",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DialogUtils.dismissDialog(infoDialog);
                        SharedPreferencesUtils.getInstance().setValue(Constant.SHARED_KEY.IGNORE_VERSION_CODE, info.version_code);

                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DialogUtils.dismissDialog(infoDialog);
                        startDownload(false);
                    }
                }, false);

    }

    private void startDownload(boolean isMustUpdate) {
        if (NetUtil.isWifi(mContext)) {
            bindService();
        } else
            showNotWifiDownloadDialog(isMustUpdate);
    }

    private void showNoUpdate() {
        Toast.makeText(mContext, "当前已是最新版本", Toast.LENGTH_SHORT).show();
    }

    private int resolveVersionCode() {

        int remoteVersionCode = Integer.valueOf(info.version_code);
        int localVersionCode = getVersionCode();
        DebugLog.e("local version code:" + localVersionCode);
        DebugLog.e("remote version code:" + remoteVersionCode);

        if (localVersionCode < remoteVersionCode) {
            String ignore = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.IGNORE_VERSION_CODE);
            DebugLog.e("ignore version code:" + ignore);

            if (!isManual && !TextUtils.isEmpty(ignore) && Integer.valueOf(ignore) == remoteVersionCode)
                return Constant.UPDATE.HAS_IGNORE_THIS_VERSION;
            else if (info.isMustUpgrade())
                return Constant.UPDATE.MUST_UPDATE;
            else
                return Constant.UPDATE.NEED_UPDATE;
        } else
            return Constant.UPDATE.NO_UPDATE;


    }

    private void showNotWifiDownloadDialog(final boolean isMustUpdate) {
        wifiDialog = DialogUtils.showAlterDialog(mContext, "下载新版本", "检查到您的网络处于非wifi状态,下载新版本将消耗一定的流量,是否继续下载?", "以后再说", "继续下载",
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DialogUtils.dismissDialog(wifiDialog);
//                        unbindService();
                        if (isMustUpdate) {
                            TheActivityManager.getInstance().finishAll();

                        } else {
                            SharedPreferencesUtils.getInstance().setValue(Constant.SHARED_KEY.IGNORE_VERSION_CODE, info.version_code);

                        }
                    }
                }, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        DialogUtils.dismissDialog(wifiDialog);
                        bindService();

                    }
                }, false);

    }

    /**
     * 获取当前应用版本号
     */
    private int getVersionCode() {
        try {
            PackageManager packageManager = mContext.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(mContext.getPackageName(), 0);
            DebugLog.e("get version:" + packageInfo.versionCode);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private void bindService() {
        mContext.bindService(new Intent(mContext, DownloadService.class), this, Context.BIND_AUTO_CREATE);
    }

    private void unbindService() {

        mContext.unbindService(this);
    }

    @Override
    public void onServiceConnected(ComponentName name, IBinder service) {

        if (mService == null)
            mService = ((DownloadService.DownloadBinder) service).getService();
        if (info.isMustUpgrade())
            mService.startDownload(info.apk_url, downloadListener);
        else
            mService.startDownload(info.apk_url, null);
    }

    @Override
    public void onServiceDisconnected(ComponentName name) {


    }

    private void showDownloadDialog() {

        downloadDialog = DialogUtils.showProgressDialogDeterminate(mContext, "下载中", false);


    }
}
