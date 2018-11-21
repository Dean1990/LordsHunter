package com.deanlib.lordshunter.ui.view;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;

import com.deanlib.lordshunter.data.entity.Report;

import java.util.ArrayList;
import java.util.List;

public class ViewJump {

    public static final int CODE_REPORT_SAVE_TO_DETAIL = 1012;

    public static void toMain(Activity activity){
        activity.startActivity(new Intent(activity,MainActivity.class));
    }

    public static void toReportList(Activity activity,String group,String name,long startTime,long endTime){
        Intent intent = new Intent(activity,ReportListActivity.class);
        intent.putExtra("group",group);
        intent.putExtra("name",name);
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

    public static void toWebView(Activity activity,String url){
        Intent intent = new Intent(activity, WebViewActivity.class);
        intent.putExtra("url",url);
        activity.startActivity(intent);
    }

    public static Intent getSaveIntent(Context context, List<Report> reports){
        Intent intent = new Intent();
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setClass(context, SaveActivity.class);
        intent.putParcelableArrayListExtra("reports", (ArrayList<? extends Parcelable>) reports);
        return intent;
    }

    public static void toMemberReportList(Activity activity,long startTime,long endTime){
        Intent intent = new Intent(activity,MemberReportListActivity.class);
        intent.putExtra("startTime",startTime);
        intent.putExtra("endTime",endTime);
        activity.startActivity(intent);
    }

    public static void toSettings(Activity activity){
        toSettings(activity,false);
    }

    public static void toSettings(Activity activity,boolean autoDownloadOCRData){
        Intent intent = new Intent(activity,SettingsActivity.class);
        intent.putExtra("autoDownloadOCRData",autoDownloadOCRData);
        activity.startActivity(intent);
    }

    public static void toMemberManage(Activity activity){
        Intent intent = new Intent(activity,MemberManageActivity.class);
        activity.startActivity(intent);
    }
}
