package com.zcy.selector.internal.entity;

import android.content.pm.ActivityInfo;

import androidx.annotation.StyleRes;

import com.zcy.selector.MimeType;
import com.zcy.selector.R;
import com.zcy.selector.engine.ImageEngine;
import com.zcy.selector.engine.impl.GlideEngine;
import com.zcy.selector.filter.Filter;
import com.zcy.selector.listener.OnCheckedListener;
import com.zcy.selector.listener.OnSelectedListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public final class SelectionSpec {

    private MimeType[] mimeTypeSet;
    private boolean mediaTypeExclusive;
    private boolean showSingleMediaType;
    @StyleRes
    private int themeId;
    private int orientation;
    private boolean countable;
    private int maxSelectable;
    private int maxImageSelectable;
    private int maxVideoSelectable;
    private List<Filter> filters;
    private boolean capture;
    private CaptureStrategy captureStrategy;
    private int spanCount;
    private int gridExpectedSize;
    private float thumbnailScale;
    private ImageEngine imageEngine;
    private boolean initialized;
    private OnSelectedListener onSelectedListener;
    private boolean original;
    private boolean autoHideToolbar;
    private int originalMaxSize;
    private OnCheckedListener onCheckedListener;
    private boolean showPreview;

    private SelectionSpec() {
    }

    public static SelectionSpec getInstance() {
        return InstanceHolder.INSTANCE;
    }

    public static SelectionSpec getCleanInstance() {
        SelectionSpec selectionSpec = getInstance();
        selectionSpec.reset();
        return selectionSpec;
    }

    private void reset() {
        mimeTypeSet = null;
        mediaTypeExclusive = true;
        showSingleMediaType = false;
        themeId = R.style.Matisse_Zhihu;
        orientation = 0;
        countable = false;
        maxSelectable = 1;
        maxImageSelectable = 0;
        maxVideoSelectable = 0;
        filters = null;
        capture = false;
        captureStrategy = null;
        spanCount = 3;
        gridExpectedSize = 0;
        thumbnailScale = 0.5f;
        imageEngine = new GlideEngine();
        initialized = true;
        original = false;
        autoHideToolbar = false;
        originalMaxSize = Integer.MAX_VALUE;
        showPreview = true;
    }

    public boolean singleSelectionModeEnabled() {
        return !countable && (maxSelectable == 1 || (maxImageSelectable == 1 && maxVideoSelectable == 1));
    }

    public boolean needOrientationRestriction() {
        return orientation != ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
    }

    public boolean onlyShowImages() {
        return showSingleMediaType && Arrays.equals(MimeType.ofImage(), mimeTypeSet);
    }

    public boolean onlyShowVideos() {
        return showSingleMediaType && Arrays.equals(MimeType.ofVideo(), mimeTypeSet);
    }

    public boolean onlyShowGif() {
        return showSingleMediaType && Arrays.equals(MimeType.ofGif(), mimeTypeSet);
    }

    private static final class InstanceHolder {
        private static final SelectionSpec INSTANCE = new SelectionSpec();
    }

    public MimeType[] getMimeTypeSet() {
        return mimeTypeSet;
    }

    public void setMimeTypeSet(MimeType[] mimeTypeSet) {
        this.mimeTypeSet = mimeTypeSet;
    }

    public boolean isMediaTypeExclusive() {
        return mediaTypeExclusive;
    }

    public void setMediaTypeExclusive(boolean mediaTypeExclusive) {
        this.mediaTypeExclusive = mediaTypeExclusive;
    }

    public boolean isShowSingleMediaType() {
        return showSingleMediaType;
    }

    public void setShowSingleMediaType(boolean showSingleMediaType) {
        this.showSingleMediaType = showSingleMediaType;
    }

    public int getThemeId() {
        return themeId;
    }

    public void setThemeId(int themeId) {
        this.themeId = themeId;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public boolean isCountable() {
        return countable;
    }

    public void setCountable(boolean countable) {
        this.countable = countable;
    }

    public int getMaxSelectable() {
        return maxSelectable;
    }

    public void setMaxSelectable(int maxSelectable) {
        this.maxSelectable = maxSelectable;
    }

    public int getMaxImageSelectable() {
        return maxImageSelectable;
    }

    public void setMaxImageSelectable(int maxImageSelectable) {
        this.maxImageSelectable = maxImageSelectable;
    }

    public int getMaxVideoSelectable() {
        return maxVideoSelectable;
    }

    public void setMaxVideoSelectable(int maxVideoSelectable) {
        this.maxVideoSelectable = maxVideoSelectable;
    }

    public List<Filter> getFilters() {
        return filters;
    }

    public void setFilters(List<Filter> filters) {
        this.filters = filters;
    }

    public boolean isCapture() {
        return capture;
    }

    public void setCapture(boolean capture) {
        this.capture = capture;
    }

    public CaptureStrategy getCaptureStrategy() {
        return captureStrategy;
    }

    public void setCaptureStrategy(CaptureStrategy captureStrategy) {
        this.captureStrategy = captureStrategy;
    }

    public int getSpanCount() {
        return spanCount;
    }

    public void setSpanCount(int spanCount) {
        this.spanCount = spanCount;
    }

    public int getGridExpectedSize() {
        return gridExpectedSize;
    }

    public void setGridExpectedSize(int gridExpectedSize) {
        this.gridExpectedSize = gridExpectedSize;
    }

    public float getThumbnailScale() {
        return thumbnailScale;
    }

    public void setThumbnailScale(float thumbnailScale) {
        this.thumbnailScale = thumbnailScale;
    }

    public ImageEngine getImageEngine() {
        return imageEngine;
    }

    public void setImageEngine(ImageEngine imageEngine) {
        this.imageEngine = imageEngine;
    }

    public boolean isInitialized() {
        return initialized;
    }

    public void setInitialized(boolean initialized) {
        this.initialized = initialized;
    }

    public OnSelectedListener getOnSelectedListener() {
        return onSelectedListener;
    }

    public void setOnSelectedListener(OnSelectedListener onSelectedListener) {
        this.onSelectedListener = onSelectedListener;
    }

    public boolean isOriginal() {
        return original;
    }

    public void setOriginal(boolean original) {
        this.original = original;
    }

    public boolean isAutoHideToolbar() {
        return autoHideToolbar;
    }

    public void setAutoHideToolbar(boolean autoHideToolbar) {
        this.autoHideToolbar = autoHideToolbar;
    }

    public int getOriginalMaxSize() {
        return originalMaxSize;
    }

    public void setOriginalMaxSize(int originalMaxSize) {
        this.originalMaxSize = originalMaxSize;
    }

    public OnCheckedListener getOnCheckedListener() {
        return onCheckedListener;
    }

    public void setOnCheckedListener(OnCheckedListener onCheckedListener) {
        this.onCheckedListener = onCheckedListener;
    }

    public boolean isShowPreview() {
        return showPreview;
    }

    public void setShowPreview(boolean showPreview) {
        this.showPreview = showPreview;
    }
}
