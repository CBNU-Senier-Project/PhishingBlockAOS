package com.example.phishingblock;

import android.content.Context;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.storage.BlobInfo;
import com.google.cloud.storage.Storage;
import com.google.cloud.storage.StorageOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CloudStorageHelper {

    private ExecutorService executorService = Executors.newSingleThreadExecutor();
    private Context context;

    // CloudStorageHelper 생성 시 Context를 전달받아 초기화
    public CloudStorageHelper(Context context) {
        this.context = context;
    }

    // 서비스 계정 인증 파일 경로
    private static final String CREDENTIALS_FILE_PATH = "key.json";

    // 파일을 GCS에 업로드하고, 완료 후 콜백을 실행
    public void uploadFileToGCS(final File file, final Runnable onUploadComplete) {
        executorService.execute(() -> {
            try (FileInputStream fileInputStream = new FileInputStream(file)) {

                // 인증 정보 설정
                InputStream credentialsStream = context.getAssets().open(CREDENTIALS_FILE_PATH);
                GoogleCredentials credentials = GoogleCredentials.fromStream(credentialsStream)
                        .createScoped(List.of("https://www.googleapis.com/auth/cloud-platform"));

                // GCS 클라이언트 생성
                Storage storage = StorageOptions.newBuilder()
                        .setCredentials(credentials)
                        .build()
                        .getService();

                // 업로드할 파일 설정
                BlobInfo blobInfo = BlobInfo.newBuilder("phishing_block", file.getName()).build();

                // GCS에 파일 업로드
                storage.create(blobInfo, fileInputStream);
                System.out.println("File successfully uploaded: " + file.getName());

                // 업로드 완료 후 콜백 실행
                if (onUploadComplete != null) {
                    onUploadComplete.run();
                }
            } catch (Exception e) {
                System.err.println("File upload failed: " + file.getName());
                e.printStackTrace();
            } finally {
                executorService.shutdown();
            }
        });
    }

    public void shutdown() {
        executorService.shutdown(); // 작업이 끝난 후 스레드를 종료
    }
}
