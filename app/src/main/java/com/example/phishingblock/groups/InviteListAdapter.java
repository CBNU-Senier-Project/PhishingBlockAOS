package com.example.phishingblock.groups;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.R;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.AcceptInvitationRequest;
import com.example.phishingblock.network.payload.InvitationResponse;
import java.util.List;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import android.widget.Toast;

public class InviteListAdapter extends RecyclerView.Adapter<InviteListAdapter.InviteViewHolder> {

    private List<InvitationResponse> invitationList;
    private String token;

    public InviteListAdapter(List<InvitationResponse> invitationList, String token) {
        this.invitationList = invitationList;
        this.token = token;
    }

    @NonNull
    @Override
    public InviteViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_invite, parent, false);
        return new InviteViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull InviteViewHolder holder, int position) {
        InvitationResponse invitation = invitationList.get(position);
        holder.tvGroupName.setText(invitation.getGroupName());
        holder.tvSenderName.setText("보낸 사람: " + invitation.getSenderName());

        // 수락 버튼 클릭 시 처리
        holder.btnAccept.setOnClickListener(v -> {
            acceptInvitation(holder, invitation.getInvitationId());
        });

        // 거절 버튼 클릭 시 처리
        holder.btnReject.setOnClickListener(v -> {
            rejectInvitation(holder, invitation.getInvitationId());
        });
    }

    @Override
    public int getItemCount() {
        return invitationList.size();
    }

    public static class InviteViewHolder extends RecyclerView.ViewHolder {
        TextView tvGroupName, tvSenderName;
        Button btnAccept, btnReject;

        public InviteViewHolder(@NonNull View itemView) {
            super(itemView);
            tvGroupName = itemView.findViewById(R.id.tv_group_name);
            tvSenderName = itemView.findViewById(R.id.tv_sender_name);
            btnAccept = itemView.findViewById(R.id.btn_accept);
            btnReject = itemView.findViewById(R.id.btn_reject);
        }
    }

    // 초대 수락 처리
    private void acceptInvitation(InviteViewHolder holder, Long invitationId) {
        ApiService apiService = RetrofitClient.getApiService();

        AcceptInvitationRequest acceptRequest = new AcceptInvitationRequest("ACCEPTED");

        Call<Void> call = apiService.acceptInvitation(invitationId, acceptRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "초대 수락 성공!", Toast.LENGTH_SHORT).show();
                    // 초대 리스트에서 해당 항목 제거
                    invitationList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                } else {
                    Toast.makeText(holder.itemView.getContext(), "초대 수락 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(holder.itemView.getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // 초대 거절 처리
    private void rejectInvitation(InviteViewHolder holder, Long invitationId) {
        ApiService apiService = RetrofitClient.getApiService();

        AcceptInvitationRequest rejectRequest = new AcceptInvitationRequest("REJECTED");

        Call<Void> call = apiService.acceptInvitation(invitationId, rejectRequest);
        call.enqueue(new Callback<Void>() {
            @Override
            public void onResponse(Call<Void> call, Response<Void> response) {
                if (response.isSuccessful()) {
                    Toast.makeText(holder.itemView.getContext(), "초대 거절 성공!", Toast.LENGTH_SHORT).show();
                    // 초대 리스트에서 해당 항목 제거
                    invitationList.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                } else {
                    Toast.makeText(holder.itemView.getContext(), "초대 거절 실패", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<Void> call, Throwable t) {
                Toast.makeText(holder.itemView.getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
