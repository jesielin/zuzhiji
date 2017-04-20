package com.zzj.zuzhiji.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Binder;
import android.os.Environment;
import android.os.IBinder;
import android.widget.Toast;

import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.download.DownloadProgressListener;
import com.zzj.zuzhiji.network.entity.UpdateInfo;
import com.zzj.zuzhiji.util.DebugLog;

import java.io.File;

import rx.Subscriber;

public class VersionUpdateService extends Service {


    private static final String TAG = VersionUpdateService.class.getSimpleName();
    private final int NOTIFICATION_ID = 100;
    private LocalBinder binder = new LocalBinder();
    private DownLoadListener downLoadListener;
    private boolean downLoading;
    private int progress;
    private NotificationManager mNotificationManager;
    private NotificationUpdaterThread notificationUpdaterThread;
    private Notification.Builder notificationBuilder;
    private UpdateInfo versionUpdateModel;
    private CheckVersionCallBack checkVersionCallBack;

    public VersionUpdateService() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        DebugLog.e("onCreate called");
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        DebugLog.e("onDestroy called");
        setDownLoadListener(null);
        setCheckVersionCallBack(null);
        stopDownLoadForground();
        if (mNotificationManager != null)
            mNotificationManager.cancelAll();
        downLoading = false;
    }

    public void setCheckVersionCallBack(CheckVersionCallBack checkVersionCallBack) {
        this.checkVersionCallBack = checkVersionCallBack;
    }

    public boolean isDownLoading() {
        return downLoading;
    }

    public void setDownLoading(boolean downLoading) {
        this.downLoading = downLoading;
    }

    /**
     * 让Service保持活跃,避免出现:
     * 如果启动此服务的前台Activity意外终止时Service出现的异常(也将意外终止)
     */
    private void starDownLoadForground() {
        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "下载中,请稍后...";
        // The PendingIntent to launch our activity if the user selects this notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MainActivity.class), 0);
        notificationBuilder = new Notification.Builder(this);
        notificationBuilder.setSmallIcon(R.mipmap.ic_launcher);  // the status icon
        notificationBuilder.setTicker(text);  // the status text
        notificationBuilder.setWhen(System.currentTimeMillis());  // the time stamp
        notificationBuilder.setContentText(text);  // the contents of the entry
//        notificationBuilder.setContentIntent(contentIntent);  // The intent to send when the entry is clicked
        notificationBuilder.setContentTitle("正在下载更新" + 0 + "%"); // the label of the entry
        notificationBuilder.setProgress(100, 0, false);
        notificationBuilder.setOngoing(true);
        notificationBuilder.setAutoCancel(true);
        Notification notification = notificationBuilder.getNotification();
        startForeground(NOTIFICATION_ID, notification);
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

    private void stopDownLoadForground() {
        stopForeground(true);
    }

    public void doCheckUpdateTask() {
        final int currentBuild = getVersionCode();

        Network.getInstance().update()
                .subscribe(new Subscriber<UpdateInfo>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        DebugLog.e("update error:" + e.getMessage());
                        Toast.makeText(VersionUpdateService.this, "获取版本信息失败", Toast.LENGTH_SHORT).show();
                        if (checkVersionCallBack != null) {
                            checkVersionCallBack.onError();
                        }
                    }

                    @Override
                    public void onNext(UpdateInfo updateInfo) {

                        versionUpdateModel = updateInfo;
                        if (Integer.valueOf(versionUpdateModel.version_code) < currentBuild) {
                            versionUpdateModel.setNeedUpgrade(false);
                        }
                        //TEST DATA
                        versionUpdateModel.setNeedUpgrade(true);

//                        App.getAppContext().setVersionUpdateModelCache(versionUpdateModel);
                        if (checkVersionCallBack != null)
                            checkVersionCallBack.onSuccess();
                    }
                });

    }

    public void doDownLoadTask() {
        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        starDownLoadForground();

        notificationUpdaterThread = new NotificationUpdaterThread();
        notificationUpdaterThread.start();

        final String url = versionUpdateModel.apk_url;
        final File destFile = new File(Environment.getExternalStoragePublicDirectory
                (Environment.DIRECTORY_DOWNLOADS), "zzj.apk");
        if (destFile.exists()) {
            destFile.delete();
        }

        downLoading = true;

        if (downLoadListener != null) {
            downLoadListener.begin();
        }


        Network.getInstance().downloadApk(url, destFile, new DownloadProgressListener() {
            @Override
            public void update(long bytesRead, long contentLength, boolean done) {
//                DebugLog.d("download:" + bytesRead + "/" + contentLength);

                progress = (int) (bytesRead * 100 / contentLength);
                if (downLoadListener != null) {
                    downLoadListener.inProgress(bytesRead, contentLength);
                }
                if (progress >= 100) {
                    mNotificationManager.cancelAll();
                }


            }
        }, new Subscriber() {
            @Override
            public void onCompleted() {

                if (downLoadListener != null) {
                    downLoadListener.downLoadLatestSuccess(destFile);
                }
                downLoading = false;
                DebugLog.e("apk address:" + destFile.getAbsolutePath());
                installApk(destFile, VersionUpdateService.this);

            }

            @Override
            public void onError(Throwable e) {
                downLoading = false;
                if (mNotificationManager != null)
                    mNotificationManager.cancelAll();
                if (downLoadListener != null) {
                    downLoadListener.downLoadLatestFailed();
                }

            }

            @Override
            public void onNext(Object o) {
                DebugLog.d("downloading....");


            }
        });

//        setCheckVersionCallBack
    }

    public UpdateInfo getVersionUpdateModel() {
        return versionUpdateModel;
    }

    public void setDownLoadListener(DownLoadListener downLoadListener) {
        this.downLoadListener = downLoadListener;
    }

    //安装apk
    public void installApk(File file, Context context) {
        Intent intent = new Intent();
        intent.setAction(Intent.ACTION_VIEW);
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //执行的数据类型
        intent.setDataAndType(Uri.fromFile(file), "application/vnd.android.package-archive");
        context.startActivity(intent);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    public interface DownLoadListener {
        void begin();

        void inProgress(long current, long total);

        void downLoadLatestSuccess(File file);

        void downLoadLatestFailed();
    }

    public interface CheckVersionCallBack {
        void onSuccess();

        void onError();
    }

    private class NotificationUpdaterThread extends Thread {
        @Override
        public void run() {
            while (true) {
                notificationBuilder.setContentTitle("正在下载更新" + progress + "%"); // the label of the entry
                notificationBuilder.setProgress(100, progress, false);
                mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.getNotification());
                if (progress >= 100) {
                    break;
                }
            }
        }
    }

    public class LocalBinder extends Binder {
        public VersionUpdateService getService() {
            return VersionUpdateService.this;
        }
    }
}
