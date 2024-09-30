package com.example.phishingblock;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.GroupMemberViewHolder> {
    private List<GroupMember> groupMembers;
    private OnItemClickListener listener;
    private Context context;

    public interface OnItemClickListener {
        void onItemClick(GroupMember groupMember);
        void onDeleteClick(GroupMember groupMember);
    }

    public GroupMemberAdapter(List<GroupMember> groupMembers, OnItemClickListener listener, Context context) {
        this.groupMembers = groupMembers;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public GroupMemberViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_group_member, parent, false);
        return new GroupMemberViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull GroupMemberViewHolder holder, int position) {
        GroupMember groupMember = groupMembers.get(position);
        holder.bind(groupMember, listener);
    }

    @Override
    public int getItemCount() {
        return groupMembers.size();
    }

    public static class GroupMemberViewHolder extends RecyclerView.ViewHolder {
        private ImageView profileImageView;
        private TextView nameTextView;
        private ImageView deleteButton;

        public GroupMemberViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
        }

        public void bind(final GroupMember groupMember, final OnItemClickListener listener) {
            nameTextView.setText(groupMember.getName());
            // Glide를 사용해 URL로부터 이미지를 로드
            Glide.with(itemView.getContext())
                    .load(groupMember.getProfileImageUrl())
                    .into(profileImageView);

            itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onItemClick(groupMember);
                }
            });

            deleteButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    listener.onDeleteClick(groupMember);
                }
            });
        }
    }
}
