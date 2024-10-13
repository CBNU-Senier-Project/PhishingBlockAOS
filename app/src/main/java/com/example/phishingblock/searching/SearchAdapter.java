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
import com.example.phishingblock.background.TokenManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.AddReportItemRequest;
import com.example.phishingblock.network.payload.ReportItemResponse;
import com.example.phishingblock.network.payload.SearchPhishingDataResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchAdapter extends RecyclerView.Adapter<SearchAdapter.SimpleViewHolder> {

    private List<Object> reportItems;
    private Context context;
    private Map<String, Integer> reportCountMap;  // 신고 항목과 신고 수를 저장하는 맵

    public SearchAdapter(Context context) {
        this.reportItems = new ArrayList<>();  // 신고 항목을 저장하는 리스트로 초기화
        this.reportCountMap = new HashMap<>();  // 신고 항목과 신고 수를 저장하는 맵 초기화
        this.context = context;
    }

    @NonNull
    @Override
    public SimpleViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reports, parent, false);
        return new SimpleViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SimpleViewHolder holder, int position) {
        Object item = reportItems.get(position);

        if (item instanceof ReportItemResponse) {
            ReportItemResponse reportValue = (ReportItemResponse) item;
            String key = reportValue.getPhishingType() + "_" + reportValue.getValue();
            int count = reportCountMap.get(key);  // 신고 수 가져오기

            holder.tvItem.setText(reportValue.getValue());  // Display the value
            holder.tvCheckCount.setText(String.valueOf(count));  // 신고된 항목의 개수 표시

            // Handle the Check button click event
            holder.btnCheck.setOnClickListener(v -> {
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

        } else if (item instanceof SearchPhishingDataResponse) {
            SearchPhishingDataResponse searchResult = (SearchPhishingDataResponse) item;
            String key = searchResult.getPhishingType() + "_" + searchResult.getValue();
            int count = reportCountMap.get(key);  // 신고 수 가져오기

            holder.tvItem.setText(searchResult.getValue());  // Display the value
            holder.tvCheckCount.setText(String.valueOf(count));  // 신고된 항목의 개수 표시

            // Handle the Report button click event
            holder.btnReport.setOnClickListener(v -> {
                showReportDialog(searchResult.getValue(), searchResult.getPhishingType());
            });

            // Handle the Check button click event
            holder.btnCheck.setOnClickListener(v -> {
                ReportDetailsFragment reportDetailsFragment = new ReportDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("VALUE", searchResult.getValue());
                bundle.putString("TYPE", searchResult.getPhishingType());
                reportDetailsFragment.setArguments(bundle);
                if (context instanceof FragmentActivity) {
                    ((FragmentActivity) context).getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, reportDetailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });
        }
    }

    @Override
    public int getItemCount() {
        return reportItems.size();
    }

    // Update the adapter with items loaded by type and calculate counts
    public void updateReportItemsByType(List<ReportItemResponse> reportItemsByType) {
        reportItems.clear();
        reportCountMap.clear();

        // 중복 항목을 그룹화하여 신고 수 계산
        for (ReportItemResponse item : reportItemsByType) {
            String key = item.getPhishingType() + "_" + item.getValue();
            if (!reportCountMap.containsKey(key)) {
                reportCountMap.put(key, 1);  // 최초로 발견된 항목은 신고 수를 1로 설정
                reportItems.add(item);
            } else {
                reportCountMap.put(key, reportCountMap.get(key) + 1);  // 중복된 항목의 신고 수 증가
            }
        }

        notifyDataSetChanged();  // 어댑터 갱신
    }

    // Update the adapter with search results and calculate counts
    public void updateSearchResults(List<SearchPhishingDataResponse> searchResults) {
        reportItems.clear();
        reportCountMap.clear();

        // 중복 항목을 그룹화하여 신고 수 계산
        for (SearchPhishingDataResponse item : searchResults) {
            String key = item.getPhishingType() + "_" + item.getValue();
            if (!reportCountMap.containsKey(key)) {
                reportCountMap.put(key, 1);  // 최초로 발견된 항목은 신고 수를 1로 설정
                reportItems.add(item);
            } else {
                reportCountMap.put(key, reportCountMap.get(key) + 1);  // 중복된 항목의 신고 수 증가
            }
        }

        notifyDataSetChanged();  // 어댑터 갱신
    }

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
