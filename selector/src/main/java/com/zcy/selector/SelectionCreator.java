package com.zcy.selector;

import android.app.Activity;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;
import androidx.annotation.StyleRes;
import androidx.fragment.app.Fragment;

import com.zcy.selector.engine.ImageEngine;
import com.zcy.selector.filter.Filter;
import com.zcy.selector.internal.entity.CaptureStrategy;
import com.zcy.selector.internal.entity.SelectionSpec;
import com.zcy.selector.listener.OnCheckedListener;
import com.zcy.selector.listener.OnSelectedListener;
import com.zcy.selector.ui.SelectorActivity;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.ArrayList;

import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_BEHIND;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_FULL_USER;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_LOCKED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_NOSENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE;
import static android.content.pm.ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT;

public final class SelectionCreator {

    private final Selector mSelector;
    private final SelectionSpec mSelectionSpec;

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR2)
    @IntDef({
            SCREEN_ORIENTATION_UNSPECIFIED,
            SCREEN_ORIENTATION_LANDSCAPE,
            SCREEN_ORIENTATION_PORTRAIT,
            SCREEN_ORIENTATION_USER,
            SCREEN_ORIENTATION_BEHIND,
            SCREEN_ORIENTATION_SENSOR,
            SCREEN_ORIENTATION_NOSENSOR,
            SCREEN_ORIENTATION_SENSOR_LANDSCAPE,
            SCREEN_ORIENTATION_SENSOR_PORTRAIT,
            SCREEN_ORIENTATION_REVERSE_LANDSCAPE,
            SCREEN_ORIENTATION_REVERSE_PORTRAIT,
            SCREEN_ORIENTATION_FULL_SENSOR,
            SCREEN_ORIENTATION_USER_LANDSCAPE,
            SCREEN_ORIENTATION_USER_PORTRAIT,
            SCREEN_ORIENTATION_FULL_USER,
            SCREEN_ORIENTATION_LOCKED
    })
    @Retention(RetentionPolicy.SOURCE)
    @interface ScreenOrientation {
    }

    /**
     * Constructs a new specification builder on the context.
     *
     * @param selector  a requester context wrapper.
     * @param mimeTypes MIME type set to select.
     */
    SelectionCreator(Selector selector, @NonNull MimeType[] mimeTypes, boolean mediaTypeExclusive) {
        mSelector = selector;
        mSelectionSpec = SelectionSpec.getCleanInstance();
        mSelectionSpec.setMimeTypeSet(mimeTypes);
        mSelectionSpec.setMediaTypeExclusive(mediaTypeExclusive);
        mSelectionSpec.setOrientation(SCREEN_ORIENTATION_UNSPECIFIED);
    }

    /**
     * Whether to show only one media type if choosing medias are only images or videos.
     *
     * @param showSingleMediaType whether to show only one media type, either images or videos.
     * @return {@link SelectionCreator} for fluent API.
     * @see SelectionSpec#onlyShowImages()
     * @see SelectionSpec#onlyShowVideos()
     */
    public SelectionCreator showSingleMediaType(boolean showSingleMediaType) {
        mSelectionSpec.setShowSingleMediaType(showSingleMediaType);
        return this;
    }

    /**
     * Theme for media selecting Activity.
     * <p>
     * There are two built-in themes:
     * 1. com.zcy.selector.R.style.Matisse_Zhihu;
     * 2. com.zcy.selector.R.style.Matisse_Dracula
     * you can define a custom theme derived from the above ones or other themes.
     *
     * @param themeId theme resource id. Default value is com.zhihu.matisse.R.style.Matisse_Zhihu.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator theme(@StyleRes int themeId) {
        mSelectionSpec.setThemeId(themeId);
        return this;
    }

    /**
     * Show a auto-increased number or a check mark when user select media.
     *
     * @param countable true for a auto-increased number from 1, false for a check mark. Default
     *                  value is false.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator countable(boolean countable) {
        mSelectionSpec.setCountable(countable);
        return this;
    }

    /**
     * Maximum selectable count.
     *
     * @param maxSelectable Maximum selectable count. Default value is 1.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator maxSelectable(int maxSelectable) {
        if (maxSelectable < 1) {
            throw new IllegalArgumentException("maxSelectable must be greater than or equal to one");
        }
        if (mSelectionSpec.getMaxImageSelectable() > 0 || mSelectionSpec.getMaxVideoSelectable() > 0) {
            throw new IllegalStateException("already set maxImageSelectable and maxVideoSelectable");
        }
        mSelectionSpec.setMaxSelectable(maxSelectable);
        return this;
    }

    /**
     * Only useful when {@link SelectionSpec#isMediaTypeExclusive()} set true and you want to set different maximum
     * selectable files for image and video media types.
     *
     * @param maxImageSelectable Maximum selectable count for image.
     * @param maxVideoSelectable Maximum selectable count for video.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator maxSelectablePerMediaType(int maxImageSelectable, int maxVideoSelectable) {
        if (maxImageSelectable < 1 || maxVideoSelectable < 1) {
            throw new IllegalArgumentException(("max selectable must be greater than or equal to one"));
        }
        mSelectionSpec.setMaxSelectable(-1);
        mSelectionSpec.setMaxImageSelectable(maxImageSelectable);
        mSelectionSpec.setMaxVideoSelectable(maxVideoSelectable);
        return this;
    }

    /**
     * Add filter to filter each selecting item.
     *
     * @param filter {@link Filter}
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator addFilter(@NonNull Filter filter) {
        if (mSelectionSpec.getFilters() == null) {
            mSelectionSpec.setFilters(new ArrayList<Filter>());
        }
        if (filter == null) {
            throw new IllegalArgumentException("filter cannot be null");
        }
        mSelectionSpec.getFilters().add(filter);
        return this;
    }

    /**
     * Determines whether the photo capturing is enabled or not on the media grid view.
     * <p>
     * If this value is set true, photo capturing entry will appear only on All Media's page.
     *
     * @param enable Whether to enable capturing or not. Default value is false;
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator capture(boolean enable) {
        mSelectionSpec.setCapture(enable);
        return this;
    }

    /**
     * Show a original photo check options.Let users decide whether use original photo after select
     *
     * @param enable Whether to enable original photo or not
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator originalEnable(boolean enable) {
        mSelectionSpec.setOriginal(enable);
        return this;
    }


    /**
     * Determines Whether to hide top and bottom toolbar in PreView mode ,when user tap the picture
     *
     * @param enable
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator autoHideToolbarOnSingleTap(boolean enable) {
        mSelectionSpec.setAutoHideToolbar(enable);
        return this;
    }

    /**
     * Maximum original size,the unit is MB. Only useful when {link@originalEnable} set true
     *
     * @param size Maximum original size. Default value is Integer.MAX_VALUE
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator maxOriginalSize(int size) {
        mSelectionSpec.setOriginalMaxSize(size);
        return this;
    }

    /**
     * Capture strategy provided for the location to save photos including internal and external
     * storage and also a authority for {@link androidx.core.content.FileProvider}.
     *
     * @param captureStrategy {@link CaptureStrategy}, needed only when capturing is enabled.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator captureStrategy(CaptureStrategy captureStrategy) {
        mSelectionSpec.setCaptureStrategy(captureStrategy);
        return this;
    }

    /**
     * Set the desired orientation of this activity.
     *
     * @param orientation An orientation constant as used in {@link ScreenOrientation}.
     *                    Default value is {@link android.content.pm.ActivityInfo#SCREEN_ORIENTATION_PORTRAIT}.
     * @return {@link SelectionCreator} for fluent API.
     * @see Activity#setRequestedOrientation(int)
     */
    public SelectionCreator restrictOrientation(@ScreenOrientation int orientation) {
        mSelectionSpec.setOrientation(orientation);
        return this;
    }

    /**
     * Set a fixed span count for the media grid. Same for different screen orientations.
     * <p>
     * This will be ignored when {@link #gridExpectedSize(int)} is set.
     *
     * @param spanCount Requested span count.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator spanCount(int spanCount) {
        if (spanCount < 1) {
            throw new IllegalArgumentException("spanCount cannot be less than 1");
        }
        mSelectionSpec.setSpanCount(spanCount);
        return this;
    }

    /**
     * Set expected size for media grid to adapt to different screen sizes. This won't necessarily
     * be applied cause the media grid should fill the view container. The measured media grid's
     * size will be as close to this value as possible.
     *
     * @param size Expected media grid size in pixel.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator gridExpectedSize(int size) {
        mSelectionSpec.setGridExpectedSize(size);
        return this;
    }

    /**
     * Photo thumbnail's scale compared to the View's size. It should be a float value in (0.0,
     * 1.0].
     *
     * @param scale Thumbnail's scale in (0.0, 1.0]. Default value is 0.5.
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator thumbnailScale(float scale) {
        if (scale <= 0f || scale > 1f) {
            throw new IllegalArgumentException("Thumbnail scale must be between (0.0, 1.0]");
        }
        mSelectionSpec.setThumbnailScale(scale);
        return this;
    }

    /**
     * Provide an image engine.
     * <p>
     * There are two built-in image engines:
     * 1. {@link com.zcy.selector.engine.impl.GlideEngine}
     * 2. {@link com.zcy.selector.engine.impl.PicassoEngine}
     * And you can implement your own image engine.
     *
     * @param imageEngine {@link ImageEngine}
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator imageEngine(ImageEngine imageEngine) {
        mSelectionSpec.setImageEngine(imageEngine);
        return this;
    }

    /**
     * Set listener for callback immediately when user select or unselect something.
     * <p>
     * It's a redundant API with {@link Selector#obtainResult(Intent)},
     * we only suggest you to use this API when you need to do something immediately.
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link SelectionCreator} for fluent API.
     */
    @NonNull
    public SelectionCreator setOnSelectedListener(@Nullable OnSelectedListener listener) {
        mSelectionSpec.setOnSelectedListener(listener);
        return this;
    }

    /**
     * Set listener for callback immediately when user check or uncheck original.
     *
     * @param listener {@link OnSelectedListener}
     * @return {@link SelectionCreator} for fluent API.
     */
    public SelectionCreator setOnCheckedListener(@Nullable OnCheckedListener listener) {
        mSelectionSpec.setOnCheckedListener(listener);
        return this;
    }

    /**
     * Start to select media and wait for result.
     *
     * @param requestCode Identity of the request Activity or Fragment.
     */
    public void forResult(int requestCode) {
        Activity activity = mSelector.getActivity();
        if (activity == null) {
            return;
        }

        Intent intent = new Intent(activity, SelectorActivity.class);

        Fragment fragment = mSelector.getFragment();
        if (fragment != null) {
            fragment.startActivityForResult(intent, requestCode);
        } else {
            activity.startActivityForResult(intent, requestCode);
        }
    }

    public SelectionCreator showPreview(boolean showPreview) {
        mSelectionSpec.setShowPreview(showPreview);
        return this;
    }

}
