package com.example.phishingblock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SIMPLE = 1;
    private static final int VIEW_TYPE_DETAIL = 2;

    private List<ReportItem> reportItems;

    public ReportAdapter(List<ReportItem> reportItems) {
        this.reportItems = reportItems;
    }

    // 데이터 업데이트를 위한 메서드
    public void updateReportItems(List<ReportItem> newReportItems) {
        this.reportItems.clear();
        this.reportItems.addAll(newReportItems);
        notifyDataSetChanged();  // RecyclerView 갱신
    }

    // 특정 항목(전화번호, URL, 계좌)별로 신고 내역 필터링
    public List<ReportItem> getReportItemsForIdentifier(String identifier) {
        List<ReportItem> filteredList = new ArrayList<>();
        for (ReportItem reportItem : reportItems) {
            if (reportItem.getItem().equals(identifier)) {
                filteredList.add(reportItem);
            }
        }
        return filteredList;
    }

    @Override
    public int getItemViewType(int position) {
        // position이나 데이터에 따라 레이아웃 타입을 구분
        // 예시로 짝수는 첫 번째 레이아웃, 홀수는 두 번째 레이아웃으로 설정
        return (position % 2 == 0) ? VIEW_TYPE_SIMPLE : VIEW_TYPE_DETAIL;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == VIEW_TYPE_SIMPLE) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reports, parent, false);
            return new SimpleViewHolder(view);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_details, parent, false);
            return new DetailViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        ReportItem reportItem = reportItems.get(position);

        if (holder instanceof SimpleViewHolder) {
            ((SimpleViewHolder) holder).bind(reportItem);
        } else if (holder instanceof DetailViewHolder) {
            ((DetailViewHolder) holder).bind(reportItem);
        }
    }

    @Override
    public int getItemCount() {
        return reportItems.size();
    }

    // 첫 번째 레이아웃을 위한 ViewHolder
    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView tvItem, tvCheckCount;

        public SimpleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.tv_item);
            tvCheckCount = itemView.findViewById(R.id.tv_check_count);
        }

        public void bind(ReportItem reportItem) {
            tvItem.setText(reportItem.getItem());
            tvCheckCount.setText("0");  // 예시로 신고 건수를 설정
        }
    }

    // 두 번째 레이아웃을 위한 ViewHolder
    public static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);
            tvTime = itemView.findViewById(R.id.tv_time);
        }

        public void bind(ReportItem reportItem) {
            tvContent.setText(reportItem.getContent());
            tvTime.setText(reportItem.getTime());
        }
    }
}
