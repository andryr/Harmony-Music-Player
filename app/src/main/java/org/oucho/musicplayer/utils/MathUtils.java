package org.oucho.musicplayer.utils;

public class MathUtils {


    public static float getValueInRange(float val, float min, float max) {
        return Math.min(max, Math.max(min, val));
    }

}
