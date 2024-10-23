package com.example.phishingblock.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import java.util.concurrent.TimeUnit;

public class RetrofitClient {

    private static final String BASE_URL = "http://34.64.59.85:8080/";

    private static Retrofit retrofit = null;

    // Singleton 패턴을 사용하여 Retrofit 인스턴스를 관리
    public static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    // 로깅 인터셉터 추가
                    HttpLoggingInterceptor logging = new HttpLoggingInterceptor();
                    logging.setLevel(HttpLoggingInterceptor.Level.BODY);

                    OkHttpClient client = new OkHttpClient.Builder()
                            .addInterceptor(logging) // 로깅 인터셉터 추가
                            .connectTimeout(30, TimeUnit.SECONDS)
                            .readTimeout(30, TimeUnit.SECONDS)
                            .build();

                    Gson gson = new GsonBuilder().setLenient().create();
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
                            .client(client) // OkHttp 클라이언트 추가
                            .addConverterFactory(GsonConverterFactory.create(gson))
                            .build();
                }
            }
        }
        return retrofit;
    }

    // 예를 들어, API에 접근할 서비스 인터페이스를 제공하는 메소드
    public static ApiService getApiService() {
        return getInstance().create(ApiService.class);
    }
}
