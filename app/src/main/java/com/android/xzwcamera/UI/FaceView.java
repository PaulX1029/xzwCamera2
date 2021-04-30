package com.android.xzwcamera.UI;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.RectF;
import android.hardware.camera2.params.Face;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.util.ArrayList;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-30 上午11:46
 */
public class FaceView extends View {

    private Paint mPaint;
    private String mColor = "#42ed45";
    private ArrayList<RectF> mFaces;
    public FaceView(Context context) {
        super(context);
        init();
    }

    public FaceView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public FaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        mPaint = new Paint();
        mPaint.setColor(Color.parseColor(mColor));
        mPaint.setStyle(Paint.Style.STROKE);
        mPaint.setAntiAlias(true);
    }

    public FaceView(Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mFaces != null){
            for (int i = 0; i < mFaces.size(); i++) {
                canvas.drawRect(mFaces.get(i),mPaint);
            }
        }
    }

    private void setFaces(ArrayList<RectF> faces){
        this.mFaces = faces;
        invalidate();
    }
}
