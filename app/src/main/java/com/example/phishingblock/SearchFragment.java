package com.example.phishingblock;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import android.app.AlertDialog;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class SearchFragment extends Fragment {

    private EditText etSearch;
    private RecyclerView recyclerView;
    private PhoneNumberAdapter adapter;
    private List<String> originalPhoneNumbers;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_search, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        etSearch = view.findViewById(R.id.et_search);
        recyclerView = view.findViewById(R.id.recycler_view);

        MainActivity activity = (MainActivity) getActivity();
        if (activity != null) {
            adapter = activity.getPhoneNumberAdapter();
            originalPhoneNumbers = new ArrayList<>(adapter.phoneNumbers);
            adapter.setOnItemClickListener(new PhoneNumberAdapter.OnItemClickListener() {
                @Override
                public void onReportClick(String phoneNumber) {
                    // 신고 버튼 클릭 처리
                }

                @Override
                public void onCheckClick(String phoneNumber) {
                    openReportDetailsFragment(phoneNumber);
                }
            });

            recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
            recyclerView.setAdapter(adapter);
        }

        etSearch.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_SEARCH ||
                        (event != null && event.getKeyCode() == KeyEvent.KEYCODE_ENTER && event.getAction() == KeyEvent.ACTION_DOWN)) {
                    String phoneNumber = etSearch.getText().toString().trim();
                    if (!phoneNumber.isEmpty()) {
                        hideKeyboard();
                        filterPhoneNumbers(phoneNumber);
                    }
                    return true;
                }
                return false;
            }
        });
    }

    private void filterPhoneNumbers(String query) {
        List<String> filteredList = new ArrayList<>();

        for (String phoneNumber : originalPhoneNumbers) {
            if (phoneNumber.contains(query)) {
                filteredList.add(phoneNumber);
            }
        }

        if (filteredList.isEmpty()) {
            showNoResultsDialog(query);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            adapter.updatePhoneNumbers(filteredList);
            adapter.notifyDataSetChanged();
        }
    }

    private void showNoResultsDialog(String phoneNumber) {
        new AlertDialog.Builder(getContext())
                .setTitle("검색된 번호가 없습니다.")
                .setMessage("신고하시겠습니까?")
                .setPositiveButton("확인", (dialog, which) -> showReportDialog(phoneNumber))
                .setNegativeButton("취소", null)
                .show();
    }

    private void showReportDialog(String phoneNumber) {
        View dialogView = LayoutInflater.from(getContext()).inflate(R.layout.dialog_report, null);
        EditText etReportContent = dialogView.findViewById(R.id.et_report_content);

        new AlertDialog.Builder(getContext())
                .setTitle("신고 내용 작성")
                .setView(dialogView)
                .setPositiveButton("제출", (dialog, which) -> {
                    String reportContent = etReportContent.getText().toString().trim();
                    if (!reportContent.isEmpty()) {
                        MainActivity activity = (MainActivity) getActivity();
                        if (activity != null) {
                            PhoneNumberAdapter adapter = activity.getPhoneNumberAdapter();
                            adapter.addPhoneNumber(phoneNumber);
                            adapter.addReport(phoneNumber, reportContent);

                            adapter.notifyDataSetChanged();
                            recyclerView.setVisibility(View.VISIBLE);
                            Toast.makeText(getContext(), "신고가 접수되었습니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(getContext(), "신고 내용을 입력해주세요.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("취소", null)
                .show();
    }

    private void openReportDetailsFragment(String phoneNumber) {
        Bundle bundle = new Bundle();
        bundle.putString("PHONE_NUMBER", phoneNumber);
        ReportDetailsFragment fragment = new ReportDetailsFragment();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getParentFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit();
    }

    private void hideKeyboard() {
        if (getActivity() != null && getView() != null) {
            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
        }
    }
}
