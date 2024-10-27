package com.example.phishingblock;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.example.phishingblock.background.TokenManager;
import com.example.phishingblock.home.LoginFragment;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.UserProfileResponse;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SettingFragment extends Fragment {

    private TextView textViewProfileName;
    private TextView textViewProfilePhone;
    private Button buttonChangeProfile;
    private Button buttonLogout;
    private Button buttonResign;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // UI 요소 초기화
        textViewProfileName = view.findViewById(R.id.textview_profile_name);
        textViewProfilePhone = view.findViewById(R.id.textview_profile_phone);
        buttonChangeProfile = view.findViewById(R.id.button_change_profile);
        buttonLogout = view.findViewById(R.id.button_logout);
        buttonResign = view.findViewById(R.id.button_resign);
        // 사용자 정보 API 호출
        loadUserProfile();

        // 프로필 변경 버튼 클릭 시 프로필 수정 프래그먼트로 이동
        buttonChangeProfile.setOnClickListener(v -> {
            // Fragment 전환 코드
            FragmentManager fragmentManager = getParentFragmentManager();
            fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, new ProfileEditFragment()) // ProfileEditFragment로 전환
                    .addToBackStack(null)
                    .commit();
        });

        // 로그아웃 버튼 클릭 시 처리
        buttonLogout.setOnClickListener(v -> {
            logout();
        });
        // 회원탈퇴 버튼 클릭시
        buttonResign.setOnClickListener(v -> {
            logout();
        });

        return view;
    }

    // 사용자 정보를 API로부터 로드하는 메서드
    private void loadUserProfile() {
        ApiService apiService = RetrofitClient.getApiService();
        String token = TokenManager.getAccessToken(getContext());

        Call<UserProfileResponse> call = apiService.getUserProfile("Bearer " + token);
        call.enqueue(new Callback<UserProfileResponse>() {
            @Override
            public void onResponse(Call<UserProfileResponse> call, Response<UserProfileResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    // 성공적으로 사용자 정보를 불러온 경우 UI에 반영
                    String username = response.body().getUserInfo().getNickname();
                    String phone = response.body().getUserInfo().getPhnum();

                    if (phone.length() > 7 ) {
                       phone=phone.replaceFirst("(\\d{3})(\\d{4})(\\d+)", "$1-$2-$3");
                    }

                    textViewProfileName.setText("사용자 이름: " + username);
                    textViewProfilePhone.setText("전화번호: " + phone);
                } else {
                    // 실패 시 처리
                    Toast.makeText(getContext(), "사용자 정보를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<UserProfileResponse> call, Throwable t) {
                // 네트워크 오류 또는 서버 오류 처리
                Toast.makeText(getContext(), "오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 로그아웃 처리
    private void logout() {
        ApiService apiService = RetrofitClient.getApiService();
        String atoken = TokenManager.getAccessToken(getContext());
        String rtoken = TokenManager.getRefreshToken(getContext());

        Call<Void> call = apiService.logout(atoken,rtoken); // 로그아웃 API 호출
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // 로그아웃 성공 시 로그인 화면으로 이동
                    Intent intent = new Intent(getActivity(), LoginFragment.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();  // 현재 액티비티 종료
                } else {
                    // 로그아웃 실패 처리
                    Toast.makeText(getContext(), "로그아웃 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 네트워크 오류 또는 서버 오류 처리
                Toast.makeText(getContext(), "오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 회원 탈퇴 처리
    private void resign() {
        ApiService apiService = RetrofitClient.getApiService();
        String token = TokenManager.getAccessToken(getContext());
        Call<Void> call = apiService.resign(token); // 회원탈퇴 API 호출
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    // 회원 탈퇴 성공 시 로그인 화면으로 이동
                    Intent intent = new Intent(getActivity(), LoginFragment.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                    getActivity().finish();  // 현재 액티비티 종료
                } else {
                    // 회원탈퇴 실패 처리
                    Toast.makeText(getContext(), "회원탈퇴 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                // 네트워크 오류 또는 서버 오류 처리
                Toast.makeText(getContext(), "오류 발생: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
