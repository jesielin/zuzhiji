package com.zzj.zuzhiji;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.zzj.zuzhiji.network.Network;
import com.zzj.zuzhiji.network.download.DownloadProgressListener;
import com.zzj.zuzhiji.network.entity.UpdateInfo;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.GlideCacheUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;
import com.zzj.zuzhiji.util.VersionUpdateHelper;

import java.io.File;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action0;

/**
 * Created by shawn on 17/3/29.
 */

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.cache_size)
    TextView tvCacheSize;

    private MaterialDialog infoDialog;
    private MaterialDialog downloadProgressDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        tvCacheSize.setText(GlideCacheUtils.getInstance().getCacheSize(getApplicationContext()));
    }

    private ProgressDialog progressDialog;
    @OnClick(R.id.logout)
    public void logout(View view) {
        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage("退出登录中，请稍后...");
        progressDialog.show();
        // 调用sdk的退出登录方法，第一个参数表示是否解绑推送的token，没有使用推送或者被踢都要传false
        EMClient.getInstance().logout(false, new EMCallBack() {
            @Override
            public void onSuccess() {
                DebugLog.e( "logout success");
                // 调用退出成功，结束app
                SharedPreferencesUtils.getInstance().setLogout();
                startActivity(new Intent(SettingActivity.this,LoginActivity.class));
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
    VersionUpdateHelper versionUpdateHelper;
    @OnClick(R.id.check_version)
    public void update(View view){
        VersionUpdateHelper.resetCancelFlag();//重置cancel标记
        if (versionUpdateHelper == null) {
            versionUpdateHelper = new VersionUpdateHelper(SettingActivity.this);
            versionUpdateHelper.setShowDialogOnStart(true);
            versionUpdateHelper.setCheckCallBack(new VersionUpdateHelper.CheckCallBack() {
                @Override
                public void callBack(int code) {
                    DebugLog.e("call back code:"+code);
                    //EventBus发送消息通知红点消失
//                                            VersionUpdateEvent versionUpdateEvent = new VersionUpdateEvent();
//                                            versionUpdateEvent.setShowTips(false);
//                                            EventBus.getDefault().postSticky(versionUpdateEvent);
                }
            });
        }
        versionUpdateHelper.startUpdateVersion();
//        Network.getInstance().update()
//                .doOnSubscribe(new Action0() {
//                    @Override
//                    public void call() {
//                        progressDialog = new ProgressDialog(SettingActivity.this);
//                        progressDialog.setMessage("正在获取版本，请稍后...");
//                        progressDialog.show();
//                    }
//                })
//                .subscribeOn(AndroidSchedulers.mainThread()) // 指定主线程
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<UpdateInfo>() {
//            @Override
//            public void onCompleted() {
//
//            }
//
//            @Override
//            public void onError(Throwable e) {
//
//                progressDialog.dismiss();
//            }
//
//            @Override
//            public void onNext(final UpdateInfo updateInfo) {
//                progressDialog.dismiss();
//
//
//                Integer.valueOf(updateInfo.version_code);
//
//
//
//                new MaterialDialog.Builder(SettingActivity.this)
//                        .title("新版本")
//                        .content("有新版本。。")
//                        .positiveText("确定")
//                        .negativeText("取消")
//                        .onPositive(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//
//
//                                Toast.makeText(SettingActivity.this, "确定", Toast.LENGTH_SHORT).show();
//                            }
//                        })
//                        .onNegative(new MaterialDialog.SingleButtonCallback() {
//                            @Override
//                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
//                                Toast.makeText(SettingActivity.this, "取消", Toast.LENGTH_SHORT).show();
//
//                            }
//                        })
//                        .show();
//
//
//
//            }
//        });
    }

    @OnClick(R.id.question)
    public void question(View view) {

    }

    @OnClick(R.id.about)
    public void about(View view) {
        startActivity(new Intent(this, AboutActivity.class));
    }

    @OnClick(R.id.back)
    public void back(View view){
        onBackPressed();
    }


    @OnClick(R.id.clean_cache)
    public void clean(View view){
        GlideCacheUtils.getInstance().clearImageDiskCache(getApplicationContext());
        Toast.makeText(this, "正在清除缓存", Toast.LENGTH_SHORT).show();
        tvCacheSize.postDelayed(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(SettingActivity.this, "已清除", Toast.LENGTH_SHORT).show();
                tvCacheSize.setText(GlideCacheUtils.getInstance().getCacheSize(getApplicationContext()));
            }
        },2000);
    }
}
