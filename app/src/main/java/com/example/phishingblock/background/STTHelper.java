package com.example.phishingblock.background;

import static java.security.AccessController.getContext;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.CallDialogueRequest;
import com.example.phishingblock.network.payload.PredictionResponse;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1.*;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;

import java.io.InputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class STTHelper {
    private Context context;

    public STTHelper(Context context) {
        this.context = context;
    }

    // Google Cloud Storage에 있는 파일을 비동기적으로 텍스트 변환
    public void asyncRecognizeGcs(String gcsUri) throws Exception {
        // 인증 파일을 InputStream으로 읽어오기 (assets에서 로드)
        InputStream credentialsStream = context.getAssets().open("key.json");
        GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));  // 올바른 스코프 설정



        // SpeechClient 생성
        try (SpeechClient speechClient = SpeechClient.create(SpeechSettings.newBuilder()
                .setCredentialsProvider(() -> credentials)  // 인증 정보 설정
                .build())) {
            // 오디오 파일 설정 (Google Cloud Storage 파일)
            RecognitionAudio audio = RecognitionAudio.newBuilder()
                    .setUri(gcsUri)  // GCS 파일 경로
                    .build();

            // 오디오 인코딩 및 설정
            RecognitionConfig config = RecognitionConfig.newBuilder()
                    .setEncoding(AudioEncoding.LINEAR16)  // 파일 인코딩 (LINEAR16, FLAC 등)
                    .setSampleRateHertz(16000)  // 샘플링 레이트
                    .setLanguageCode("ko-KR")  // 언어 코드 (한국어)
                    .build();

            // 비동기 요청 생성 및 실행 (OperationFuture 사용)
            OperationFuture<LongRunningRecognizeResponse, LongRunningRecognizeMetadata> response =
                    speechClient.longRunningRecognizeAsync(config, audio);

            // 비동기 작업 상태를 폴링 (10초 간격으로 확인)
            while (!response.isDone()) {
                System.out.println("Waiting for response...");
                Thread.sleep(10000);  // 10초 간격으로 대기
            }

            // 처리 결과 가져오기
            List<SpeechRecognitionResult> results = response.get().getResultsList();

            String fullTranscription="";

            // 결과 출력
            for (SpeechRecognitionResult result : results) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                Log.d("sttresult", "Transcription: \n" + alternative.getTranscript());
                fullTranscription += alternative.getTranscript();
            }
            // 최종 연결된 결과를 로그에 출력
            Log.d("sttresult", "Full Transcription: \n" + fullTranscription.toString().trim());
            String dialogue=fullTranscription.toString().trim();
            String currentTime = getCurrentTime();
            makePrediction(dialogue,currentTime);
        }
    }

    public static String getCurrentTime() {
        // 현재 시간을 가져옴
        LocalDateTime now = LocalDateTime.now();
        // 원하는 형식으로 시간을 포맷팅
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return now.format(formatter);
    }

    private void makePrediction(String dialogue, String callTime) {
        ApiService apiService = RetrofitClient.getApiService();
        String token = TokenManager.getAccessToken(context);

        // CallDialogueRequest 객체 생성
        CallDialogueRequest callDialogueRequest = new CallDialogueRequest(dialogue, callTime);

        // API 호출
        Call<PredictionResponse> call = apiService.predictdialogue(token, callDialogueRequest);
        call.enqueue(new Callback<PredictionResponse>() {
            @Override
            public void onResponse(Call<PredictionResponse> call, Response<PredictionResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    PredictionResponse result = response.body(); // "normal" 또는 "voice phishing"

                    // 결과 처리
                    if ("normal".equals(result.getPrediction())) {
                        // 정상적인 통화일 때의 처리
                        Toast.makeText(context, "통화 결과: 정상", Toast.LENGTH_SHORT).show();
                    } else if ("voice phishing".equals(result.getPrediction())) {
                        // 보이스 피싱으로 판단될 때의 처리
                        Toast.makeText(context, "통화 결과: 보이스 피싱 의심", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(context, "알 수 없는 결과: " + result, Toast.LENGTH_SHORT).show();
                    }
                } else {
                    // API 호출이 실패한 경우
                    Toast.makeText(context, "AI 판단 요청 실패: " + response.message(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<PredictionResponse> call, Throwable t) {
                // 네트워크 오류 또는 서버 오류 처리
                Toast.makeText(context, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                Log.d("e","네트워크 오류: " + t.getMessage());
            }
        });
    }

}
