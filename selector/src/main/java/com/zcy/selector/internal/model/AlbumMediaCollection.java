package com.zcy.selector.internal.model;

import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.provider.MediaStore;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;


import com.zcy.selector.internal.entity.Album;
import com.zcy.selector.internal.entity.Item;
import com.zcy.selector.internal.entity.SelectionSpec;
import com.zcy.selector.internal.loader.RxCursorLoader;
import com.zcy.selector.internal.utils.MediaStoreCompat;

import java.lang.ref.WeakReference;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AlbumMediaCollection {

    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");

    private static final String ORDER_BY = MediaStore.Images.Media.DATE_TAKEN + " DESC";

    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.SIZE,
            "duration"};

    private static String[] getSelectionArgsForGifType(int mediaType) {
        return new String[]{String.valueOf(mediaType), "image/gif"};
    }

    private static String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[]{String.valueOf(mediaType)};
    }

    private static final String[] SELECTION_ALL_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    private static String[] getSelectionAlbumArgsForGifType(int mediaType, String albumId) {
        return new String[]{String.valueOf(mediaType), albumId, "image/gif"};
    }

    private static String[] getSelectionAlbumArgsForSingleMediaType(int mediaType, String albumId) {
        return new String[]{String.valueOf(mediaType), albumId};
    }


    private static String[] getSelectionAlbumArgs(String albumId) {
        return new String[]{
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
                String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
                albumId
        };
    }

    private static final String SELECTION_ALL =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String SELECTION_ALL_FOR_GIF =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND "
                    + MediaStore.MediaColumns.MIME_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String SELECTION_ALBUM_FOR_GIF =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND "
                    + " bucket_id=?"
                    + " AND "
                    + MediaStore.MediaColumns.MIME_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND "
                    + " bucket_id=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String SELECTION_ALBUM =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND "
                    + " bucket_id=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private WeakReference<Context> mContext;
    private AlbumMediaCallbacks mCallbacks;
    private Disposable mCursorDisposable;
    private boolean mEnableCapture;


    public void onCreate(@NonNull FragmentActivity context, @NonNull AlbumMediaCallbacks callbacks) {
        mContext = new WeakReference<Context>(context);
        mCallbacks = callbacks;
    }

    public void onDestroy() {
        if (null != mCallbacks) {
            mCallbacks = null;
        }
        if (null != mCursorDisposable) {
            mCursorDisposable.dispose();
        }
    }

    public void startLoad(@Nullable Album target) {
        startLoad(target, false);
    }

    public void startLoad(@Nullable Album target, boolean enableCapture) {
        mEnableCapture = enableCapture;
        if (null != mCursorDisposable) {
            mCursorDisposable.dispose();
        }
        String selection = getSelection(target);
        String[] selectionArgs = getSelectionArgs(target);

        final RxCursorLoader.Query query = new RxCursorLoader.Query.Builder()
                .setContentUri(QUERY_URI)
                .setProjection(PROJECTION)
                .setSortOrder(ORDER_BY)
                .setSelection(selection)
                .setSelectionArgs(selectionArgs)
                .create();

        mCursorDisposable = RxCursorLoader
                .flowable(mContext.get().getContentResolver(),
                        query, Schedulers.io(), BackpressureStrategy.LATEST)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Cursor>() {
                    @Override
                    public void accept(Cursor cursor) {
                        if (null != mCallbacks) {
                            if (!mEnableCapture || !MediaStoreCompat.hasCameraFeature(mContext.get())) {
                                mCallbacks.onAlbumMediaLoad(cursor);
                                return;
                            }
                            MatrixCursor dummy = new MatrixCursor(PROJECTION);
                            dummy.addRow(new Object[]{Item.ITEM_ID_CAPTURE, Item.ITEM_DISPLAY_NAME_CAPTURE, "", 0, 0});
                            MergeCursor mergeCursor = new MergeCursor(new Cursor[]{dummy, cursor});
                            mCallbacks.onAlbumMediaLoad(mergeCursor);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) {
                        if (null != mCallbacks) {
                            mCallbacks.onLoadFailed(throwable);
                        }
                    }
                });
    }

    private String[] getSelectionArgs(@Nullable Album target) {
        String[] selectionArgs;
        if (target.isAll()) {
            if (SelectionSpec.getInstance().onlyShowGif()) {
                selectionArgs = getSelectionArgsForGifType(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            } else if (SelectionSpec.getInstance().onlyShowImages()) {
                selectionArgs = getSelectionArgsForSingleMediaType(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
            } else if (SelectionSpec.getInstance().onlyShowVideos()) {
                selectionArgs = getSelectionArgsForSingleMediaType(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
            } else {
                selectionArgs = SELECTION_ALL_ARGS;
            }
        } else {
            if (SelectionSpec.getInstance().onlyShowGif()) {
                selectionArgs = getSelectionAlbumArgsForGifType(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE, target.getId());
            } else if (SelectionSpec.getInstance().onlyShowImages()) {
                selectionArgs = getSelectionAlbumArgsForSingleMediaType(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE,
                        target.getId());
            } else if (SelectionSpec.getInstance().onlyShowVideos()) {
                selectionArgs = getSelectionAlbumArgsForSingleMediaType(
                        MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO,
                        target.getId());
            } else {
                selectionArgs = getSelectionAlbumArgs(target.getId());
            }
            mEnableCapture = false;
        }
        return selectionArgs;
    }

    private String getSelection(@Nullable Album target) {
        String selection;
        if (target.isAll()) {
            if (SelectionSpec.getInstance().onlyShowGif()) {
                selection = SELECTION_ALL_FOR_GIF;
            } else if (SelectionSpec.getInstance().onlyShowImages()) {
                selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE;
            } else if (SelectionSpec.getInstance().onlyShowVideos()) {
                selection = SELECTION_ALL_FOR_SINGLE_MEDIA_TYPE;
            } else {
                selection = SELECTION_ALL;
            }
        } else {
            if (SelectionSpec.getInstance().onlyShowGif()) {
                selection = SELECTION_ALBUM_FOR_GIF;
            } else if (SelectionSpec.getInstance().onlyShowImages()) {
                selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE;
            } else if (SelectionSpec.getInstance().onlyShowVideos()) {
                selection = SELECTION_ALBUM_FOR_SINGLE_MEDIA_TYPE;
            } else {
                selection = SELECTION_ALBUM;
            }
            mEnableCapture = false;
        }
        return selection;
    }

    public interface AlbumMediaCallbacks {

        void onAlbumMediaLoad(Cursor cursor);

        void onLoadFailed(Throwable throwable);
    }
}
