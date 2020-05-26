package com.zcy.selector.internal.model;

import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.database.MergeCursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;

import androidx.fragment.app.FragmentActivity;

import com.zcy.selector.MimeType;
import com.zcy.selector.internal.entity.Album;
import com.zcy.selector.internal.entity.SelectionSpec;
import com.zcy.selector.internal.loader.RxCursorLoader;

import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers;
import io.reactivex.rxjava3.core.BackpressureStrategy;
import io.reactivex.rxjava3.disposables.Disposable;
import io.reactivex.rxjava3.functions.Consumer;
import io.reactivex.rxjava3.schedulers.Schedulers;

public class AlbumCollection {

    private static final String COLUMN_BUCKET_ID = "bucket_id";
    private static final String COLUMN_BUCKET_DISPLAY_NAME = "bucket_display_name";
    public static final String COLUMN_URI = "uri";
    public static final String COLUMN_COUNT = "count";
    private static final Uri QUERY_URI = MediaStore.Files.getContentUri("external");
    private static final String STATE_CURRENT_SELECTION = "state_current_selection";
    private Cursor cursor = null;

    private static final String[] COLUMNS = {
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            COLUMN_URI,
            COLUMN_COUNT};

    private static final String[] PROJECTION = {
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE,
            "COUNT(*) AS " + COLUMN_COUNT};

    private static final String[] PROJECTION_29 = {
            MediaStore.Files.FileColumns._ID,
            COLUMN_BUCKET_ID,
            COLUMN_BUCKET_DISPLAY_NAME,
            MediaStore.MediaColumns.MIME_TYPE};

