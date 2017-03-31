package com.zzj.zuzhiji;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.zzj.zuzhiji.app.Constant;
import com.zzj.zuzhiji.util.ActivityManager;

import butterknife.ButterKnife;
import butterknife.OnClick;

/**
 * Created by shawn on 17/3/31.
 */

public class CaseDetailActivity extends AppCompatActivity {


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_case_detail);
        ButterKnife.bind(this);
        ActivityManager.getInstance().addActivity(this);
    }

    @OnClick(R.id.back)
    public void back() {

        ActivityManager.getInstance().finshActivities(getClass());
    }


}
