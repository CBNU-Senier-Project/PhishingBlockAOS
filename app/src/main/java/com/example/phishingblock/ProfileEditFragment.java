package com.example.phishingblock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.phishingblock.background.TokenManager;
import com.example.phishingblock.background.UserManager;
import com.example.phishingblock.home.LoginFragment;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.UserProfileRequest;
import com.example.phishingblock.network.payload.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProfileEditFragment extends Fragment {

    private EditText editTextUsername;
    private EditText editTextPhone;
    private Button buttonSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        // UI 요소 초기화
        editTextUsername = view.findViewById(R.id.edittext_username);
        editTextPhone = view.findViewById(R.id.edittext_phone);
        buttonSave = view.findViewById(R.id.button_save);
        String token = TokenManager.getAccessToken(getContext());  // 저장된 토큰을 가져옴

        // 저장 버튼 클릭 시 정보 저장
        buttonSave.setOnClickListener(v -> {
            String newNickname = editTextUsername.getText().toString().trim();
            String newPhone = editTextPhone.getText().toString().trim();

            // 빈 값 확인
            if (newNickname.isEmpty() || newPhone.isEmpty()) {
                Toast.makeText(getContext(), "모든 필드를 입력하세요.", Toast.LENGTH_SHORT).show();
                return;
            }

            // API 호출을 통해 사용자 정보 수정 요청
            updateUserProfile(token, newNickname, newPhone);
            loadUserProfile(token);
        });

        return view;
    }

    private void loadUserProfile(String accessToken) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<UserProfileResponse> call = apiService.getUserProfile(accessToken);

        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    UserProfileResponse userProfile = response.body();
                    // UserManager에 프로필 저장
                    UserManager.saveUserProfile(getContext(), userProfile);
                    Toast.makeText(getContext(), "수정 후 유저 프로필 불러오기 성공!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "수정 후 유저 프로필 불러오기 실패!", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                Toast.makeText(getContext(), "프로필 불러오기 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 사용자 정보를 업데이트하는 메서드
    private void updateUserProfile(String token, String nickname, String phone) {
        ApiService apiService = RetrofitClient.getApiService();


        // 요청 객체 생성
        UserProfileRequest.UserInfo userInfo = new UserProfileRequest.UserInfo(nickname, phone);
        UserProfileRequest userProfileRequest = new UserProfileRequest(userInfo);

        // API 호출
        Call<Void> call = apiService.editUserProfile(token, userProfileRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // 성공적으로 업데이트된 경우
                    Toast.makeText(getContext(), "프로필이 성공적으로 수정되었습니다.", Toast.LENGTH_SHORT).show();
                    // 이전 화면으로 돌아가기
                    getParentFragmentManager().popBackStack();
                } else {
                    // 오류 발생 시
                    Toast.makeText(getContext(), "프로필 수정 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 네트워크 오류 또는 서버 오류 처리
                Toast.makeText(getContext(), "프로필 수정 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
