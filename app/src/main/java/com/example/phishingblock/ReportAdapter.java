package com.example.phishingblock;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_SIMPLE = 1;
    private static final int VIEW_TYPE_DETAIL = 2;

    private List<ReportItem> reportItems;
    private Context context;

    public ReportAdapter(List<ReportItem> reportItems, Context context) {
        this.reportItems = reportItems;
        this.context = context;
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
        ReportItem reportItem = reportItems.get(position);

        // 타입에 따라 간단하게 뷰를 구분하는 로직
        if ("URL".equals(reportItem.getType())) {
            return VIEW_TYPE_DETAIL;  // URL 타입일 경우 디테일 뷰를 사용
        } else {
            return VIEW_TYPE_SIMPLE;  // 기본적으로는 Simple 뷰를 사용
        }
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

    public void updateReportItems(List<ReportItem> newReportItems) {
        this.reportItems.clear();
        this.reportItems.addAll(newReportItems);
        notifyDataSetChanged();
    }

    // SimpleViewHolder: 기본 항목을 위한 뷰홀더
    public class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView tvItem, tvCheckCount;
        Button btnReport, btnCheck;

        public SimpleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.tv_item);
            tvCheckCount = itemView.findViewById(R.id.tv_check_count);
            btnReport = itemView.findViewById(R.id.btn_report);
            btnCheck = itemView.findViewById(R.id.btn_check);
        }

        public void bind(ReportItem reportItem) {
            tvItem.setText(reportItem.getItem());
            tvCheckCount.setText(String.valueOf(reportItem.getReportCount()));

            btnReport.setOnClickListener(v -> {
                showReportDialog(reportItem.getItem());
                reportItem.incrementReportCount();
            });

            // 조회 버튼 클릭 이벤트
            btnCheck.setOnClickListener(v -> {
                // MainActivity로부터 Fragment 전환 처리
                if (context instanceof MainActivity) {
                    MainActivity mainActivity = (MainActivity) context;

                    // 선택된 아이템의 데이터를 Bundle에 담아 ReportDetailsFragment로 전달
                    Bundle bundle = new Bundle();
                    bundle.putString("ITEM", reportItem.getItem());
                    bundle.putString("TYPE", reportItem.getType());

                    ReportDetailsFragment detailsFragment = new ReportDetailsFragment();
                    detailsFragment.setArguments(bundle);

                    mainActivity.getSupportFragmentManager()
                            .beginTransaction()
                            .replace(R.id.fragment_container, detailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }

    // DetailViewHolder: 상세 항목을 위한 뷰홀더
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

    // 신고 다이얼로그를 어댑터에서 관리
    public void showReportDialog(String item) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_report, null);
        EditText etReportContent = dialogView.findViewById(R.id.et_report_content);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        Button buttonSubmit = dialogView.findViewById(R.id.buttonSubmit);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        buttonSubmit.setOnClickListener(v -> {
            String reportContent = etReportContent.getText().toString().trim();
            if (!reportContent.isEmpty()) {
                String reportType = "번호";  // 기본 신고 유형
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId == R.id.radioButton2) {
                    reportType = "URL";
                } else if (selectedId == R.id.radioButton3) {
                    reportType = "계좌";
                }

                // 신고 항목 추가 로직을 구현합니다.
                ReportItem newReportItem = new ReportItem(item, reportType, "2024-10-05", reportContent);
                reportItems.add(newReportItem);
                notifyDataSetChanged();

                Toast.makeText(context, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(context, "신고 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }
}
