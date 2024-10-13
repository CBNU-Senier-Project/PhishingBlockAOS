package com.example.phishingblock.home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Outline;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewOutlineProvider;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.example.phishingblock.R;

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
        ImageView imageView = view.findViewById(R.id.top_background_image);
        // 크롤링 시작
        new FetchNewsTask().execute();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            imageView.setClipToOutline(true);  // 뷰의 외곽선을 기준으로 잘라냄
            imageView.setOutlineProvider(new ViewOutlineProvider() {
                @Override
                public void getOutline(View view, Outline outline) {
                    // 이미지뷰의 모양을 둥글게 만듦 (radius 값을 적절히 설정)
                    int cornerRadius = 30;  // 30px의 둥근 모서리
                    outline.setRoundRect(0, -20, view.getWidth(), view.getHeight(), cornerRadius);
                }
            });
        }

        return view;
    }


    private class FetchNewsTask extends AsyncTask<Void, Void, List<Article>> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            // 데이터를 로드하는 동안 로딩 스피너를 표시
            loadingSpinner.setVisibility(View.VISIBLE);
        }

        @Override
        protected List<Article> doInBackground(Void... voids) {
            List<Article> articlesList = new ArrayList<>();
            try {
                // 네이버 검색 결과 페이지 크롤링
                Document doc = Jsoup.connect("https://search.naver.com/search.naver?query=보이스피싱&where=news").get();

                // 뉴스 항목 추출 (뉴스 제목, 링크, 이미지)
                Elements newsItems = doc.select("li.bx");  // 뉴스 항목을 감싸고 있는 li 태그 선택

                for (Element item : newsItems) {
                    // 뉴스 제목과 링크 추출
                    Element titleElement = item.select("a.news_tit").first();
                    if (titleElement != null) {
                        String newsTitle = titleElement.text();
                        String newsLink = titleElement.attr("href");

                        // 이미지 추출
                        Element imgElement = item.select("img").first();
                        String imgUrl = imgElement != null ? imgElement.attr("src") : null;

                        // Article 객체에 저장
                        Article article = new Article(newsTitle, newsLink, imgUrl);
                        articlesList.add(article);
                    }
                }

            } catch (IOException e) {
                e.printStackTrace();
            }
            return articlesList;  // Article 리스트 반환
        }

        @Override
        protected void onPostExecute(List<Article> articlesList) {
            // 로딩 완료 후 로딩 스피너를 숨기기
            loadingSpinner.setVisibility(View.GONE);

            // 가져온 기사 정보를 동적으로 추가
            for (Article article : articlesList) {
                String title = article.getTitle();
                String link = article.getLink();
                String imageUrl = article.getImageUrl();  // Base64 데이터가 있을 수 있음

                // 동적으로 TextView 생성 (기사 제목)
                TextView tv = new TextView(getContext());
                tv.setText(title);
                tv.setTextSize(16f);
                tv.setPadding(16, 16, 16, 16);
                tv.setBackgroundResource(R.drawable.bg_news);  // 배경 설정

                // 기사 제목 클릭 시 해당 링크로 이동
                tv.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(link));
                    startActivity(intent);
                });

                // 동적으로 ImageView 생성 (이미지)
                ImageView imageView = new ImageView(getContext());
                imageView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 400));  // 고정된 높이 지정
                imageView.setPadding(16, 16, 16, 16);

                // Base64 이미지 여부 확인 후 로딩
                if (imageUrl != null && imageUrl.startsWith("data:image")) {
                    // Base64로 인코딩된 이미지를 Bitmap으로 변환
                    String base64Image = imageUrl.split(",")[1];  // "data:image/..." 앞부분 제외
                    Bitmap bitmap = ImageUtil.base64ToBitmap(base64Image);
                    imageView.setImageBitmap(bitmap);
                    System.out.println(imageUrl);
                } else {
                    // Glide 등을 사용하여 이미지 로딩 (URL일 경우)
                    Glide.with(getContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_image)  // 로딩 중 표시할 이미지
                            .error(R.drawable.ic_eye_off)  // 에러 시 표시할 이미지
                            .into(imageView);
                }

                // 동적으로 생성한 TextView와 ImageView를 레이아웃에 추가
                newsContainer.addView(imageView);
                newsContainer.addView(tv);

                // 각 기사 제목과 이미지 사이에 여백 추가
                LinearLayout.LayoutParams params = (LinearLayout.LayoutParams) tv.getLayoutParams();
                params.setMargins(0, 0, 0, 16);  // 아래에 16dp 여백 추가
                tv.setLayoutParams(params);
            }
        }
    }
}
