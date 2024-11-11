package com.example.phishingblock.groups;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.R;
import com.example.phishingblock.network.payload.GroupLeaderResponse;

import java.util.List;

public class GroupLeaderAdapter extends RecyclerView.Adapter<GroupLeaderAdapter.ViewHolder> {
    private List<GroupLeaderResponse> leaders;
    private Context context;

    public GroupLeaderAdapter(List<GroupLeaderResponse> leaders, Context context) {
        this.leaders = leaders;
        this.context = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_leader, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupLeaderResponse leader = leaders.get(position);
        holder.nameTextView.setText(leader.getNickname());
        holder.phoneTextView.setText(leader.getPhnum());
    }

    @Override
    public int getItemCount() {
        return leaders.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView nameTextView, phoneTextView;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            phoneTextView = itemView.findViewById(R.id.phoneTextView);
        }
    }
}
