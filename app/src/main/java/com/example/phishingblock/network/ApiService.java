package com.example.phishingblock.network;



import com.example.phishingblock.network.payload.AcceptInvitationRequest;
import com.example.phishingblock.network.payload.AddReportItemRequest;
import com.example.phishingblock.network.payload.CallDialogueRequest;
import com.example.phishingblock.network.payload.DetailPhishingDataResponse;
import com.example.phishingblock.network.payload.GroupMemberResponse;
import com.example.phishingblock.network.payload.GroupRequest;
import com.example.phishingblock.network.payload.ImageRequest;
import com.example.phishingblock.network.payload.ImageResponse;
import com.example.phishingblock.network.payload.InvitationResponse;
import com.example.phishingblock.network.payload.InviteMemberRequest;
import com.example.phishingblock.network.payload.LoginRequest;
import com.example.phishingblock.network.payload.LoginResponse;
import com.example.phishingblock.network.payload.NewsResponse;
import com.example.phishingblock.network.payload.NicknameRequest;
import com.example.phishingblock.network.payload.PredictionResponse;
import com.example.phishingblock.network.payload.RecentPhishingResponse;
import com.example.phishingblock.network.payload.RegisterFCMTokenRequest;
import com.example.phishingblock.network.payload.ReportItemResponse;
import com.example.phishingblock.network.payload.SearchPhishingDataRequest;
import com.example.phishingblock.network.payload.SearchPhishingDataResponse;
import com.example.phishingblock.network.payload.SignUpRequest;
import com.example.phishingblock.network.payload.UserIdResponse;
import com.example.phishingblock.network.payload.UserProfileRequest;
import com.example.phishingblock.network.payload.UserProfileResponse;
import com.google.api.client.auth.oauth2.TokenRequest;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.PATCH;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {
    @POST("/user/api/v1/user/signup")
    Call<Void> signUp(@Body SignUpRequest request);

    // Email duplicate check API
    @GET("/user/api/v1/user/users/check")
    Call<Void> checkEmailDuplicate(@Query("email") String email);

    // 로그인 API
    @POST("/user/api/v1/auth/signin")
    Call<LoginResponse> login(@Body LoginRequest loginRequest);

    // 로그아웃 API
    @POST("/user/api/v1/auth/signout")
    Call<Void> logout(
            @Header("Authorization") String accessToken,@Header("RefreshToken") String refreshToken   // AccessToken 헤더
    );

    @POST("/user/api/v1/user/resign")
    Call<Void> resign(
            @Header("Authorization") String accessToken
    );

    // 그룹 생성 API
    @POST("/user/api/v1/groups")
    Call<Void> createGroup(@Header("Authorization") String token, @Body GroupRequest groupRequest);

    // 신고 데이터 추가
    @POST("/user/api/v1/phish/add")
    Call<Void> addReportItem(@Header("Authorization") String token, @Body AddReportItemRequest addreportItemRequest);

    // 신고 데이터 조회 type별
    @GET("/user/api/v1/phish/data")
    Call<List<ReportItemResponse>> getReportItems(@Header("Authorization") String authorizationToken,@Query("type") String type);

    // 피싱 데이터 검색 API (리스트 형태로 받음)
    @POST("/user/api/v1/phish/search/type-and-value")
    Call<List<SearchPhishingDataResponse>> searchPhishingData(@Header("Authorization") String token,@Body SearchPhishingDataRequest searchRequest);

    @POST("/user/api/v1/phish/detail/search")
    Call<List<DetailPhishingDataResponse>> DetailPhishingData(
            @Header("Authorization") String token,
            @Query("phishingType") String phishingType,
            @Query("value") String value
    );


    // 전화번호로 회원 ID 조회
    @GET("/user/api/v1/user/users/phone/{phoneNumber}")
    Call<UserIdResponse> getUserIdByPhoneNumber(@Header("Authorization") String token,@Path("phoneNumber") String phoneNumber);

    // 그룹 초대 메시지 전송 API
    @POST("/user/api/v1/groups/{groupId}/invite")
    Call<Void> inviteMember(
            @Header("Authorization") String authorizationToken,
            @Path("groupId") long groupId,
            @Body InviteMemberRequest inviteMemberRequest
    );

    // 초대장 리스트 조회 API
    @GET("/user/api/v1/groups/invitations/{receive_id}")
    Call<List<InvitationResponse>> getInvitations(
            @Header("Authorization") String authorizationToken,
            @Path("receive_id") long receiveId
    );

    // 초대 수락 및 거절 API
    @PATCH("/user/api/v1/groups/invitations/{invitationId}/status")
    Call<Void> acceptInvitation(
            @Header("Authorization") String token,  // Authorization 토큰
            @Path("invitationId") long invitationId,  // 초대 ID
            @Body AcceptInvitationRequest request  // 상태(ACCEPTED/REJECTED)를 담은 요청 본문
    );

    // 회원 정보 조회 API
    @GET("/user/api/v1/user/users/profile")
    Call<UserProfileResponse> getUserProfile(@Header("Authorization") String authorization);

    // 그룹 ID 조회 API
    @GET("/user/api/v1/groups/creator/{creatorId}/group-ids")
    Call<List<Long>> getGroupIds(@Header("Authorization") String token,@Path("creatorId") long creatorId);

    // 그룹 멤버 조회 API
    @GET("/user/api/v1/groups/group/{groupId}/members")
    Call<List<GroupMemberResponse>> getGroupMembers(
            @Header("Authorization") String token,
            @Path("groupId") long groupId
    );

    // 그룹 멤버 삭제 API
    @DELETE("/user/api/v1/groups/{groupId}/members/{memberId}")
    Call<Void> deleteGroupMember(
            @Path("groupId") long groupId,
            @Path("memberId") long memberId,
            @Header("Authorization") String token
    );
    // 회원 정보 수정 API
    @PUT("/user/api/v1/user/users/edit")
    Call<Void> editUserProfile(
            @Header("Authorization") String token,     // 인증 토큰
            @Body UserProfileRequest userProfileRequest  // 요청 본문
    );

    //ai 판단
    @POST("predict/api/v1/predict")
    Call<PredictionResponse> predictdialogue(
            @Header("Authorization") String token,
            @Body CallDialogueRequest callDialogueRequset
    );

    //크롤링
    @GET("user/api/v1/news/view")
    Call<List<NewsResponse>> getNews(@Header("Authorization") String token);

    @PATCH("/user/api/v1/groups/{groupId}/members/{memberId}/nickname")
    Call<Void> updateGroupMemberNickname(
            @Header("Authorization") String authorization,
            @Path("groupId") long groupId,
            @Path("memberId") long memberId,
            @Body NicknameRequest nicknameRequest
    );

    // FCM 토큰 등록 API
    @POST("/noti/api/v1/register")
    Call<Void> registerFCMToken(
            @Header("Authorization") String authorization,
            @Body RegisterFCMTokenRequest request  // 요청 본문
    );

    //최근 신고 내역
    @GET("/user/api/v1/phish/latest")
    Call<List<RecentPhishingResponse>> getLatestPhishingData(@Header("Authorization") String authorization);

    //맴버 이미지 수정
    @PATCH("/user/api/v1/groups/{groupId}/members/{userId}/image")
    Call<ImageResponse> updateImage(
            @Header("Authorization") String token,
            @Path("groupId") long groupId,
            @Path("userId") long userId,
            @Body ImageRequest imageRequest
    );
}

