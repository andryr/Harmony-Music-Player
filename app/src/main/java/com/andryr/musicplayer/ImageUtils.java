package com.andryr.musicplayer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.TransitionDrawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.widget.ImageView;

import com.andryr.musicplayer.musicbrainz.MB;
import com.andryr.musicplayer.musicbrainz.MBArtist;
import com.andryr.musicplayer.musicbrainz.MBRelation;

public class ImageUtils {

    private static final String PREFS = "com.andryr.musicplayer.ImageUtils";

    private static final Map<Long, Drawable> sArtworkCache = new HashMap<>();
    private static Drawable sDefaultArtwork;
    private static final Uri sArtworkUri = Uri
            .parse("content://media/external/audio/albumart");

    private static final Map<String, Drawable> sArtistCache = new HashMap<>();
    private static Drawable sDefaultArtist;

    public static Drawable getArtwork(Context context, long albumId) {
        if (albumId == -1) {
            return null;
        }
        synchronized (sArtworkCache) {
            if (sArtworkCache.containsKey(albumId)) {
                return sArtworkCache.get(albumId);
            }
        }

        Uri uri = ContentUris.withAppendedId(sArtworkUri, albumId);

        try {
            if (uri != null) {
                ContentResolver res = context.getContentResolver();
                Bitmap b = BitmapFactory.decodeStream(res.openInputStream(uri));
                Drawable d = new BitmapDrawable(context.getResources(), b);
                synchronized (sArtworkCache) {
                    sArtworkCache.put(albumId, d);

                }
                return d;
            }
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e("io", "io", e);
            return null;
        }

        return null;

    }

    public static Drawable getDefaultArtwork(Context c) {
        if (sDefaultArtwork == null) {
            sDefaultArtwork = c.getResources().getDrawable(
                    R.drawable.default_artwork);

        }
        return sDefaultArtwork;
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void loadArtworkAsync(final long albumId, final ImageView view) {

        Drawable currentDrawable = view.getDrawable();
        if (currentDrawable instanceof TransitionDrawable) {
            currentDrawable = ((TransitionDrawable) currentDrawable)
                    .getDrawable(1);
        }

        final Drawable oldDrawable = currentDrawable;

        final Context context = view.getContext();

        AsyncTask<Void, Void, Drawable> task = new AsyncTask<Void, Void, Drawable>() {

            @Override
            protected Drawable doInBackground(Void... params) {

                if (sDefaultArtwork == null) {
                    sDefaultArtwork = context.getResources()
                            .getDrawable(R.drawable.default_artwork);

                }
                Drawable artwork = getArtwork(context, albumId);
                if (artwork == null) {
                    artwork = sDefaultArtwork;
                }

                artwork = artwork.getConstantState().newDrawable();
                artwork.mutate();

                if (oldDrawable != null && artwork != null
                        && oldDrawable != artwork) {
                    TransitionDrawable transition = new TransitionDrawable(
                            new Drawable[]{oldDrawable, artwork});
                    transition.setCrossFadeEnabled(true);
                    return transition;
                }
                return artwork;
            }

            @Override
            protected void onPostExecute(Drawable result) {
                view.setImageDrawable(result);
                if (result instanceof TransitionDrawable) {
                    ((TransitionDrawable) result).startTransition(200);
                }

            }

        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        } else {
            task.execute((Void) null);
        }
    }

    public static void clearArtworkCache() {
        sArtworkCache.clear();
    }

    public static Drawable getArtistImage(Context context, String artistName) {
        if (artistName == null || artistName.length() == 0) {
            return null;
        }

        synchronized (sArtistCache) {
            if (sArtistCache.containsKey(artistName)) {
                return sArtistCache.get(artistName);
            }
        }

        Drawable d = getArtistFromDiskCache(context, artistName);

        if (d == null) {
            d = getArtistFromMB(context, artistName);
        }

        return d;
    }

    private static Drawable getArtistFromDiskCache(Context context,
                                                   String artistName) {
        // on récupère le mbid depuis les SharedPreferences
        SharedPreferences prefs = context.getSharedPreferences(PREFS,
                Context.MODE_PRIVATE);
        String mbid = prefs.getString(artistName, null);
        if (mbid == null) {

            List<MBArtist> searchResults = MB.searchArtist(artistName);

            if (searchResults != null && searchResults.size() > 0) {
                // on utilise le mbid comme nom de fichier
                mbid = searchResults.get(0).getId();

            }
        }

        if (mbid != null) {
            final String id = mbid;
            File[] list = context.getCacheDir().listFiles(
                    new FilenameFilter() {

                        @Override
                        public boolean accept(File dir, String filename) {

                            return filename.equals(id);
                        }
                    });
            if (list != null && list.length > 0) {
                try {
                    File file = list[0];
                    FileInputStream in = new FileInputStream(file);

                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    Drawable d = new BitmapDrawable(context.getResources(),
                            bitmap);
                    synchronized (sArtistCache) {
                        sArtistCache.put(artistName, d);
                    }
                    return d;
                } catch (FileNotFoundException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);
                }
            }
        }
        return null;

    }

