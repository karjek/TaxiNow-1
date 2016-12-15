package com.home.yassine.taxinow;

import android.app.Application;
import android.util.Log;

/**
 * Created by Yassine on 23/09/2016.
 */
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        Thread.setDefaultUncaughtExceptionHandler (new Thread.UncaughtExceptionHandler()
        {
            @Override
            public void uncaughtException (Thread thread, Throwable e)
            {
                Log.wtf("Alert", e.getMessage(), e);
                System.exit(2); //Prevents the service/app from freezing
            }
        });
    }
}
