package com.zzj.zuzhiji;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.yayandroid.theactivitymanager.TheActivityManager;
import com.zzj.zuzhiji.util.SharedPreferencesUtils;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 17/3/29.
 */

public class SettingActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.bind(this);
    }

    @OnClick(R.id.logout)
    public void logout(View view) {

        SharedPreferencesUtils.getInstance().setLogout();
        TheActivityManager.getInstance().finishAll();

    }

    @OnClick(R.id.question)
    public void question() {

    }
}
