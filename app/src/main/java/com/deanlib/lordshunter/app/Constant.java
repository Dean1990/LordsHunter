package com.deanlib.lordshunter.app;

import com.deanlib.lordshunter.BuildConfig;
import com.deanlib.lordshunter.data.entity.Member;
import com.deanlib.lordshunter.data.entity.Prey;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Constant {

    public static final boolean isDebug = BuildConfig.isDebug;
    public static final List<String> PREY_NAMES = new ArrayList<>();
    public static final Map<Character,Set<Prey>> PREY_NAME_INDEX_MAP = new HashMap<>();//文字索引 存放拆分开的文字做为Key

    public static boolean isAppRunForeground = false;

    public static File APP_FILE_OCR_TRAINEDDATA;//字库文件
    public static String OCR_LANGUAGE;//OCR 语言
    public static String README_URL_HEADER = "http://deanlib.com/app/lords_hunter/";//使用说明的地址前部分

    public static List<Member> hideMemberList;//隐藏成员列表

}
