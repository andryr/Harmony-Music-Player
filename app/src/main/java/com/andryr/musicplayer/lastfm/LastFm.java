package com.andryr.musicplayer.lastfm;

import java.io.IOException;

import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Call;
import retrofit2.MoshiConverterFactory;
import retrofit2.Retrofit;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Andry on 23/01/16.
 */
public class LastFm {

    private static final String API_KEY = "***REMOVED***";

    private static final OkHttpClient sHttpClient = new OkHttpClient.Builder().addInterceptor(new RequestInterceptor()).build();


    private static final LastFmService sService = new Retrofit.Builder()
            .baseUrl("http://ws.audioscrobbler.com/2.0/")
            .client(sHttpClient)
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(LastFm.LastFmService.class);


    public static Call<ArtistInfo> getArtistInfo(String artist) {
        return sService.getArtistInfo(artist);
    }

    public static Call<AlbumInfo> getAlbumInfo(String album, String artist) {
        return sService.getAlbumInfo(album, artist);
    }

    public interface LastFmService {


        @GET("?method=artist.getInfo&format=json")
        Call<ArtistInfo> getArtistInfo(@Query("artist") String artist);

        @GET("?method=album.getInfo&format=json")
        Call<AlbumInfo> getAlbumInfo(@Query("album") String album, @Query("artist") String artist);


    }

    private static class RequestInterceptor implements Interceptor {
        @Override
        public Response intercept(Chain chain) throws IOException {
            Request original = chain.request();

            Request request = original.newBuilder()
                    .url(original.url() + "&api_key=" + LastFm.API_KEY)
                    .build();

            return chain.proceed(request);

        }
    }
}
