package com.andryr.musicplayer.lastfm;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

/**
 * Created by Andry on 18/01/16.
 */
public interface LastFmService {

    String API_KEY = "***REMOVED***";


    @GET("?method=artist.getInfo&format=json")
    Call<ArtistInfo> getArtistInfo(@Query("artist") String artist);

}
