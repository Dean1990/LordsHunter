package com.deanlib.lordshunter;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.text.TextUtils;

import com.deanlib.lordshunter.entity.ImageInfo;
import com.deanlib.lordshunter.entity.Report;
import com.deanlib.ootblite.utils.DLog;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.apache.commons.codec.digest.DigestUtils;

import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

public class Utils {

    /**
     * 解析微信以邮件形式发送过来的内容
     * 耗时
     * @param text
     * @param images
     * @return
     */
    public static List<Report> parseText(String text, List<Uri> images){
        if (TextUtils.isEmpty(text)){
            return null;
        }
        if (images==null || images.size()==0){
            return null;
        }
        text = text.trim();

        Pattern groupPattern = Pattern.compile("(.+) 微信群上的聊天记录如下，请查收。");
        Pattern datePattern = Pattern.compile("—————  (\\d{4}-\\d{2}-\\d{2})  —————");
        Pattern reportPattern = Pattern.compile("(.+) {2}(\\d{2}:\\d{2})\\n\\n\\[图片: (\\w{32}\\.jpg)\\(\\S+\\)\\]");
        Matcher groupMatcher = groupPattern.matcher(text);
        if (groupMatcher.find()) {
            String group = groupMatcher.group(1);

            String[] split = text.split("—————  \\d{4}-\\d{2}-\\d{2}  —————");
            if (split != null && split.length > 0) {

                List<Report> list = new ArrayList<>();
                for (String str : split) {
                    String date = "";
                    Matcher dateMatcher = datePattern.matcher(str);
                    if (dateMatcher.find()) {
                        date = dateMatcher.group(1);
                    }
                    Matcher reportMatcher = reportPattern.matcher(str);
                    while (reportMatcher.find()) {
                        Report report = new Report();
                        report.setGroup(group);
                        report.setDate(date);
                        report.setName(reportMatcher.group(1));
                        report.setTime(reportMatcher.group(2));
                        for (Uri uri : images) {
                            if (uri.toString().endsWith(reportMatcher.group(3))) {
                                ImageInfo image = new ImageInfo();
                                image.setUri(uri.toString().substring(7));
                                report.setImage(image);
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

    /**
     * 检查重复图片
     * 并给传入的List 赋md5值
     * 耗时
     * @param list
     * @return 返回重复图片的report 如果有
     */
    public static List<Report> checkRepet(List<Report> list){
        if (list==null || list.size() == 0)
            return null;
        List<Report> repetReports = new ArrayList<>();//重复图片
        Realm realm = Realm.getDefaultInstance();
        for (Report report:list){

            try {
                String md5 = DigestUtils.md5Hex(new FileInputStream(report.getImage().getUri()));
                report.getImage().setMd5(md5);
                ImageInfo find = realm.where(ImageInfo.class).equalTo("md5", md5).findFirst();
                if (find!=null){
                    repetReports.add(report);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return repetReports;
    }

    /**
     * 文字识别
     * @param list
     * @return
     */
    public static List<Report> ocr(List<Report> list){
        if (list==null || list.size() == 0)
            return list;
        TessBaseAPI tess = new TessBaseAPI();
        String language = "chi_sim";

        //todo 字库文件怎么办
        tess.init("/sdcard/datapath",language);
        for (Report report:list){
            Bitmap bitmap = BitmapFactory.decodeFile(report.getImage().getUri());
            float w = bitmap.getWidth();
            float h = bitmap.getHeight();
            bitmap = Bitmap.createBitmap(bitmap,(int)(w*0.1),(int)(h*0.12),(int)(w*0.4),(int)(h*0.05),null,false);

//            ImageUtils.saveImageFile(bitmap, FileUtils.createDir("_abc"), SystemClock.currentThreadTimeMillis()+".png", new FileUtils.FileCallback() {
//                @Override
//                public void onSuccess(File file) {
//
//                }
//
//                @Override
//                public void onFail(Exception e) {
//
//                }
//            });

            tess.setImage(bitmap);
            String result = tess.getUTF8Text();
            DLog.d(result);
            //清洗信息
            Pattern pattern = Pattern.compile("[\\S\\s]*(\\d) (\\S+)");
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                report.getImage().setPreyName(correctPreyName(matcher.group(2)));
                report.getImage().setPreyLevel(Integer.valueOf(matcher.group(1)));
                report.getImage().setKill(true);//默认 true
            }
        }
        return list;
    }

    public static String correctPreyName(String preyName){
        String name = preyName;

        if(!Constant.PREY_NAMES.contains(name)){
            //todo 查找 对应 修复
        }

        return name;
    }
}
