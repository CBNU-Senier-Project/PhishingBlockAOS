package com.example.phishingblock;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.payload.SignUpRequest;
import com.example.phishingblock.network.RetrofitClient;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SignupFragment extends AppCompatActivity {
    private EditText etEmail, etPassword, etConfirmPassword, etPhone, etBirthDate, etNickname; // Add etNickname
    private Button btnSignup;
    private TextView tvLogin;
    private String email, password, confirmPassword, phone, birthDate, nickname; // Add nickname

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);

        // Initialize views
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etPhone = findViewById(R.id.et_phone);
        etBirthDate = findViewById(R.id.et_birth_date);
        etNickname = findViewById(R.id.et_nickname); // Initialize etNickname

        btnSignup = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);

        // Signup button click listener
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Get input values
                email = etEmail.getText().toString().trim();
                password = etPassword.getText().toString().trim();
                confirmPassword = etConfirmPassword.getText().toString().trim();
                phone = etPhone.getText().toString().trim();
                birthDate = etBirthDate.getText().toString().trim(); // Get birth date
                nickname = etNickname.getText().toString().trim(); // Get nickname

                // Input validation
                if (email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty() || phone.isEmpty() || birthDate.isEmpty() || nickname.isEmpty()) {
                    Toast.makeText(SignupFragment.this, "모든 페이지를 입력하세요.", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(SignupFragment.this, "비밀번호가 일치하지 않습니다.", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Create SignUpRequest
                SignUpRequest.UserCertification userCertification = new SignUpRequest.UserCertification(email, password);
                SignUpRequest.UserInfo userInfo = new SignUpRequest.UserInfo(nickname, phone, birthDate); // Include nickname
                SignUpRequest signUpRequest = new SignUpRequest(userCertification, userInfo);

                // API call
                ApiService apiService = RetrofitClient.getApiService();
                Call<Void> call = apiService.signUp(signUpRequest);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(SignupFragment.this, "회원가입 성공!", Toast.LENGTH_SHORT).show();
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
}
