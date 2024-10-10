package com.example.phishingblock.searching;

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
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.R;
import com.example.phishingblock.TokenManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.AddReportItemRequest;
import com.example.phishingblock.network.payload.ReportItemResponse;
import com.example.phishingblock.network.payload.SearchPhishingDataResponse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private List<SearchPhishingDataResponse> reportItems;
    private Context context;
    private int viewType;  // 1: SearchFragment, 2: ReportDetailsFragment
    private Map<String, Integer> reportCountMap;  // 신고 항목과 해당 신고 수를 저장하는 맵

    public ReportAdapter(List<SearchPhishingDataResponse> reportItems, Context context, int viewType) {
        this.reportItems = reportItems;
        this.context = context;
        this.viewType = viewType;
        groupReportItems();  // 신고 항목을 그룹화하여 처리
    }

    // 같은 type과 value를 가진 항목을 그룹화하여 신고 수를 계산하는 메서드
    private void groupReportItems() {
        reportCountMap = new HashMap<>();
        Map<String, SearchPhishingDataResponse> uniqueItemsMap = new HashMap<>();

        for (SearchPhishingDataResponse item : reportItems) {
            String key = item.getPhishingType() + "_" + item.getValue();  // type과 value를 키로 사용

            // 중복된 항목을 제거하고, 신고 수를 계산
            if (!uniqueItemsMap.containsKey(key)) {
                uniqueItemsMap.put(key, item);  // 중복되지 않은 항목 추가
                reportCountMap.put(key, 1);     // 첫 번째 신고로 설정
            } else {
                // 중복된 항목의 경우 신고 수를 증가시킴
                reportCountMap.put(key, reportCountMap.get(key) + 1);
            }
        }

        // 중복이 제거된 항목으로 reportItems 리스트를 갱신
        reportItems.clear();
        reportItems.addAll(uniqueItemsMap.values());
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (this.viewType == 1) {  // SearchFragment ViewHolder
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reports, parent, false);
            return new SimpleViewHolder(view);
        } else {  // ReportDetailsFragment ViewHolder
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_details, parent, false);
            return new DetailViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        SearchPhishingDataResponse reportValue = reportItems.get(position);

        if (viewType == 1) {  // If in SearchFragment, bind the simple view
            SimpleViewHolder simpleViewHolder = (SimpleViewHolder) holder;
            String key = reportValue.getPhishingType() + "_" + reportValue.getValue();
            int count = reportCountMap.get(key);  // Get the count for the specific type and value

            simpleViewHolder.tvItem.setText(reportValue.getValue());  // Display the value
            simpleViewHolder.tvCheckCount.setText(String.valueOf(count));  // Display the count

            // Handle the Report button click event
            simpleViewHolder.btnReport.setOnClickListener(v -> {
                showReportDialog(reportValue.getValue(), reportValue.getPhishingType());
            });

            // Handle the Check button click event
            simpleViewHolder.btnCheck.setOnClickListener(v -> {
                ReportDetailsFragment reportDetailsFragment = new ReportDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("VALUE", reportValue.getValue());
                bundle.putString("TYPE", reportValue.getPhishingType());
                reportDetailsFragment.setArguments(bundle);
                if (context instanceof FragmentActivity) {
                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, reportDetailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

        } else {  // If in ReportDetailsFragment, bind the detailed view
            DetailViewHolder detailViewHolder = (DetailViewHolder) holder;
            // Add binding logic for detailed view here if needed
        }
    }

    @Override
    public int getItemCount() {
        return reportItems.size();  // Return the size of the report items list
    }

    // Update the adapter with items loaded by type
    public void updateReportItemsByType(List<ReportItemResponse> newReportItems) {
        this.reportItems.clear();
        for (ReportItemResponse item : newReportItems) {
            this.reportItems.add(new SearchPhishingDataResponse(item.getPhishingId(), item.getPhishingType(), item.getValue()));  // Adjusted for SearchPhishingDataResponse constructor
        }
        groupReportItems();  // Regroup the updated items
        notifyDataSetChanged();  // Notify adapter that data has changed
    }

    // Update the adapter with search results
    public void updateSearchResults(List<SearchPhishingDataResponse> searchResults) {
        this.reportItems.clear();
        this.reportItems.addAll(searchResults);  // 검색 결과 리스트를 어댑터에 전달
        groupReportItems();  // 데이터 그룹화
        notifyDataSetChanged();  // 어댑터에 변경 사항 반영
    }

    // ViewHolder for SearchFragment
    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView tvItem, tvCheckCount;
        Button btnReport, btnCheck;

        public SimpleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.tv_value);  // 신고된 항목 (번호, URL, 계좌)
            tvCheckCount = itemView.findViewById(R.id.tv_check_count);  // 신고된 항목의 개수
            btnReport = itemView.findViewById(R.id.btn_report);  // 신고 버튼
            btnCheck = itemView.findViewById(R.id.btn_check);  // 조회 버튼
        }
    }

    // ViewHolder for ReportDetailsFragment
    public static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent, tvTime;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);  // 신고 내용만 표시
            tvTime = itemView.findViewById(R.id.tv_time);  // 신고된 시간 표시
        }
    }

    // 다이얼로그를 통해 신고 기능을 구현
    private void showReportDialog(String value, String type) {
        View dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_report, null);
        EditText etReportContent = dialogView.findViewById(R.id.et_report_content);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        Button buttonSubmit = dialogView.findViewById(R.id.buttonSubmit);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        // Type에 따라 해당 라디오 버튼을 선택하고 비활성화
        if ("PHONE".equals(type)) {
            radioGroup.check(R.id.radioButton);
        } else if ("URL".equals(type)) {
            radioGroup.check(R.id.radioButton2);
        } else if ("ACCOUNT".equals(type)) {
            radioGroup.check(R.id.radioButton3);
        }

        radioGroup.findViewById(R.id.radioButton).setEnabled(false);
        radioGroup.findViewById(R.id.radioButton2).setEnabled(false);
        radioGroup.findViewById(R.id.radioButton3).setEnabled(false);

        AlertDialog reportDialog = new AlertDialog.Builder(context).setView(dialogView).create();
        buttonSubmit.setOnClickListener(v -> {
            String reportContent = etReportContent.getText().toString().trim();
            if (!reportContent.isEmpty()) {
                String token = TokenManager.getAccessToken(context);
                if (token == null) {
                    Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ApiService apiService = RetrofitClient.getApiService();
                AddReportItemRequest addReportRequest = new AddReportItemRequest(type, value, reportContent);  // No value needed here
                apiService.addReportItem("Bearer " + token, addReportRequest).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "신고가 성공적으로 접수되었습니다.", Toast.LENGTH_SHORT).show();
                            reportDialog.dismiss();  // 신고 성공 시 다이얼로그 닫기
                        } else {
                            Toast.makeText(context, "신고 접수에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(context, "신고 접수 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                Toast.makeText(context, "신고 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });
        buttonCancel.setOnClickListener(v -> reportDialog.dismiss());
        reportDialog.show();
    }
}
