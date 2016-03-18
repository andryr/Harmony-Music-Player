package org.oucho.musicplayer.utils;


public class ColorUtils {

    public static int applyAlpha(int color, float alpha) {
        return (color & 0x00FFFFFF) | ((int) (255 * alpha) << 24);
    }
}
