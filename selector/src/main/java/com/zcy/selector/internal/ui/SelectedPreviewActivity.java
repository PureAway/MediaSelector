package com.zcy.selector.internal.ui;

import android.os.Bundle;

import androidx.annotation.Nullable;


import com.zcy.selector.internal.entity.Item;
import com.zcy.selector.internal.entity.SelectionSpec;
import com.zcy.selector.internal.model.SelectedItemCollection;

import java.util.List;


public class SelectedPreviewActivity extends BasePreviewActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SelectionSpec.getInstance().isInitialized()) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }

        Bundle bundle = getIntent().getBundleExtra(EXTRA_DEFAULT_BUNDLE);
        List<Item> selected = bundle.getParcelableArrayList(SelectedItemCollection.STATE_SELECTION);
        mAdapter.addAll(selected);
        mAdapter.notifyDataSetChanged();
        if (mSpec.isCountable()) {
            mCheckView.setCheckedNum(1);
        } else {
            mCheckView.setChecked(true);
        }
        mPreviousPos = 0;
        updateSize(selected.get(0));
    }

}
