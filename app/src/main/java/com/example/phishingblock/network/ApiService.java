package com.example.phishingblock.network;



import com.example.phishingblock.network.payload.AcceptInvitationRequest;
import com.example.phishingblock.network.payload.GroupRequest;
import com.example.phishingblock.network.payload.InvitationResponse;
import com.example.phishingblock.network.payload.InviteMemberRequest;
import com.example.phishingblock.network.payload.LoginRequest;
import com.example.phishingblock.network.payload.LoginResponse;
import com.example.phishingblock.network.payload.SignUpRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("user/api/v1/signup")
    Call<Void> signUp(@Body SignUpRequest request);

    // Email duplicate check API
    @GET("/user/api/v1/users/check")
    Call<Void> checkEmailDuplicate(@Query("email") String email);

    // 로그인 API
    @POST("/user/api/v1/signin")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    // 그룹 생성 API
    @POST("/user/api/v1/groups")
    Call<Void> createGroup(@Header("Authorization") String token, @Body GroupRequest groupRequest);

    // 그룹 멤버 초대 API
    @POST("/user/api/v1/groups/{groupId}/invite")
    Call<Void> inviteMember(@Path("groupId") Long groupId, @Header("Authorization") String token, @Body InviteMemberRequest inviteMemberRequest);

    // 초대 리스트 조회 API
    @GET("/user/api/v1/groups/invitations/{receive_id}")
    Call<List<InvitationResponse>> getInvitationList(@Path("receive_id") Long receiveId, @Header("Authorization") String token);

    // 초대 수락 API
    @PATCH("/user/api/v1/groups/invitations/{invitationId}/status")
    Call<Void> acceptInvitation(@Path("invitationId") Long invitationId, @Header("Authorization") String token, @Body AcceptInvitationRequest acceptInvitationRequest);

}
