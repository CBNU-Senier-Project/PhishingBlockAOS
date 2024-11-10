package com.example.phishingblock.groups;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.phishingblock.R;
import com.example.phishingblock.background.TokenManager;
import com.example.phishingblock.network.ApiService;
import com.example.phishingblock.network.RetrofitClient;
import com.example.phishingblock.network.payload.GroupMemberResponse;
import com.example.phishingblock.network.payload.ImageRequest;
import com.example.phishingblock.network.payload.ImageResponse;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class GroupMemberAdapter extends RecyclerView.Adapter<GroupMemberAdapter.ViewHolder> {

    private List<GroupMemberResponse> groupMemberResponses;
    private Context context;
    private OnMemberDeleteListener deleteListener;
    private OnMemberEditListener editListener; // 추가된 부분
    private OnImageEditListener imageEditListener;
    private long groupId;

    public GroupMemberAdapter(List<GroupMemberResponse> groupMemberResponses, Context context, long groupId, OnMemberDeleteListener deleteListener, OnMemberEditListener editListener,OnImageEditListener imageEditListener) {
        this.groupMemberResponses = groupMemberResponses;
        this.context = context;
        this.groupId = groupId;
        this.deleteListener = deleteListener;
        this.editListener = editListener; // 추가된 부분
        this.imageEditListener = imageEditListener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_group_member, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        GroupMemberResponse member = groupMemberResponses.get(position);
        holder.nameTextView.setText(member.getName());
        holder.phoneTextview.setText(member.getPhnum());


        // Glide를 사용하여 프로필 이미지 로드
        Glide.with(context).load(member.getImageUrl()).into(holder.profileImageView);

        // 프로필 이미지 클릭 시 확대 다이얼로그 표시
        holder.profileImageView.setOnClickListener(v -> showZoomedImageDialog(member.getImageUrl(),member));

        // 삭제 버튼 클릭 시
        holder.deleteButton.setOnClickListener(v -> deleteListener.onMemberDelete(member.getUserId()));

        // 닉네임 변경 버튼 클릭 시
        holder.editButton.setOnClickListener(v -> editListener.onMemberEdit(member.getUserId(), member.getName()));
    }

    private void showZoomedImageDialog(String imageUrl, GroupMemberResponse member) {
        Dialog dialog = new Dialog(context);
        dialog.setContentView(R.layout.dialog_zoomed_image);
        dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        ImageView zoomedImageView = dialog.findViewById(R.id.zoomedImageView);
        ImageView closeButton = dialog.findViewById(R.id.closeButton);
        Button editButton = dialog.findViewById(R.id.editButton);

        Glide.with(context).load(imageUrl).into(zoomedImageView);

        closeButton.setOnClickListener(v -> dialog.dismiss());

        // Trigger the image edit listener when the edit button is clicked
        editButton.setOnClickListener(v -> {
            if (imageEditListener != null) {
                imageEditListener.onImageEditRequest(member);
            }
            dialog.dismiss();
        });

        dialog.show();
    }


    public void updateMemberImage(GroupMemberResponse member, Uri imageUri) {
        if (imageUri != null) {
            // updateImage 메서드를 호출하여 서버에 업로드
            updateImage(groupId + member.getName() + ".png", member, imageUri);
        }
    }

    private void updateImage(String newImageName, GroupMemberResponse member, Uri imageUri) {
        ApiService apiService = RetrofitClient.getApiService();
        String token = TokenManager.getAccessToken(context);

        ImageRequest imageRequest = new ImageRequest(newImageName);

        // 서버에 URL 요청
        apiService.updateImage(token, groupId, member.getUserId(), imageRequest).enqueue(new Callback<ImageResponse>() {
            @Override
            public void onResponse(Call<ImageResponse> call, Response<ImageResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    String uploadUrl = response.body().getImagename(); // ImageResponse 객체에서 URL 가져오기
                    uploadImageToUrl(uploadUrl, imageUri);
                } else {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "이미지 URL 요청 실패", Toast.LENGTH_SHORT).show()
                    );
                }
            }

            @Override
            public void onFailure(Call<ImageResponse> call, Throwable t) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "네트워크 오류: " + t.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }
        });
    }


    public interface OnImageEditListener {
        void onImageEditRequest(GroupMemberResponse member);
    }


    private void uploadImageToUrl(String uploadUrl, Uri imageUri) {
        OkHttpClient client = new OkHttpClient();

        try {
            // ContentResolver를 통해 Uri로부터 InputStream을 가져옴
            InputStream inputStream = context.getContentResolver().openInputStream(imageUri);
            if (inputStream == null) {
                new Handler(Looper.getMainLooper()).post(() ->
                        Toast.makeText(context, "이미지 파일을 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                );
                return;
            }

            // InputStream을 RequestBody로 변환
            byte[] imageBytes = getBytesFromInputStream(inputStream);
            RequestBody requestFile = RequestBody.create(MediaType.parse("image/png"), imageBytes);

            // PUT 요청을 생성하여 부여받은 GCS signed URL로 이미지 업로드
            Request request = new Request.Builder()
                    .url(uploadUrl)
                    .put(requestFile)
                    .addHeader("Content-Type", "image/png") // 파일 형식에 따라 Content-Type 설정
                    .build();

            client.newCall(request).enqueue(new okhttp3.Callback() {
                @Override
                public void onResponse(okhttp3.Call call, okhttp3.Response response) {
                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (response.isSuccessful()) {
                            Toast.makeText(context, "이미지 업로드 성공", Toast.LENGTH_SHORT).show();
                        } else {
                            // response.body()를 통해 오류 메시지 확인
                            try {
                                String errorMessage = response.body() != null ? response.body().string() : "Unknown error";
                                Toast.makeText(context, "이미지 업로드 실패: " + errorMessage, Toast.LENGTH_LONG).show();
                            } catch (IOException e) {
                                Toast.makeText(context, "오류 메시지 가져오기 실패", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                }

                @Override
                public void onFailure(okhttp3.Call call, IOException e) {
                    new Handler(Looper.getMainLooper()).post(() ->
                            Toast.makeText(context, "업로드 중 오류 발생: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                    );
                }
            });
        } catch (IOException e) {
            new Handler(Looper.getMainLooper()).post(() ->
                    Toast.makeText(context, "이미지 파일을 열 수 없습니다: " + e.getMessage(), Toast.LENGTH_SHORT).show()
            );
        }
    }

    // InputStream을 바이트 배열로 변환하는 유틸리티 메서드
    private byte[] getBytesFromInputStream(InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteBuffer = new ByteArrayOutputStream();
        int bufferSize = 1024;
        byte[] buffer = new byte[bufferSize];

        int len;
        while ((len = inputStream.read(buffer)) != -1) {
            byteBuffer.write(buffer, 0, len);
        }
        return byteBuffer.toByteArray();
    }





    @Override
    public int getItemCount() {
        return groupMemberResponses.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView profileImageView, deleteButton, editButton; // 수정된 부분
        TextView nameTextView,phoneTextview;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            profileImageView = itemView.findViewById(R.id.profileImageView);
            nameTextView = itemView.findViewById(R.id.nameTextView);
            deleteButton = itemView.findViewById(R.id.deleteButton);
            editButton = itemView.findViewById(R.id.editButton); // 추가된 부분
            phoneTextview = itemView.findViewById(R.id.phoneTextView);

        }
    }

    // 멤버 삭제 리스너 인터페이스
    public interface OnMemberDeleteListener {
        void onMemberDelete(long memberId);
    }

    // 멤버 닉네임 변경 리스너 인터페이스
    public interface OnMemberEditListener { // 추가된 부분
        void onMemberEdit(long memberId, String currentName);
    }
}