    private static Drawable getArtistFromMB(Context context, String artistName) {
        List<MBArtist> searchResults = MB.searchArtist(artistName);
        if (searchResults != null && searchResults.size() > 0) {
            MBArtist artist = searchResults.get(0);
            MB.updateRelations(artist);

            List<MBRelation> relations = artist.getRelationList("image");
            if (relations.size() > 0) {

                String url = relations.get(0).getTarget();
                if (isWikimediaImage(url)) {
                    url = getWikimediaImageUrl(url);
                }

                Log.e("url", "url : " + url);
                try {
                    HttpURLConnection connection = (HttpURLConnection) new URL(
                            url).openConnection();
                    connection.setConnectTimeout(10000);
                    connection.setReadTimeout(30000);
                    connection.setDoInput(true);
                    connection.connect();
                    if (connection.getResponseCode() == HttpURLConnection.HTTP_OK) {
                        InputStream in = connection.getInputStream();
                        byte[] data = inputStreamToByteArray(in);
                        Bitmap bitmap = BitmapFactory.decodeByteArray(data, 0,
                                data.length);
                        in.close();
                        if (bitmap != null) {
                            Drawable d = new BitmapDrawable(
                                    context.getResources(), bitmap);
                            synchronized (sArtistCache) {
                                sArtistCache.put(artistName, d);
                            }
                            saveBitmap(bitmap, context.getCacheDir(),
                                    artist.getId());

                            //on stocke le mbid
                            Editor editor = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit();
                            editor.putString(artist.getName(), artist.getId());
                            editor.commit();
                            return d;
                        }
                    }
                } catch (MalformedURLException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);
                } catch (IOException e) {
                    // TODO Auto-generated catch block
                    Log.e("io", "io", e);
                }
            }
        }
        return null;
    }

    private static boolean isWikimediaImage(String url) {
        return url.startsWith("https://commons.wikimedia.org");
    }

    private static String getWikimediaImageUrl(String url) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            synchronized (md) {
                md.reset();
                Pattern p = Pattern.compile("File:([a-z0-9A-Z_.]+)");
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

    final protected static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return new String(hexChars);
    }

    private static byte[] inputStreamToByteArray(InputStream in)
            throws IOException {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        int r;
        while ((r = in.read()) != -1) {
            out.write(r);
        }
        return out.toByteArray();
    }

    private static void saveBitmap(Bitmap bitmap, File dir, String filename) {

        File file = new File(dir, filename);
        try {
            FileOutputStream out = new FileOutputStream(file);
            bitmap.compress(CompressFormat.PNG, 100, out);
            out.close();
        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            Log.e("io", "io", e);
        } catch (IOException e) {
            // TODO Auto-generated catch block
            Log.e("io", "io", e);
        }
    }

    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static void loadArtistImageAsync(final String artistName,
                                            final ImageView view) {

        Drawable currentDrawable = view.getDrawable();
        if (currentDrawable instanceof TransitionDrawable) {
            currentDrawable = ((TransitionDrawable) currentDrawable)
                    .getDrawable(1);
        }

        final Drawable oldDrawable = currentDrawable;

        AsyncTask<Void, Void, Drawable> task = new AsyncTask<Void, Void, Drawable>() {

            @Override
            protected Drawable doInBackground(Void... params) {

                if (sDefaultArtist == null) {
                    sDefaultArtist = view.getContext().getResources()
                            .getDrawable(R.drawable.default_artwork);

                }
                Drawable drawable = getArtistImage(view.getContext(),
                        artistName);
                if (drawable == null) {
                    drawable = sDefaultArtist;
                }

                if (oldDrawable != null && drawable != null
                        && oldDrawable != drawable) {
                    TransitionDrawable transition = new TransitionDrawable(
                            new Drawable[]{oldDrawable, drawable});
                    transition.setCrossFadeEnabled(true);
                    return transition;
                }
                return drawable;
            }

            @Override
            protected void onPostExecute(Drawable result) {
                view.setImageDrawable(result);
                if (result instanceof TransitionDrawable) {
                    ((TransitionDrawable) result).startTransition(200);
                }

            }

        };

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            task.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, (Void) null);
        } else {
            task.execute((Void) null);
        }
    }

    public static void clearArtistImageCache() {
        sArtistCache.clear();
    }
}
