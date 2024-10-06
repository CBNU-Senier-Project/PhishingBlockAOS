package com.example.phishingblock.network;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static final String BASE_URL = "http://172.27.64.51:8082";


    private static Retrofit retrofit = null;

    // Singleton 패턴을 사용하여 Retrofit 인스턴스를 관리
    public static Retrofit getInstance() {
        if (retrofit == null) {
            synchronized (RetrofitClient.class) {
                if (retrofit == null) {
                    Gson gson = new GsonBuilder().setLenient().create();
                    retrofit = new Retrofit.Builder()
                            .baseUrl(BASE_URL)
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
