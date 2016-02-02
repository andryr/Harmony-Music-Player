/*
 * Copyright 2016 andryr
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.andryr.musicplayer.api.lastfm;

import com.andryr.musicplayer.api.ApiKeys;

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
                    .url(original.url() + "&api_key=" + ApiKeys.API_KEY)
                    .build();

            return chain.proceed(request);

        }
    }
}
