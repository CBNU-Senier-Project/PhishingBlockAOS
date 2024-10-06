package com.example.phishingblock;

import android.os.Bundle;
import android.util.Log;
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
    private List<ReportItem> reportItems;  // 전체 신고 항목 리스트
    private String item;  // 선택된 항목의 아이템 식별자
    private String type;  // 선택된 항목의 타입(전화번호, URL, 계좌 등)

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_report_details, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        recyclerView = view.findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 전체 신고 항목을 초기화 (MainActivity 등에서 전달되었을 수 있음)
        reportItems = new ArrayList<>();

        // 선택된 아이템과 타입을 Bundle에서 가져옴
        if (getArguments() != null) {
            item = getArguments().getString("ITEM");
            type = getArguments().getString("TYPE");
        }

        // 어댑터 생성 및 설정
        adapter = new ReportAdapter(reportItems, getContext());
        recyclerView.setAdapter(adapter);

        // 선택된 아이템과 타입에 맞는 신고 항목을 필터링하여 보여줌
        loadFilteredReports();
    }

    // 선택된 아이템과 타입에 맞는 신고 항목 로드
    private void loadFilteredReports() {
        List<ReportItem> filteredReports = new ArrayList<>();

        for (ReportItem reportItem : reportItems) {
            Log.d("details",reportItem.getContent());
            if (reportItem.getItem().equals(item) && reportItem.getType().equals(type)) {
                filteredReports.add(reportItem);
            }
        }

        adapter.updateReportItems(filteredReports);
    }
}
