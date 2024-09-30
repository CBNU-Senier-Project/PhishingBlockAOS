package com.example.phishingblock;

import android.content.Context;
import android.util.Log;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.api.gax.longrunning.OperationFuture;
import com.google.cloud.speech.v1.*;
import com.google.cloud.speech.v1.SpeechClient;
import com.google.cloud.speech.v1.RecognitionAudio;
import com.google.cloud.speech.v1.RecognitionConfig.AudioEncoding;

import java.io.InputStream;
import java.util.List;

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

            // 결과 출력
            for (SpeechRecognitionResult result : results) {
                SpeechRecognitionAlternative alternative = result.getAlternativesList().get(0);
                Log.d("sttresult", "Transcription: \n" + alternative.getTranscript());
            }
        }
    }
}
