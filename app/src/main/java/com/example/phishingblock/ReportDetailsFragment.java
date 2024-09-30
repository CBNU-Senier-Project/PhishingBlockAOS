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
    private List<ReportDetail> reports;  // List<String>에서 List<ReportDetail>로 변경

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 신고 내역을 List<ReportDetail>로 관리
        reports = new ArrayList<>();
        adapter = new ReportDetailsAdapter(reports);
        recyclerView.setAdapter(adapter);

        String phoneNumber = getArguments().getString("PHONE_NUMBER");

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            loadReports(phoneNumber, activity.getPhoneNumberAdapter());
        }
    }

    private void loadReports(String phoneNumber, PhoneNumberAdapter adapter) {
        // PhoneNumberAdapter에서 List<ReportDetail> 가져오기
        List<ReportDetail> reportsForPhoneNumber = adapter.getReportsForPhoneNumber(phoneNumber);
        reports.clear();

        if (reportsForPhoneNumber.isEmpty()) {
            Toast.makeText(getContext(), "신고 내역이 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            reports.addAll(reportsForPhoneNumber);
        }

        this.adapter.notifyDataSetChanged();
    }

    private void showReportDialog(String phoneNumber) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);
        EditText etReportContent = dialogView.findViewById(R.id.et_report_content);

        new AlertDialog.Builder(getContext())
                .setTitle("신고 내용 작성")
                .setView(dialogView)
                .setPositiveButton("제출", (dialog, which) -> {
                    String reportContent = etReportContent.getText().toString().trim();
                    if (!reportContent.isEmpty()) {
                        addReportToAdapter(phoneNumber, reportContent);
                        Toast.makeText(getContext(), "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(getContext(), "신고 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void addReportToAdapter(String phoneNumber, String reportContent) {
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            PhoneNumberAdapter phoneNumberAdapter = activity.getPhoneNumberAdapter();
            phoneNumberAdapter.addPhoneNumber(phoneNumber);  // 번호가 없을 경우 리스트에 추가
            phoneNumberAdapter.addReport(phoneNumber, reportContent);  // 신고 내용 추가

            loadReports(phoneNumber, phoneNumberAdapter);
        }
    }
}
