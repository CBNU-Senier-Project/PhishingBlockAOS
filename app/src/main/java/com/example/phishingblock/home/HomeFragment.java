package com.example.phishingblock.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.phishingblock.R;
import com.example.phishingblock.background.TokenManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.NewsResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private LinearLayout newsContainer;
    private ProgressBar loadingSpinner;

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        newsContainer = view.findViewById(R.id.news_container);
        loadingSpinner = view.findViewById(R.id.loading_spinner);

        // 뉴스 API 호출 시작
        fetchNewsFromApi();

        return view;
    }

    private void fetchNewsFromApi() {
        ApiService apiService = RetrofitClient.getApiService();
        String token = TokenManager.getAccessToken(getContext());
        loadingSpinner.setVisibility(View.VISIBLE);  // 로딩 스피너 표시

        // API 호출
        Call<List<NewsResponse>> call = apiService.getNews(token);
        call.enqueue(new Callback<List<NewsResponse>>() {
            @Override
            public void onResponse(Call<List<NewsResponse>> call, Response<List<NewsResponse>> response) {
                loadingSpinner.setVisibility(View.GONE);  // 로딩 스피너 숨기기
                if (response.isSuccessful() && response.body() != null) {
                    List<NewsResponse> newsList = response.body();

                    // 뉴스 5개만 표시
                    for (int i = 0; i < Math.min(newsList.size(), 5); i++) {
                        NewsResponse news = newsList.get(i);

                        // 뉴스 제목 표시
                        TextView tv = new TextView(getContext());
                        tv.setText(news.getTitle());
                        tv.setTextSize(16f);
                        tv.setPadding(16, 16, 16, 16);
                        tv.setBackgroundResource(R.drawable.bg_news);  // 배경 설정

                        // 뉴스 클릭 시 링크로 이동
                        tv.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getLinkUrl()));
                            startActivity(intent);
                        });

                        // 뉴스 이미지 표시
                        ImageView imageView = new ImageView(getContext());
                        imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400));  // 고정된 높이 지정
                        imageView.setPadding(16, 16, 16, 16);

                        Glide.with(getContext())
                                .load(news.getImageUrl())
                                .placeholder(R.drawable.ic_image)  // 로딩 중 표시할 이미지
                                .error(R.drawable.ic_eye_off)  // 에러 시 표시할 이미지
                                .into(imageView);

                        // 레이아웃에 추가
                        newsContainer.addView(imageView);
                        newsContainer.addView(tv);

                        // 여백 추가
                        LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
                        params.setMargins(0, 0, 0, 16);  // 아래에 16dp 여백 추가
                        tv.setLayoutParams(params);
                    }
                } else {
                    // 오류 처리
                    TextView errorText = new TextView(getContext());
                    errorText.setText("뉴스를 불러오는 데 실패했습니다.");
                    newsContainer.addView(errorText);
                }
            }

            @Override
            public void onFailure(Call<List<NewsResponse>> call, Throwable t) {
                loadingSpinner.setVisibility(View.GONE);  // 로딩 스피너 숨기기
                // 네트워크 오류 처리
                TextView errorText = new TextView(getContext());
                errorText.setText("네트워크 오류가 발생했습니다.");
                newsContainer.addView(errorText);
            }
        });
    }
}
