package com.zcy.selector.internal.ui.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatImageView;

import com.facebook.drawee.view.SimpleDraweeView;

public class RoundedRectangleSimpleImage extends SimpleDraweeView {

    private float mRadius; // dp
    private Path mRoundedRectPath;
    private RectF mRectF;

    public RoundedRectangleSimpleImage(Context context) {
        super(context);
        init(context);
    }

    public RoundedRectangleSimpleImage(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context);
    }

    public RoundedRectangleSimpleImage(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context);
    }

    private void init(Context context) {
        float density = context.getResources().getDisplayMetrics().density;
        mRadius = 2.0f * density;
        mRoundedRectPath = new Path();
        mRectF = new RectF();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        mRectF.set(0.0f, 0.0f, getMeasuredWidth(), getMeasuredHeight());
        mRoundedRectPath.addRoundRect(mRectF, mRadius, mRadius, Path.Direction.CW);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.clipPath(mRoundedRectPath);
        super.onDraw(canvas);
    }
}