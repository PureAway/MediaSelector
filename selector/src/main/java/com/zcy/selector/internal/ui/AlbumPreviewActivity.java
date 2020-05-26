package com.zcy.selector.internal.ui;

import android.database.Cursor;
import android.os.Bundle;

import androidx.annotation.Nullable;


import com.facebook.drawee.backends.pipeline.Fresco;
import com.zcy.selector.internal.entity.Album;
import com.zcy.selector.internal.entity.Item;
import com.zcy.selector.internal.entity.SelectionSpec;
import com.zcy.selector.internal.model.AlbumMediaCollection;
import com.zcy.selector.internal.ui.adapter.PreviewPagerAdapter;

import java.util.ArrayList;
import java.util.List;

public class AlbumPreviewActivity extends BasePreviewActivity implements
        AlbumMediaCollection.AlbumMediaCallbacks {

    public static final String EXTRA_ALBUM = "extra_album";
    public static final String EXTRA_ITEM = "extra_item";

    private AlbumMediaCollection mCollection = new AlbumMediaCollection();

    private boolean mIsAlreadySetPosition;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (!SelectionSpec.getInstance().isInitialized()) {
            setResult(RESULT_CANCELED);
            finish();
            return;
        }
        mCollection.onCreate(this, this);
        Album album = getIntent().getParcelableExtra(EXTRA_ALBUM);
        mCollection.startLoad(album);

        Item item = getIntent().getParcelableExtra(EXTRA_ITEM);
        if (mSpec.isCountable()) {
            mCheckView.setCheckedNum(mSelectedCollection.checkedNumOf(item));
        } else {
            mCheckView.setChecked(mSelectedCollection.isSelected(item));
        }
        updateSize(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mCollection.onDestroy();
    }

    @Override
    public void onAlbumMediaLoad(Cursor cursor) {
        try {
            List<Item> items = new ArrayList<>();
            while (cursor.moveToNext()) {
                items.add(Item.valueOf(cursor));
            }

            if (items.isEmpty()) {
                return;
            }

            PreviewPagerAdapter adapter = (PreviewPagerAdapter) mPager.getAdapter();
            adapter.addAll(items);
            adapter.notifyDataSetChanged();
            if (!mIsAlreadySetPosition) {
                //onAlbumMediaLoad is called many times..
                mIsAlreadySetPosition = true;
                Item selected = getIntent().getParcelableExtra(EXTRA_ITEM);
                int selectedIndex = items.indexOf(selected);
                mPager.setCurrentItem(selectedIndex, false);
                mPreviousPos = selectedIndex;
            }
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        } finally {
            if (null != cursor) {
                cursor.close();
            }
        }
    }

    @Override
    public void onLoadFailed(Throwable throwable) {

    }

}
