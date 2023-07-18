package com.doo.playerinfo.utils;

public abstract class MixinUtil {

    private MixinUtil() {
    }

    @SuppressWarnings("unchecked")
    public static <T> T get(Object o) {
        return (T) o;
    }
}
