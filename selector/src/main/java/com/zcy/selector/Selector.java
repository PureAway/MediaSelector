package com.zcy.selector;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zcy.selector.ui.SelectorActivity;

import java.lang.ref.WeakReference;
import java.util.List;

public final class Selector {

    private final WeakReference<Activity> mContext;
    private final WeakReference<Fragment> mFragment;

    private Selector(Activity activity) {
        this(activity, null);
    }

    private Selector(Fragment fragment) {
        this(fragment.getActivity(), fragment);
    }

    private Selector(Activity activity, Fragment fragment) {
        mContext = new WeakReference<>(activity);
        mFragment = new WeakReference<>(fragment);
    }

    public static Selector from(Activity activity) {
        return new Selector(activity);
    }


    public static Selector from(Fragment fragment) {
        return new Selector(fragment);
    }


    public static List<Uri> obtainResult(Intent data) {
        return data.getParcelableArrayListExtra(SelectorActivity.EXTRA_RESULT_SELECTION);
    }

    public static List<String> obtainPathResult(Intent data) {
        return data.getStringArrayListExtra(SelectorActivity.EXTRA_RESULT_SELECTION_PATH);
    }

    public static boolean obtainOriginalState(Intent data) {
        return data.getBooleanExtra(SelectorActivity.EXTRA_RESULT_ORIGINAL_ENABLE, false);
    }

    public SelectionCreator choose(MimeType[] mimeTypes) {
        return this.choose(mimeTypes, true);
    }


    public SelectionCreator choose(MimeType[] mimeTypes, boolean mediaTypeExclusive) {
        return new SelectionCreator(this, mimeTypes, mediaTypeExclusive);
    }

    @Nullable
    Activity getActivity() {
        return mContext.get();
    }

    @Nullable
    Fragment getFragment() {
        return mFragment != null ? mFragment.get() : null;
    }

}
