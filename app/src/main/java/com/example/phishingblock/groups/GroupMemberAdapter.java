package com.example.phishingblock.groups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.R;
import com.example.phishingblock.network.payload.GroupMemberResponse;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private List<GroupMemberResponse> groupMemberResponses;
    private Context context;
    private OnMemberDeleteListener deleteListener;

    public GroupMemberAdapter(List<GroupMemberResponse> groupMemberResponses, Context context, OnMemberDeleteListener deleteListener) {
        this.groupMemberResponses = groupMemberResponses;
        this.context = context;
        this.deleteListener = deleteListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMemberResponse member = groupMemberResponses.get(position);
        holder.nameTextView.setText(member.getName());
        // 프로필 이미지 세팅 (예: Glide 라이브러리 사용 가능)
        // Glide.with(context).load(member.getProfileImageUrl()).into(holder.profileImageView);

        // 삭제 버튼 클릭 시
        holder.deleteButton.setOnClickListener(v -> deleteListener.onMemberDelete(member.getUserId()));
    }

    @Override
    public int getItemCount() {
        return groupMemberResponses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView, deleteButton;
        TextView nameTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }
    }

    // 멤버 삭제 리스너 인터페이스
    public interface OnMemberDeleteListener {
        void onMemberDelete(int memberId);
    }
}
