package com.example.geofencinglocation.retrofitsdk;

import android.content.Context;

import com.example.geofencinglocation.BuildConfig;
import com.example.geofencinglocation.R;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class APIClient {
    private final Retrofit retrofit;
    private APIInterface APIInterface;

    public APIClient(Retrofit retrofit) {
        this.retrofit = retrofit;
        createService();
    }

    public static class Builder {
        public Builder() {
        }
        public APIClient build(Context context) {

            OkHttpClient.Builder okHttpClient = new OkHttpClient.Builder();
            HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor();
            loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);
            if (BuildConfig.DEBUG) {
                okHttpClient.addInterceptor(loggingInterceptor);
            }
            Retrofit retrofit = null;
                retrofit = new Retrofit.Builder()
                        .client(okHttpClient.build())
                        .addConverterFactory(GsonConverterFactory.create())
                        .baseUrl(context.getResources().getString(R.string.base_url))
                        .build();

                return new APIClient(retrofit);

        }

    }
    private void createService() {
        APIInterface = retrofit.create(APIInterface.class);
    }
    public APIInterface getAPIInterface(){
        return APIInterface;
    }
}
