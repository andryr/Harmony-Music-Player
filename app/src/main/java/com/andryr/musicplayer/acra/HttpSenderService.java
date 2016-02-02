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

package com.andryr.musicplayer.acra;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import org.acra.ACRA;
import org.acra.ACRAConfiguration;
import org.acra.sender.HttpSender;

import java.io.IOException;
import java.net.URL;
import java.util.concurrent.TimeUnit;

import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

import static org.acra.ACRA.LOG_TAG;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p/>
 * TODO: Customize class - update intent actions, extra parameters and static
 * helper methods.
 */
public class HttpSenderService extends IntentService {

    public static final String ACTION_SEND = "com.andryr.musicplayer.acra.ACTION_SEND";
    public static final String EXTRA_URL = "com.andryr.musicplayer.acra.EXTRA_URL";
    public static final String EXTRA_CONTENT = "com.andryr.musicplayer.acra.EXTRA_CONTENT";
    public static final String EXTRA_LOGIN = "com.andryr.musicplayer.acra.EXTRA_LOGIN";
    public static final String EXTRA_PASSWORD = "com.andryr.musicplayer.acra.EXTRA_PASSWORD";
    public static final MediaType MEDIA_TYPE_JSON
            = MediaType.parse("application/json; charset=utf-8");

    private OkHttpClient client;

    private int connectionTimeOut = 3000;

    public HttpSenderService() {
        super("HttpSenderService");
        client = new OkHttpClient.Builder().connectTimeout(connectionTimeOut, TimeUnit.MILLISECONDS).build();
    }


    public static void sendReport(Context context, String url, String content, String login, String password) {
        Intent intent = new Intent(context, HttpSenderService.class);
        intent.setAction(ACTION_SEND);
        intent.putExtra(EXTRA_URL, url);
        intent.putExtra(EXTRA_CONTENT, content);
        intent.putExtra(EXTRA_LOGIN, login);
        intent.putExtra(EXTRA_PASSWORD, password);
        context.startService(intent);
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            final String action = intent.getAction();
            if (ACTION_SEND.equals(action)) {
                try {

                    final URL url = new URL(intent.getStringExtra(EXTRA_URL));
                    final String content = intent.getStringExtra(EXTRA_CONTENT);
                    final String login = intent.getStringExtra(EXTRA_LOGIN);
                    final String password = intent.getStringExtra(EXTRA_PASSWORD);

                    ACRAConfiguration config = ACRA.getConfig();
                    send(url, config.httpMethod(), content, login, password);

                } catch (IOException e) {
                    Log.e("HttpSenderService", "url", e);
                }

            }
        }
    }

    /**
     * Posts to a URL.
     *
     * @param url     URL to which to post.
     * @param content Map of parameters to post to a URL.
     * @throws IOException if the data cannot be posted.
     */
    public void send(URL url, HttpSender.Method method, String content, String login, String password) throws IOException {


        Request.Builder builder = new Request.Builder().url(url);


        //final HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();

        // Configure SSL
        /*if (urlConnection instanceof HttpsURLConnection) {
            try {
                final HttpsURLConnection httpsUrlConnection = (HttpsURLConnection) urlConnection;

                final String algorithm = TrustManagerFactory.getDefaultAlgorithm();
                final TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);

                final KeyStore keyStore = ACRA.getConfig().keyStore();
                tmf.init(keyStore);

                final SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, tmf.getTrustManagers(), null);

                httpsUrlConnection.setSSLSocketFactory(sslContext.getSocketFactory());
            } catch (GeneralSecurityException e) {
                ACRA.log.e(LOG_TAG, "Could not configure SSL for ACRA request to " + url, e);
            }
        }*/

        //urlConnection.setRequestMethod(method.name());

        // Set Credentials
        /*if ((login != null) && (password != null)) {
            final String credentials = login + ":" + password;
            final String encoded = new String(Base64.encode(credentials.getBytes("UTF-8"), Base64.DEFAULT), "UTF-8");
            urlConnection.setRequestProperty("Authorization", "Basic " + encoded);
        }*/
        String credentials = Credentials.basic(login, password);
        builder.addHeader("Authorization", credentials);


        // Set Headers
        //urlConnection.setRequestProperty("User-Agent", "Android");
        //urlConnection.setRequestProperty("Accept",
        //        "text/html,application/xml,application/json,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");
        //urlConnection.setRequestProperty("Content-Type", type.getContentType());
        builder.addHeader("User-Agent", "Android");
        builder.addHeader("Accept", "text/html,application/xml,application/json,application/xhtml+xml,text/html;q=0.9,text/plain;q=0.8,image/png,*/*;q=0.5");


        final byte[] contentAsBytes = content.getBytes("UTF-8");


        // Disable ConnectionPooling because otherwise OkHttp ConnectionPool will try to start a Thread on #connect
        System.setProperty("http.keepAlive", "false");

        //urlConnection.connect();

        /*final OutputStream outputStream = new BufferedOutputStream(urlConnection.getOutputStream());
        outputStream.write(contentAsBytes);
        outputStream.flush();
        outputStream.close();*/

        Request request = builder.put(RequestBody.create(MEDIA_TYPE_JSON, contentAsBytes)).build();

        Response response = client.newCall(request).execute();

        ACRA.log.d(LOG_TAG, "Sending request to " + url);
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, "Http " + method.name() + " content : ");
        if (ACRA.DEV_LOGGING) ACRA.log.d(LOG_TAG, content);

        final int responseCode = response.code();
        ACRA.log.d(LOG_TAG, "Request response : " + responseCode + " : " + response.message());
        if ((responseCode >= 200) && (responseCode < 300)) {
            // All is good
            ACRA.log.d(LOG_TAG, "Request received by server");
        } else if (responseCode == 403) {
            // 403 is an explicit data validation refusal from the server. The request must not be repeated. Discard it.
            ACRA.log.d(LOG_TAG, "Data validation error on server - request will be discarded");
        } else if (responseCode == 409) {
            // 409 means that the report has been received already. So we can discard it.
            ACRA.log.d(LOG_TAG, "Server has already received this post - request will be discarded");
        } else if ((responseCode >= 400) && (responseCode < 600)) {
            if (ACRA.DEV_LOGGING) {
                ACRA.log.d(LOG_TAG, "Could not send ACRA Post");
            }
            throw new IOException("Host returned error code " + responseCode + " " + response.body().string());
        } else {
            ACRA.log.w(LOG_TAG, "Could not send ACRA Post - request will be discarded");
        }

    }
}
