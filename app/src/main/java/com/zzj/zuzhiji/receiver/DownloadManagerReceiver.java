package com.zzj.zuzhiji.receiver;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;

import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import java.io.File;
import java.util.Arrays;

/**
 * Created by shawn on 2017-04-23.
 */

public class DownloadManagerReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        if (DownloadManager.ACTION_NOTIFICATION_CLICKED.equals(action)) {
            System.out.println("用户点击了通知");

            // 点击下载进度通知时, 对应的下载ID以数组的方式传递
            long[] ids = intent.getLongArrayExtra(DownloadManager.EXTRA_NOTIFICATION_CLICK_DOWNLOAD_IDS);
            System.out.println("ids: " + Arrays.toString(ids));

        } else if (DownloadManager.ACTION_DOWNLOAD_COMPLETE.equals(action)) {
            System.out.println("下载完成");


            String id = SharedPreferencesUtils.getInstance().getValue(Constant.SHARED_KEY.DOWNLOAD_ID);
            if (TextUtils.isEmpty(id))
                return;
            long downloadId = Long.valueOf(id);

            // 根据获取到的ID，使用上面第3步的方法查询是否下载成功

            // 获取下载管理器服务的实例
            DownloadManager manager = (DownloadManager) context.getSystemService(Context.DOWNLOAD_SERVICE);

            // 创建一个查询对象
            DownloadManager.Query query = new DownloadManager.Query();

            // 根据 下载ID 过滤结果
            query.setFilterById(downloadId);

            // 还可以根据状态过滤结果
            // query.setFilterByStatus(DownloadManager.STATUS_SUCCESSFUL);

            // 执行查询, 返回一个 Cursor (相当于查询数据库)
            Cursor cursor = manager.query(query);

            if (!cursor.moveToFirst()) {
                cursor.close();
                return;
            }

            // 下载请求的状态
            int status = cursor.getInt(cursor.getColumnIndex(DownloadManager.COLUMN_STATUS));
            // 下载文件在本地保存的路径（Android 7.0 以后 COLUMN_LOCAL_FILENAME 字段被弃用, 需要用 COLUMN_LOCAL_URI 字段来获取本地文件路径的 Uri）
            String localFilename = cursor.getString(cursor.getColumnIndex(DownloadManager.COLUMN_LOCAL_FILENAME));
            // 已下载的字节大小
            long downloadedSoFar = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_BYTES_DOWNLOADED_SO_FAR));
            // 下载文件的总字节大小
            long totalSize = cursor.getLong(cursor.getColumnIndex(DownloadManager.COLUMN_TOTAL_SIZE_BYTES));


            cursor.close();

            DebugLog.e("下载进度: " + downloadedSoFar + "/" + totalSize);

        /*
         * 判断是否下载成功，其中状态 status 的值有 5 种:
         *     DownloadManager.STATUS_SUCCESSFUL:   下载成功
         *     DownloadManager.STATUS_FAILED:       下载失败
         *     DownloadManager.STATUS_PENDING:      等待下载
         *     DownloadManager.STATUS_RUNNING:      正在下载
         *     DownloadManager.STATUS_PAUSED:       下载暂停
         */
            if (status == DownloadManager.STATUS_SUCCESSFUL) {
        /*
         * 特别注意: 查询获取到的 localFilename 才是下载文件真正的保存路径，在创建
         * 请求时设置的保存路径不一定是最终的保存路径，因为当设置的路径已是存在的文件时，
         * 下载器会自动重命名保存路径，例如: .../demo-1.apk, .../demo-2.apk
         */
                System.out.println("下载成功, 打开文件, 文件路径: " + localFilename);

                Intent installIntent = new Intent(Intent.ACTION_VIEW);
                installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                installIntent.setDataAndType(Uri.fromFile(new File(localFilename)),
                        "application/vnd.android.package-archive");
                context.startActivity(installIntent);
            }

        }
    }


}