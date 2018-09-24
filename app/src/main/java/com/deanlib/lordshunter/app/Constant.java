package com.deanlib.lordshunter.app;

import com.deanlib.lordshunter.entity.Prey;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class Constant {

    public static final boolean isDebug = true;
    public static final List<String> PREY_NAMES = new ArrayList<>();
    public static final Map<Character,Set<Prey>> PREY_NAME_INDEX_MAP = new HashMap<>();//文字索引 存放拆分开的文字做为Key

}
