package org.oucho.musicplayer.utils;


public class ColorUtils {

    public static int applyAlpha(int color, float alpha) {
        int colorAlpha  = ((color & 0xFF000000) >> 24) + 256;
        return (color & 0x00FFFFFF) | ((int) (colorAlpha * alpha) << 24);
    }
}
