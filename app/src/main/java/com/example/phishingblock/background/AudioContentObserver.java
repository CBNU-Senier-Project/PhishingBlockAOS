package com.example.phishingblock.background;

import android.content.Context;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.HashSet;
import java.util.Set;

public class AudioContentObserver extends ContentObserver {

    private Context context;
    private Set<String> processedFiles;
    private CloudStorageHelper cloudStorageHelper;

    public AudioContentObserver(Handler handler, Context context) {
        super(handler);
        this.context = context;
        this.processedFiles = new HashSet<>();
        // CloudStorageHelper 인스턴스 생성 시 context 전달
        this.cloudStorageHelper = new CloudStorageHelper(context);
    }

    @Override
    public void onChange(boolean selfChange, Uri uri) {
        super.onChange(selfChange, uri);
        Log.d("AudioContentObserver", "MediaStore 변경 감지: " + uri.toString());

        // 새로 추가된 파일이 오디오 파일인지 확인하고 처리
        if (uri.toString().contains(MediaStore.Audio.Media.EXTERNAL_CONTENT_URI.toString())) {
            queryNewAudioFile(uri);
        }
    }

    // 새로 추가된 오디오 파일의 정보를 조회하고 처리
    private void queryNewAudioFile(Uri uri) {
        String[] projection = {
                MediaStore.Audio.Media.DISPLAY_NAME
        };

        try (Cursor cursor = context.getContentResolver().query(
                uri, projection, null, null, MediaStore.Audio.Media.DATE_ADDED + " DESC")) {

            if (cursor != null && cursor.moveToFirst()) {
                String fileName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME));
                Log.d("AudioContentObserver", "새로운 오디오 파일 감지: " + fileName);

                // 파일 이름이 이미 처리된 목록에 있는지 확인
                if (!processedFiles.contains(fileName)) {
                    processedFiles.add(fileName);
                    // Android 10 이상에서는 Uri로 처리
                    processNewAudioFile(uri, fileName);
                } else {
                    Log.d("AudioContentObserver", "이미 처리된 파일: " + fileName);
                }
            }
        } catch (Exception e) {
            Log.e("AudioContentObserver", "오디오 파일 조회 중 오류 발생", e);
        }
    }

    // 새 오디오 파일 처리: 변환 후 STT 작업
    private void processNewAudioFile(Uri uri, String fileName) {
        try {
            // Android 10 이상에서는 Uri에서 InputStream을 얻어 임시 파일로 변환
            File tempFile = createTempFileFromUri(uri, fileName);

            // 변환된 파일 경로
            String outputWavFilePath = context.getExternalFilesDir(null) + "/converted_" + fileName + ".wav";

            // 1. 파일 변환 (m4a → wav)
            AudioConverter.convertM4AToWav(tempFile.getAbsolutePath(), outputWavFilePath);

            // 2. 변환된 파일을 GCS에 업로드 (비동기)
            cloudStorageHelper.uploadFileToGCS(new File(outputWavFilePath), () -> {
                // GCS 업로드 완료 후 STT 작업 수행
                String bucketName = "phishing_block";
                String objectName = "converted_" + fileName + ".wav";
                String gcsUri = "gs://" + bucketName + "/" + objectName;

                STTExecutor sttExecutor = new STTExecutor(context, gcsUri);
                sttExecutor.startSTTTask();
            });

        } catch (Exception e) {
            Log.e("AudioContentObserver", "오디오 파일 처리 중 오류 발생", e);
        }
    }

    // Uri에서 InputStream을 읽어 임시 파일을 생성하는 메서드
    private File createTempFileFromUri(Uri uri, String fileName) throws Exception {
        File tempFile = new File(context.getCacheDir(), fileName);
        try (InputStream inputStream = context.getContentResolver().openInputStream(uri);
             FileOutputStream outputStream = new FileOutputStream(tempFile)) {

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
        } catch (Exception e) {
            throw new Exception("임시 파일 생성 중 오류 발생", e);
        }
        return tempFile;
    }
}
