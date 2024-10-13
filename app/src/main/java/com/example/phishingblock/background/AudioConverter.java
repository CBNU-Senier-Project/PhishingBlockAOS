package com.example.phishingblock.background;

import android.util.Log;

import com.arthenica.mobileffmpeg.FFmpeg;

public class AudioConverter {
    public static void convertM4AToWav(String inputPath, String outputPath) {
        // .m4a 파일을 .wav로 변환하는 FFmpeg 명령어
        String[] command = {
                "-i", inputPath,  // 입력 파일
                "-acodec", "pcm_s16le",  // 오디오 코덱
                "-ar", "16000",  // 샘플링 레이트
                outputPath  // 출력 파일
        };

        // FFmpeg 실행
        int result = FFmpeg.execute(command);
        if (result == 0) {
           Log.d("converter","변환 성공: " + outputPath);
        } else {
            Log.d("converter","변환 실패: " + result);
        }
    }
}
