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
import com.example.phishingblock.network.payload.ReportItemRequest;
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
    private int viewType; // 1: SearchFragment, 2: ReportDetailsFragment
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
        for (SearchPhishingDataResponse item : reportItems) {
            String key = item.getPhishingType() + "_" + item.getValue();  // type과 value를 키로 사용
            if (reportCountMap.containsKey(key)) {
                reportCountMap.put(key, reportCountMap.get(key) + 1);  // 같은 항목이면 개수 증가
            } else {
                reportCountMap.put(key, 1);  // 새로운 항목이면 1로 시작
            }
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view;
        if (this.viewType == 1) {
            // SearchFragment에 사용할 뷰홀더
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_reports, parent, false);
            return new SimpleViewHolder(view);
        } else {
            // ReportDetailsFragment에 사용할 뷰홀더
            view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_report_details, parent, false);
            return new DetailViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        // 항목 리스트에서 해당 position의 항목을 가져옵니다.
        SearchPhishingDataResponse reportValue = reportItems.get(position);

        // 같은 type과 value를 가진 항목을 하나로 묶고 그 수를 tv_check_count에 표시
        if (viewType == 1) {
            SimpleViewHolder simpleViewHolder = (SimpleViewHolder) holder;
            String key = reportValue.getPhishingType() + "_" + reportValue.getValue();  // type과 value로 키 생성

            // 해당 key에 대한 신고 건수를 가져옴
            int count = reportCountMap.get(key);

            simpleViewHolder.tvItem.setText(reportValue.getValue());  // 신고된 항목 (예: 번호, URL, 계좌)
            simpleViewHolder.tvCheckCount.setText(String.valueOf(count));  // 신고된 동일 항목의 개수 표시

            simpleViewHolder.btnReport.setOnClickListener(v -> {
                showReportDialog(reportValue.getValue(), reportValue.getPhishingType());  // 다이얼로그 표시
            });

            simpleViewHolder.btnCheck.setOnClickListener(v -> {
                ReportDetailsFragment reportDetailsFragment = new ReportDetailsFragment();
                Bundle bundle = new Bundle();
                bundle.putString("VALUE", reportValue.getValue());  // 신고 항목 값 (예: 전화번호, URL)
                bundle.putString("TYPE", reportValue.getPhishingType());  // 신고 유형 (예: ACCOUNT, PHONE, URL)
                reportDetailsFragment.setArguments(bundle);

                if (context instanceof FragmentActivity) {
                    FragmentActivity activity = (FragmentActivity) context;
                    activity.getSupportFragmentManager().beginTransaction()
                            .replace(R.id.fragment_container, reportDetailsFragment)
                            .addToBackStack(null)
                            .commit();
                }
            });

        } else {
            DetailViewHolder detailViewHolder = (DetailViewHolder) holder;
            detailViewHolder.tvContent.setText(reportValue.getContent());
        }
    }

    @Override
    public int getItemCount() {
        return reportCountMap.size();  // 신고 항목의 고유한 개수만 반환
    }

    public void updateReportItems(List<SearchPhishingDataResponse> newReportItems) {
        this.reportItems.clear();
        this.reportItems.addAll(newReportItems);
        groupReportItems();  // 새롭게 항목을 그룹화
        notifyDataSetChanged();
    }

    // SimpleViewHolder: SearchFragment용 뷰홀더
    public static class SimpleViewHolder extends RecyclerView.ViewHolder {
        TextView tvItem, tvCheckCount;
        Button btnReport, btnCheck;

        public SimpleViewHolder(@NonNull View itemView) {
            super(itemView);
            tvItem = itemView.findViewById(R.id.tv_value);  // 신고된 항목 (예: 번호, URL, 계좌)
            tvCheckCount = itemView.findViewById(R.id.tv_check_count);  // 신고된 항목의 개수
            btnReport = itemView.findViewById(R.id.btn_report);  // 신고 버튼
            btnCheck = itemView.findViewById(R.id.btn_check);  // 조회 버튼
        }
    }

    // DetailViewHolder: ReportDetailsFragment용 뷰홀더
    public static class DetailViewHolder extends RecyclerView.ViewHolder {
        TextView tvContent;

        public DetailViewHolder(@NonNull View itemView) {
            super(itemView);
            tvContent = itemView.findViewById(R.id.tv_content);  // 신고 내용만 표시
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

        dialogView.findViewById(R.id.radioButton).setEnabled(false);  //버튼 비활성화
        dialogView.findViewById(R.id.radioButton2).setEnabled(false);
        dialogView.findViewById(R.id.radioButton3).setEnabled(false);

        AlertDialog reportDialog = new AlertDialog.Builder(context)
                .setView(dialogView)
                .create();

        buttonSubmit.setOnClickListener(v -> {
            String reportContent = etReportContent.getText().toString().trim();
            if (!reportContent.isEmpty()) {
                // 선택된 타입을 유지하여 서버로 신고 데이터 전송
                String reportValue = value;  // 신고된 값

                // TokenManager에서 저장된 토큰을 가져옴
                String token = TokenManager.getAccessToken(context);
                if (token == null) {
                    Toast.makeText(context, "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;  // 토큰이 없으면 신고를 진행할 수 없음
                }

                // 신고 데이터를 서버로 전송하는 로직 추가
                ApiService apiService = RetrofitClient.getApiService();
                ReportItemRequest reportRequest = new ReportItemRequest(type, reportValue, reportContent);

                // API 호출
                Call<Void> call = apiService.addReportItem("Bearer " + token, reportRequest);
                call.enqueue(new Callback<Void>() {
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