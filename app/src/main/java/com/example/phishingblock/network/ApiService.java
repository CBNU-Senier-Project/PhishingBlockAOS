package com.example.phishingblock.network;



import com.example.phishingblock.network.payload.AcceptInvitationRequest;
import com.example.phishingblock.network.payload.AddReportItemRequest;
import com.example.phishingblock.network.payload.DetailPhishingDataRequest;
import com.example.phishingblock.network.payload.DetailPhishingDataResponse;
import com.example.phishingblock.network.payload.GroupRequest;
import com.example.phishingblock.network.payload.InvitationResponse;
import com.example.phishingblock.network.payload.InviteMemberRequest;
import com.example.phishingblock.network.payload.LoginRequest;
import com.example.phishingblock.network.payload.LoginResponse;
import com.example.phishingblock.network.payload.ReportItemResponse;
import com.example.phishingblock.network.payload.SearchPhishingDataRequest;
import com.example.phishingblock.network.payload.SearchPhishingDataResponse;
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

    // 신고 데이터 추가
    @POST("/phish/api/v1/add")
    Call<Void> addReportItem(@Header("Authorization") String token, @Body AddReportItemRequest addreportItemRequest);

    // 신고 데이터 조회 type별
    @GET("/phish/api/v1/data")
    Call<List<ReportItemResponse>> getReportItems(@Query("type") String type);

    // 피싱 데이터 검색 API (리스트 형태로 받음)
    @POST("/phish/api/v1/search/type-and-value")
    Call<List<SearchPhishingDataResponse>> searchPhishingData(@Body SearchPhishingDataRequest searchRequest);

    // 피싱 데이터 세부 사항 조회
    @POST("/phish/api/v1/detail/search")
    Call<List<DetailPhishingDataResponse>> DetailPhishingData(@Body DetailPhishingDataRequest DetailRequest);

    // 초대 수락 API
    @PATCH("/user/api/v1/groups/invitations/{invitationId}/status")
    Call<Void> acceptInvitation(@Path("invitationId") long invitationId, @Body AcceptInvitationRequest request);

    // 초대장 조회 API
    @GET("/user/api/v1/groups/invitations/{receive_id}")
    Call<List<InvitationResponse>> getInvitations(@Header("Authorization") String token, @Path("receive_id") long receiveId);

    // 그룹원 초대 API
    @POST("/user/api/v1/groups/{groupId}/invite")
    Call<Void> inviteMember(@Path("groupId") long groupId, @Body InviteMemberRequest request);

}

