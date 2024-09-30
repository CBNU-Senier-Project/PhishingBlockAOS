package com.example.phishingblock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportDetailsAdapter extends RecyclerView.Adapter<ReportDetailsAdapter.ViewHolder> {

    private List<ReportDetail> reports;  // List<String> 대신 List<ReportDetail>

    public ReportDetailsAdapter(List<ReportDetail> reports) {
        this.reports = reports;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_report_details, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        ReportDetail reportDetail = reports.get(position);  // ReportDetail 객체 사용
        holder.tvReport.setText(reportDetail.getContent());  // 신고 내용을 설정
        holder.tvTimestamp.setText(reportDetail.getTimestamp());  // 신고한 시각을 설정
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReport;
        TextView tvTimestamp;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReport = itemView.findViewById(R.id.tv_report);
            tvTimestamp = itemView.findViewById(R.id.tv_report_timestamp);  // 신고 시각을 표시할 TextView
        }
    }
}
