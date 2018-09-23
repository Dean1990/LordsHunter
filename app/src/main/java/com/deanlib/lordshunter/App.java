package com.deanlib.lordshunter;

import android.app.Application;

import com.deanlib.ootblite.OotbConfig;

import io.realm.Realm;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        OotbConfig.init(this,Constant.isDebug);
        Realm.init(this);

        String[] preyNames = getResources().getStringArray(R.array.prey_name_chi_sim);
        for (String name: preyNames) {
            Constant.PREY_NAMES.add(name);
        }
    }
}
