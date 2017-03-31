package com.zzj.zuzhiji.app;

import android.app.Application;
import android.content.Context;

import com.squareup.leakcanary.LeakCanary;
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
        if (LeakCanary.isInAnalyzerProcess(this)) {
            // This process is dedicated to LeakCanary for heap analysis.
            // You should not init your app in this process.
            return;
        }
        LeakCanary.install(this);


        SharedPreferencesUtils.getInstance().init(this);
        context = this;
    }

}
