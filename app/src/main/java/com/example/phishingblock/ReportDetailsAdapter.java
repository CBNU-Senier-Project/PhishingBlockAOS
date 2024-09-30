package com.example.phishingblock;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class ReportDetailsAdapter extends RecyclerView.Adapter<ReportDetailsAdapter.ViewHolder> {

    private List<String> reports;

    public ReportDetailsAdapter(List<String> reports) {
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
        String report = reports.get(position);
        holder.tvReport.setText(report);
    }

    @Override
    public int getItemCount() {
        return reports.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvReport;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvReport = itemView.findViewById(R.id.tv_report);
        }
    }
}

