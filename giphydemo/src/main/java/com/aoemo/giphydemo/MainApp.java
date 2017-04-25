package com.aoemo.giphydemo;

import android.app.Application;

/**
 * Created by liyiwei
 * on 2017/4/25.
 */

public class MainApp extends Application implements Thread.UncaughtExceptionHandler {
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler(this);
    }

    @Override
    public void uncaughtException(Thread t, Throwable e) {
        e.printStackTrace();
    }
}
