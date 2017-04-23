package com.zzj.zuzhiji;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.UpdateInfo;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.GlideCacheUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by shawn on 17/3/29.
 */

public class SettingActivity extends BaseActivity {

    @BindView(R.id.cache_size)
    TextView tvCacheSize;
    Gson gson = new Gson();

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        tvCacheSize.setText(GlideCacheUtils.getInstance().getCacheSize(getApplicationContext()));
    }

    @OnClick(R.id.logout)
    public void logout(View view) {
        mDialog = DialogUtils.showProgressDialog(this, "退出登录中，请稍后...");
        // 调用sdk的退出登录方法，第一个参数表示是否解绑推送的token，没有使用推送或者被踢都要传false
        EMClient.getInstance().logout(false, new EMCallBack() {
            @Override
            public void onSuccess() {
                DebugLog.e("logout success");
                // 调用退出成功，结束app
                DialogUtils.dismissDialog(mDialog);
                SharedPreferencesUtils.getInstance().setLogout();
                startActivity(new Intent(SettingActivity.this, LoginActivity.class));
            }

            @Override
            public void onError(int i, String s) {
                DebugLog.e("logout error " + i + " - " + s);
                Toast.makeText(SettingActivity.this, s, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onProgress(int i, String s) {

            }
        });


    }

    /**
     * 获取当前应用版本号
     */
    private int getVersionCode() {
        try {
            PackageManager packageManager = getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            DebugLog.e("get version:" + packageInfo.versionCode);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    @OnClick(R.id.check_version)
    public void update(View view) {

        Network.getInstance().update()
                .doOnSubscribe(new Action0() {
                    @Override
                    public void call() {
                        mDialog = DialogUtils.showProgressDialog(SettingActivity.this, "正在获取版本信息..."); // 需要在主线程执行
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
                        Toast.makeText(SettingActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                        DialogUtils.dismissDialog(mDialog);
                    }

                    @Override
                    public void onNext(final UpdateInfo updateInfo) {
                        DialogUtils.dismissDialog(mDialog);
                        DebugLog.d("version code:" + getVersionCode());
                        DebugLog.d("remote version code:" + updateInfo.version_code);
                        if (Integer.valueOf(updateInfo.version_code) > getVersionCode()) {
                            if (updateInfo.isMustUpgrade()) {
                                Toast.makeText(SettingActivity.this, "强制更新", Toast.LENGTH_SHORT).show();
                                mDialog = DialogUtils.showContentDialog(SettingActivity.this, "有新版本", updateInfo.description, "关闭应用", "立即更新",
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                DialogUtils.dismissDialog(mDialog);
                                            }
                                        },
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                DialogUtils.dismissDialog(mDialog);

                                                startDownload(updateInfo.apk_url, "zzj.apk");
                                            }
                                        });
                            } else {
                                Toast.makeText(SettingActivity.this, "不强制更新", Toast.LENGTH_SHORT).show();
                                mDialog = DialogUtils.showContentDialog(SettingActivity.this, "有新版本", updateInfo.description, "以后再说", "立即更新",
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                DialogUtils.dismissDialog(mDialog);
                                            }
                                        },
                                        new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                DialogUtils.dismissDialog(mDialog);
                                                startDownload(updateInfo.apk_url, "zzj.apk");
                                            }
                                        });
                            }
                        } else {
                            Toast.makeText(SettingActivity.this, "当前已是最新版本", Toast.LENGTH_SHORT).show();
                            DialogUtils.dismissDialog(mDialog);
                        }

                    }
                });
    }


    private void startDownload(String url, String fileName) {
        /*
        * 1. 封装下载请求
        */

        // http 下载链接（该链接为 CSDN APP 的下载链接，仅做参考）
        String downloadUrl = url;

        // 创建下载请求
        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(downloadUrl));
        /*
         * 设置在通知栏是否显示下载通知(下载进度), 有 3 个值可选:
         *    VISIBILITY_VISIBLE:                   下载过程中可见, 下载完后自动消失 (默认)
         *    VISIBILITY_VISIBLE_NOTIFY_COMPLETED:  下载过程中和下载完成后均可见
         *    VISIBILITY_HIDDEN:                    始终不显示通知
         */
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);

        // 设置通知的标题和描述
//        request.setTitle("知足脊");
//        request.setDescription("新版本");

        /*
         * 设置允许使用的网络类型, 可选值:
         *     NETWORK_MOBILE:      移动网络
         *     NETWORK_WIFI:        WIFI网络
         *     NETWORK_BLUETOOTH:   蓝牙网络
         * 默认为所有网络都允许
         */
        // request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI);

        // 添加请求头
        // request.addRequestHeader("User-Agent", "Chrome Mozilla/5.0");

        // 设置下载文件的保存位置
//        File saveFile = new File(Environment.getExternalStorageDirectory(), fileName);
//        request.setDestinationUri(Uri.fromFile(saveFile));
        request.setDestinationInExternalFilesDir(this, Environment.DIRECTORY_DOWNLOADS, "知足脊");
//        request.setDestinationUri(Environment.DIRECTORY_DOWNLOADS,fileName);

        /*
         * 2. 获取下载管理器服务的实例, 添加下载任务
         */
        DownloadManager manager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);

        // 将下载请求加入下载队列, 返回一个下载ID
        long downloadId = manager.enqueue(request);

        manager. ()

        // 如果中途想取消下载, 可以调用remove方法, 根据返回的下载ID取消下载, 取消下载后下载保存的文件将被删除
        // manager.remove(downloadId);
        DebugLog.e("download id:" + downloadId);

        SharedPreferencesUtils.getInstance().setValue(Constant.SHARED_KEY.DOWNLOAD_ID, String.valueOf(downloadId));
    }






    @OnClick(R.id.question)
    public void question(View view) {

    }

    @OnClick(R.id.about)
    public void about(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    @OnClick(R.id.back)
    public void back(View view) {
        onBackPressed();
    }


    @OnClick(R.id.clean_cache)
    public void clean(View view) {
        GlideCacheUtils.getInstance().clearImageDiskCache(getApplicationContext());
        Toast.makeText(this, "正在清除缓存", Toast.LENGTH_SHORT).show();
        tvCacheSize.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingActivity.this, "已清除", Toast.LENGTH_SHORT).show();
                tvCacheSize.setText(GlideCacheUtils.getInstance().getCacheSize(getApplicationContext()));
            }
        }, 2000);
    }
}
