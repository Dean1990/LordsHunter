package com.deanlib.lordshunter;

import android.app.Application;

import com.deanlib.ootb.OotbConfig;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        OotbConfig.init(this,true);
    }
}
