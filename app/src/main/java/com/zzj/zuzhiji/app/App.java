package com.zzj.zuzhiji.app;

import android.app.Application;
import android.content.Context;

import com.zzj.zuzhiji.util.SharedPreferencesUtils;

/**
 * Created by shawn on 17/3/28.
 */

public class App extends Application {


    private static Context context;

    public static Context getContext() {
        return context;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        SharedPreferencesUtils.getInstance().init(this);
        context = this;
    }

}
