package com.catchat.app.ui.progress;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.DashPathEffect;
import android.graphics.Paint;
import android.graphics.PathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.animation.LinearInterpolator;

import com.catchat.app.R;

import java.util.ArrayList;
import java.util.List;

public class SvgPathView extends View {
    private final Paint mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private final SvgHelper mSvg = new SvgHelper(mPaint);
    private final Object mSvgLock = new Object();

    private List<SvgHelper.SvgPath> mPaths = new ArrayList<SvgHelper.SvgPath>(0);
    private Thread mLoader;

    private float mPhase;

    private int mDuration;
    private int mSvgResource;

    private ObjectAnimator mSvgAnimator;

    public SvgPathView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public SvgPathView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);

        mPaint.setStyle(Paint.Style.STROKE);

        TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.SvgPathView, defStyle, 0);
        try {
            if (a != null) {
                mPaint.setStrokeWidth(a.getFloat(R.styleable.SvgPathView_strokeWidth, 1.0f));
                mPaint.setColor(a.getColor(R.styleable.SvgPathView_strokeColor, 0xff000000));
                mPhase = a.getFloat(R.styleable.SvgPathView_phase, 1.0f);
                mDuration = a.getInt(R.styleable.SvgPathView_duration, 5000);
            }
        } finally {
            if (a != null) a.recycle();
        }
    }

    private void updatePathsPhaseLocked() {
        final int count = mPaths.size();
        for (int i = 0; i < count; i++) {
            SvgHelper.SvgPath svgPath = mPaths.get(i);
            svgPath.paint.setPathEffect(createPathEffect(svgPath.length, mPhase, 0.0f));
        }
    }

    // used in property animation
    public float getPhase() {
        return mPhase;
    }

    public void setPhase(float phase) {
        mPhase = phase;
        synchronized (mSvgLock) {
            updatePathsPhaseLocked();
        }
        invalidate();
    }

    public void setSvgResource(int svgResource) {
        mSvgResource = svgResource;
    }

    @Override
    protected void onSizeChanged(final int w, final int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);

        if (mLoader != null) {
            try {
                mLoader.join();
            } catch (InterruptedException e) {
                Log.e("CatChatTag", "Unexpected error", e);
            }
        }

        mLoader = new Thread(new Runnable() {
            @Override
            public void run() {
                mSvg.load(getContext(), mSvgResource);
                synchronized (mSvgLock) {
                    mPaths = mSvg.getPathsForViewport(
                            w - getPaddingLeft() - getPaddingRight(),
                            h - getPaddingTop() - getPaddingBottom());
                    updatePathsPhaseLocked();
                }
            }
        }, "SVG Loader");
        mLoader.start();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        synchronized (mSvgLock) {
            canvas.save();
            canvas.translate(getPaddingLeft(), getPaddingTop());
            final int count = mPaths.size();
            for (int i = 0; i < count; i++) {
                SvgHelper.SvgPath svgPath = mPaths.get(i);
                canvas.drawPath(svgPath.path, svgPath.paint);
            }
            canvas.restore();
        }
    }

    private static PathEffect createPathEffect(float pathLength, float phase, float offset) {
        return new DashPathEffect(new float[]{pathLength, pathLength}, Math.max(phase * pathLength, offset));
    }

    public void startAnimation() {
        if (mSvgAnimator == null) {
            mSvgAnimator = ObjectAnimator.ofFloat(this, "phase", mPhase, 0.0f);
            mSvgAnimator.setRepeatCount(ValueAnimator.INFINITE);
            mSvgAnimator.setDuration(mDuration);
            mSvgAnimator.setInterpolator(new LinearInterpolator());
            mSvgAnimator.start();
        }
    }

    public void stopAnimation() {
        mSvgAnimator.cancel();
    }
}