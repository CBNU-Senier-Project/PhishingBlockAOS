package com.example.phishingblock.home;

import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import com.example.phishingblock.MainActivity;
import com.example.phishingblock.R;
import com.example.phishingblock.background.TokenManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.LoginRequest;
import com.example.phishingblock.network.payload.LoginResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.text.Editable;
import android.text.TextWatcher;

public class LoginFragment extends AppCompatActivity {

    private EditText etEmail, etPassword;
    private Button btnLogin, btnSignup;
    private boolean isPasswordVisible = false; // 비밀번호 가시성 상태 변수

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_login);

        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        btnLogin = findViewById(R.id.btn_login);
        btnSignup = findViewById(R.id.tv_signup);
        btnLogin.setEnabled(false);
        btnLogin.setBackgroundResource(R.drawable.button_secondary);
        // TextWatcher로 이메일과 비밀번호 입력 상태를 감지
        TextWatcher loginTextWatcher = new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String emailInput = etEmail.getText().toString().trim();
                String passwordInput = etPassword.getText().toString().trim();

                // 이메일과 비밀번호가 모두 입력되면 로그인 버튼 활성화
                if (!emailInput.isEmpty() && !passwordInput.isEmpty()) {
                    btnLogin.setEnabled(true);
                    btnLogin.setBackgroundResource(R.drawable.button_primary); // 활성화 상태일 때의 배경 리소스
                } else {
                    btnLogin.setEnabled(false);
                    btnLogin.setBackgroundResource(R.drawable.button_secondary); // 비활성화 상태일 때의 배경 리소스
                }
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };

        etEmail.addTextChangedListener(loginTextWatcher);
        etPassword.addTextChangedListener(loginTextWatcher);

        // 나머지 버튼 및 이벤트 리스너 설정
        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = etEmail.getText().toString().trim();
                String password = etPassword.getText().toString().trim();

                if (!email.isEmpty() && !password.isEmpty()) {
                    performLogin(email, password);
                } else {
                    Toast.makeText(LoginFragment.this, "이메일과 비밀번호를 입력하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginFragment.this, SignupFragment.class);
                startActivity(intent);
            }
        });

        // 비밀번호 가시성 토글 기능 추가
        etPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2; // 오른쪽 Drawable index
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        // 비밀번호 가시성 상태 토글
                        if (isPasswordVisible) {
                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD); // 비밀번호 숨기기
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye, 0); // 눈 아이콘 변경
                        } else {
                            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD); // 비밀번호 보이기
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye_off, 0); // 눈 감긴 아이콘 변경
                        }
                        isPasswordVisible = !isPasswordVisible; // 상태 변경
                        etPassword.setSelection(etPassword.getText().length()); // 커서 위치 설정
                        return true;
                    }
                }
                return false;
            }
        });
    }

    private void performLogin(String email, String password) {
        ApiService apiService = RetrofitClient.getApiService();

        LoginRequest loginRequest = new LoginRequest(email, password);
        Call<LoginResponse> call = apiService.login(loginRequest);

        call.enqueue(new Callback<LoginResponse>() {
            @Override
            public void onResponse(Call<LoginResponse> call, Response<LoginResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String accessToken = response.body().getAccessToken();
                    String refreshToken = response.body().getRefreshToken();

                    // 토큰 저장
                    TokenManager.saveAccessToken(LoginFragment.this, accessToken);
                    TokenManager.saveRefreshToken(LoginFragment.this, refreshToken);

                    Toast.makeText(LoginFragment.this, "로그인 성공!", Toast.LENGTH_SHORT).show();

                    // 메인 액티비티로 이동
                    Intent intent = new Intent(LoginFragment.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    Toast.makeText(LoginFragment.this, "로그인 실패. 이메일 또는 비밀번호를 확인하세요.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<LoginResponse> call, Throwable t) {
                Toast.makeText(LoginFragment.this, "오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
