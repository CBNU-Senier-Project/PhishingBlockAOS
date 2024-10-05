package com.example.phishingblock;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.app.AlertDialog;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
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
    private ReportAdapter adapter;
    private List<ReportItem> reports;  // List<ReportItem>로 변경

    private String reportType = "번호";  // 기본 신고 유형

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 신고 내역을 List<ReportItem>로 관리
        reports = new ArrayList<>();
        adapter = new ReportAdapter(reports);  // 새로 만든 ReportAdapter 사용
        recyclerView.setAdapter(adapter);

    }

    // 신고 내역을 ReportAdapter에서 로드
    private void loadReports(String itemIdentifier, ReportAdapter adapter) {
        List<ReportItem> reportsForItem = adapter.getReportItemsForIdentifier(itemIdentifier);  // ReportAdapter에서 새로운 메서드 사용
        reports.clear();

        if (reportsForItem.isEmpty()) {
            Toast.makeText(getContext(), "신고 내역이 없습니다.", Toast.LENGTH_SHORT).show();
        } else {
            reports.addAll(reportsForItem);
        }

        this.adapter.notifyDataSetChanged();
    }
}
