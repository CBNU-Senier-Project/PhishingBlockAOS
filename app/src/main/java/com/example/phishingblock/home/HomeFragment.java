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
import androidx.cardview.widget.CardView;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.bumptech.glide.Glide;
import com.example.phishingblock.MainActivity;
import com.example.phishingblock.R;
import com.example.phishingblock.background.TokenManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.NewsResponse;
import com.example.phishingblock.searching.SearchFragment;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeFragment extends Fragment {

    private LinearLayout newsContainer;
    private ProgressBar loadingSpinner;
    private CardView recentNumberCard, recentUrlCard, recentAccountCard;

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        newsContainer = view.findViewById(R.id.news_container);
        loadingSpinner = view.findViewById(R.id.loading_spinner);

        // CardView 초기화
        recentNumberCard = view.findViewById(R.id.recent_number_card);
        recentUrlCard = view.findViewById(R.id.recent_url_card);
        recentAccountCard = view.findViewById(R.id.recent_account_card);

        // 클릭 리스너 설정
        recentNumberCard.setOnClickListener(v -> navigateToSearchFragment("PHONE"));
        recentUrlCard.setOnClickListener(v -> navigateToSearchFragment("URL"));
        recentAccountCard.setOnClickListener(v -> navigateToSearchFragment("ACCOUNT"));


        // 뉴스 API 호출 시작
        fetchNewsFromApi();

        return view;
    }

    private void navigateToSearchFragment(String type) {
        // SearchFragment로 이동하기 위해 프래그먼트 전환을 설정
        SearchFragment searchFragment = new SearchFragment();
        MainActivity mainActivity = (MainActivity) getActivity();
        if (mainActivity != null) {
            mainActivity.navigateToFragment(new SearchFragment(), R.id.nav_search); // R.id.nav_search는 SearchFragment에 해당하는 메뉴 ID
        }
        // 필요한 데이터 전달 (예: 검색 유형)
        Bundle args = new Bundle();
        args.putString("search_type", type);
        searchFragment.setArguments(args);

        // 프래그먼트 매니저를 사용하여 프래그먼트 전환
        FragmentManager fragmentManager = getParentFragmentManager();
        FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(R.id.fragment_container, searchFragment);  // `fragment_container`는 액티비티의 Fragment를 담을 컨테이너 ID
        transaction.addToBackStack(null);  // 뒤로 가기 기능을 위해 추가
        transaction.commit();
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

                        // 뉴스 항목 View를 생성하여 추가
                        View newsView = LayoutInflater.from(getContext()).inflate(R.layout.item_news, newsContainer, false);

                        // 뉴스 이미지 설정
                        ImageView newsImage = newsView.findViewById(R.id.news_image);
                        Glide.with(getContext())
                                .load(news.getImageUrl())
                                .placeholder(R.drawable.ic_image)  // 로딩 중 표시할 이미지
                                .error(R.drawable.ic_eye_off)  // 에러 시 표시할 이미지
                                .into(newsImage);

                        // 뉴스 제목 설정
                        TextView newsTitle = newsView.findViewById(R.id.news_title);
                        newsTitle.setText(news.getTitle());

                        // 뉴스 내용 설정
                        TextView newsContent = newsView.findViewById(R.id.news_content);
                        newsContent.setText(news.getContent());

                        // 클릭 시 링크로 이동
                        newsView.setOnClickListener(v -> {
                            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(news.getLinkUrl()));
                            startActivity(intent);
                        });

                        // 뉴스 컨테이너에 추가
                        newsContainer.addView(newsView);
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
