package com.zcy.selector.internal.loader;

import android.content.ContentResolver;
import android.database.Cursor;
import android.util.Log;

import androidx.annotation.NonNull;

import io.reactivex.rxjava3.core.Single;
import io.reactivex.rxjava3.core.SingleEmitter;
import io.reactivex.rxjava3.core.SingleOnSubscribe;

import static com.zcy.selector.internal.loader.RxCursorLoader.TAG;
import static com.zcy.selector.internal.loader.RxCursorLoader.isDebugLoggingEnabled;


final class RxCursorLoaderSingleFactory {

    @NonNull
    static Single<Cursor> single(
            @NonNull final ContentResolver resolver,
            @NonNull final RxCursorLoader.Query query) {
        //noinspection ConstantConditions
        if (resolver == null) {
            throw new NullPointerException("ContentResolver param must not be null");
        }
        //noinspection ConstantConditions
        if (query == null) {
            throw new NullPointerException("Params param must not be null");
        }

        return Single.create(new CursorLoaderOnSubscribeSingle(resolver, query));
    }

    private static final class CursorLoaderOnSubscribeSingle
            implements SingleOnSubscribe<Cursor> {

        @NonNull
        private final ContentResolver mContentResolver;

        @NonNull
        private final RxCursorLoader.Query mQuery;

        CursorLoaderOnSubscribeSingle(
                @NonNull final ContentResolver resolver,
                @NonNull final RxCursorLoader.Query query) {
            mContentResolver = resolver;
            mQuery = query;
        }

        @Override
        public void subscribe(final SingleEmitter<Cursor> emitter) {
            if (isDebugLoggingEnabled()) {
                Log.d(TAG, mQuery.toString());
            }

            final Cursor c = mContentResolver.query(
                    mQuery.contentUri,
                    mQuery.projection,
                    mQuery.selection,
                    mQuery.selectionArgs,
                    mQuery.sortOrder);

            if (c != null) {
                emitter.onSuccess(c);
            } else {
                emitter.onError(new QueryReturnedNullException());
            }
        }
    }
}
