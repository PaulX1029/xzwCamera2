package com.android.xzwcamera.UI;

import android.content.Context;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-19 下午4:38
 */
public class ShutterButton extends ImageView {
    private class LongClickListener implements View.OnLongClickListener {
        public boolean onLongClick(View v) {
            if ( null != mListener ) {
                mListener.onShutterButtonLongClick();
                return true;
            }
            return false;
        }
    }

    private boolean mTouchEnabled = true;
    private LongClickListener mLongClick = new LongClickListener();

    /**
     * A callback to be invoked when a ShutterButton's pressed state changes.
     */
    public interface OnShutterButtonListener {
        /**
         * Called when a ShutterButton has been pressed.
         *
         * @param pressed The ShutterButton that was pressed.
         */
        void onShutterButtonFocus(boolean pressed);
        void onShutterButtonClick();
        void onShutterButtonLongClick();
    }

    private OnShutterButtonListener mListener;
    private boolean mOldPressed;
    private boolean isEnableClick = true;

    public ShutterButton(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public void setOnShutterButtonListener(OnShutterButtonListener listener) {
        mListener = listener;
        setOnLongClickListener(mLongClick);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent m) {
        if (mTouchEnabled) {
            return super.dispatchTouchEvent(m);
        } else {
            return false;
        }
    }

    public void enableTouch(boolean enable) {
        mTouchEnabled = enable;
        setLongClickable(enable);
    }

    /**
     * Hook into the drawable state changing to get changes to isPressed -- the
     * onPressed listener doesn't always get called when the pressed state
     * changes.
     */
    @Override
    protected void drawableStateChanged() {
        super.drawableStateChanged();
        final boolean pressed = isPressed();
        if (pressed != mOldPressed) {
            if (!pressed) {
                // When pressing the physical camera button the sequence of
                // events is:
                //    focus pressed, optional camera pressed, focus released.
                // We want to emulate this sequence of events with the shutter
                // button. When clicking using a trackball button, the view
                // system changes the drawable state before posting click
                // notification, so the sequence of events is:
                //    pressed(true), optional click, pressed(false)
                // When clicking using touch events, the view system changes the
                // drawable state after posting click notification, so the
                // sequence of events is:
                //    pressed(true), pressed(false), optional click
                // Since we're emulating the physical camera button, we want to
                // have the same order of events. So we want the optional click
                // callback to be delivered before the pressed(false) callback.
                //
                // To do this, we delay the posting of the pressed(false) event
                // slightly by pushing it on the event queue. This moves it
                // after the optional click notification, so our client always
                // sees events in this sequence:
                //     pressed(true), optional click, pressed(false)
                post(new Runnable() {
                    @Override
                    public void run() {
                        callShutterButtonFocus(pressed);
                    }
                });
            } else {
                callShutterButtonFocus(pressed);
            }
            mOldPressed = pressed;
        }
    }

    private void callShutterButtonFocus(boolean pressed) {
        if (mListener != null) {
            mListener.onShutterButtonFocus(pressed);
        }
    }

    @Override
    public boolean performClick() {
        boolean result = super.performClick();
        /* Liverpool code for VEYRON-2122 by yangzekai at 2021/02/24 start */
        if (mListener != null && getVisibility() == View.VISIBLE && isEnableClick) {
            isEnableClick = false;
            mListener.onShutterButtonClick();
        }
        /* Liverpool code for VEYRON-2122 by yangzekai at 2021/02/24 end */
        return result;
    }
}
