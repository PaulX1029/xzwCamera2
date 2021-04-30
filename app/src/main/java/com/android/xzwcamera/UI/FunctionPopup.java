package com.android.xzwcamera.UI;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.PopupWindow;

import com.android.xzwcamera.R;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-15 下午6:07
 */
public class FunctionPopup extends PopupWindow {

    Context mContext;
    private LayoutInflater mInflater;
    private View mContentView;
    private  Listener mListener;
    private ImageView mImageView;

    public FunctionPopup(Context context){
        super(context);

        this.mContext=context;
        mContext = context;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        this.setOnDismissListener(new OnDismissListener() {
            @Override
            public void onDismiss() {
                mListener.onPopupWindowDismissListener();
            }
        });


        mContentView = mInflater.inflate(R.layout.function_in_main,null);

        setContentView(mContentView);

        this.setFocusable(true);


    }

    public interface Listener{
        void onPopupWindowDismissListener();  //弹框消失监听
        void onItemClickListener(View v);  //条目点击监听
    }

    public void setFUnctionsListener(Listener listener) {
        mListener = listener;
    }



}
