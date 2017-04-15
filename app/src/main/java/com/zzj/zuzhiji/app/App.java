package com.zzj.zuzhiji.app;

import android.app.Application;
import android.content.Context;
import android.support.multidex.MultiDex;

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
//        if (LeakCanary.isInAnalyzerProcess(this)) {
//            // This process is dedicated to LeakCanary for heap analysis.
//            // You should not init your app in this process.
//            return;
//        }
//        LeakCanary.install(this);


        TheActivityManager.getInstance().configure(this);

        SharedPreferencesUtils.getInstance().init(this);
        context = this;

        //当程序发生Uncaught异常时捕获
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {
            @Override
            public void uncaughtException(Thread thread, Throwable e) {
                //做你要做的处理，比如把e.getMessage()保存到文件，发送一个email等等，不是本篇重点，不再赘述
                //TODO:
            }
        });

    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }
}
