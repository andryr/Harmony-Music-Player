package com.andryr.musicplayer.musicbrainz;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by Andry on 17/10/15.
 */
public class ArtistImageUtils {

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();
    private static final Map<String, Bitmap> sImageCache = new HashMap<>();
    private static BitmapFactory.Options sBitmapOptions = new BitmapFactory.Options();

    static {
        sBitmapOptions.inScaled = false;
        sBitmapOptions.inDither = false;
        sBitmapOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
    }

    public static void loadArtistImage(String artistName, ImageView view) {
        if (artistName == null || artistName.length() == 0) {
            return;
        }

        Bitmap b;
        if (sImageCache.containsKey(artistName)) {
            b = sImageCache.get(artistName);
        } else {

            b = getArtistFromDb(view.getContext(), artistName);
            sImageCache.put(artistName, b);
        }

        if (b != null) {
            view.setImageBitmap(b);
            return;
        }

        loadImageFromMB(artistName, view);


    }

    private static Bitmap getArtistFromDb(Context context,
                                          String artistName) {


        ArtistImageDbHelper dbHelper = new ArtistImageDbHelper(context);
        Bitmap b = dbHelper.getArtistImage(artistName);
        dbHelper.close();
        return b;
    }

    private static void loadImageFromMB(final String artistName, final ImageView view) {
        final MB mb = MB.getInstance();
        mb.search(MB.EntityType.Artist, artistName, new RequestRunnable.RequestListener() {
            @Override
            public void onRequestResult(List<? extends MBObject> result) {
                if (result.size() > 0) {
                    final MBArtist artist = (MBArtist) result.get(0);
                    mb.getRelations(MB.EntityType.Artist, artist.getId(), new RequestRunnable.RequestListener() {
                        @Override
                        public void onRequestResult(List<? extends MBObject> result) {
                            MBRelation imgRel = null;
                            for (MBObject o : result) {
                                if (o instanceof MBRelation) {
                                    MBRelation rel = (MBRelation) o;
                                    if (rel.getType().equals("image")) {
                                        imgRel = rel;
                                        break;
                                    }
                                }
                            }

                            if (imgRel == null) {
                                return;
                            }

                            String url = imgRel.getTarget();
                            if (isWikimediaImage(url)) {
                                url = getWikimediaImageUrl(url);
                            }

                            Log.e("url", "url : " + url);

                            ImageDownloader.getInstance().download(url, new ImageDownloader.OnDownloadCompleteListener() {
                                @Override
                                public void onDownloadComplete(Bitmap bitmap) {
                                    sImageCache.put(artistName, bitmap);
                                    save(view.getContext(), artist.getId(), artistName, bitmap);
                                    view.setImageBitmap(bitmap);
                                }
                            });

                        }

                        @Override
                        public void onRequestError() {

                        }
                    });
                }
            }

            @Override
            public void onRequestError() {

            }
        });


    }

    private static boolean isWikimediaImage(String url) {
        return url.startsWith("https://commons.wikimedia.org");
    }

    private static String getWikimediaImageUrl(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            synchronized (md) {
                md.reset();
                Pattern p = Pattern.compile("File:([a-z0-9A-Z_\\-.]+)");
                Matcher m = p.matcher(url);
                if (m.find()) {
                    String filename = m.group(1).replace(' ', '_');
                    Log.e("ee", "file : " + filename);
                    String md5 = new String(bytesToHex(md.digest(filename
                            .getBytes("UTF-8"))));

                    Locale l = Locale.US;
                    String hash1 = md5.substring(0, 1).toLowerCase(l);
                    String hash2 = md5.substring(0, 2).toLowerCase(l);

                    return "https://upload.wikimedia.org/wikipedia/commons/"
                            + hash1 + "/" + hash2 + "/" + filename;
                }
            }
        } catch (NoSuchAlgorithmException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return null;
    }

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }


    private static void save(Context context, String mbid, String artistName, Bitmap image) {
        ArtistImageDbHelper dbHelper = new ArtistImageDbHelper(context);

        dbHelper.insertOrUpdate(mbid, artistName, image);
        dbHelper.close();

    }


    public static void clearArtistImageCache() {
        sImageCache.clear();
    }


}
