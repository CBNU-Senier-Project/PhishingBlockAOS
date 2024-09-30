package com.example.phishingblock;
import android.util.Log;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.app.AlertDialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class PhoneNumberAdapter extends RecyclerView.Adapter<PhoneNumberAdapter.ViewHolder> {

    List<String> phoneNumbers;
    private OnItemClickListener listener;
    private FragmentActivity activity;
    private Map<String, List<ReportDetail>> reportMap = new HashMap<>();  // 전화번호별 신고 내용을 저장하는 맵

    public interface OnItemClickListener {
        void onReportClick(String phoneNumber);
        void onCheckClick(String phoneNumber);
    }

    public PhoneNumberAdapter(List<String> phoneNumbers, OnItemClickListener listener, FragmentActivity activity) {
        this.phoneNumbers = phoneNumbers;
        this.listener = listener;
        this.activity = activity;
    }

    public void updatePhoneNumbers(List<String> newPhoneNumbers) {
        this.phoneNumbers.clear();
        this.phoneNumbers.addAll(newPhoneNumbers);
    }

    public void clearPhoneNumbers() {
        phoneNumbers.clear();
    }

    public void addPhoneNumber(String phoneNumber) {
        if (!phoneNumbers.contains(phoneNumber)) {
            phoneNumbers.add(phoneNumber);
            notifyDataSetChanged(); // RecyclerView 갱신
        }
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_phone_number, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String phoneNumber = phoneNumbers.get(position);
        holder.tvPhoneNumber.setText(phoneNumber);

        // 신고 버튼 클릭 시 다이얼로그를 띄워 신고 내용을 입력받고 저장
        holder.btnReport.setOnClickListener(v -> showReportDialog(phoneNumber));

        // 조회 버튼 클릭 시 리스너에 전달
        holder.btnCheck.setOnClickListener(v -> listener.onCheckClick(phoneNumber));

        // 해당 번호의 신고 건수를 가져와서 조회 버튼 위에 반영
        int reportCount = getReportsForPhoneNumber(phoneNumber).size();
        holder.tvcheckcount.setText(String.valueOf(reportCount));
    }

    @Override
    public int getItemCount() {
        return phoneNumbers.size();
    }

    private void showReportDialog(String phoneNumber) {
        if (activity.isFinishing() || activity.isDestroyed()) {
            return;  // 액티비티가 종료되었으면 다이얼로그를 띄우지 않음
        }

        View dialogView = LayoutInflater.from(activity).inflate(R.layout.dialog_report, null);
        EditText etReportContent = dialogView.findViewById(R.id.et_report_content);

        new AlertDialog.Builder(activity)
                .setTitle("신고 내용 작성")
                .setView(dialogView)
                .setPositiveButton("제출", (dialog, which) -> {
                    String reportContent = etReportContent.getText().toString().trim();
                    if (!reportContent.isEmpty()) {
                        addReport(phoneNumber, reportContent);
                        Toast.makeText(activity, "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(activity, "신고 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // addReport에 로그 추가
    void addReport(String phoneNumber, String reportContent) {
        if (!reportMap.containsKey(phoneNumber)) {
            reportMap.put(phoneNumber, new ArrayList<>());
        }

        // 현재 시간 가져오기
        String currentTimestamp = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(new Date());

        // 신고 내용 및 시간 추가
        reportMap.get(phoneNumber).add(new ReportDetail(reportContent, currentTimestamp));

        notifyDataSetChanged();  // RecyclerView 업데이트
    }

    public List<ReportDetail> getReportsForPhoneNumber(String phoneNumber) {
        return reportMap.getOrDefault(phoneNumber, new ArrayList<>());
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvPhoneNumber;
        TextView tvcheckcount;
        Button btnReport;
        Button btnCheck;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            tvPhoneNumber = itemView.findViewById(R.id.tv_phone_number);
            tvcheckcount = itemView.findViewById(R.id.tv_check_count);
            btnReport = itemView.findViewById(R.id.btn_report);
            btnCheck = itemView.findViewById(R.id.btn_check);
        }
    }
}
