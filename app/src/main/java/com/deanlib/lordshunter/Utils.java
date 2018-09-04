package com.deanlib.lordshunter;

import android.net.Uri;
import android.text.TextUtils;

import com.deanlib.lordshunter.entity.Report;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {

    public static List<Report> parseText(String text, List<Uri> images){
        if (TextUtils.isEmpty(text)){
            return null;
        }
        if (images==null || images.size()==0){
            return null;
        }
        text = text.trim();

        Pattern groupPattern = Pattern.compile("(.+) 微信群上的聊天记录如下，请查收。");
        Pattern dataPattern = Pattern.compile("—————  (\\d{4}-\\d{2}-\\d{2})  —————");
        Pattern reportPattern = Pattern.compile("(.+) {2}(\\d{2}:\\d{2})\\n\\n\\[图片: (\\w{32}\\.jpg)\\(\\S+\\)\\]");
        Matcher groupMatcher = groupPattern.matcher(text);
        if (groupMatcher.find()) {
            String group = groupMatcher.group(1);

            String[] split = text.split("—————  \\d{4}-\\d{2}-\\d{2}  —————");
            if (split != null && split.length > 0) {

                List<Report> list = new ArrayList<>();
                for (String str : split) {
                    String data = "";
                    Matcher dataMatcher = dataPattern.matcher(str);
                    if (dataMatcher.find()) {
                        data = dataMatcher.group(1);
                    }
                    Matcher reportMatcher = reportPattern.matcher(str);
                    while (reportMatcher.find()) {
                        Report report = new Report();
                        report.setGroup(group);
                        report.setData(data);
                        report.setName(reportMatcher.group(1));
                        report.setTime(reportMatcher.group(2));
                        for (Uri uri : images) {
                            if (uri.toString().endsWith(reportMatcher.group(3))) {
                                report.setImage(uri.toString());
                                break;
                            }
                        }
                        list.add(report);
                    }
                }
                return list;
            }
        }
        return null;
    }
}
