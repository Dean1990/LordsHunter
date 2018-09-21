package com.deanlib.lordshunter;

import android.app.Application;

import com.deanlib.ootblite.OotbConfig;

import io.realm.Realm;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        OotbConfig.init(this,true);
        Realm.init(this);

    }
}
