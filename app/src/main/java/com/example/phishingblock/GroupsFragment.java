package com.example.phishingblock;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.bitmap.RoundedCorners;
import com.bumptech.glide.request.RequestOptions;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class GroupsFragment extends Fragment {

    private List<GroupMember> groupMembers;
    private GroupMemberAdapter adapter;
    private RecyclerView recyclerView;
    private LinearLayout emptyStateLayout; // 가족이 없을 때 표시하는 레이아웃
    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;
    private AlertDialog dialog;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGroupMembers);
        emptyStateLayout = view.findViewById(R.id.emptyStateLayout); // 가족이 없을 때 표시할 레이아웃
        Button btnAddMember = view.findViewById(R.id.btnAddMember);

        // RecyclerView를 그리드 레이아웃으로 설정
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));
        groupMembers = new ArrayList<>();

        adapter = new GroupMemberAdapter(groupMembers, new GroupMemberAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupMember groupMember) {
                Toast.makeText(getContext(), groupMember.getName() + " 클릭됨", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(GroupMember groupMember) {
                groupMembers.remove(groupMember);
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), groupMember.getName() + " 삭제되었습니다", Toast.LENGTH_SHORT).show();
                toggleEmptyState(); // 삭제 후 상태 업데이트
            }
        }, getContext());

        recyclerView.setAdapter(adapter);

        // 그룹원 추가 버튼 클릭 시 처리
        btnAddMember.setOnClickListener(v -> showAddMemberDialog());

        toggleEmptyState();  // 처음 로드 시 상태 업데이트

        return view;
    }

    // 그룹원이 있는지 확인하여 UI 업데이트
    private void toggleEmptyState() {
        if (groupMembers.isEmpty()) {
            emptyStateLayout.setVisibility(View.VISIBLE);  // 가족 추가 안내를 보임
            recyclerView.setVisibility(View.GONE);         // RecyclerView 숨김
        } else {
            emptyStateLayout.setVisibility(View.GONE);     // 안내 숨김
            recyclerView.setVisibility(View.VISIBLE);      // RecyclerView 보임
        }
    }

    private void showAddMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        EditText editTextEmail = dialogView.findViewById(R.id.editTextEmail);
        ImageView imageViewProfile = dialogView.findViewById(R.id.imageViewProfile);
        Button buttonSelectProfileImage = dialogView.findViewById(R.id.buttonSelectProfileImage);
        Button buttonCancel = dialogView.findViewById(R.id.buttonCancel);
        Button buttonAdd = dialogView.findViewById(R.id.buttonAdd);

        int radiusInDp = 16;
        int radiusInPx = (int) (radiusInDp * getResources().getDisplayMetrics().density);

        // 기본 이미지 설정
        if (selectedImageUri == null) {
            Glide.with(this)
                    .load(R.drawable.ic_image)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(radiusInPx)))
                    .into(imageViewProfile);
        } else {
            Glide.with(this)
                    .load(selectedImageUri)
                    .apply(RequestOptions.bitmapTransform(new RoundedCorners(radiusInPx)))
                    .into(imageViewProfile);
        }

        buttonSelectProfileImage.setOnClickListener(v -> openGallery());

        dialog = builder.create();
        dialog.show();

        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        buttonAdd.setOnClickListener(v -> {
            String name = editTextName.getText().toString().trim();
            String email = editTextEmail.getText().toString().trim();

            if (!TextUtils.isEmpty(name) && selectedImageUri != null) {
                for (GroupMember member : groupMembers) {
                    if (member.getName().equals(name)) {
                        Toast.makeText(getContext(), "이미 존재하는 이름입니다.", Toast.LENGTH_SHORT).show();
                        return;
                    }
                }

                GroupMember newMember = new GroupMember(name, email, selectedImageUri.toString());
                groupMembers.add(newMember);
                adapter.notifyItemInserted(groupMembers.size() - 1);
                recyclerView.scrollToPosition(groupMembers.size() - 1);
                Toast.makeText(getContext(), "가족 구성원 추가됨: " + name, Toast.LENGTH_SHORT).show();
                dialog.dismiss();
                toggleEmptyState(); // 가족 구성원 추가 후 상태 업데이트
            } else {
                if (TextUtils.isEmpty(name)) {
                    editTextName.setError("이름을 입력하세요.");
                }
                if (selectedImageUri == null) {
                    Toast.makeText(getContext(), "이미지를 선택하세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 갤러리 열기
    private void openGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        int radiusInDp = 16;
        int radiusInPx = (int) (radiusInDp * getResources().getDisplayMetrics().density);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            if (dialog != null && dialog.isShowing()) {
                ImageView imageViewProfile = dialog.findViewById(R.id.imageViewProfile);
                Glide.with(this).load(selectedImageUri).apply(RequestOptions.bitmapTransform(new RoundedCorners(radiusInPx))).into(imageViewProfile);
            }
        }
    }
}
