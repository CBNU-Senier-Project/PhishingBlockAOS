package com.example.phishingblock.searching;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.R;
import com.example.phishingblock.TokenManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.ReportItemRequest;
import com.example.phishingblock.network.payload.ReportItemResponse;
import com.example.phishingblock.network.payload.SearchPhishingDataRequest;
import com.example.phishingblock.network.payload.SearchPhishingDataResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class SearchFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private String reportType = "PHONE";  // 기본 신고 유형 설정

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
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        // 어댑터 생성 시 viewType 1을 넘김 (SearchFragment용)
        adapter = new ReportAdapter(new ArrayList<>(), getContext(), 1);
        recyclerView.setAdapter(adapter);

        // 라디오 버튼을 설정하여 reportType을 선택
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);

        // 기본으로 PHONE 버튼을 선택
        radioGroup.check(R.id.radioButton);

        // 초기 화면 로드
        loadReportItems(reportType, "", false);

        // 라디오 버튼 변경 시
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButton) {
                reportType = "PHONE";  // 신고 유형을 "번호"로 설정
            } else if (checkedId == R.id.radioButton2) {
                reportType = "URL";  // 신고 유형을 "URL"로 설정
            } else if (checkedId == R.id.radioButton3) {
                reportType = "ACCOUNT";  // 신고 유형을 "계좌"로 설정
            }
            loadReportItems(reportType, "", false);  // 검색어 없이 라디오 버튼 선택에 따른 데이터 로드
        });

        // 검색 입력 필드에서 엔터키를 누르면 검색 실행
        etSearch.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String query = etSearch.getText().toString().trim();
                Log.d("SearchFragment", "검색 트리거됨 - Query: " + query);
                if (!query.isEmpty()) {
                    hideKeyboard();  // 키보드 숨기기
                    loadReportItems(reportType, query, true);  // 검색어가 있을 때는 다이얼로그 사용
                }
                return true;
            }
            return false;
        });
    }

    // 데이터를 로드하는 메서드 (라디오 버튼에 따라 데이터를 로드, 검색어 포함)
    private void loadReportItems(String type, String query, boolean isSearch) {
        ApiService apiService = RetrofitClient.getApiService();
        SearchPhishingDataRequest request = new SearchPhishingDataRequest(type, query, "");

        Log.d("SearchFragment", "API 요청 시작 - Type: " + type + ", Query: " + query);

        Call<List<SearchPhishingDataResponse>> call = apiService.searchPhishingData(request);
        call.enqueue(new Callback<List<SearchPhishingDataResponse>>() {
            @Override
            public void onResponse(Call<List<SearchPhishingDataResponse>> call, Response<List<SearchPhishingDataResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SearchPhishingDataResponse> searchResults = response.body();
                    Log.d("SearchFragment", "API 응답 성공 - 결과 개수: " + searchResults.size());

                    if (searchResults.isEmpty()) {
                        if (isSearch) {
                            // 검색어가 있을 때만 다이얼로그 표시
                            showNoResultsDialog(query);
                        } else {
                            // 초기 화면 또는 라디오 버튼으로 로드한 경우는 다이얼로그 대신 Toast 사용
                            Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // 어댑터에 검색 결과 업데이트 및 리사이클러 뷰 갱신
                        adapter.updateReportItems(searchResults);  // 검색 결과를 어댑터에 전달
                        Log.d("SearchFragment", "어댑터 데이터 업데이트 완료 - 검색 결과 반영됨");
                    }

                } else {
                    Log.e("SearchFragment", "API 응답 실패 - 상태 코드: " + response.code());
                    Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SearchPhishingDataResponse>> call, Throwable t) {
                Log.e("SearchFragment", "API 요청 실패: " + t.getMessage());
                Toast.makeText(getContext(), "검색 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private void showNoResultsDialog(String query) {
        // 다이얼로그 레이아웃을 Inflate
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_no_results, null);

        // 다이얼로그에 검색어를 표시할 TextView를 찾음
        TextView tvNoResultsMessage = dialogView.findViewById(R.id.tv_no_results_message);

        // 검색어를 다이얼로그 메시지에 포함하여 설정
        String message = "\"" + query + "\"에 대한 검색 결과가 없습니다.";
        tvNoResultsMessage.setText(message);

        // 다이얼로그 생성
        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        // 다이얼로그 버튼 설정
        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        // "신고" 버튼 클릭 시 실행할 동작
        buttonConfirm.setOnClickListener(v -> {
            showReportDialog(query);  // 신고 다이얼로그 표시
            dialog.dismiss();
        });
        // "취소" 버튼 클릭 시 다이얼로그 닫기
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    // 신고 다이얼로그를 표시하는 메서드
    private void showReportDialog(String query) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);
        EditText etReportContent = dialogView.findViewById(R.id.et_report_content);
        RadioGroup radioGroup = dialogView.findViewById(R.id.radioGroup);
        Button buttonSubmit = dialogView.findViewById(R.id.buttonSubmit);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);


        // 기본으로 PHONE 버튼을 선택
        radioGroup.check(R.id.radioButton);

        AlertDialog reportDialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        buttonSubmit.setOnClickListener(v -> {
            String reportContent = etReportContent.getText().toString().trim();
            if (!reportContent.isEmpty()) {
                String reportType = "PHONE";  // 기본 신고 유형
                String reportValue = query;  // 검색한 의심되는 번호, URL, 계좌값을 전달
                int selectedId = radioGroup.getCheckedRadioButtonId();
                if (selectedId == R.id.radioButton2) {
                    reportType = "URL";
                } else if (selectedId == R.id.radioButton3) {
                    reportType = "ACCOUNT";
                }

                // TokenManager에서 저장된 토큰을 가져옴
                String token = TokenManager.getAccessToken(getContext());
                if (token == null) {
                    Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;  // 토큰이 없으면 신고를 진행할 수 없음
                }

                // 신고 데이터를 서버로 전송하는 로직 추가
                ApiService apiService = RetrofitClient.getApiService();
                ReportItemRequest reportRequest = new ReportItemRequest(reportType, reportValue, reportContent);

                // API 호출
                Call<Void> call = apiService.addReportItem("Bearer " + token, reportRequest);
                call.enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "신고가 성공적으로 접수되었습니다.", Toast.LENGTH_SHORT).show();
                            reportDialog.dismiss();  // 신고 성공 시 다이얼로그 닫기
                        } else {
                            Toast.makeText(getContext(), "신고 접수에 실패했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onFailure(Call<Void> call, Throwable t) {
                        Toast.makeText(getContext(), "신고 접수 실패: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });

            } else {
                Toast.makeText(getContext(), "신고 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
            }
        });



        buttonCancel.setOnClickListener(v -> reportDialog.dismiss());
        reportDialog.show();
    }

    // 키보드 숨기기 처리
    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
