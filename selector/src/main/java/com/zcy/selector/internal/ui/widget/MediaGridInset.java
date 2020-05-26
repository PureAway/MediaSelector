package com.zcy.selector.internal.ui.widget;

import android.graphics.Rect;
import android.view.View;

import androidx.recyclerview.widget.RecyclerView;


public class MediaGridInset extends RecyclerView.ItemDecoration {

    private int mSpanCount;
    private int mSpacing;
    private boolean mIncludeEdge;

    public MediaGridInset(int spanCount, int spacing, boolean includeEdge) {
        this.mSpanCount = spanCount;
        this.mSpacing = spacing;
        this.mIncludeEdge = includeEdge;
    }

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent,
                               RecyclerView.State state) {
        // item position
        int position = parent.getChildAdapterPosition(view);
        // item column
        int column = position % mSpanCount;

        if (mIncludeEdge) {
            // spacing - column * ((1f / spanCount) * spacing)
            outRect.left = mSpacing - column * mSpacing / mSpanCount;
            // (column + 1) * ((1f / spanCount) * spacing)
            outRect.right = (column + 1) * mSpacing / mSpanCount;
            // top edge
            if (position < mSpanCount) {
                outRect.top = mSpacing;
            }
            // item bottom
            outRect.bottom = mSpacing;
        } else {
            // column * ((1f / spanCount) * spacing)
            outRect.left = column * mSpacing / mSpanCount;
            // spacing - (column + 1) * ((1f / spanCount) * spacing)
            outRect.right = mSpacing - (column + 1) * mSpacing / mSpanCount;
            if (position >= mSpanCount) {
                // item top
                outRect.top = mSpacing;
            }
        }
    }
}