    private static final String SELECTION =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                    + ") GROUP BY (bucket_id";
    private static final String SELECTION_29 =
            "(" + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " OR "
                    + MediaStore.Files.FileColumns.MEDIA_TYPE + "=?)"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";
    private static final String[] SELECTION_ARGS = {
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE),
            String.valueOf(MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO),
    };

    private static final String SELECTION_FOR_SINGLE_MEDIA_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                    + ") GROUP BY (bucket_id";
    private static final String SELECTION_FOR_SINGLE_MEDIA_TYPE_29 =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0";

    private static final String SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                    + " AND " + MediaStore.MediaColumns.MIME_TYPE + "=?"
                    + ") GROUP BY (bucket_id";
    private static final String SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29 =
            MediaStore.Files.FileColumns.MEDIA_TYPE + "=?"
                    + " AND " + MediaStore.MediaColumns.SIZE + ">0"
                    + " AND " + MediaStore.MediaColumns.MIME_TYPE + "=?";

    private static final String BUCKET_ORDER_BY = "datetaken DESC";

    private WeakReference<Context> mContext;
    private AlbumCallbacks mCallbacks;
    private int mCurrentSelection;
    private boolean mLoadFinished;
    private Disposable mCursorDisposable;

    public void onCreate(FragmentActivity activity, final AlbumCallbacks callbacks) {
        mContext = new WeakReference<Context>(activity);
        mCallbacks = callbacks;
    }

    public void startLoad() {
        if (null != mCursorDisposable) {
            mCursorDisposable.dispose();
        }
        String selection = getSelection();
        String[] selectionArgs = getSelectionArgs();

        final RxCursorLoader.Query query = new RxCursorLoader.Query.Builder()
                .setContentUri(QUERY_URI)
                .setProjection(beforeAndroidTen() ? PROJECTION : PROJECTION_29)
                .setSortOrder(BUCKET_ORDER_BY)
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
                        if (!mLoadFinished) {
                            mLoadFinished = true;
                        }
                        if (null != mCallbacks) {
                            Cursor result = getReturnCursor(cursor);
                            if (null != AlbumCollection.this.cursor) {
                                AlbumCollection.this.cursor.close();
                                AlbumCollection.this.cursor = result;
                            }
                            mCallbacks.onAlbumLoad(result);
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

    private Cursor getReturnCursor(Cursor albums) {
        MatrixCursor allAlbum = new MatrixCursor(COLUMNS);
        if (beforeAndroidTen()) {
            return getBeforeAndroidTenCursor(albums, allAlbum);
        } else {
            return getAfterAndroidTenCursor(albums, allAlbum);
        }
    }

    private Cursor getAfterAndroidTenCursor(Cursor albums, MatrixCursor allAlbum) {
        int totalCount = 0;
        Uri allAlbumCoverUri = null;
        Map<Long, Long> countMap = new HashMap<>();
        if (albums != null) {
            while (albums.moveToNext()) {
                long bucketId = albums.getLong(albums.getColumnIndex(COLUMN_BUCKET_ID));
                Long count = countMap.get(bucketId);
                if (count == null) {
                    count = 1L;
                } else {
                    count++;
                }
                countMap.put(bucketId, count);
            }
        }
        MatrixCursor otherAlbums = new MatrixCursor(COLUMNS);
        if (albums != null) {
            if (albums.moveToFirst()) {
                allAlbumCoverUri = getUri(albums);
                Set<Long> done = new HashSet<>();
                do {
                    long bucketId = albums.getLong(albums.getColumnIndex(COLUMN_BUCKET_ID));
                    if (done.contains(bucketId)) {
                        continue;
                    }
                    long fileId = albums.getLong(
                            albums.getColumnIndex(MediaStore.Files.FileColumns._ID));
                    String bucketDisplayName = albums.getString(
                            albums.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME));
                    String mimeType = albums.getString(
                            albums.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
                    Uri uri = getUri(albums);
                    long count = countMap.get(bucketId);
                    otherAlbums.addRow(new String[]{
                            Long.toString(fileId),
                            Long.toString(bucketId),
                            bucketDisplayName,
                            mimeType,
                            uri.toString(),
                            String.valueOf(count)});
                    done.add(bucketId);
                    totalCount += count;
                } while (albums.moveToNext());
            }
        }
        allAlbum.addRow(new String[]{
                Album.ALBUM_ID_ALL,
                Album.ALBUM_ID_ALL, Album.ALBUM_NAME_ALL, null,
                allAlbumCoverUri == null ? null : allAlbumCoverUri.toString(),
                String.valueOf(totalCount)});
        return new MergeCursor(new Cursor[]{allAlbum, otherAlbums});
    }

    private Cursor getBeforeAndroidTenCursor(Cursor albums, MatrixCursor allAlbum) {
        int totalCount = 0;
        Uri allAlbumCoverUri = null;
        MatrixCursor otherAlbums = new MatrixCursor(COLUMNS);
        if (albums != null) {
            while (albums.moveToNext()) {
                long fileId = albums.getLong(
                        albums.getColumnIndex(MediaStore.Files.FileColumns._ID));
                long bucketId = albums.getLong(
                        albums.getColumnIndex(COLUMN_BUCKET_ID));
                String bucketDisplayName = albums.getString(
                        albums.getColumnIndex(COLUMN_BUCKET_DISPLAY_NAME));
                String mimeType = albums.getString(
                        albums.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
                Uri uri = getUri(albums);
                int count = albums.getInt(albums.getColumnIndex(COLUMN_COUNT));

                otherAlbums.addRow(new String[]{
                        Long.toString(fileId),
                        Long.toString(bucketId), bucketDisplayName, mimeType, uri.toString(),
                        String.valueOf(count)});
                totalCount += count;
            }
            if (albums.moveToFirst()) {
                allAlbumCoverUri = getUri(albums);
            }
        }

        allAlbum.addRow(new String[]{
                Album.ALBUM_ID_ALL, Album.ALBUM_ID_ALL, Album.ALBUM_NAME_ALL, null,
                allAlbumCoverUri == null ? null : allAlbumCoverUri.toString(),
                String.valueOf(totalCount)});

        return new MergeCursor(new Cursor[]{allAlbum, otherAlbums});
    }

    private Uri getUri(Cursor cursor) {
        long id = cursor.getLong(cursor.getColumnIndex(MediaStore.Files.FileColumns._ID));
        String mimeType = cursor.getString(
                cursor.getColumnIndex(MediaStore.MediaColumns.MIME_TYPE));
        Uri contentUri;

        if (MimeType.isImage(mimeType)) {
            contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        } else if (MimeType.isVideo(mimeType)) {
            contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        } else {
            contentUri = MediaStore.Files.getContentUri("external");
        }
        Uri uri = ContentUris.withAppendedId(contentUri, id);
        return uri;
    }

    private String[] getSelectionArgs() {
        String[] selectionArgs;
        if (SelectionSpec.getInstance().onlyShowGif()) {
            selectionArgs = getSelectionArgsForSingleMediaGifType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
        } else if (SelectionSpec.getInstance().onlyShowImages()) {
            selectionArgs = getSelectionArgsForSingleMediaType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_IMAGE);
        } else if (SelectionSpec.getInstance().onlyShowVideos()) {
            selectionArgs = getSelectionArgsForSingleMediaType(
                    MediaStore.Files.FileColumns.MEDIA_TYPE_VIDEO);
        } else {
            selectionArgs = SELECTION_ARGS;
        }
        return selectionArgs;
    }

    private String[] getSelectionArgsForSingleMediaType(int mediaType) {
        return new String[]{String.valueOf(mediaType)};
    }

    private String[] getSelectionArgsForSingleMediaGifType(int mediaType) {
        return new String[]{String.valueOf(mediaType), "image/gif"};
    }

    private String getSelection() {
        String selection;
        if (SelectionSpec.getInstance().onlyShowGif()) {
            selection = beforeAndroidTen()
                    ? SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE : SELECTION_FOR_SINGLE_MEDIA_GIF_TYPE_29;
        } else if (SelectionSpec.getInstance().onlyShowImages()) {
            selection = beforeAndroidTen()
                    ? SELECTION_FOR_SINGLE_MEDIA_TYPE : SELECTION_FOR_SINGLE_MEDIA_TYPE_29;
        } else if (SelectionSpec.getInstance().onlyShowVideos()) {
            selection = beforeAndroidTen()
                    ? SELECTION_FOR_SINGLE_MEDIA_TYPE : SELECTION_FOR_SINGLE_MEDIA_TYPE_29;
        } else {
            selection = beforeAndroidTen() ? SELECTION : SELECTION_29;
        }
        return selection;
    }

    public void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState == null) {
            return;
        }
        mCurrentSelection = savedInstanceState.getInt(STATE_CURRENT_SELECTION);
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_CURRENT_SELECTION, mCurrentSelection);
    }

    public void onDestroy() {
        if (null != mCallbacks) {
            mCallbacks = null;
        }
        if (null != mCursorDisposable) {
            mCursorDisposable.dispose();
        }
        if (null != AlbumCollection.this.cursor) {
            AlbumCollection.this.cursor.close();
            AlbumCollection.this.cursor = null;
        }
    }

    public int getCurrentSelection() {
        return mCurrentSelection;
    }

    public void setStateCurrentSelection(int currentSelection) {
        mCurrentSelection = currentSelection;
    }

    private boolean beforeAndroidTen() {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.Q;
    }

    public interface AlbumCallbacks {

        void onAlbumLoad(Cursor cursor);

        void onLoadFailed(Throwable throwable);

    }
}
