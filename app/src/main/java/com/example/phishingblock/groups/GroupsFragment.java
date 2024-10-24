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
import com.example.phishingblock.background.UserManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.GroupMemberResponse;
import com.example.phishingblock.network.payload.InviteMemberRequest;
import com.example.phishingblock.network.payload.UserProfileResponse;

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
    private long groupId = -1;  // 그룹 ID를 저장할 전역 변수
    private long userId = 0;
    private String UserphoneNumber="null";

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);
        recyclerView = view.findViewById(R.id.recyclerViewGroupMembers);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout);
        Button btnAddMember = view.findViewById(R.id.btnAddMember);
        btnViewInviteList = view.findViewById(R.id.btn_view_invite_list);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        UserProfileResponse userProfile = UserManager.getUserProfile(getContext());
        if (userProfile != null) {
            userId = userProfile.getUserId();
            String nickname = userProfile.getUserInfo().getNickname();
            UserphoneNumber = userProfile.getUserInfo().getPhnum();
            Toast.makeText(getContext(), "유저 닉네임: " + nickname, Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(getContext(), "유저 프로필을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
        }

        // 그룹원 추가 버튼 클릭 시 초대 기능 수행
        btnAddMember.setOnClickListener(v -> {
            String token = TokenManager.getAccessToken(getContext());
            if (groupId == -1) {
                // 그룹 ID가 없으면 먼저 그룹 ID를 로드한 후 초대 다이얼로그를 띄움
                loadGroupId(userId, token);  // creatorId는 미리 설정된 값이어야 합니다
            } else {
                showInviteMemberDialog();
            }
        });

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

    private void loadGroupId(long creatorId, String token) {
        ApiService apiService = RetrofitClient.getApiService();

        Call<List<Long>> call = apiService.getGroupIds(token, creatorId);
        call.enqueue(new Callback<List<Long>>() {
            @Override
            public void onResponse(Call<List<Long>> call, Response<List<Long>> response) {
                if (response.isSuccessful() && response.body() != null && !response.body().isEmpty()) {
                    groupId = response.body().get(0);  // 첫 번째 그룹 ID 사용
                    Toast.makeText(getContext(), "그룹 ID: " + groupId, Toast.LENGTH_SHORT).show();
                    showInviteMemberDialog();  // 그룹 ID가 로드된 후 다이얼로그 띄움
                } else {
                    Toast.makeText(getContext(), "그룹 ID를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<Long>> call, Throwable t) {
                Toast.makeText(getContext(), "그룹 ID 로드 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
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
            String phoneNumber = editTextPhoneNumber.getText().toString().trim();  // 입력된 전화번호에서 공백 제거
            if (!TextUtils.isEmpty(phoneNumber)) {
                String token = TokenManager.getAccessToken(getContext());

                // 입력한 전화번호와 자신의 전화번호를 비교
                if (!phoneNumber.equals(UserphoneNumber)) {  // 자신의 전화번호와 비교
                    if (groupId != -1) {  // 그룹 ID가 유효한지 확인

                        // 그룹 멤버들의 전화번호와 비교
                        if (!isPhoneNumberAlreadyInGroup(phoneNumber)) {
                            // 그룹에 해당 전화번호가 없을 경우 초대 실행
                            inviteMemberToGroup(groupId, phoneNumber, token);
                            dialog.dismiss();
                        } else {
                            // 중복된 전화번호일 경우 경고 메시지 표시
                            Toast.makeText(getContext(), "이미 그룹에 있는 멤버입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "그룹 ID를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // 자신의 전화번호일 경우 경고 메시지 표시
                    Toast.makeText(getContext(), "자신의 전화번호로는 초대할 수 없습니다.", Toast.LENGTH_SHORT).show();
                }
            } else {
                editTextPhoneNumber.setError("초대받을 사람의 전화번호를 입력하세요.");
            }
        });
    }

    // 그룹 멤버 목록에 이미 해당 전화번호가 있는지 확인하는 메서드
    private boolean isPhoneNumberAlreadyInGroup(String phoneNumber) {
        for (GroupMemberResponse member : groupMemberResponseList) {
            if (member.getPhnum().equals(phoneNumber)) {
                return true;  // 그룹에 이미 존재하는 전화번호
            }
        }
        return false;  // 중복되지 않음
    }



    // 그룹 초대 API 호출 메서드
    private void inviteMemberToGroup(long groupId, String phoneNumber, String token) {
        ApiService apiService = RetrofitClient.getApiService();
        InviteMemberRequest inviteMemberRequest = new InviteMemberRequest(phoneNumber);  // 전화번호를 요청 본문에 추가

        Call<Void> call = apiService.inviteMember(token, groupId, inviteMemberRequest);  // Authorization 토큰과 그룹 ID를 넘김
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

        Call<List<GroupMemberResponse>> call = apiService.getGroupMembers(token);
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
    private void deleteGroupMember(long memberId) {
        ApiService apiService = RetrofitClient.getApiService();
        String token = TokenManager.getAccessToken(getContext());

        Call<Void> call = apiService.deleteGroupMember(groupId, memberId, token); // Bearer 토큰 전달
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
