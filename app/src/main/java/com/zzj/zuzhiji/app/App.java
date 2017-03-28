package com.zzj.zuzhiji.app;

import android.app.Application;

import com.zzj.zuzhiji.util.SharedPreferencesUtils;

/**
 * Created by shawn on 17/3/28.
 */

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesUtils.getInstance().init(this);
    }
}
