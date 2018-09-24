package com.deanlib.lordshunter.ui.view;

import android.app.Activity;
import android.content.Intent;

public class ViewJump {

    public static void toMain(Activity activity){
        activity.startActivity(new Intent(activity,MainActivity.class));
    }
}
