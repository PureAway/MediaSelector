package com.zcy.selector.internal.ui;

import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.zcy.selector.R;
import com.zcy.selector.engine.impl.FrescoEngine;
import com.zcy.selector.internal.entity.Item;
import com.zcy.selector.internal.entity.SelectionSpec;
import com.zcy.selector.listener.OnFragmentInteractionListener;


public class PreviewItemFragment extends Fragment {

    private static final String ARGS_ITEM = "args_item";
    private OnFragmentInteractionListener mListener;

    public static PreviewItemFragment newInstance(Item item) {
        PreviewItemFragment fragment = new PreviewItemFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(ARGS_ITEM, item);
        fragment.setArguments(bundle);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (SelectionSpec.getInstance().getImageEngine() instanceof FrescoEngine) {
            return inflater.inflate(R.layout.fragment_preview_item_fresco, container, false);
        }
        return inflater.inflate(R.layout.fragment_preview_item, container, false);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        final Item item = getArguments().getParcelable(ARGS_ITEM);
        if (item == null) {
            return;
        }

        View videoPlayButton = view.findViewById(R.id.video_play_button);
        ImageView image = view.findViewById(R.id.image_view);
        if (item.isVideo()) {
            videoPlayButton.setVisibility(View.VISIBLE);
            videoPlayButton.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(item.getContentUri(), "video/*");
                try {
                    startActivity(intent);
                } catch (ActivityNotFoundException e) {
                    Toast.makeText(getContext(), R.string.error_no_video_activity, Toast.LENGTH_SHORT).show();
                }
            });
        } else {
            videoPlayButton.setVisibility(View.GONE);
        }

        image.setOnClickListener(v -> {
            if (mListener != null) {
                mListener.onClick();
            }
        });

        Point size = new Point(1080, 2163);
        if (item.isGif()) {
            SelectionSpec.getInstance().getImageEngine().loadGifImage(getContext(), size.x, size.y, image,
                    item.getContentUri());
        } else {
            SelectionSpec.getInstance().getImageEngine().loadImage(getContext(), size.x, size.y, image,
                    item.getContentUri());
        }
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }
}
