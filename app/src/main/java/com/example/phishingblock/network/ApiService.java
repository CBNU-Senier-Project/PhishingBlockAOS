package com.example.phishingblock.network;



import com.example.phishingblock.network.payload.SignUpRequest;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.POST;

public interface ApiService {
    @POST("user/api/v1/signup")
    Call<Void> signUp(@Body SignUpRequest request);
}
