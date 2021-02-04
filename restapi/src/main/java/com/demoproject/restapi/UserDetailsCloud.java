package com.demoproject.restapi;

import android.annotation.SuppressLint;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import com.demoproject.restapi.models.Data;
import com.demoproject.restapi.models.UserDetailsData;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * UserDetails
 * <p>
 * Singleton Class.
 *
 * @author Gbayesola Emmanuel
 * @role Software Engineer
 * @created 31/1/2021 02:00 AM
 */

public class UserDetailsCloud {
    @SuppressLint("StaticFieldLeak")
    private static UserDetailsCloud instance;
    private static UserDetailsAPI apiService;

    Context context;


    /**
     * Private UserDetails constructor.
     *
     * @param context Application Context.
     */
    private UserDetailsCloud(final Context context) {
        this.context = context;

        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .cache(new Cache(context.getCacheDir(), 10 * 1024 * 1024))
                .addInterceptor(new Interceptor() {
                    @Override
                    public okhttp3.Response intercept(Chain chain) throws IOException {
                        Request request = chain.request();
                        if (hasNetwork(context))
                            request = request.newBuilder().header("Cache-Control", "public, max-age=" + 60).build();
                        else
                            request = request.newBuilder().header("Cache-Control", "public, only-if-cached, max-stale=" + 60 * 60 * 24 * 7).build();
                        return chain.proceed(request);
                    }
                })
                .build();

        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        httpClient.readTimeout(60, TimeUnit.SECONDS);
        // httpClient.writeTimeout(60, TimeUnit.SECONDS);
        if (BuildConfig.DEBUG) {
            // Intercept and Log Response Body in Debug Mode.
            HttpLoggingInterceptor interceptor = new HttpLoggingInterceptor();
            interceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            httpClient.addInterceptor(interceptor);
        }


        Retrofit.Builder retrofitBuilder = new Retrofit.Builder();

        retrofitBuilder.addConverterFactory(GsonConverterFactory.create())
                .client(okHttpClient);

        apiService = retrofitBuilder.baseUrl(UserDetailsCloudConfig.getInstance().getBaseUrl()).build().create(UserDetailsAPI.class);
    }

    public static Boolean hasNetwork(Context context) {
        boolean isConnected = false; // Initial Value
        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
        if (activeNetwork != null && activeNetwork.isConnectedOrConnecting())
            isConnected = true;
        return isConnected;
    }

    public UserDetailsAPI getApiService() {
        return apiService;
    }

    /**
     * Singleton Get instance method
     *
     * @param context Application Context.
     * @return UserDetails Instance.
     */
    public static UserDetailsCloud getInstance(Context context) {
        if (instance == null) {
            synchronized (UserDetailsCloud.class) {
                if (instance == null) {
                    instance = new UserDetailsCloud(context);
                }
            }
        }
        return instance;
    }

    /**
     * Get all users details.
     *
     * @param appID    App ID.
     * @param callback Retrofit Callback.
     */
    public void getUsersDetails(String appID, Callback<Data> callback) {
        Call<Data> asyncFetch = apiService.getUsersDetails(appID);
        asyncFetch.enqueue(callback);
    }

    /**
     * Get particular user details.
     *
     * @param appID    App ID.
     * @param userID   User ID.
     * @param callback Retrofit Callback.
     */
    public void getUserDetailData(String appID, String userID, Callback<UserDetailsData> callback) {
        Call<UserDetailsData> asyncFetch = apiService.getUserDetailData(appID, userID);
        asyncFetch.enqueue(callback);
    }
}
