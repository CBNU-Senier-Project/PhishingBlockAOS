package com.example.phishingblock.groups;

import android.graphics.Rect;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class GridSpacingItemDecoration extends RecyclerView.ItemDecoration {
    private int spanCount;
    private int spacing;
    private boolean includeEdge;

    public GridSpacingItemDecoration(int spanCount, int spacing, boolean includeEdge) {
        this.spanCount = spanCount;
        this.spacing = spacing;
        this.includeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(@NonNull Rect outRect, @NonNull View view,
                               @NonNull RecyclerView parent, @NonNull RecyclerView.State state) {
        int position = parent.getChildAdapterPosition(view); // 아이템 위치
        int column = position % spanCount; // 열 번호

        if (includeEdge) {
            outRect.left = spacing - column * spacing / spanCount; // 왼쪽 간격
            outRect.right = (column + 1) * spacing / spanCount; // 오른쪽 간격

            if (position < spanCount) { // 첫 번째 줄
                outRect.top = spacing;
            }
            outRect.bottom = spacing; // 아래 간격
        } else {
            outRect.left = column * spacing / spanCount; // 왼쪽 간격
            outRect.right = spacing - (column + 1) * spacing / spanCount; // 오른쪽 간격
            if (position >= spanCount) {
                outRect.top = spacing; // 위 간격
            }
        }
    }
}
