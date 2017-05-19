package com.zzj.zuzhiji;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.hyphenate.EMCallBack;
import com.hyphenate.chat.EMClient;
import com.zzj.zuzhiji.util.DebugLog;
import com.zzj.zuzhiji.util.DialogUtils;
import com.zzj.zuzhiji.util.GlideCacheUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 17/3/29.
 */

public class SettingActivity extends BaseActivity {

    @BindView(R.id.cache_size)
    TextView tvCacheSize;
//    private UpdateHelper updateHelper;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        tvCacheSize.setText(GlideCacheUtils.getInstance().getCacheSize(getApplicationContext()));
//        updateHelper = new UpdateHelper(this, true);

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



    @OnClick(R.id.check_version)
    public void update(View view) {
//        updateHelper.check();
        Toast.makeText(this, "有BUG，在调试", Toast.LENGTH_SHORT).show();
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
