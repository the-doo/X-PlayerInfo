package com.doo.playerinfo;

import com.doo.playerinfo.utils.InfoRegisters;

import java.util.function.UnaryOperator;

public class XPlayerInfo {
    public static final String MOD_ID = "x_player_info";

    // 0 - forge    1 - fabric
    public static int LOADER_FLAG = -1;
    private static UnaryOperator<String> nameGetter;

    public static void init(int flag, UnaryOperator<String> nameGetter) {
        LOADER_FLAG = flag;
        XPlayerInfo.nameGetter = nameGetter;

        InfoRegisters.initMinecraft();
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object t) {
        return (T) t;
    }

    public static String name(String id) {
        return nameGetter == null ? id : nameGetter.apply(id);
    }

    public static boolean isForge() {
        return LOADER_FLAG == 0;
    }
}
