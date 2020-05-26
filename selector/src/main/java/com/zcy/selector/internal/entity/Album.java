package com.zcy.selector.internal.entity;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Parcel;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.Nullable;

import com.zcy.selector.R;
import com.zcy.selector.internal.model.AlbumCollection;

import java.util.Arrays;


public class Album implements Parcelable {

    private static final String COLUMN_URI = "uri";
    private static final String COLUMN_COUNT = "count";

    public static final Creator<Album> CREATOR = new Creator<Album>() {
        @Nullable
        @Override
        public Album createFromParcel(Parcel source) {
            return new Album(source);
        }

        @Override
        public Album[] newArray(int size) {
            return new Album[size];
        }
    };
    public static final String ALBUM_ID_ALL = String.valueOf(-1);
    public static final String ALBUM_NAME_ALL = "All";

    private final String mId;
    private final Uri mCoverUri;
    private final String mDisplayName;
    private long mCount;

    public Album(String id, Uri coverUri, String albumName, long count) {
        mId = id;
        mCoverUri = coverUri;
        mDisplayName = albumName;
        mCount = count;
    }

    private Album(Parcel source) {
        mId = source.readString();
        mCoverUri = source.readParcelable(Uri.class.getClassLoader());
        mDisplayName = source.readString();
        mCount = source.readLong();
    }

    /**
     * Constructs a new {@link Album} entity from the {@link Cursor}.
     * This method is not responsible for managing cursor resource, such as close, iterate, and so on.
     */
    public static Album valueOf(Cursor cursor) {
        String column = cursor.getString(cursor.getColumnIndex(AlbumCollection.COLUMN_URI));
        return new Album(
                cursor.getString(cursor.getColumnIndex("bucket_id")),
                Uri.parse(column != null ? column : ""),
                cursor.getString(cursor.getColumnIndex("bucket_display_name")),
                cursor.getLong(cursor.getColumnIndex(AlbumCollection.COLUMN_COUNT)));
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(mId);
        dest.writeParcelable(mCoverUri, 0);
        dest.writeString(mDisplayName);
        dest.writeLong(mCount);
    }

    public String getId() {
        return mId;
    }

    public Uri getCoverUri() {
        return mCoverUri;
    }

    public long getCount() {
        return mCount;
    }

    public void addCaptureCount() {
        mCount++;
    }

    public String getDisplayName(Context context) {
        if (isAll()) {
            return context.getString(R.string.album_name_all);
        }
        return mDisplayName;
    }

    public boolean isAll() {
        return ALBUM_ID_ALL.equals(mId);
    }

    public boolean isEmpty() {
        return mCount == 0;
    }
}