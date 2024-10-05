package com.example.phishingblock;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private List<ReportItem> originalReportItems;
    private String reportType = "번호";  // 기본 신고 유형 설정

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.et_search);
        recyclerView = view.findViewById(R.id.recycler_view);

        // 초기 데이터 설정 (예: 신고 항목 리스트)
        originalReportItems = new ArrayList<>(); // 예: 서버나 DB에서 데이터를 불러올 수 있습니다.
        // 예시로 신고 항목 추가
        originalReportItems.add(new ReportItem("010-1234-5678", "번호", "2024-10-02", "스팸 신고"));
        originalReportItems.add(new ReportItem("http://example.com", "URL", "2024-10-03", "피싱 사이트"));
        originalReportItems.add(new ReportItem("123-456-789012", "계좌", "2024-10-04", "의심되는 계좌"));

        // 어댑터 생성
        adapter = new ReportAdapter(originalReportItems);

        // RecyclerView 설정
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        recyclerView.setAdapter(adapter);

        // 초기에는 모든 신고 항목을 보여줌
        showAllReportItems();

        // 라디오 버튼을 설정하여 reportType을 선택
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButton) {
                reportType = "번호";  // 신고 유형을 "번호"로 설정
            } else if (checkedId == R.id.radioButton2) {
                reportType = "URL";  // 신고 유형을 "URL"로 설정
            } else if (checkedId == R.id.radioButton3) {
                reportType = "계좌";  // 신고 유형을 "계좌"로 설정
            }
            filterReportItems("", reportType);  // 신고 유형 변경 시 다시 검색
        });

        // 검색창에서 엔터키를 눌렀을 때 처리
        etSearch.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    hideKeyboard();  // 키보드 숨기기
                    filterReportItems(query, reportType);  // 검색 필터링 처리
                }
                return true;
            }
            return false;
        });
    }

    // 모든 신고 항목을 보여주는 메서드
    private void showAllReportItems() {
        recyclerView.setVisibility(View.VISIBLE);
        adapter.updateReportItems(originalReportItems);
        adapter.notifyDataSetChanged();
    }

    // 신고 항목 검색 및 필터링 처리
    private void filterReportItems(String query, String type) {
        List<ReportItem> filteredList = new ArrayList<>();

        for (ReportItem reportItem : originalReportItems) {
            // 의심되는 항목(번호, URL, 계좌)이 검색어를 포함하고 있는지 확인
            if (reportItem.getItem().contains(query) && reportItem.getType().equals(type)) {
                filteredList.add(reportItem);
            }
        }

        if (filteredList.isEmpty()) {
            if (!query.isEmpty()) {
                showNoResultsDialog(query);  // 검색 결과가 없을 때 다이얼로그 표시
            } else {
                recyclerView.setVisibility(View.GONE);  // 필터링 결과가 없을 때는 리스트 숨기기
            }
        } else {
            recyclerView.setVisibility(View.VISIBLE);  // 검색 결과가 있을 때만 RecyclerView 보이기
            adapter.updateReportItems(filteredList);
            adapter.notifyDataSetChanged();
        }
    }

    // 신고 다이얼로그를 표시하는 메서드
    private void showReportDialog(String item) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);
        EditText etReportContent = dialogView.findViewById(R.id.et_report_content);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        Button buttonSubmit = dialogView.findViewById(R.id.buttonSubmit);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
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
                originalReportItems.add(newReportItem);
                adapter.notifyDataSetChanged();

                Toast.makeText(getContext(), "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                dialog.dismiss();
            } else {
                Toast.makeText(getContext(), "신고 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    // 검색된 항목이 없을 때 다이얼로그 표시
    private void showNoResultsDialog(String query) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_no_results, null);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        // 확인 버튼 클릭 시 신고 작성 다이얼로그 표시
        buttonConfirm.setOnClickListener(v -> {
            showReportDialog(query);  // 검색된 항목이 없을 경우 신고 추가
            dialog.dismiss();
        });

        // 취소 버튼 클릭 시 다이얼로그 닫기
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // 신고 상세 보기 프래그먼트로 이동
    private void openReportDetailsFragment(ReportItem reportItem) {
        Bundle bundle = new Bundle();
        bundle.putString("REPORT_ITEM", reportItem.getItem());  // 신고 항목 전달
        ReportDetailsFragment fragment = new ReportDetailsFragment();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // 키보드 숨기기 처리
    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
