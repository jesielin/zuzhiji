package com.zzj.zuzhiji;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.liulishuo.filedownloader.BaseDownloadTask;
import com.liulishuo.filedownloader.FileDownloadSampleListener;
import com.liulishuo.filedownloader.FileDownloader;
import com.liulishuo.filedownloader.model.FileDownloadStatus;
import com.liulishuo.filedownloader.notification.BaseNotificationItem;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationHelper;
import com.liulishuo.filedownloader.notification.FileDownloadNotificationListener;
import com.liulishuo.filedownloader.util.FileDownloadHelper;
import com.liulishuo.filedownloader.util.FileDownloadUtils;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.entity.UpdateInfo;
import com.zzj.zuzhiji.util.CommonUtils;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.GlideCacheUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.io.File;
import java.lang.ref.WeakReference;

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

    @BindView(R.id.progressBar)
    ProgressBar progressBar;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
        listener = new NotificationListener(new WeakReference<>(this));

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
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return -1;
        }
    }

    private NotificationListener listener;

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

                                                if (downloadId == 0) {
//                                                    FileDownloader.getImpl().startForeground();
                                                    downloadId = FileDownloader.getImpl()
                                                            .create(updateInfo.apk_url)
                                                            .setPath(FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "zzj", true)
                                                            .setListener(listener)

                                                            .start();
                                                }
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
                                                if (downloadId == 0) {
                                                    downloadId = FileDownloader.getImpl().create(updateInfo.apk_url)
                                                            .setPath(FileDownloadUtils.getDefaultSaveRootPath() + File.separator + "zzj"
                                                                    + File.separator + "zzj.apk", false)
                                                            .setListener(listener)
                                                            .start();
                                                }
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

    private final FileDownloadNotificationHelper<NotificationItem> notificationHelper =
            new FileDownloadNotificationHelper<>();
    private int downloadId = 0;

    FileDownloadSampleListener myListener = new FileDownloadSampleListener() {

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
        }

        @Override
        protected void blockComplete(BaseDownloadTask task) {
            super.blockComplete(task);
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
        }

        @Override
        protected void paused(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.paused(task, soFarBytes, totalBytes);
        }

        @Override
        protected void error(BaseDownloadTask task, Throwable e) {
            super.error(task, e);
        }

        @Override
        protected void warn(BaseDownloadTask task) {
            super.warn(task);
        }
    };

    private class NotificationListener extends FileDownloadNotificationListener {

        private WeakReference<SettingActivity> wActivity;

        public NotificationListener(WeakReference<SettingActivity> wActivity) {
            super(wActivity.get().notificationHelper);
            this.wActivity = wActivity;
        }

        @Override
        protected BaseNotificationItem create(BaseDownloadTask task) {
            return new NotificationItem(task.getId(), "sample demo title", "sample demo desc");
        }

        @Override
        public void addNotificationItem(BaseDownloadTask task) {
            super.addNotificationItem(task);
        }

        @Override
        public void destroyNotification(BaseDownloadTask task) {
            super.destroyNotification(task);
        }

        @Override
        protected boolean interceptCancel(BaseDownloadTask task,
                                          BaseNotificationItem n) {
            // in this demo, I don't want to cancel the notification, just show for the test
            // so return true
            return true;
        }

        @Override
        protected boolean disableNotification(BaseDownloadTask task) {

            return super.disableNotification(task);
        }

        @Override
        protected void pending(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.pending(task, soFarBytes, totalBytes);
            DebugLog.e("pending:" + soFarBytes + "/" + totalBytes + "..." + CommonUtils.getReadableFileSize(totalBytes));
            if (wActivity.get() != null) {
                wActivity.get().progressBar.setIndeterminate(true);
            }
        }

        @Override
        protected void progress(BaseDownloadTask task, int soFarBytes, int totalBytes) {
            super.progress(task, soFarBytes, totalBytes);
            DebugLog.e("progress:" + soFarBytes + "/" + totalBytes + "..." + CommonUtils.getReadableFileSize(totalBytes));
            if (wActivity.get() != null) {
                wActivity.get().progressBar.setIndeterminate(false);
                wActivity.get().progressBar.setMax(totalBytes);
                wActivity.get().progressBar.setProgress(soFarBytes);
            }
        }

        @Override
        protected void completed(BaseDownloadTask task) {
            super.completed(task);
//            (task.getTargetFilePath());
            installApk(task.getTargetFilePath());
            DebugLog.e("complete:" + task.getTargetFilePath());
            if (wActivity.get() != null) {
                wActivity.get().progressBar.setIndeterminate(false);
                wActivity.get().progressBar.setProgress(task.getSmallFileTotalBytes());
            }
        }
    }

    /**
     * 安装 APK。
     *
     * @param filePath APK 文件路径
     */
    public void installApk(String filePath) {
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setDataAndType(Uri.fromFile(new File(filePath)),
                "application/vnd.android.package-archive");
        startActivity(intent);
    }

    public static class NotificationItem extends BaseNotificationItem {

        PendingIntent pendingIntent;
        NotificationCompat.Builder builder;

        private NotificationItem(int id, String title, String desc) {
            super(id, title, desc);
//            Intent[] intents = new Intent[2];
//            intents[0] = Intent.makeMainActivity(new ComponentName(DemoApplication.CONTEXT,
//                    MainActivity.class));
//            intents[1] = new Intent(DemoApplication.CONTEXT, SettingActivity.class);
//
//            this.pendingIntent = PendingIntent.getActivities(DemoApplication.CONTEXT, 0, intents,
//                    PendingIntent.FLAG_UPDATE_CURRENT);

            builder = new NotificationCompat.
                    Builder(FileDownloadHelper.getAppContext());

            builder.setDefaults(Notification.DEFAULT_LIGHTS)
                    .setOngoing(true)
                    .setPriority(NotificationCompat.PRIORITY_MIN)
                    .setContentTitle(getTitle())
                    .setContentText(desc)
                    .setContentIntent(pendingIntent)
                    .setSmallIcon(R.mipmap.ic_launcher);

        }

        @Override
        public void show(boolean statusChanged, int status, boolean isShowProgress) {

            String desc = getDesc();
            switch (status) {
                case FileDownloadStatus.pending:
                    desc += " pending";
                    break;
                case FileDownloadStatus.started:
                    desc += " started";
                    break;
                case FileDownloadStatus.progress:
                    desc += " progress";
                    break;
                case FileDownloadStatus.retry:
                    desc += " retry";
                    break;
                case FileDownloadStatus.error:
                    desc += " error";
                    break;
                case FileDownloadStatus.paused:
                    desc += " paused";
                    break;
                case FileDownloadStatus.completed:
                    desc += " completed";
                    break;
                case FileDownloadStatus.warn:
                    desc += " warn";
                    break;
            }

            builder.setContentTitle(getTitle())
                    .setContentText(desc);


            if (statusChanged) {
                builder.setTicker(desc);
            }

            builder.setProgress(getTotal(), getSofar(), !isShowProgress);
            getManager().notify(getId(), builder.build());
        }

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        if (downloadId != 0) {
            FileDownloader.getImpl().pause(downloadId);
        }
        notificationHelper.clear();

        ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE)).
                cancelAll();
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
