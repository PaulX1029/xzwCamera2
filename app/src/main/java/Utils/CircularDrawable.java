package Utils;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.ColorFilter;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.PixelFormat;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Shader;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.android.xzwcamera.R;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-22 下午1:57
 */
public class CircularDrawable extends Drawable {

    private final BitmapShader mBitmapShader;
    private final Paint mPaint;
    private Rect mRect;
    private int mLength;

    public CircularDrawable(Bitmap bitmap , Activity mActivity) {
        int w = bitmap.getWidth();
        int h = bitmap.getHeight();
        int targetSize = mActivity.getResources().getDimensionPixelSize(R.dimen.capture_size);
        if (Math.min(w, h) < targetSize) {
            Matrix matrix = new Matrix();
            float scale = 1.0f;
            if (w > h) {
                scale = (float) targetSize / (float) h;
            } else {
                scale = (float) targetSize / (float) w;
            }
            matrix.postScale(scale, scale);
            bitmap = Bitmap.createBitmap(bitmap, 0, 0, w, h, matrix, true);
            w = (int) (w * scale);
            h = (int) (h * scale);
        }
        if (w > h) {
            mLength = h;
            bitmap = Bitmap.createBitmap(bitmap, (w - h) / 2, 0, h, h);
        } else if (w < h) {
            mLength = w;
            bitmap = Bitmap.createBitmap(bitmap, 0, (h - w) / 2, w, w);
        } else {
            mLength = w;
        }

        mBitmapShader = new BitmapShader(bitmap, Shader.TileMode.CLAMP, Shader.TileMode.CLAMP);
        mPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mPaint.setShader(mBitmapShader);
    }

    @Override
    protected void onBoundsChange(Rect bounds) {
        super.onBoundsChange(bounds);
        mRect = bounds;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawRoundRect(new RectF(mRect), (mRect.right - mRect.left) / 2,
                (mRect.bottom - mRect.top) / 2, mPaint);
    }

    @Override
    public int getOpacity() {
        return PixelFormat.TRANSLUCENT;
    }

    @Override
    public void setAlpha(int alpha) {
        mPaint.setAlpha(alpha);
    }

    @Override
    public void setColorFilter(ColorFilter filter) {
        mPaint.setColorFilter(filter);
    }

    @Override
    public int getIntrinsicWidth() {
        return mLength;
    }

    @Override
    public int getIntrinsicHeight() {
        return mLength;
    }
}
