package com.example.phishingblock;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
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
    private PhoneNumberAdapter adapter;
    private List<String> originalPhoneNumbers;  // 원본 리스트 유지

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

        // MainActivity에서 PhoneNumberAdapter 가져오기
        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            adapter = activity.getPhoneNumberAdapter();
            originalPhoneNumbers = new ArrayList<>(adapter.phoneNumbers);  // 원본 리스트 복사
            adapter.setOnItemClickListener(new PhoneNumberAdapter.OnItemClickListener() {
                @Override
                public void onReportClick(String phoneNumber) {
                    // 신고 버튼 클릭 처리
                }

                @Override
                public void onCheckClick(String phoneNumber) {
                    openReportDetailsFragment(phoneNumber);
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }

        // 키보드의 "Enter" 또는 "Search" 버튼 클릭 시 검색 수행
        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    String phoneNumber = etSearch.getText().toString().trim();
                    if (!phoneNumber.isEmpty()) {
                        // 키보드 숨기기
                        hideKeyboard();
                        // 전화번호 검색 처리
                        filterPhoneNumbers(phoneNumber);
                    }
                    return true;  // 액션 처리 완료
                }
                return false;
            }
        });
    }

    // 전화번호 검색 및 필터링 처리
    private void filterPhoneNumbers(String query) {
        List<String> filteredList = new ArrayList<>();

        // 검색어와 부분적으로 일치하는 전화번호 필터링
        for (String phoneNumber : originalPhoneNumbers) {
            if (phoneNumber.contains(query)) {
                filteredList.add(phoneNumber);
            }
        }

        if (filteredList.isEmpty()) {
            // 검색된 번호가 없을 때 신고 확인 다이얼로그 표시
            showNoResultsDialog(query);
        } else {
            // 검색된 번호가 있을 때는 필터링된 리스트를 어댑터에 설정
            recyclerView.setVisibility(View.VISIBLE);  // 리스트 보이기
            adapter.updatePhoneNumbers(filteredList);
            adapter.notifyDataSetChanged();  // RecyclerView 갱신
        }
    }

    // 검색된 번호가 없을 때 신고 여부를 묻는 다이얼로그 표시
    private void showNoResultsDialog(String phoneNumber) {
        new AlertDialog.Builder(getContext())
                .setTitle("검색된 번호가 없습니다.")
                .setMessage("신고하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> showReportDialog(phoneNumber))  // 신고 작성 다이얼로그로 이동
                .setNegativeButton("취소", null)
                .show();
    }

    // 신고 내용을 입력받는 다이얼로그 표시
    private void showReportDialog(String phoneNumber) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);
        EditText etReportContent = dialogView.findViewById(R.id.et_report_content);

        new AlertDialog.Builder(getContext())
                .setTitle("신고 내용 작성")
                .setView(dialogView)
                .setPositiveButton("제출", (dialog, which) -> {
                    String reportContent = etReportContent.getText().toString().trim();
                    if (!reportContent.isEmpty()) {
                        // 신고 내용 처리 로직 (PhoneNumberAdapter에 추가)
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null) {
                            PhoneNumberAdapter adapter = activity.getPhoneNumberAdapter();
                            adapter.addPhoneNumber(phoneNumber);  // 번호를 추가
                            adapter.addReport(phoneNumber, reportContent);  // 신고 내용 추가

                            // 즉시 RecyclerView 갱신
                            adapter.notifyDataSetChanged();

                            // 신고가 완료된 후 리스트 갱신
                            recyclerView.setVisibility(View.VISIBLE);  // RecyclerView 다시 보이기
                            Toast.makeText(getContext(), "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "신고 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    // ReportDetailsFragment 열기
    private void openReportDetailsFragment(String phoneNumber) {
        Bundle bundle = new Bundle();
        bundle.putString("PHONE_NUMBER", phoneNumber);
        ReportDetailsFragment fragment = new ReportDetailsFragment();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    // 키보드 숨기기 메서드
    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
