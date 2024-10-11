package com.example.phishingblock.searching;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.R;
import com.example.phishingblock.network.payload.DetailPhishingDataResponse;

import java.util.List;

public class DetailAdapter extends RecyclerView.Adapter<DetailAdapter.DetailViewHolder> {

    private List<DetailPhishingDataResponse> reportDetails;
    private Context context;

    public DetailAdapter(List<DetailPhishingDataResponse> reportDetails, Context context) {
        this.reportDetails = reportDetails;
        this.context = context;
    }

    @NonNull
    @Override
    public DetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_details, parent, false);
        return new DetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DetailViewHolder holder, int position) {
        DetailPhishingDataResponse reportDetail = reportDetails.get(position);

        holder.tvContent.setText(reportDetail.getContent());  // 신고 내용 표시
        holder.tvTime.setText("Reported on: " + reportDetail.getCreatedAt());  // 신고 날짜 및 시각 표시
    }

    @Override
    public int getItemCount() {
        return reportDetails.size();
    }

    public static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);  // 신고 내용
            tvTime = itemView.findViewById(R.id.tv_time);  // 신고된 시간
        }
    }
}
