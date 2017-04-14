package com.zzj.zuzhiji;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.zzj.zuzhiji.util.GlideCacheUtils;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 17/3/29.
 */

public class SettingActivity extends AppCompatActivity {

    @BindView(R.id.cache_size)
    TextView tvCacheSize;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);

        tvCacheSize.setText(GlideCacheUtils.getInstance().getCacheSize(getApplicationContext()));
    }

    @OnClick(R.id.logout)
    public void logout(View view) {

        SharedPreferencesUtils.getInstance().setLogout();
//        TheActivityManager.getInstance().finishAll();
        startActivity(new Intent(this,LoginActivity.class));


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
