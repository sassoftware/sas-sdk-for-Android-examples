package com.sas.android.covid19.ui.recycler;

import android.graphics.Rect;
import android.view.View;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.RecyclerView.ItemDecoration;
import androidx.recyclerview.widget.RecyclerView.LayoutManager;
import androidx.recyclerview.widget.RecyclerView.State;

public class LinearSpaceItemDecoration extends ItemDecoration {
    /*
     * Instance data
     */

    private int mSpace;
    private boolean atBegin;
    private boolean atEnd;

    /*
     * Constructors
     */

    public LinearSpaceItemDecoration(int space, boolean atBegin, boolean atEnd) {
        mSpace = space;
        this.atBegin = atBegin;
        this.atEnd = atEnd;
    }

    public LinearSpaceItemDecoration(int space) {
        this(space, false, false);
    }

    /*
     * ItemDecoration methods
     */

    @Override
    public void getItemOffsets(Rect outRect, View view, RecyclerView parent, State state) {
        int pos = parent.getChildAdapterPosition(view);
        if (pos == RecyclerView.NO_POSITION) {
            return;
        }

        boolean isFirst = pos == 0;
        boolean isLast = pos == state.getItemCount() - 1;
        switch (getOrientation(parent)) {
            case LinearLayoutManager.HORIZONTAL:
                outRect.left = isFirst && !atBegin ? 0 : mSpace;
                outRect.right = isLast && atEnd ? mSpace : 0;
                outRect.top = 0;
                outRect.bottom = 0;
                break;
            default:
                outRect.top = isFirst && !atBegin ? 0 : mSpace;
                outRect.bottom = isLast && atEnd ? mSpace : 0;
                outRect.left = 0;
                outRect.right = 0;
                break;
        }
    }

    /*
     * Private methods
     */

    private int getOrientation(RecyclerView parent) {
        LayoutManager mgr = parent.getLayoutManager();
        if (mgr instanceof LinearLayoutManager) {
            return ((LinearLayoutManager)mgr).getOrientation();
        }

        throw new IllegalStateException(String.format("%s can only be used with a %s",
                getClass().getSimpleName(), LinearLayoutManager.class.getSimpleName()));
    }
}
