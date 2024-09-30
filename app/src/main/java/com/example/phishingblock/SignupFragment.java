package com.example.phishingblock;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.Calendar;

public class SignupFragment extends AppCompatActivity {

    private EditText etEmail, etPassword, etConfirmPassword, etPhone, etBirthDate;
    private Button btnSignup;
    private TextView tvLogin;
    private boolean isPasswordVisible = false;
    private boolean isConfirmPasswordVisible = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_signup);

        // 뷰 연결
        etEmail = findViewById(R.id.et_email);
        etPassword = findViewById(R.id.et_password);
        etConfirmPassword = findViewById(R.id.et_confirm_password);
        etPhone = findViewById(R.id.et_phone);
        etBirthDate = findViewById(R.id.et_birth_date);
        btnSignup = findViewById(R.id.btn_signup);
        tvLogin = findViewById(R.id.tv_login);

        // 회원가입 버튼 클릭 리스너
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 회원가입 로직 처리 (예: Firebase Authentication 또는 서버로의 회원가입 요청)

                // 로그인 화면으로 이동(회원가입 완료시 이동되도록)
                Intent intent = new Intent(SignupFragment.this, LoginFragment.class);
                startActivity(intent);
            }
        });

        // 로그인 이동 버튼 클릭 리스너
        tvLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 로그인 화면으로 이동
                Intent intent = new Intent(SignupFragment.this, LoginFragment.class);
                startActivity(intent);
            }
        });

        // 비밀번호 보기/숨기기 기능 설정
        etPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (etPassword.getRight() - etPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        isPasswordVisible = !isPasswordVisible;
                        if (isPasswordVisible) {
                            // 비밀번호 보이기 설정
                            etPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye_off, 0);
                        } else {
                            // 비밀번호 숨기기 설정
                            etPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            etPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye, 0);
                        }
                        etPassword.setSelection(etPassword.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });

        // 비밀번호 확인 보기/숨기기 기능 설정
        etConfirmPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_RIGHT = 2;
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    if (event.getRawX() >= (etConfirmPassword.getRight() - etConfirmPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        isConfirmPasswordVisible = !isConfirmPasswordVisible;
                        if (isConfirmPasswordVisible) {
                            // 비밀번호 확인 보이기 설정
                            etConfirmPassword.setInputType(InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD);
                            etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye_off, 0);
                        } else {
                            // 비밀번호 확인 숨기기 설정
                            etConfirmPassword.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                            etConfirmPassword.setCompoundDrawablesWithIntrinsicBounds(R.drawable.ic_lock, 0, R.drawable.ic_eye, 0);
                        }
                        etConfirmPassword.setSelection(etConfirmPassword.getText().length());
                        return true;
                    }
                }
                return false;
            }
        });

        // 생년월일 입력 필드 클릭 시 DatePickerDialog 표시 (spinner 모드 적용)
        etBirthDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 현재 날짜를 기본값으로 설정
                final Calendar calendar = Calendar.getInstance();
                int year = calendar.get(Calendar.YEAR);
                int month = calendar.get(Calendar.MONTH);
                int day = calendar.get(Calendar.DAY_OF_MONTH);

                // DatePickerDialog 생성 및 표시 (spinner 모드 적용)
                DatePickerDialog datePickerDialog = new DatePickerDialog(SignupFragment.this, android.R.style.Theme_Holo_Light_Dialog_MinWidth,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // 선택된 날짜를 EditText에 설정 (형식: YYYY-MM-DD)
                                etBirthDate.setText(String.format("%04d-%02d-%02d", year, month + 1, dayOfMonth));
                            }
                        }, year, month, day);

                // spinner 모드로 설정하여 연도 및 월 선택을 쉽게 변경
                datePickerDialog.getDatePicker().setCalendarViewShown(false); // 캘린더 모드 비활성화
                datePickerDialog.show();
            }
        });
    }
}
