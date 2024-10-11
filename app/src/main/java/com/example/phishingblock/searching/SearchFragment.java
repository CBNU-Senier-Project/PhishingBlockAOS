package com.example.phishingblock.searching;

import android.app.AlertDialog;
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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.R;
import com.example.phishingblock.TokenManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.AddReportItemRequest;
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
    private SearchAdapter adapter;  // SearchAdapter로 변경
    private String reportType = "PHONE";  // Default type

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

        // Initialize the SearchAdapter
        adapter = new SearchAdapter(getContext());
        recyclerView.setAdapter(adapter);

        // RadioGroup to choose reportType (ACCOUNT, URL, PHONE)
        RadioGroup radioGroup = view.findViewById(R.id.radioGroup);

        // Set default type to ACCOUNT
        radioGroup.check(R.id.radioButton);

        // Load initial data based on default type
        loadReportItemsByType(reportType);

        // Handle radio button selection changes
        radioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.radioButton) {
                reportType = "PHONE";
            } else if (checkedId == R.id.radioButton2) {
                reportType = "URL";
            } else if (checkedId == R.id.radioButton3) {
                reportType = "ACCOUNT";
            }
            // Load data for the selected type without search query
            etSearch.setText("");  // Clear any previous search
            loadReportItemsByType(reportType);
        });

        // Handle search functionality when the user enters a query
        etSearch.setOnEditorActionListener((TextView v, int actionId, KeyEvent event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                    (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                String query = etSearch.getText().toString().trim();
                if (!query.isEmpty()) {
                    hideKeyboard();  // Hide the keyboard
                    loadReportItemsBySearch(reportType, query);  // Load data based on query and selected type
                }
                return true;
            }
            return false;
        });
    }

    // Method to load data by type (when radio button is clicked)
    private void loadReportItemsByType(String type) {
        ApiService apiService = RetrofitClient.getApiService();
        Call<List<ReportItemResponse>> call = apiService.getReportItems(type);

        call.enqueue(new Callback<List<ReportItemResponse>>() {
            @Override
            public void onResponse(Call<List<ReportItemResponse>> call, Response<List<ReportItemResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<ReportItemResponse> reportItems = response.body();
                    adapter.updateReportItemsByType(reportItems);  // Update the adapter by type
                } else {
                    Toast.makeText(getContext(), "데이터를 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<ReportItemResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to load data based on a search query (when the search is triggered)
    private void loadReportItemsBySearch(String type, String query) {
        ApiService apiService = RetrofitClient.getApiService();
        SearchPhishingDataRequest searchRequest = new SearchPhishingDataRequest(type, query);

        // API 호출: 피싱 데이터 검색 (검색 결과는 리스트 형태로 받음)
        Call<List<SearchPhishingDataResponse>> call = apiService.searchPhishingData(searchRequest);

        call.enqueue(new Callback<List<SearchPhishingDataResponse>>() {
            @Override
            public void onResponse(Call<List<SearchPhishingDataResponse>> call, Response<List<SearchPhishingDataResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SearchPhishingDataResponse> searchResults = response.body();

                    if (searchResults.isEmpty()) {
                        showNoResultsDialog(query);  // Show a dialog if no results are found
                    } else {
                        adapter.updateSearchResults(searchResults);  // Update the adapter with search results
                    }
                } else {
                    showNoResultsDialog(query);
                    Toast.makeText(getContext(), "검색 결과가 없습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SearchPhishingDataResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    // Method to hide the keyboard
    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }

    // Show no results dialog when no data is found during search
    private void showNoResultsDialog(String query) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_no_results, null);
        TextView tvNoResultsMessage = dialogView.findViewById(R.id.tv_no_results_message);
        String message = "\"" + query + "\"에 대한 검색 결과가 없습니다.";
        tvNoResultsMessage.setText(message);

        AlertDialog dialog = new AlertDialog.Builder(getContext())
                .setView(dialogView)
                .create();

        Button buttonConfirm = dialogView.findViewById(R.id.buttonConfirm);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);

        buttonConfirm.setOnClickListener(v -> {
            showReportDialog(query, reportType);
            dialog.dismiss();
        });

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    private void showReportDialog(String query, String type) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);
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

        AlertDialog reportDialog = new AlertDialog.Builder(getContext()).setView(dialogView).create();
        buttonSubmit.setOnClickListener(v -> {
            String reportContent = etReportContent.getText().toString().trim();
            if (!reportContent.isEmpty()) {
                String token = TokenManager.getAccessToken(getContext());
                if (token == null) {
                    Toast.makeText(getContext(), "로그인이 필요합니다.", Toast.LENGTH_SHORT).show();
                    return;
                }
                ApiService apiService = RetrofitClient.getApiService();
                AddReportItemRequest addreportRequest = new AddReportItemRequest(type,query,reportContent);
                apiService.addReportItem("Bearer " + token, addreportRequest).enqueue(new Callback<Void>() {
                    @Override
                    public void onResponse(Call<Void> call, Response<Void> response) {
                        if (response.isSuccessful()) {
                            Toast.makeText(getContext(), "신고가 성공적으로 접수되었습니다.", Toast.LENGTH_SHORT).show();
                            reportDialog.dismiss();
                            loadReportItemsBySearch(type,query);
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

}
