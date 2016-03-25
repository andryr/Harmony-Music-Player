package org.oucho.musicplayer.images;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;

import org.oucho.musicplayer.R;


public class ArtistImageHelper {

    private static Drawable sDefaultArtistImage;
    private static Drawable sDefaultArtistThumb;

    public static Drawable getDefaultArtistImage(Context c) {
        if (sDefaultArtistImage == null) {
            sDefaultArtistImage = ContextCompat.getDrawable(c, R.drawable.default_artist_image);

        }
        return sDefaultArtistImage.getConstantState().newDrawable(c.getResources()).mutate();
    }

    public static Drawable getDefaultArtistThumb(Context c) {
        if (sDefaultArtistThumb == null) {
            sDefaultArtistThumb = ContextCompat.getDrawable(c, R.drawable.default_artist_thumb);

        }
        return sDefaultArtistThumb.getConstantState().newDrawable(c.getResources()).mutate();
    }
}
