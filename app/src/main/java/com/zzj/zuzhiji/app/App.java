package com.zzj.zuzhiji.app;

import android.app.Application;
import android.content.Context;

import com.hyphenate.chat.EMClient;
import com.hyphenate.easeui.controller.EaseUI;
import com.squareup.leakcanary.LeakCanary;
import com.yayandroid.theactivitymanager.TheActivityManager;
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

        EaseUI.getInstance().init(this, null);
        EMClient.getInstance().setDebugMode(false);

        TheActivityManager.getInstance().configure(this);

        SharedPreferencesUtils.getInstance().init(this);
        context = this;
    }

}
