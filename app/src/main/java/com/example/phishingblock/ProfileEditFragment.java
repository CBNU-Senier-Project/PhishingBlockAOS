package com.example.phishingblock;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

public class ProfileEditFragment extends Fragment {

    private EditText editTextUsername;
    private EditText editTextEmail;
    private Button buttonSave;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile_edit, container, false);

        // UI 요소 초기화
        editTextUsername = view.findViewById(R.id.edittext_username);
        editTextEmail = view.findViewById(R.id.edittext_email);
        buttonSave = view.findViewById(R.id.button_save);

        // SharedPreferences에서 사용자 정보 로드
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
        String username = sharedPreferences.getString("username", "");
        String email = sharedPreferences.getString("email", "");

        // 기존 정보 설정
        editTextUsername.setText(username);
        editTextEmail.setText(email);

        // 저장 버튼 클릭 시 정보 저장
        buttonSave.setOnClickListener(v -> {
            String newUsername = editTextUsername.getText().toString();
            String newEmail = editTextEmail.getText().toString();

            // SharedPreferences에 변경된 정보 저장
            sharedPreferences.edit()
                    .putString("username", newUsername)
                    .putString("email", newEmail)
                    .apply();

            // 이전 프래그먼트로 돌아가기
            getParentFragmentManager().popBackStack();
        });

        return view;
    }
}
