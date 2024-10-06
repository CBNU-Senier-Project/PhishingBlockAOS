package com.example.phishingblock;

import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private LinearLayout newsContainer;  // 기사 제목들을 담을 레이아웃
    private List<String> articleLinks = new ArrayList<>();  // 각 기사의 링크를 저장할 리스트
    private ProgressBar loadingSpinner;  // 로딩 상태를 나타내는 ProgressBar

    @Nullable
    @Override
    public View onCreateView(@Nullable LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_home, container, false);

        newsContainer = view.findViewById(R.id.news_container);
        loadingSpinner = view.findViewById(R.id.loading_spinner);  // ProgressBar 가져오기

        // 크롤링 시작
        new FetchNewsTask().execute();

        return view;
    }

    // AsyncTask를 사용하여 네트워크에서 데이터를 가져오는 작업을 처리
    private class FetchNewsTask extends AsyncTask<Void, Void, List<String>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 데이터를 로드하는 동안 로딩 스피너를 표시
            loadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<String> doInBackground(Void... voids) {
            List<String> articleTitles = new ArrayList<>();
            try {
                // 네이버 뉴스 보이스피싱 관련 기사 크롤링
                Document doc = Jsoup.connect("https://search.naver.com/search.naver?query=보이스피싱").get();
                Elements articles = doc.select(".news_tit"); // 기사 제목을 선택 (CSS 셀렉터에 따라 다름)

                for (Element article : articles) {
                    String title = article.text(); // 기사 제목 추출
                    String link = article.absUrl("href"); // 기사 링크 추출

                    articleTitles.add(title);  // 기사 제목을 저장
                    articleLinks.add(link);  // 기사 링크를 저장
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return articleTitles;
        }

        @Override
        protected void onPostExecute(List<String> articleTitles) {
            // 로딩 완료 후 로딩 스피너를 숨기기
            loadingSpinner.setVisibility(View.GONE);

            // 가져온 제목들을 동적으로 TextView로 추가
            for (int i = 0; i < articleTitles.size(); i++) {
                final String link = articleLinks.get(i);  // 각 제목에 해당하는 링크
                String title = articleTitles.get(i);

                // 동적으로 TextView 생성
                TextView tv = new TextView(getContext());
                tv.setText(title);
                tv.setTextSize(16f);
                tv.setPadding(16, 16, 16, 16);
                tv.setBackgroundResource(R.drawable.bg_news);  // 배경 설정

                // 제목 클릭 시 해당 링크로 이동하도록 클릭 이벤트 추가
                tv.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(intent);  // 브라우저에서 링크 열기
                });

                // 동적으로 생성한 TextView를 레이아웃에 추가
                newsContainer.addView(tv);
                // 각 기사 제목 사이에 여백 추가
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
                params.setMargins(0, 0, 0, 16);  // 아래에 16dp 여백 추가
                tv.setLayoutParams(params);
            }
        }
    }
}
