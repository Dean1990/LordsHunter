package com.deanlib.lordshunter.app;

import android.app.Activity;
import android.app.Application;
import android.os.Bundle;

public class ActivityLifecycleListener implements Application.ActivityLifecycleCallbacks {

    private int refCount = 0;

    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

    }

    @Override
    public void onActivityStarted(Activity activity) {
        refCount++;
        Constant.isAppRunForeground = true;
    }

    @Override
    public void onActivityResumed(Activity activity) {

    }

    @Override
    public void onActivityPaused(Activity activity) {

    }

    @Override
    public void onActivityStopped(Activity activity) {
        refCount--;
        if (refCount == 0){
            Constant.isAppRunForeground = false;
        }
    }

    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

    }

    @Override
    public void onActivityDestroyed(Activity activity) {

    }
}
