package com.deanlib.lordshunter.ui.view;

import android.app.Activity;
import android.content.Intent;

import com.deanlib.lordshunter.entity.Report;

public class ViewJump {

    public static final int CODE_REPORT_SAVE_TO_DETAIL = 1012;

    public static void toMain(Activity activity){
        activity.startActivity(new Intent(activity,MainActivity.class));
    }

    public static void toReportList(Activity activity,long startTime,long endTime){
        Intent intent = new Intent(activity,ReportListActivity.class);
        intent.putExtra("startTime",startTime);
        intent.putExtra("endTime",endTime);
        activity.startActivity(intent);
    }

    public static void toReportDetail(Activity activity,String id){
        Intent intent = new Intent(activity,ReportDetailActivity.class);
        intent.putExtra("id",id);
        activity.startActivity(intent);
    }

    public static void toReportDetail(Activity activity, Report report){
        Intent intent = new Intent(activity,ReportDetailActivity.class);
        intent.putExtra("report",report);
        activity.startActivityForResult(intent,CODE_REPORT_SAVE_TO_DETAIL);
    }
}
