package com.example.phishingblock;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

public class SettingFragment extends Fragment {

    private TextView textViewProfileName;
    private TextView textViewProfileEmail;
    private Button buttonChangeProfile;
    private Button buttonLogout;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_setting, container, false);

        // UI 요소 초기화
        textViewProfileName = view.findViewById(R.id.textview_profile_name);
        textViewProfileEmail = view.findViewById(R.id.textview_profile_email);
        buttonChangeProfile = view.findViewById(R.id.button_change_profile);
        buttonLogout = view.findViewById(R.id.button_logout);

        // SharedPreferences에서 사용자 정보 로드
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String username = sharedPreferences.getString("username", "홍길동");
        String email = sharedPreferences.getString("email", "user@example.com");

        // 사용자 정보 UI에 반영
        textViewProfileName.setText("사용자 이름: " + username);
        textViewProfileEmail.setText("이메일: " + email);

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
            // 로그아웃 처리 (로그인 상태를 초기화하거나, 로그인 화면으로 이동)
            Intent intent = new Intent(getActivity(), LoginFragment.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            getActivity().finish();  // 현재 액티비티 종료
        });

        return view;
    }
}
