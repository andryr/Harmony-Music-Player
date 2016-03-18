package org.oucho.musicplayer.images;

import android.content.Context;
import android.graphics.drawable.Drawable;

import org.oucho.musicplayer.R;


class ArtistImageHelper {

    private static Drawable sDefaultArtistImage;
    private static Drawable sDefaultArtistThumb;

    public static Drawable getDefaultArtistImage(Context c) {
        if (sDefaultArtistImage == null) {
            sDefaultArtistImage = c.getResources().getDrawable(R.drawable.default_artist_image);

        }
        return sDefaultArtistImage.getConstantState().newDrawable(c.getResources()).mutate();
    }

    public static Drawable getDefaultArtistThumb(Context c) {
        if (sDefaultArtistThumb == null) {
            sDefaultArtistThumb = c.getResources().getDrawable(R.drawable.default_artist_thumb);

        }
        return sDefaultArtistThumb.getConstantState().newDrawable(c.getResources()).mutate();
    }
}
