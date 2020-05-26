package com.zcy.mediaselector;

import android.Manifest;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import com.zcy.selector.MimeType;
import com.zcy.selector.Selector;
import com.zcy.selector.engine.impl.FrescoEngine;
import com.zcy.selector.engine.impl.GlideEngine;
import com.zcy.selector.engine.impl.PicassoEngine;
import com.zcy.selector.filter.Filter;
import com.zcy.selector.internal.entity.CaptureStrategy;
import com.zcy.selector.listener.OnCheckedListener;
import com.zcy.selector.listener.OnSelectedListener;

import java.util.List;

public class SampleActivity extends AppCompatActivity implements View.OnClickListener {

    private static final int REQUEST_CODE_CHOOSE = 23;

    private UriAdapter mAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        findViewById(R.id.gray).setOnClickListener(this);
        findViewById(R.id.dracula).setOnClickListener(this);
        findViewById(R.id.only_gif).setOnClickListener(this);

        RecyclerView recyclerView = findViewById(R.id.recyclerview);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(mAdapter = new UriAdapter());
    }


    private void startAction(View v) {
        switch (v.getId()) {
            case R.id.gray:
                Selector.from(SampleActivity.this)
                        .choose(MimeType.ofImage(), false)
                        .countable(true)
                        .capture(true)
                        .captureStrategy(
                                new CaptureStrategy(true, "com.zcy.mediaselector.fileprovider", "test"))
                        .maxSelectable(9)
                        .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .gridExpectedSize(
                                getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.65f)
                        .imageEngine(new GlideEngine())
                        .setOnSelectedListener(new OnSelectedListener() {
                            @Override
                            public void onSelected(@NonNull List<Uri> uriList, @NonNull List<String> pathList) {
                                Log.e("onSelected", "onSelected: pathList=" + pathList);
                            }
                        })
                        .showSingleMediaType(true)
                        .originalEnable(false)
                        .maxOriginalSize(10)
                        .autoHideToolbarOnSingleTap(true)
                        .setOnCheckedListener(new OnCheckedListener() {
                            @Override
                            public void onCheck(boolean isChecked) {
                                Log.e("isChecked", "onCheck: isChecked=" + isChecked);
                            }
                        })
                        .forResult(REQUEST_CODE_CHOOSE);
                break;
            case R.id.dracula:
                Selector.from(SampleActivity.this)
                        .choose(MimeType.ofImage())
                        .theme(R.style.Matisse_Dracula)
                        .countable(false)
                        .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .maxSelectable(9)
                        .originalEnable(true)
                        .thumbnailScale(0.75f)
                        .gridExpectedSize(
                                getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .maxOriginalSize(10)
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .imageEngine(new FrescoEngine())
                        .forResult(REQUEST_CODE_CHOOSE);
                break;
            case R.id.only_gif:
                Selector.from(SampleActivity.this)
                        .choose(MimeType.of(MimeType.Type.GIF), false)
                        .countable(true)
                        .maxSelectable(9)
                        .addFilter(new GifSizeFilter(320, 320, 5 * Filter.K * Filter.K))
                        .gridExpectedSize(
                                getResources().getDimensionPixelSize(R.dimen.grid_expected_size))
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT)
                        .thumbnailScale(0.85f)
                        .imageEngine(new PicassoEngine())
                        .showSingleMediaType(true)
                        .originalEnable(true)
                        .maxOriginalSize(10)
                        .autoHideToolbarOnSingleTap(true)
                        .forResult(REQUEST_CODE_CHOOSE);
                break;
            default:
                break;
        }
        mAdapter.setData(null, null);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_CHOOSE && resultCode == RESULT_OK) {
            mAdapter.setData(Selector.obtainResult(data), Selector.obtainPathResult(data));
            Log.e("OnActivityResult ", String.valueOf(Selector.obtainOriginalState(data)));
        }
    }

    @Override
    public void onClick(final View v) {
        RxPermissions rxPermissions = new RxPermissions(this);
        rxPermissions.request(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .subscribe(aBoolean -> {
                    if (aBoolean) {
                        startAction(v);
                    } else {
                        Toast.makeText(SampleActivity.this, R.string.permission_request_denied, Toast.LENGTH_LONG)
                                .show();
                    }
                }, Throwable::printStackTrace);
    }

    private static class UriAdapter extends RecyclerView.Adapter<UriAdapter.UriViewHolder> {

        private List<Uri> mUris;
        private List<String> mPaths;

        void setData(List<Uri> uris, List<String> paths) {
            mUris = uris;
            mPaths = paths;
            notifyDataSetChanged();
        }

        @Override
        public UriViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            return new UriViewHolder(
                    LayoutInflater.from(parent.getContext()).inflate(R.layout.uri_item, parent, false));
        }

        @Override
        public void onBindViewHolder(UriViewHolder holder, int position) {
            holder.mUri.setText(mUris.get(position).toString());
            holder.mPath.setText(mPaths.get(position));

            holder.mUri.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
            holder.mPath.setAlpha(position % 2 == 0 ? 1.0f : 0.54f);
        }

        @Override
        public int getItemCount() {
            return mUris == null ? 0 : mUris.size();
        }

        static class UriViewHolder extends RecyclerView.ViewHolder {

            private TextView mUri;
            private TextView mPath;

            UriViewHolder(View contentView) {
                super(contentView);
                mUri = contentView.findViewById(R.id.uri);
                mPath = contentView.findViewById(R.id.path);
            }
        }
    }

}
