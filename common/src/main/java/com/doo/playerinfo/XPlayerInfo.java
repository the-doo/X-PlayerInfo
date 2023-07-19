package com.doo.playerinfo;

public class XPlayerInfo {
    public static final String MOD_ID = "x_player_info";

    // 0 - forge    1 - fabric
    public static int LOADER_FLAG = -1;

    public static void init(int flag) {
        LOADER_FLAG = flag;
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object t) {
        return (T) t;
    }

    public static boolean isFabric() {
        return LOADER_FLAG == 1;
    }
}
