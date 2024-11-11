package com.example.phishingblock.home;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phishingblock.R;
import com.example.phishingblock.background.UserManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.GroupRequest;
import com.example.phishingblock.network.payload.LoginRequest;
import com.example.phishingblock.network.payload.LoginResponse;
import com.example.phishingblock.network.payload.RegisterFCMTokenRequest;
import com.example.phishingblock.network.payload.SignUpRequest;
import com.example.phishingblock.network.payload.UserProfileResponse;
import com.google.firebase.messaging.FirebaseMessaging;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupFragment extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword, etPhone, etNickname;
    private Button btnSignup, btnCheckEmail; // Add btnCheckEmail
    private TextView tvLogin;
    private String email, password, confirmPassword, phone, nickname;
    private boolean isEmailAvailable = false; // Variable to track email availability

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etPhone = findViewById(R.id.et_phone);
        etNickname = findViewById(R.id.et_nickname);

        btnSignup = findViewById(R.id.btn_signup);
        btnCheckEmail = findViewById(R.id.btn_check_email2); // Initialize the email check button
        tvLogin = findViewById(R.id.tv_login);

        // Email check button listener
        btnCheckEmail.setOnClickListener(v -> {
            email = etEmail.getText().toString().trim();
            if (email.isEmpty()) {
                Toast.makeText(SignupFragment.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Call the email check API
            ApiService apiService = RetrofitClient.getApiService();
            Call<Void> call = apiService.checkEmailDuplicate(email);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        isEmailAvailable = true;
                        Toast.makeText(SignupFragment.this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        isEmailAvailable = false;
                        Toast.makeText(SignupFragment.this, "이미 사용 중인 이메일입니다.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    isEmailAvailable = false;
                    Toast.makeText(SignupFragment.this, "오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    Log.e("SignupFragment", "이메일 중복 확인 중 오류 발생", t);
                }
            });
        });

        // Signup button click listener
        btnSignup.setOnClickListener(v -> {
            // Get input values
            email = etEmail.getText().toString().trim();
            password = etPassword.getText().toString().trim();
            confirmPassword = etConfirmPassword.getText().toString().trim();
            phone = etPhone.getText().toString().trim();
            nickname = etNickname.getText().toString().trim();

            // Input validation
            if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty() || nickname.isEmpty()) {
                Toast.makeText(SignupFragment.this, "모든 페이지를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(SignupFragment.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!isEmailAvailable) {
                Toast.makeText(SignupFragment.this, "이메일 중복 검사를 진행해주세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Create SignUpRequest
            SignUpRequest.UserCertification userCertification = new SignUpRequest.UserCertification(email, password);
            SignUpRequest.UserInfo userInfo = new SignUpRequest.UserInfo(nickname, phone, "");
            SignUpRequest signUpRequest = new SignUpRequest(userCertification, userInfo);

            // API call
            ApiService apiService = RetrofitClient.getApiService();
            Call<Void> call = apiService.signUp(signUpRequest);
            call.enqueue(new Callback<Void>() {
                @Override
                public void onResponse(Call<Void> call, Response<Void> response) {
                    if (response.isSuccessful()) {
                        Toast.makeText(SignupFragment.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();

                        // 액세스 토큰을 가지고 그룹 생성 API 호출
                        loginAfterSignup(email, password); // 이메일과 비밀번호로 로그인
                    } else {
                        Toast.makeText(SignupFragment.this, "회원 가입을 거부합니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onFailure(Call<Void> call, Throwable t) {
                    Toast.makeText(SignupFragment.this, "오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            });
        });

        // Login button click listener
        tvLogin.setOnClickListener(v -> {
            Intent intent = new Intent(SignupFragment.this, LoginFragment.class);
            startActivity(intent);
        });
    }

    private void loginAfterSignup(String email, String password) {
        ApiService apiService = RetrofitClient.getApiService();
        LoginRequest loginRequest = new LoginRequest(email, password);

        Call<LoginResponse> loginCall = apiService.login(loginRequest);
        loginCall.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String accessToken = response.body().getAccessToken(); // 로그인 후 받은 토큰
                    createGroupForUser(accessToken); // 그룹 생성 후 그룹 ID 로드
                    Intent intent = new Intent(SignupFragment.this, LoginFragment.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(SignupFragment.this, "로그인 실패.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(SignupFragment.this, "로그인 중 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void createGroupForUser(String token) {
        ApiService apiService = RetrofitClient.getApiService();
        GroupRequest groupRequest = new GroupRequest("그룹초대장");

        Call<Void> call = apiService.createGroup(token, groupRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SignupFragment.this, "그룹 생성 성공!", Toast.LENGTH_SHORT).show();

                    // 그룹 생성 후 사용자 프로필 로드 및 그룹 ID 가져오기
                    loadUserProfile(token);
                } else {
                    Toast.makeText(SignupFragment.this, "그룹 생성 실패.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(SignupFragment.this, "그룹 생성 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadUserProfile(String accessToken) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<UserProfileResponse> call = apiService.getUserProfile(accessToken);

        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse userProfile = response.body();
                    long userId = userProfile.getUserId(); // 사용자 ID 가져오기
                    loadGroupId(accessToken, userId); // 그룹 ID 가져오기
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Toast.makeText(SignupFragment.this, "유저 프로필 로드 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void loadGroupId(String accessToken, long userId) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<Long>> groupCall = apiService.getGroupIds(accessToken, userId);

        groupCall.enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    long groupId = response.body().get(0); // 첫 번째 그룹 ID 가져오기
                    registerFCMToken(accessToken, userId, groupId); // FCM 토큰 등록
                } else {
                    Toast.makeText(SignupFragment.this, "그룹 ID를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                Toast.makeText(SignupFragment.this, "그룹 ID 로드 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void registerFCMToken(String accessToken, long userId, long groupId) {
        FirebaseMessaging.getInstance().getToken().addOnCompleteListener(task -> {
            if (task.isSuccessful()) {
                String fcmToken = task.getResult();
                Log.d("SignupFragment", "FCM Token: " + fcmToken);

                // FCM 토큰을 서버에 등록하는 API 호출
                ApiService apiService = RetrofitClient.getApiService();
                RegisterFCMTokenRequest request = new RegisterFCMTokenRequest(userId, groupId, fcmToken, "ANDROID", true);

                Call<Void> registerCall = apiService.registerFCMToken(accessToken, request);
                registerCall.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignupFragment.this, "FCM 토큰 등록 성공!", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(SignupFragment.this, "FCM 토큰 등록 실패", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(SignupFragment.this, "FCM 토큰 등록 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Log.e("SignupFragment", "Failed to get FCM token", task.getException());
                Toast.makeText(SignupFragment.this, "FCM 토큰을 가져오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

}
