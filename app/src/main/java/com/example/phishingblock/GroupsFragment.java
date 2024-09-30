package com.example.phishingblock;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

import static android.app.Activity.RESULT_OK;

public class GroupsFragment extends Fragment {

    private List<GroupMember> groupMembers;
    private GroupMemberAdapter adapter;
    private RecyclerView recyclerView; // RecyclerView 전역 변수로 선언

    private static final int PICK_IMAGE_REQUEST = 1;
    private Uri selectedImageUri;  // 선택된 이미지의 Uri를 저장할 변수
    private AlertDialog dialog; // 다이얼로그를 전역 변수로 선언

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_groups, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewGroupMembers); // RecyclerView 초기화
        Button btnAddMember = view.findViewById(R.id.btnAddMember); // FloatingActionButton이 아닌 Button으로 참조

        // RecyclerView를 그리드 레이아웃으로 설정 (한 줄에 2개의 아이템)
        recyclerView.setLayoutManager(new GridLayoutManager(getContext(), 2));

        groupMembers = new ArrayList<>();

        // 어댑터 설정
        adapter = new GroupMemberAdapter(groupMembers, new GroupMemberAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(GroupMember groupMember) {
                Toast.makeText(getContext(), groupMember.getName() + " 클릭됨", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onDeleteClick(GroupMember groupMember) {
                // 삭제 로직
                groupMembers.remove(groupMember);
                adapter.notifyDataSetChanged();
                Toast.makeText(getContext(), groupMember.getName() + " 삭제됨", Toast.LENGTH_SHORT).show();
            }
        }, getContext());

        recyclerView.setAdapter(adapter);

        // 그룹원 추가 버튼 클릭 시 처리
        btnAddMember.setOnClickListener(new View.OnClickListener() { // Button 클릭 리스너 설정
            @Override
            public void onClick(View v) {
                showAddMemberDialog();
            }
        });

        return view;
    }

    private void showAddMemberDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("그룹원 추가");

        // 사용자 정의 레이아웃을 포함한 뷰를 설정
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_member, null);
        builder.setView(dialogView);

        EditText editTextName = dialogView.findViewById(R.id.editTextName);
        ImageView imageViewProfile = dialogView.findViewById(R.id.imageViewProfile);
        Button buttonSelectProfileImage = dialogView.findViewById(R.id.buttonSelectProfileImage);

        // 다이얼로그 열릴 때 기본 이미지 설정
        if (selectedImageUri == null) {
            // 기본 이미지를 설정 (예: R.drawable.default_profile_image)
            Glide.with(this)
                    .load(R.drawable.ic_home)  // 기본 이미지 리소스 ID
                    .into(imageViewProfile);
        } else {
            // 이미 선택된 이미지가 있는 경우 해당 이미지 로드
            Glide.with(this)
                    .load(selectedImageUri)
                    .into(imageViewProfile);
        }

        // 이미지 선택 버튼 클릭 시 갤러리 열기
        buttonSelectProfileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                openGallery();  // 갤러리 열기 메서드 호출
            }
        });

        // 다이얼로그의 추가 버튼 클릭 리스너를 설정
        builder.setPositiveButton("추가", null); // 다이얼로그에서 자동으로 닫히지 않도록 하기 위해 null 처리

        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        dialog = builder.create(); // 다이얼로그를 변수에 저장
        dialog.show();

        // 긍정 버튼 클릭 리스너를 다이얼로그를 표시한 후에 설정하여 다이얼로그가 자동으로 닫히지 않게 처리
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = editTextName.getText().toString().trim();

                // 이름이 비어 있지 않고 이미지 URI가 유효한지 확인
                if (!TextUtils.isEmpty(name) && selectedImageUri != null) {
                    // 중복 여부 체크
                    for (GroupMember member : groupMembers) {
                        if (member.getName().equals(name)) {
                            Toast.makeText(getContext(), "이미 존재하는 이름입니다.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // UI 스레드에서 리스트 및 어댑터 업데이트
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            GroupMember newMember = new GroupMember(name, selectedImageUri.toString());
                            groupMembers.add(newMember); // 리스트에 추가
                            adapter.notifyItemInserted(groupMembers.size() - 1); // 어댑터에 추가된 위치 알림
                            recyclerView.scrollToPosition(groupMembers.size() - 1); // 스크롤 위치 이동
                        }
                    });

                    Toast.makeText(getContext(), "그룹원 추가됨: " + name, Toast.LENGTH_SHORT).show();
                    dialog.dismiss(); // 다이얼로그 닫기
                } else {
                    // 오류 메시지 추가
                    if (TextUtils.isEmpty(name)) {
                        editTextName.setError("이름을 입력하세요.");
                    }
                    if (selectedImageUri == null) {
                        Toast.makeText(getContext(), "이미지를 선택하세요.", Toast.LENGTH_SHORT).show();
                    }
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

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            selectedImageUri = data.getData();

            if (dialog != null && dialog.isShowing()) {
                // 다이얼로그가 열려 있을 때만 ImageView에 이미지 로드
                ImageView imageViewProfile = dialog.findViewById(R.id.imageViewProfile);
                Glide.with(this).load(selectedImageUri).into(imageViewProfile);
            }
        }
    }

}
