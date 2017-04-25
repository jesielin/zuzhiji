package com.zzj.zuzhiji.service;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.v4.content.FileProvider;

import com.arialyy.aria.core.Aria;
import com.arialyy.aria.core.download.DownloadTarget;
import com.arialyy.aria.core.download.DownloadTask;
import com.zzj.zuzhiji.R;
import com.zzj.zuzhiji.util.DebugLog;

import java.io.File;

/**
 * Created by shawn on 2017-04-25.
 */

public class DownloadService extends Service {

    private final int NOTIFICATION_ID = 100;
    private DownloadListener downloadListener;
    private NotificationManager mNotificationManager;
    private Notification.Builder notificationBuilder;
    private Notification notification;
    private Aria.DownloadSchedulerListener listener = new Aria.DownloadSchedulerListener() {
        @Override
        public void onTaskPre(DownloadTask task) {
            super.onTaskPre(task);
            DebugLog.e("task pre");
        }

        @Override
        public void onTaskResume(DownloadTask task) {
            super.onTaskResume(task);
            DebugLog.e("task resume");
        }

        @Override
        public void onTaskStart(DownloadTask task) {
            super.onTaskStart(task);
            starDownLoadForground();
            DebugLog.e("task start total:" + task.getFileSize());
            if (downloadListener != null)
                downloadListener.onStart();
        }

        @Override
        public void onTaskStop(DownloadTask task) {
            super.onTaskStop(task);
            DebugLog.e("task stop");
        }

        @Override
        public void onTaskCancel(DownloadTask task) {
            super.onTaskCancel(task);
            DebugLog.e("task cancel");
        }

        @Override
        public void onTaskFail(DownloadTask task) {
            super.onTaskFail(task);
            DebugLog.e("task fail");
        }

        @Override
        public void onTaskComplete(DownloadTask task) {
            super.onTaskComplete(task);

            if (downloadListener != null)
                downloadListener.onComplete();

            notificationBuilder.setContentTitle("完成");
            notificationBuilder.setTicker("下载完成");
            notificationBuilder.setContentText("下载完成");


            File file = new File(task.getDownloadEntity().getDownloadPath());
            Uri uri;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
                uri = FileProvider.getUriForFile(DownloadService.this, getApplicationContext().getPackageName() + ".provider", file);

            else
                uri = Uri.fromFile(file);

            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setDataAndType(uri, "application/vnd.android.package-archive");

            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
            PendingIntent updatePendingIntent = PendingIntent.getActivity(DownloadService.this, 0, installIntent, 0);
            notificationBuilder.setContentIntent(updatePendingIntent);
            mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.getNotification());

            installApk(uri);
            DebugLog.e("task complete");

        }

        @Override
        public void onTaskRunning(DownloadTask task) {
            super.onTaskRunning(task);

            if (downloadListener != null)
                downloadListener.onProgress((int) (task.getCurrentProgress() * 100.0 / task.getFileSize()));

            notificationBuilder.setContentTitle("正在下载更新" + (int) (task.getCurrentProgress() * 100.0 / task.getFileSize()) + "%"); // the label of the entry
            notificationBuilder.setProgress(100, (int) (task.getCurrentProgress() * 100.0 / task.getFileSize()), false);
            mNotificationManager.notify(NOTIFICATION_ID, notificationBuilder.getNotification());
            DebugLog.e("task running:" + task.getCurrentProgress());
        }

        @Override
        public void onNoSupportBreakPoint(DownloadTask task) {
            super.onNoSupportBreakPoint(task);
            DebugLog.e("task no break point");
        }
    };

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return new DownloadBinder();
    }


    public void startDownload(String apkUrl, DownloadListener downloadListener) {
        DebugLog.e("service download:" + apkUrl);
        this.downloadListener = downloadListener;


        String path;
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())) {//如果已经挂载
//            sd卡已经挂载，可以进行读写操作了
//            path = Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "zzj.apk";
            path = Environment.getExternalStorageDirectory().getPath() + File.separator + "zzj.apk";
        } else {
            //sd未挂载，在此进行提示


            path = getExternalCacheDir().getAbsolutePath() + File.separator + "zzj.apk";
//            path = getFilesDir().getAbsolutePath() + File.separator + "zzj.apk";
        }

        DebugLog.e("path:" + path);


        boolean b = Aria.download(this).taskExists(apkUrl);
        DownloadTarget downloadTarget = Aria.download(this)
                .addSchedulerListener(listener)
                .load(apkUrl) //读取下载地址
                .setDownloadPath(path);//设置文件保存的完整路径
        if (b) {

            downloadTarget.resume();   //启动下载
        } else {
            downloadTarget.start();
        }






    }

    /**
     * 让Service保持活跃,避免出现:
     * 如果启动此服务的前台Activity意外终止时Service出现的异常(也将意外终止)
     */
    private void starDownLoadForground() {

        if (mNotificationManager == null)
            mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);

        // In this sample, we'll use the same text for the ticker and the expanded notification
        CharSequence text = "下载中,请稍后...";
        // The PendingIntent to launch our activity if the user selects this notification
//        PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
//                new Intent(this, MainActivity.class), 0);
        if (notificationBuilder == null) {
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
            notification = notificationBuilder.getNotification();
            startForeground(NOTIFICATION_ID, notification);
        }
    }

    private void installApk(Uri uri) {
        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        installIntent.setDataAndType(uri,
                "application/vnd.android.package-archive");
        startActivity(installIntent);
    }

    public interface DownloadListener {
        void onStart();

        void onProgress(int progress);

        void onComplete();
    }

    public class DownloadBinder extends Binder {
        public DownloadService getService() {
            return DownloadService.this;
        }
    }
}
