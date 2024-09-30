package com.example.phishingblock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

public class ReportDetailsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReportDetailsAdapter adapter;
    private List<String> reports;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Fragment의 레이아웃을 설정하고 뷰를 반환
        return inflater.inflate(R.layout.fragment_report_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // RecyclerView 설정
        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));


        // 신고 내역 리스트 및 어댑터 설정
        reports = new ArrayList<>();
        adapter = new ReportDetailsAdapter(reports);
        recyclerView.setAdapter(adapter);

        // 전화번호 가져오기
        String phoneNumber = getArguments().getString("PHONE_NUMBER");

        // MainActivity에서 PhoneNumberAdapter 가져오기
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            loadReports(phoneNumber, activity.getPhoneNumberAdapter());  // MainActivity에서 생성한 어댑터 사용
        }

    }

    private void loadReports(String phoneNumber, PhoneNumberAdapter adapter) {
        List<String> reportsForPhoneNumber = adapter.getReportsForPhoneNumber(phoneNumber);  // 해당 번호의 신고 내역 가져오기
        reports.clear();

        if (reportsForPhoneNumber.isEmpty()) {
            // 신고 내역이 없을 경우 신고하기 버튼을 보이도록 설정
            Toast.makeText(getContext(), "신고 내역이 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            reports.addAll(reportsForPhoneNumber);
        }

        this.adapter.notifyDataSetChanged();
    }

    // 신고 내용을 입력받는 다이얼로그 표시
    private void showReportDialog(String phoneNumber) {
        // 다이얼로그 레이아웃 설정
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);
        EditText etReportContent = dialogView.findViewById(R.id.et_report_content);

        // 신고 다이얼로그 생성
        new AlertDialog.Builder(getContext())
                .setTitle("신고 내용 작성")
                .setView(dialogView)
                .setPositiveButton("제출", (dialog, which) -> {
                    String reportContent = etReportContent.getText().toString().trim();
                    if (!reportContent.isEmpty()) {
                        addReportToAdapter(phoneNumber, reportContent);  // 신고 내역 추가
                        Toast.makeText(getContext(), "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "신고 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // 신고 내용을 PhoneNumberAdapter에 추가
    private void addReportToAdapter(String phoneNumber, String reportContent) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            PhoneNumberAdapter phoneNumberAdapter = activity.getPhoneNumberAdapter();
            phoneNumberAdapter.addPhoneNumber(phoneNumber);  // 번호가 없을 경우 리스트에 추가
            phoneNumberAdapter.addReport(phoneNumber, reportContent);  // 신고 내용 추가

            // 신고 내역 새로고침
            loadReports(phoneNumber, phoneNumberAdapter);
        }
    }
}
