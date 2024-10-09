package com.example.phishingblock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.InvitationResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class InviteListFragment extends Fragment {

    private RecyclerView inviteRecyclerView;
    private Button btnRefresh;
    private InviteListAdapter inviteListAdapter;
    private List<InvitationResponse> invitationList;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.invite_list_fragment, container, false);

        inviteRecyclerView = view.findViewById(R.id.rv_invite_list);
        btnRefresh = view.findViewById(R.id.btn_refresh);

        inviteRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 초기 데이터 로드
        loadInviteList();

        // 새로고침 버튼 클릭 시 초대 리스트 다시 불러오기
        btnRefresh.setOnClickListener(v -> loadInviteList());

        return view;
    }

    // API 호출하여 초대 리스트 로드
    private void loadInviteList() {
        ApiService apiService = RetrofitClient.getApiService();

        // 사용자 ID와 토큰은 실제로 사용해야 하는 값을 넣어야 함
        Long receiverId = 123L; // 예시 값
        String token = "Bearer your_access_token_here"; // 실제 액세스 토큰 사용

        Call<List<InvitationResponse>> call = apiService.getInvitationList(receiverId, token);
        call.enqueue(new Callback<List<InvitationResponse>>() {
            @Override
            public void onResponse(Call<List<InvitationResponse>> call, Response<List<InvitationResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    invitationList = response.body();

                    // InviteListAdapter에 데이터 및 토큰 전달
                    inviteListAdapter = new InviteListAdapter(invitationList, token);
                    inviteRecyclerView.setAdapter(inviteListAdapter);
                } else {
                    Toast.makeText(getContext(), "초대 리스트를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<InvitationResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
