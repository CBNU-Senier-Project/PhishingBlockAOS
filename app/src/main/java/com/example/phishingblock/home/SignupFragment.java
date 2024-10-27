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
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.GroupRequest;
import com.example.phishingblock.network.payload.LoginRequest;
import com.example.phishingblock.network.payload.LoginResponse;
import com.example.phishingblock.network.payload.SignUpRequest;

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
        btnCheckEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                email = etEmail.getText().toString().trim();
                if (email.isEmpty()) {
                    Toast.makeText(SignupFragment.this, "이메일을 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Call the email check API
                ApiService apiService = RetrofitClient.getApiService();
                Call<Void> call = apiService.checkEmailDuplicate(email); // Using the correct API path
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            // Email is available
                            isEmailAvailable = true;
                            Toast.makeText(SignupFragment.this, "사용 가능한 이메일입니다.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Email is already in use or invalid response
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
            }
        });

        // Signup button click listener
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
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

                            // 로그인 화면으로 이동
                            Intent intent = new Intent(SignupFragment.this, LoginFragment.class);
                            startActivity(intent);
                            finish(); // End current activity
                        } else {
                            Toast.makeText(SignupFragment.this, "회원 가입을 거부합니다. 다시 시도하세요.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(SignupFragment.this, "오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });

        // Login button click listener
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(SignupFragment.this, LoginFragment.class);
                startActivity(intent);
            }
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
                    createGroupForUser(accessToken); // 그룹 생성
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
        GroupRequest groupRequest = new GroupRequest("New User Group"); // 기본 그룹 이름

        Call<Void> call = apiService.createGroup( token, groupRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(SignupFragment.this, "그룹 생성 성공!", Toast.LENGTH_SHORT).show();
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
}
