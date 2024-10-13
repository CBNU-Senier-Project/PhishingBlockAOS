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
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.R;
import com.example.phishingblock.background.TokenManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.GroupMemberResponse;
import com.example.phishingblock.network.payload.InviteMemberRequest;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupsFragment extends Fragment {

    private RecyclerView recyclerView;
    private GroupMemberAdapter adapter;
    private LinearLayout emptyStateLayout;
    private Button btnViewInviteList;
    private List<GroupMemberResponse> groupMemberResponseList = new ArrayList<>();
    private int groupId = 1; // 동적으로 받아야 함

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGroupMembers);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        Button btnAddMember = view.findViewById(R.id.btnAddMember);
        btnViewInviteList = view.findViewById(R.id.btn_view_invite_list);

        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

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

        // 그룹 멤버 목록 가져오기
        loadGroupMembers();

        return view;
    }

    // 그룹원 초대 다이얼로그를 띄우고 초대 API 호출
    private void showInviteMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
        builder.setView(dialogView);

        EditText editTextPhoneNumber = dialogView.findViewById(R.id.editTextPhoneNumber);  // 전화번호 입력 필드
        Button buttonAdd = dialogView.findViewById(R.id.buttonAdd);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = builder.create();
        dialog.show();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonAdd.setOnClickListener(v -> {
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();
            if (!TextUtils.isEmpty(phoneNumber)) {
                String token = TokenManager.getAccessToken(getContext());
                inviteMemberToGroup(groupId, phoneNumber, token);  // 전화번호를 넘김
                dialog.dismiss();
            } else {
                editTextPhoneNumber.setError("초대받을 사람의 전화번호를 입력하세요.");
            }
        });
    }

    // 그룹 초대 API 호출 메서드
    private void inviteMemberToGroup(int groupId, String phoneNumber, String token) {
        ApiService apiService = RetrofitClient.getApiService();
        InviteMemberRequest inviteMemberRequest = new InviteMemberRequest(phoneNumber);  // 전화번호를 요청 본문에 추가

        Call<Void> call = apiService.inviteMember("Bearer " + token, groupId, inviteMemberRequest);  // Authorization 토큰과 그룹 ID를 넘김
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "그룹 초대 성공!", Toast.LENGTH_SHORT).show();
                    loadGroupMembers(); // 초대 후 목록 새로고침
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

    // 그룹 멤버 목록 API 호출
    private void loadGroupMembers() {
        ApiService apiService = RetrofitClient.getApiService();
        String token = TokenManager.getAccessToken(getContext());

        Call<List<GroupMemberResponse>> call = apiService.getGroupMembers("Bearer " + token); // Bearer 토큰 전달
        call.enqueue(new Callback<List<GroupMemberResponse>>() {
            @Override
            public void onResponse(Call<List<GroupMemberResponse>> call, Response<List<GroupMemberResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    groupMemberResponseList = response.body();
                    updateRecyclerView();
                } else {
                    showEmptyState();
                }
            }

            @Override
            public void onFailure(Call<List<GroupMemberResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "그룹 멤버를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // RecyclerView 업데이트
    private void updateRecyclerView() {
        if (groupMemberResponseList.isEmpty()) {
            showEmptyState();
        } else {
            emptyStateLayout.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            adapter = new GroupMemberAdapter(groupMemberResponseList, getContext(), memberId -> {
                // 멤버 삭제 로직 처리
                deleteGroupMember(memberId);
            });
            recyclerView.setAdapter(adapter);
        }
    }

    // 빈 상태 표시
    private void showEmptyState() {
        emptyStateLayout.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
    }

    // 그룹 멤버 삭제 로직 (서버와 통신)
    private void deleteGroupMember(int memberId) {
        ApiService apiService = RetrofitClient.getApiService();
        String token = TokenManager.getAccessToken(getContext());

        Call<Void> call = apiService.deleteGroupMember(groupId, memberId, "Bearer " + token); // Bearer 토큰 전달
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(getContext(), "그룹 멤버 삭제 성공", Toast.LENGTH_SHORT).show();
                    loadGroupMembers(); // 멤버 삭제 후 목록 새로 고침
                } else {
                    Toast.makeText(getContext(), "멤버 삭제 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(getContext(), "멤버 삭제 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
