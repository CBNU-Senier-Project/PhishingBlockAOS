package com.example.phishingblock.searching;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.phishingblock.R;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.SearchPhishingDataRequest;
import com.example.phishingblock.network.payload.SearchPhishingDataResponse;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ReportDetailsFragment extends Fragment {
    private RecyclerView recyclerView;
    private ReportAdapter adapter;
    private String value;  // 선택된 항목의 값 (예: 전화번호, URL, 계좌 등)
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

        // 어댑터 생성 시 viewType 2를 넘김 (ReportDetailsFragment용)
        adapter = new ReportAdapter(new ArrayList<>(), getContext(), 2);
        recyclerView.setAdapter(adapter);

        // 선택된 아이템과 타입을 Bundle에서 가져옴
        if (getArguments() != null) {
            value = getArguments().getString("VALUE");
            type = getArguments().getString("TYPE");
        }

        // 선택된 아이템과 타입에 맞는 신고 항목을 API를 통해 로드
        loadFilteredReports();
    }

    // 서버에서 선택된 아이템과 타입에 맞는 신고 항목을 필터링하여 로드
    private void loadFilteredReports() {
        ApiService apiService = RetrofitClient.getApiService();
        SearchPhishingDataRequest request = new SearchPhishingDataRequest(type, value, "");

        Call<List<SearchPhishingDataResponse>> call = apiService.searchPhishingData(request);
        call.enqueue(new Callback<List<SearchPhishingDataResponse>>() {
            @Override
            public void onResponse(Call<List<SearchPhishingDataResponse>> call, Response<List<SearchPhishingDataResponse>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<SearchPhishingDataResponse> filteredReports = response.body();
                    if (filteredReports.isEmpty()) {
                        Toast.makeText(getContext(), "해당 값에 대한 신고 내역이 없습니다.", Toast.LENGTH_SHORT).show();
                    } else {
                        adapter.updateReportItems(filteredReports);  // 필터링된 데이터를 어댑터에 업데이트
                    }
                } else {
                    Toast.makeText(getContext(), "신고 항목을 불러오지 못했습니다.", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<List<SearchPhishingDataResponse>> call, Throwable t) {
                Toast.makeText(getContext(), "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
