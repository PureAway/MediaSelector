package com.zcy.selector.engine.impl;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.widget.ImageView;

import com.facebook.drawee.backends.pipeline.Fresco;
import com.facebook.drawee.backends.pipeline.PipelineDraweeControllerBuilder;
import com.facebook.drawee.interfaces.DraweeController;
import com.facebook.drawee.view.SimpleDraweeView;
import com.facebook.imagepipeline.common.ImageDecodeOptions;
import com.facebook.imagepipeline.common.ImageDecodeOptionsBuilder;
import com.facebook.imagepipeline.common.Priority;
import com.facebook.imagepipeline.common.ResizeOptions;
import com.facebook.imagepipeline.request.ImageRequest;
import com.facebook.imagepipeline.request.ImageRequestBuilder;
import com.zcy.selector.engine.ImageEngine;
import com.zcy.selector.internal.ui.zoomableview.ZoomableDraweeView;

public class FrescoEngine implements ImageEngine {

    @Override
    public void loadThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        if (imageView instanceof SimpleDraweeView) {
            ResizeOptions resizeOptions = new ResizeOptions(resize, resize);
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setResizeOptions(resizeOptions)
                    .setRequestPriority(Priority.HIGH)
                    .setLocalThumbnailPreviewsEnabled(true)
                    .build();
            DraweeController controller =
                    Fresco.newDraweeControllerBuilder()
                            .setImageRequest(imageRequest)
                            .setOldController(((SimpleDraweeView) imageView).getController())
                            .build();
            ((SimpleDraweeView) imageView).setController(controller);
        }
    }

    @Override
    public void loadGifThumbnail(Context context, int resize, Drawable placeholder, ImageView imageView, Uri uri) {
        loadThumbnail(context, resize, placeholder, imageView, uri);
    }

    @Override
    public void loadImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        if (imageView instanceof ZoomableDraweeView) {
            ResizeOptions resizeOptions = new ResizeOptions(resizeX, resizeY);
            ImageRequest imageRequest = ImageRequestBuilder.newBuilderWithSource(uri)
                    .setResizeOptions(resizeOptions)
                    .setRequestPriority(Priority.HIGH)
                    .build();
            DraweeController controller =
                    Fresco.newDraweeControllerBuilder()
                            .setImageRequest(imageRequest)
                            .build();
            ((ZoomableDraweeView) imageView).setAllowTouchInterceptionWhileZoomed(true);
            ((ZoomableDraweeView) imageView).setIsLongpressEnabled(false);
            ((ZoomableDraweeView) imageView).setController(controller);
        }
    }

    @Override
    public void loadGifImage(Context context, int resizeX, int resizeY, ImageView imageView, Uri uri) {
        if (imageView instanceof ZoomableDraweeView) {
            PipelineDraweeControllerBuilder controllerBuilder =
                    Fresco.newDraweeControllerBuilder()
                            .setAutoPlayAnimations(true)
                            .setOldController(((ZoomableDraweeView) imageView).getController());
            final ImageDecodeOptionsBuilder optionsBuilder =
                    ImageDecodeOptions.newBuilder().setMaxDimensionPx(4000);
            controllerBuilder.setImageRequest(
                    ImageRequestBuilder.newBuilderWithSource(uri)
                            .setRequestPriority(Priority.HIGH)
                            .setImageDecodeOptions(optionsBuilder.build())
                            .build());
            ((ZoomableDraweeView) imageView).setController(controllerBuilder.build());
        }
    }

    @Override
    public boolean supportAnimatedGif() {
        return true;
    }

}
