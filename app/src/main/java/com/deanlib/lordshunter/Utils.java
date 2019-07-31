package com.deanlib.lordshunter;

import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;

import com.baidu.ocr.sdk.OnResultListener;
import com.baidu.ocr.sdk.exception.OCRError;
import com.baidu.ocr.sdk.model.GeneralBasicParams;
import com.baidu.ocr.sdk.model.GeneralResult;
import com.baidu.ocr.sdk.model.WordSimple;
import com.deanlib.lordshunter.app.Constant;
import com.deanlib.lordshunter.data.entity.ImageInfo;
import com.deanlib.lordshunter.data.entity.Member;
import com.deanlib.lordshunter.data.entity.OCR;
import com.deanlib.lordshunter.data.entity.Prey;
import com.deanlib.lordshunter.data.entity.Report;
import com.deanlib.lordshunter.event.CollectTaskEvent;
import com.deanlib.ootblite.data.FileUtils;
import com.deanlib.ootblite.utils.DLog;
import com.deanlib.ootblite.utils.MD5;
import com.googlecode.tesseract.android.TessBaseAPI;

import org.greenrobot.eventbus.EventBus;

import java.io.File;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Realm;

public class Utils {

    public static final String UNDEFINDE = "Undefined";

    /**
     * 解析微信以邮件形式发送过来的内容
     * 耗时
     *
     * @param text
     * @param images
     * @return
     */
    public static List<Report> parseText(Context context, String text, List<Uri> images) {
        DLog.d("parseText");
        if (TextUtils.isEmpty(text)) {
            return null;
        }
        if (images == null || images.size() == 0) {
            return null;
        }
        text = text.trim();

        //有繁体字问题
        Pattern groupPattern = Pattern.compile(context.getString(R.string.pattern_group));
//        Pattern groupPattern = Pattern.compile("(.+) WeChat群組的聊天記錄如下，請查收。");
        Pattern datePattern = Pattern.compile("—————  (\\d{4}-\\d{2}-\\d{2})  —————");
        Pattern reportPattern = Pattern.compile(context.getString(R.string.pattern_report));
//        Pattern reportPattern = Pattern.compile("(.+) {2}(\\d{2}:\\d{2})\\n\\n\\[圖片: (\\w{32}\\.jpg)\\(\\S+\\)\\]");
        Matcher groupMatcher = groupPattern.matcher(text);
        if (groupMatcher.find()) {
            String group = groupMatcher.group(1);

            SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

            String[] split = text.split("—————  \\d{4}-\\d{2}-\\d{2}  —————");
            if (split != null && split.length > 1) {
                Matcher dateMatcher = datePattern.matcher(text);
                List<Report> list = new ArrayList<>();
                //直接舍弃第一个，第一个是group信息
                for (int i = 1; i < split.length; i++) {
                    String date = "";
                    if (dateMatcher.find()) {
                        date = dateMatcher.group(1);
                    }
                    Matcher reportMatcher = reportPattern.matcher(split[i]);
                    while (reportMatcher.find()) {
                        Report report = new Report();
                        report.setGroup(group);
                        report.setDate(date);
                        report.setName(reportMatcher.group(1));
                        report.setTime(reportMatcher.group(2));
                        for (Uri uri : images) {
                            if (uri.toString().endsWith(reportMatcher.group(3))) {
                                ImageInfo image = new ImageInfo();
                                if (uri.toString().startsWith("file:///")) {
                                    image.setUri(uri.toString().substring(7));
                                } else {
                                    //适配新版本微信 Version.7.0.0
                                    image.setUri(getFPUriToPath(context, uri));
                                }
                                image.setDataTime(report.getDate() + " " + report.getTime());
                                report.setImage(image);
                                break;
                            }
                        }
                        //转换时间戳
                        try {
                            Date parse = dateFormat.parse(report.getDate() + " " + report.getTime());
                            report.setTimestamp(parse.getTime());
                        } catch (ParseException e) {
                            e.printStackTrace();
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
     *
     * @param list
     * @return
     */
    public static List<Report> checkRepet(List<Report> list) {
        DLog.d("checkRepet");
        if (list == null || list.size() == 0)
            return null;
        Realm realm = Realm.getDefaultInstance();
        //对同一批次(此时未保存到数据库)的MD5保存，用以判断同一批次重复图片的问题
        Map<String, Report> md5Map = new HashMap<>();
        for (Report report : list) {
            DLog.d(report.toString());
            try {
                String md5 = MD5.md5(new File(report.getImage().getUri()));
                DLog.d("md5:" + md5);
                report.getImage().setMd5(md5);
                //判断重复性
                //本批次判断
                if (md5Map.containsKey(md5)) {
                    //重复
                    report.setStatus(Report.STATUS_REPET);
                } else {
                    md5Map.put(md5, report);
                    //数据库判断
                    ImageInfo find = realm.where(ImageInfo.class)
                            .equalTo("md5", md5)
                            .findFirst();

                    if (find != null) {
                        DLog.d(find.toString());
                        //排除日期相同的情况，是数据库中存在的 ，这种情况一般是用户操作不当
                        if (find.getDataTime().equals(report.getDate() + " " + report.getTime())) {
                            report.setStatus(Report.STATUS_EXIST);
                        } else {
                            report.setStatus(Report.STATUS_REPET);
                        }
                    } else {
                        report.setStatus(Report.STATUS_NEW);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        md5Map = null;
        return list;
    }

    /**
     * 云OCR服务
     *
     * @param report
     * @return
     */
    public static void cloudOCR(Context context, Report report, OnResultListener<GeneralResult> listener) {
        if (report != null && report.getImage() != null
                && !TextUtils.isEmpty(report.getImage().getUri())) {
            try {
//                BitmapFactory.Options options = new BitmapFactory.Options();
//                options.inJustDecodeBounds = true;
//                int scale = (int)(options.outHeight/(float)200);//我们只用高度或宽度计算均可
//                if(scale<=0){
//                    scale = 1;
//                }
//                options.inSampleSize = scale;
//                Bitmap bitmap = BitmapFactory.decodeFile(report.getImage().getUri(),options);
                Bitmap bitmap = BitmapFactory.decodeFile(report.getImage().getUri());
                float w = bitmap.getWidth();
                float h = bitmap.getHeight();
                bitmap = Bitmap.createBitmap(bitmap, (int) (w * 0.1), (int) (h * 0.12), (int) (w * 0.4), (int) (h * 0.05), null, false);
                File file = new File(FileUtils.createDir("LordsHunter"), System.currentTimeMillis() + ".temp");
                FileOutputStream fos = new FileOutputStream(file);
                bitmap.compress(Bitmap.CompressFormat.PNG, 90, fos);
                if(bitmap!=null && !bitmap.isRecycled()){
                    bitmap.recycle();
                    bitmap = null;
                }
                System.gc();


                GeneralBasicParams params = new GeneralBasicParams();
                params.setDetectDirection(true);
//                params.setImageFile(new File(report.getImage().getUri()));
                params.setImageFile(file);

                com.baidu.ocr.sdk.OCR.getInstance(context).recognizeGeneralBasic(params, listener);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 文字识别
     *
     * @param list
     * @return
     */
    public static List<Report> cloudOCR(Context context, List<Report> list) {
        DLog.d("cloud ocr");
        if (list == null || list.size() == 0)
            return list;
        try {
            CountDownLatch countDownLatch = new CountDownLatch(list.size());
            for (int i = 0;i<list.size();i++) {
                Report report = list.get(i);
                int finalI = i;
                cloudOCR(context, report, new OnResultListener<GeneralResult>() {
                    @Override
                    public void onResult(GeneralResult generalResult) {
                        StringBuilder sb = new StringBuilder();
                        for (WordSimple wordSimple : generalResult.getWordList()) {
                            // wordSimple不包含位置信息
                            WordSimple word = wordSimple;
                            sb.append(word.getWords());
                            sb.append("\n");
                        }
                        DLog.d("Baidu OCR Result:" + sb.toString());
                        Pattern pattern = Pattern.compile("L\\S*([1-5])[ ]*(\\S+)");
                        Matcher matcher = pattern.matcher(sb.toString());
                        if (matcher.find()) {
                            report.getImage().setPreyName(correctPreyName(matcher.group(2)));
                            report.getImage().setPreyLevel(Integer.valueOf(matcher.group(1)));
                            report.getImage().setKill(true);//默认 true
                        }
                        EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_SERVICE_MESSAGE, context.getString(R.string.text_extraction_, finalI +1,list.size())));
                        countDownLatch.countDown();
                    }

                    @Override
                    public void onError(OCRError ocrError) {
                        DLog.d("Baidu OCR Result Error:" + report.getImage().getUri());
                        ocrError.printStackTrace();
                        DLog.d("Change to localOCR");
                        localOCR(report, Constant.OCR_LANGUAGE, null);
                        EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_SERVICE_MESSAGE, context.getString(R.string.text_extraction_, finalI +1,list.size())));
                        countDownLatch.countDown();
                    }
                });
            }
            countDownLatch.await();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        return list;
    }

    /**
     * 文字识别
     *
     * @param report
     * @param lang      要使用的字库名
     * @param usedLangs 已经用过的字库列表
     * @return
     */
    public static String localOCR(Report report, String lang, List<String> usedLangs) {
        TessBaseAPI tess = new TessBaseAPI();
        //字库文件
        File traineddata = Utils.getOCR(lang).getFile();
        if (traineddata.exists()) {
            tess.init(traineddata.getParentFile().getParent(), Constant.OCR_LANGUAGE);
//            BitmapFactory.Options options = new BitmapFactory.Options();
//            options.inJustDecodeBounds = true;
//            int scale = (int)(options.outHeight/(float)200);//我们只用高度或宽度计算均可
//            if(scale<=0){
//                scale = 1;
//            }
//            options.inSampleSize = scale;
//            Bitmap bitmap = BitmapFactory.decodeFile(report.getImage().getUri(),options);
            Bitmap bitmap = BitmapFactory.decodeFile(report.getImage().getUri());
            float w = bitmap.getWidth();
            float h = bitmap.getHeight();
            bitmap = Bitmap.createBitmap(bitmap, (int) (w * 0.1), (int) (h * 0.12), (int) (w * 0.4), (int) (h * 0.05), null, false);

//            ImageUtils.saveImageFile(bitmap, FileUtils.createDir("_abc"), System.currentTimeMillis() + ".png", new FileUtils.FileCallback() {
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
            if(bitmap!=null && !bitmap.isRecycled()){
                bitmap.recycle();
                bitmap = null;
            }
            tess = null;
            System.gc();


            if (usedLangs == null)
                usedLangs = new ArrayList<>();
            usedLangs.add(lang);

            DLog.d("local ocr result : " + result);
            //清洗信息
            String[] lvLikes = {"1JjLlIi", "2Zz", "3B8", "4HqPpg", "5Ssb"};//识别存在误差，将相识，可能被识别成的字符列举出来
            //[\S\s]*([1JjLlIi2Zz3B4HqPpg5Ssb])[ ]*(\S+)
            Pattern pattern = Pattern.compile("[\\S\\s]*(" + Arrays.toString(lvLikes).replaceAll("[,\\s]", "") + ")[ ]*(\\S+)");
            Matcher matcher = pattern.matcher(result);
            if (matcher.find()) {
                report.getImage().setPreyName(correctPreyName(matcher.group(2)));
                for (int i = 0; i < lvLikes.length; i++) {
                    if (lvLikes[i].contains(matcher.group(1))) {
                        report.getImage().setPreyLevel(i + 1);
                        break;
                    }
                }
                report.getImage().setKill(true);//默认 true
            } else {
                DLog.d("change local ocr");
                //使用 Constant.OCR_LANGUAGE 本地字库没有识别成功的
                //尝试使用其他字库
                //查看当前存在的字库包
                if (Constant.OCR_LANGUAGES != null) {
                    for (String l : Constant.OCR_LANGUAGES) {
                        OCR ocr = getOCR(l);
                        if (ocr.isExist() && !usedLangs.contains(l)) {
                            result = localOCR(report, l, usedLangs);
                        }
                    }
                }
            }

            return result;
        }

        return "";
    }

    /**
     * 文字识别
     *
     * @param list
     * @return
     */
    public static List<Report> localOCR(Context context,List<Report> list) {
        DLog.d("local ocr");
        if (list == null || list.size() == 0)
            return list;

        for (int i = 0;i<list.size() ; i++) {
            localOCR(list.get(i), Constant.OCR_LANGUAGE, null);
            EventBus.getDefault().post(new CollectTaskEvent(CollectTaskEvent.ACTION_SERVICE_MESSAGE, context.getString(R.string.text_extraction_,i+1,list.size())));
        }

        return list;
    }


    /**
     * 校准猎物名称
     *
     * @param preyName
     * @return
     */
    public static String correctPreyName(String preyName) {
        DLog.d("correctPreyName");
        String name = preyName;

        if (!Constant.PREY_NAMES.contains(name)) {
            //查找 对应 修复
            Map<Prey, Integer> scoreMap = new HashMap<>();//加权记录
            char[] chars = name.toCharArray();
            for (char ch : chars) {
                Set<Prey> preys = Constant.PREY_NAME_INDEX_MAP.get(ch);
                if (preys != null) {
                    for (Prey prey : preys) {
                        Integer score = scoreMap.get(prey);
                        if (score == null) score = 0;
                        score++;
                        scoreMap.put(prey, score);
                    }
                }
            }

            if (scoreMap.size() > 0) {
                //查找权重最高的
                Prey maxScorePrey = null;
                Set<Map.Entry<Prey, Integer>> entries = scoreMap.entrySet();
                for (Map.Entry<Prey, Integer> entry : entries) {
                    if (maxScorePrey == null) {
                        maxScorePrey = entry.getKey();
                        continue;
                    }
                    Integer maxScore = scoreMap.get(maxScorePrey);
                    if (maxScore == null) maxScore = 0;
                    Integer score = entry.getValue();
                    if (maxScore == null) score = 0;
                    if (maxScore < score) {
                        maxScorePrey = entry.getKey();
                    }
                }
                name = maxScorePrey.getNameLocal();
            } else {
                //没有权重记录时，preyName 将无法确定
                //可以统一到一个名称
                name = UNDEFINDE;
            }

        }

        return name;
    }

    /**
     * 计算折合一级怪
     *
     * @param level
     * @param count 怪只数
     * @return
     */
    public static long equivalentLv1(int level, long count) {
        if (level != 1) {
            count *= 3;
            level--;
            count = equivalentLv1(level, count);
        }
        return count;
    }

    /**
     * 获取cache路径
     *
     * @param context
     * @return
     */
    public static String getDiskCachePath(Context context) {
        if (Environment.MEDIA_MOUNTED.equals(Environment.getExternalStorageState())
                || !Environment.isExternalStorageRemovable()) {
            if (context.getExternalCacheDir() != null)
                return context.getExternalCacheDir().getPath();
            else return context.getCacheDir().getPath();
        } else {
            return context.getCacheDir().getPath();
        }
    }

    /**
     * 筛选成员 隐藏与否
     *
     * @param member
     * @return
     */
    public static Member memberFiler(Member member) {
        member.setHide(false);
        if (Constant.hideMemberList != null)
            for (Member m : Constant.hideMemberList) {
                if (m.getName().equals(member.getName())
                        && m.getGroup().equals(member.getGroup())) {
                    member.setHide(true);
                    break;
                }
            }
        return member;
    }

    /**
     * 得到OCR信息
     *
     * @param name 语言的名字，描述形式使用OCR形式
     * @return
     */
    public static OCR getOCR(String name) {
        OCR ocr = new OCR();
        ocr.setName(name);
        ocr.setFile(new File(Constant.APP_FILE_OCR_DIR, name + ".traineddata"));
        ocr.setExist(ocr.getFile().exists());
        return ocr;
    }

    /**
     * 获取FileProvider path
     * fileprovider 转 file 绝对路径
     */
    private static String getFPUriToPath(Context context, Uri uri) {
        try {
            List<PackageInfo> packs = context.getPackageManager().getInstalledPackages(PackageManager.GET_PROVIDERS);
            if (packs != null) {
                String fileProviderClassName = FileProvider.class.getName();
                for (PackageInfo pack : packs) {
                    ProviderInfo[] providers = pack.providers;
                    if (providers != null) {
                        for (ProviderInfo provider : providers) {
                            if (uri.getAuthority().equals(provider.authority)) {
                                if (provider.name.equalsIgnoreCase(fileProviderClassName)) {
                                    Class<FileProvider> fileProviderClass = FileProvider.class;
                                    try {
                                        Method getPathStrategy = fileProviderClass.getDeclaredMethod("getPathStrategy", Context.class, String.class);
                                        getPathStrategy.setAccessible(true);
                                        Object invoke = getPathStrategy.invoke(null, context, uri.getAuthority());
                                        if (invoke != null) {
                                            String PathStrategyStringClass = FileProvider.class.getName() + "$PathStrategy";
                                            Class<?> PathStrategy = Class.forName(PathStrategyStringClass);
                                            Method getFileForUri = PathStrategy.getDeclaredMethod("getFileForUri", Uri.class);
                                            getFileForUri.setAccessible(true);
                                            Object invoke1 = getFileForUri.invoke(invoke, uri);
                                            if (invoke1 instanceof File) {
                                                String filePath = ((File) invoke1).getAbsolutePath();
                                                return filePath;
                                            }
                                        }
                                    } catch (NoSuchMethodException e) {
                                        e.printStackTrace();
                                    } catch (InvocationTargetException e) {
                                        e.printStackTrace();
                                    } catch (IllegalAccessException e) {
                                        e.printStackTrace();
                                    } catch (ClassNotFoundException e) {
                                        e.printStackTrace();
                                    }
                                    break;
                                }
                                break;
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

}
