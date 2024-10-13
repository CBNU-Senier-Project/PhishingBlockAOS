package com.example.phishingblock.background;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

import com.example.phishingblock.MainActivity;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class STTExecutor {
    private Context context;
    private String gcsUri;
    private ExecutorService executorService;

    public STTExecutor(Context context, String gcsUri) {
        this.context = context;
        this.gcsUri = gcsUri;
        this.executorService = Executors.newSingleThreadExecutor(); // 하나의 스레드로 작업 처리
    }

    public void startSTTTask() {
        executorService.execute(new Runnable() {
            @Override
            public void run() {
                try {
                    STTHelper sttHelper = new STTHelper(context);
                    sttHelper.asyncRecognizeGcs(gcsUri);

                    // 작업 완료 후 UI 업데이트 (메인 스레드에서 실행)
                    ((MainActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "STT 작업이 성공적으로 완료되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });

                } catch (Exception e) {
                    Log.e("STTExecutor", "STT 작업 중 오류 발생", e);

                    // 오류 발생 시 UI 업데이트 (메인 스레드에서 실행)
                    ((MainActivity) context).runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(context, "STT 작업 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }

    public void shutdown() {
        executorService.shutdown(); // 작업이 끝난 후 스레드를 종료
    }
}
