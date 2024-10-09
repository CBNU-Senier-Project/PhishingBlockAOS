package com.example.phishingblock.groups;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.R;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.InviteMemberRequest;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsFragment extends Fragment {

    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout;
    private Button btnViewInviteList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGroupMembers);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        Button btnAddMember = view.findViewById(R.id.btnAddMember);
        btnViewInviteList = view.findViewById(R.id.btn_view_invite_list);

        // 그룹원 추가 버튼 클릭 시 초대 기능 수행
        btnAddMember.setOnClickListener(v -> showInviteMemberDialog());

        // 초대 리스트 보기 버튼 클릭 시 InviteListFragment로 이동
        btnViewInviteList.setOnClickListener(v -> {
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container, new InviteListFragment());
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
        });

        return view;
    }

    // 그룹원 초대 다이얼로그를 띄우고 초대 API 호출
    private void showInviteMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
        builder.setView(dialogView);

        EditText editTextReceiverId = dialogView.findViewById(R.id.editTextEmail);
        Button buttonAdd = dialogView.findViewById(R.id.buttonAdd);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonAdd.setOnClickListener(v -> {
            String receiverId = editTextReceiverId.getText().toString().trim();
            if (!TextUtils.isEmpty(receiverId)) {
                Long groupId = 123L;  // 실제 그룹 ID
                String token = "Bearer your_access_token"; // 유효한 토큰
                inviteMemberToGroup(groupId, Long.parseLong(receiverId), token);
                dialog.dismiss();
            } else {
                editTextReceiverId.setError("초대받을 사람의 Email을 입력하세요.");
            }
        });
    }

    // 그룹 초대 API 호출 메서드
    private void inviteMemberToGroup(Long groupId, Long receiverId, String token) {
        ApiService apiService = RetrofitClient.getApiService();
        InviteMemberRequest inviteMemberRequest = new InviteMemberRequest(receiverId);

        Call<Void> call = apiService.inviteMember(groupId, "Bearer " + token, inviteMemberRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "그룹 초대 성공!", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getContext(), "그룹 초대 실패.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "그룹 초대 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}