package com.android.xzwcamera;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.SurfaceTexture;
import android.media.ThumbnailUtils;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Chronometer;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.android.xzwcamera.UI.AutoFitTextureView;
import com.android.xzwcamera.UI.Camera2TextureView;
import com.android.xzwcamera.UI.FunctionPopup;

import Utils.CircularDrawable;
import Utils.ImageUtils;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-19 上午10:51
 */
public class VideoFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "VideoFragment";

    protected boolean isCreated = false;

    private Context mContext;

    private View mRootView;
    private Fragment mParentView;

    private Camera2TextureView mTextrureView;

    private FragmentActivity mActivity;
    private ImageView mCloseButton;
    private ImageView mFlashButton;
    private ImageView mCountButton;
    private ImageView mFilterButton;
    private ImageView mThumbnailImage;
    private ImageView mSwitchCameraDevice;

    private LinearLayout mFlashPopView;
    private LinearLayout mCountDownPopView;
    private LinearLayout mFilterPopView;

    ImageView mImageViewDisforFlash;
    TextView mTextViewOffforFlash;
    TextView mTextViewAutoforFlash;
    TextView mTextViewOnforFlash;
    TextView mTextViewKeepforFlash;

    ImageView mImageViewDisforCountDown;
    TextView mTextViewOffforCountDown;
    TextView mTextView3sforCountDown;
    TextView mTextView7sforCountDown;
    TextView mTextView10sforCountDown;

    ImageView mDissmissFilter;
    ImageView mMonochromeFilter;
    ImageView mSepiaFilter;
    ImageView mNegativeFilter;
    ImageView mSolarizeFilter;
    ImageView mPosterizeFilter;
    ImageView mAquaFilter;
    TextView mDissmissFilterText;
    TextView mMonochromeFilterText;
    TextView mSepiaFilterText;
    TextView mNegativeFilterText;
    TextView mSolarizeFilterText;
    TextView mPosterizeFilterText;
    TextView mAquaFilterText;

    private FunctionPopup.Listener mFunctionPopupListener;
    private FunctionPopup mFunctionPopup;
    private FunctionPopup mCountDownPopup;
    private FunctionPopup.Listener mCountDownPopupListener;
    private FunctionPopup mFilterPopup;
    private FunctionPopup.Listener mFilterPopupListener;

    private static int flag1 = 0;
    private static int flag2 = 0;
    private static int flag3 = 0;

    /*record*/
    private ImageView mStopRecordingButton;
    private ImageView mRecordButton;
    private Camera2TextureView mCameraView;
    private Camera2Proxy mCameraProxy;
    private Chronometer mCountTime;

    /*CountDown*/
    private CountDownTimer mCountDownTimer;
    private TextView mCountDownText;
    private int mCountDownTime = 0;
    private int mCountDownTimeTemp = 0;
    private boolean isCountDown = false;
    private boolean isCountDownFinished = false;
    private Handler mTimerUpdateHandler;

    /*ThumbNails*/
    private ImageView mPreviewPicture;
    private Bitmap mThumbnailBitmap;
    private CircularDrawable mThumbnailDrawable;

    private static RadioGroup mModuleSelectMain;

    /*从拍照模式切换过来时刷新相册显示*/
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isCreated = true;
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        if (!isCreated) {
            return;
        }
        if (isVisibleToUser) {
            if (!mCameraView.isAvailable()) {
                mCameraView.setSurfaceTextureListener(mSurfaceTextureListener);
            } else {
                reopenCamera();
            }
            Bitmap bitmap = ImageUtils.getLatestThumbBitmap();
            mPreviewPicture.setImageDrawable(ImageUtils.updateThumbnail(bitmap , mActivity));
        }else{
            mCameraProxy.releaseCamera();
        }
    }


    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.video_layout,container,false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        mTextrureView = (Camera2TextureView)view.findViewById(R.id.texture_view);
        mContext = getContext();
        mActivity = getActivity();
        mRootView = view;
        mParentView = getParentFragment();
        mCloseButton = (ImageView)view.findViewById(R.id.close_button);
        mFlashButton = (ImageView)view.findViewById(R.id.flash_button);
        mCountButton = (ImageView)view.findViewById(R.id.count_down);
        mFilterButton = (ImageView)view.findViewById(R.id.filter_mode_switcher);
        mThumbnailImage = (ImageView)view.findViewById(R.id.preview_thumb);
        mSwitchCameraDevice = (ImageView)view.findViewById(R.id.front_back_switcher);
        mStopRecordingButton = (ImageView)view.findViewById(R.id.stop_recording);
        mRecordButton = (ImageView)view.findViewById(R.id.record_button);
        mRecordButton.setVisibility(View.VISIBLE);
        mRecordButton.setOnClickListener(this);
        mStopRecordingButton.setOnClickListener(this);

        mModuleSelectMain = (RadioGroup)mActivity.findViewById(R.id.module_select_main);

        /*preview*/
        mCameraView = (Camera2TextureView) view.findViewById(R.id.texture_view);
        mCameraProxy = mCameraView.getCameraProxy();

        mFlashButton.setImageResource(R.drawable.icon_btn_flashlightoff_normal);
        mCloseButton.setOnClickListener(this);
        mFlashButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFlashPop();
            }
        });
        mCountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showCountDownPop();
            }
        });
        mFilterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showFilterPop();
            }
        });

        /*CountTime*/
        mCountTime = (Chronometer) view.findViewById(R.id.timer);

        /*ThumbNail*/
        mPreviewPicture = (ImageView)view.findViewById(R.id.preview_thumb);
        mPreviewPicture.setOnClickListener(this);
        Bitmap bitmap = ImageUtils.getLatestThumbBitmap();
        mPreviewPicture.setImageDrawable(ImageUtils.updateThumbnail(bitmap , mActivity));

        /*SwitchCamera*/
        mSwitchCameraDevice = (ImageView)view.findViewById(R.id.front_back_switcher);
        mSwitchCameraDevice.setOnClickListener(this);

        /*CountDown*/
        mCountDownText = (TextView)view.findViewById(R.id.count_down_text);
        mTimerUpdateHandler = new Handler();
    }

    private void reopenCamera(){
        if(mCameraView.isAvailable()){
            mCameraProxy.setupCamera(mCameraView.getWidth(),mCameraView.getHeight());
            mCameraProxy.openCamera();
        }else {
            mCameraView.setSurfaceTextureListener(mCameraView.getSurfaceTextureListener());
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mCameraProxy.startBackgroundThread();
        if (!mCameraView.isAvailable()) {
            mCameraView.setSurfaceTextureListener(mSurfaceTextureListener);
        } else {
            reopenCamera();
        }
    }

    private TextureView.SurfaceTextureListener mSurfaceTextureListener = new TextureView.SurfaceTextureListener() {
        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
            Log.v(TAG, "onSurfaceTextureAvailable. width: " + width + ", height: " + height);
            mCameraProxy.setPreviewSurface(surface);
            mCameraProxy.setupCamera(width, height);
//            mCameraProxy.openCamera();
            // resize TextureView
            int previewWidth = mCameraProxy.getPreviewSize().getWidth();
            int previewHeight = mCameraProxy.getPreviewSize().getHeight();
            if (width > height) {
                mCameraView.setAspectRatio(previewWidth, previewHeight);
            } else {
                mCameraView.setAspectRatio(previewHeight, previewWidth);
            }
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
            Log.v(TAG, "onSurfaceTextureSizeChanged. width: " + width + ", height: " + height);
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
            Log.v(TAG, "onSurfaceTextureDestroyed");
            mCameraProxy.releaseCamera();
            return false;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture surface) {
        }
    };

    @Override
    public void onPause() {
//        mCameraProxy.releaseCamera();
        super.onPause();
    }

    @Override
    public void onStop() {
        mCameraProxy.releaseCamera();
        super.onStop();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mCameraProxy.releaseCamera();
    }

    private Runnable mTimerUpdateTask = new Runnable() {
        @Override
        public void run() {
            if(mCountDownTime > 1){
                mCountDownTimer.start();
                mTimerUpdateHandler.postDelayed(mTimerUpdateTask,mCountDownTimeTemp * 1000);
            }else{
                mCountDownText.setVisibility(View.GONE);
                mCountTime.setBase(SystemClock.elapsedRealtime());//计时器清零
                int hour = (int) ((SystemClock.elapsedRealtime() - mCountTime.getBase()) / 1000 / 60);
                mCountTime.setFormat("0"+String.valueOf(hour)+":%s");
                mCountTime.start();
                StartRecording();
            }
        }

    };

    private void initCountDownTimer(final int time){
        mCountDownTime = time;
        isCountDownFinished = false;
        if(mCountDownTime != 0){
            isCountDown = true;
        }else {
            isCountDown = false;
        }
        mCountDownTimer = new CountDownTimer((mCountDownTime + 2) * 1000,1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                Log.d(TAG,"mCountDownTime: " + mCountDownTime);
                mCountDownText.setText(mCountDownTime + "");
                if (mCountDownTime == 0){
                    mCountDownTimer.cancel();
                    isCountDownFinished = true;
                    return;
                }else {
                    mCountDownTime--;
                }
            }

            @Override
            public void onFinish() {
                Log.d(TAG,"CountDown finished");
            }
        };
    }

    public void StartRecording(){
        mCloseButton.setVisibility(View.INVISIBLE);
        mFilterButton.setVisibility(View.INVISIBLE);
        mFlashButton.setVisibility(View.INVISIBLE);
        mCountButton.setVisibility(View.INVISIBLE);
        mSwitchCameraDevice.setVisibility(View.INVISIBLE);
        mThumbnailImage.setVisibility(View.INVISIBLE);
        mRecordButton.setVisibility(View.INVISIBLE);
        mModuleSelectMain.setVisibility(View.INVISIBLE);
        mStopRecordingButton.setVisibility(View.VISIBLE);
        mCameraProxy.setupMediaRecorder();
        mCameraProxy.startRecordingVideo(mCameraView);
    }

    public void RecordingCompleted(){
        mCloseButton.setVisibility(View.VISIBLE);
        mFilterButton.setVisibility(View.VISIBLE);
        mFlashButton.setVisibility(View.VISIBLE);
        mCountButton.setVisibility(View.VISIBLE);
        mSwitchCameraDevice.setVisibility(View.VISIBLE);
        mThumbnailImage.setVisibility(View.VISIBLE);
        mStopRecordingButton.setVisibility(View.INVISIBLE);
        mModuleSelectMain.setVisibility(View.VISIBLE);
        mRecordButton.setVisibility(View.VISIBLE);
        String videoThumbnailPath = mCameraProxy.getVideoPath();
        mCameraProxy.stopRecording();
        Bitmap videoThumbnailBitmap = ThumbnailUtils.createVideoThumbnail(videoThumbnailPath, MediaStore.Video.Thumbnails.MINI_KIND);
        mPreviewPicture.setImageDrawable(new CircularDrawable(videoThumbnailBitmap,mActivity));
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.record_button:
                mCountTime.setVisibility(View.VISIBLE);
                mTimerUpdateHandler.post(mTimerUpdateTask);
                /*mCountTime.setBase(SystemClock.elapsedRealtime());//计时器清零
                int hour = (int) ((SystemClock.elapsedRealtime() - mCountTime.getBase()) / 1000 / 60);
                mCountTime.setFormat("0"+String.valueOf(hour)+":%s");
                mCountTime.start();
                StartRecording();*/
                break;
            case R.id.stop_recording:
                RecordingCompleted();
                mCountTime.stop();
                mCountTime.setBase(SystemClock.elapsedRealtime());
                Log.d(TAG,"mCountDownTimeTemp: " + mCountDownTimeTemp);
                if(mCountDownTimeTemp != 0) {
                    initCountDownTimer(mCountDownTimeTemp);
                    mCountDownText.setVisibility(View.VISIBLE);
                    mCountDownText.setText(mCountDownTimeTemp + "");
                }
                mCountTime.setVisibility(View.GONE);
                break;
            case R.id.front_back_switcher:
                mCameraProxy.switchCamera(mCameraView.getWidth(),mCameraView.getHeight());
                mCameraProxy.startPreview();
                break;
            case R.id.flash_off:
                mCameraProxy.setFlashMode("off");
                mTextViewOffforFlash.setTextColor(Color.parseColor("#F8E71C"));
                mFlashButton.setImageResource(R.drawable.icon_btn_flashlightoff_normal);
                flag1 = 1;
                mFunctionPopup.dismiss();
                break;
            case R.id.flash_auto:
                mCameraProxy.setFlashMode("auto");
                mTextViewAutoforFlash.setTextColor(Color.parseColor("#F8E71C"));
                mFlashButton.setImageResource(R.drawable.icon_btn_flashlight_auto_selected);
                mFunctionPopup.dismiss();
                flag1 = 2;
                break;
            case R.id.flash_on:
                mCameraProxy.setFlashMode("on");
                mTextViewOnforFlash.setTextColor(Color.parseColor("#F8E71C"));
                mFlashButton.setImageResource(R.drawable.icon_btn_flashlighton_selected2);
                mFunctionPopup.dismiss();
                flag1 = 3;
                break;
            case R.id.flash_keep:
                mCameraProxy.setFlashMode("keep");
                mTextViewKeepforFlash.setTextColor(Color.parseColor("#F8E71C"));
                mFlashButton.setImageResource(R.drawable.icon_btn_keep_selected);
                flag1 = 4;
                mFunctionPopup.dismiss();
                break;
            case R.id.count_off:
                mCountDownText.setVisibility(View.GONE);
                initCountDownTimer(0);
                mCountDownTimeTemp = 0;
                mTextViewOffforCountDown.setTextColor(Color.parseColor("#F8E71C"));
                mCountButton.setImageResource(R.drawable.icon_countdown);
                flag2 = 5;
                mCountDownPopup.dismiss();
                break;
            case R.id.count_three:
                mCountDownText.setVisibility(View.VISIBLE);
                mCountDownText.setText("3");
                initCountDownTimer(3);
                mCountDownTimeTemp = 3;
                mTextView3sforCountDown.setTextColor(Color.parseColor("#F8E71C"));
                mCountButton.setImageResource(R.drawable.icon_btn_countdown3s);
                mCountDownPopup.dismiss();
                flag2 = 6;
                break;
            case R.id.count_seven:
                mCountDownText.setVisibility(View.VISIBLE);
                mCountDownText.setText("7");
                initCountDownTimer(7);
                mCountDownTimeTemp = 7;
                mTextView7sforCountDown.setTextColor(Color.parseColor("#F8E71C"));
                mCountButton.setImageResource(R.drawable.icon_btn_countdown7s);
                flag2 = 7;
                mCountDownPopup.dismiss();
                break;
            case R.id.count_ten:
                mCountDownText.setVisibility(View.VISIBLE);
                mCountDownText.setText("10");
                initCountDownTimer(10);
                mCountDownTimeTemp = 10;
                mTextView10sforCountDown.setTextColor(mContext.getColor(R.color.selected));
                mCountButton.setImageResource(R.drawable.icon_btn_countdown10s);
                flag2 = 8;
                mCountDownPopup.dismiss();
                break;
            case R.id.filter_dismiss:
                mFilterButton.setImageResource(R.drawable.icon_filter);
                flag3 = 9;
                checkSelected(flag3);
                break;
            case R.id.filter_monochrome:
                mFilterButton.setImageResource(R.drawable.icon_filter_selected);
                flag3 = 10;
                checkSelected(flag3);
                break;
            case R.id.filter_sepia:
                mFilterButton.setImageResource(R.drawable.icon_filter_selected);
                flag3 = 11;
                checkSelected(flag3);
                break;
            case R.id.filter_negative:
                mFilterButton.setImageResource(R.drawable.icon_filter_selected);
                flag3 = 12;
                checkSelected(flag3);
                break;
            case R.id.filter_solarize:
                mFilterButton.setImageResource(R.drawable.icon_filter_selected);
                flag3 = 13;
                checkSelected(flag3);
                break;
            case R.id.filter_posterize:
                mFilterButton.setImageResource(R.drawable.icon_filter_selected);
                flag3 = 14;
                checkSelected(flag3);
                break;
            case R.id.filter_aqua:
                mFilterButton.setImageResource(R.drawable.icon_filter_selected);
                flag3 = 15;
                checkSelected(flag3);
                break;
            case R.id.close_button:
                mActivity.finish();
                break;
        }
    }

    public void showFlashPop(){
        hideTopUIForFlash();
        if(mFunctionPopupListener == null) {
            mFunctionPopupListener = new FunctionPopup.Listener() {
                @Override
                public void onPopupWindowDismissListener() {
                    showUIForFlash();
                }

                @Override
                public void onItemClickListener(View v) {

                }
            };
        }
        if(mFunctionPopup == null) {
            mFunctionPopup = new FunctionPopup(mActivity);
            mFunctionPopup.setBackgroundDrawable(null);
            mFunctionPopup.setOutsideTouchable(true);
        }
        LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View contentView = inflater.inflate(R.layout.function_in_main_flash,null);
        mFunctionPopup.setContentView(contentView);
        mFunctionPopup.setFUnctionsListener(mFunctionPopupListener);

        mImageViewDisforFlash = mFunctionPopup.getContentView().findViewById(R.id.flash_dismiss);
        mTextViewOffforFlash = mFunctionPopup.getContentView().findViewById(R.id.flash_off);
        mTextViewAutoforFlash = mFunctionPopup.getContentView().findViewById(R.id.flash_auto);
        mTextViewOnforFlash = mFunctionPopup.getContentView().findViewById(R.id.flash_on);
        mTextViewKeepforFlash = mFunctionPopup.getContentView().findViewById(R.id.flash_keep);
        checkSelected(flag1);

        mTextViewAutoforFlash.setVisibility(View.GONE);
        mTextViewOnforFlash.setVisibility(View.GONE);

        mFlashPopView = mFunctionPopup.getContentView().findViewById(R.id.flash_popup_view);
        mTextViewOffforFlash.setOnClickListener(this);
        mTextViewAutoforFlash.setOnClickListener(this);
        mTextViewOnforFlash.setOnClickListener(this);
        mTextViewKeepforFlash.setOnClickListener(this);

        mImageViewDisforFlash.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mFunctionPopup.dismiss();
            }
        });

        mFunctionPopup.showAtLocation(mRootView, Gravity.CENTER,0,-750);
    }

    public void hideTopUIForFlash(){
        mFilterButton.setVisibility(View.INVISIBLE);
        mCountButton.setVisibility(View.INVISIBLE);
        mCloseButton.setVisibility(View.INVISIBLE);
    }

    public void hideTopUIForCountDown(){
        mFilterButton.setVisibility(View.INVISIBLE);
        mFlashButton.setVisibility(View.INVISIBLE);
        mCloseButton.setVisibility(View.INVISIBLE);
    }

    public void hideTopUIForFIlter(){
        mCloseButton.setVisibility(View.INVISIBLE);
    }

    public void showUIForFilter(){
        mCountButton.setVisibility(View.VISIBLE);
        mFlashButton.setVisibility(View.VISIBLE);
        mCloseButton.setVisibility(View.VISIBLE);
    }

    public void showUIForFlash(){
        mFilterButton.setVisibility(View.VISIBLE);
        mCountButton.setVisibility(View.VISIBLE);
        mCloseButton.setVisibility(View.VISIBLE);
    }

    public void showUIForCountDown(){
        mFilterButton.setVisibility(View.VISIBLE);
        mFlashButton.setVisibility(View.VISIBLE);
        mCloseButton.setVisibility(View.VISIBLE);
    }

    public void showCountDownPop(){
        hideTopUIForCountDown();
        if(mCountDownPopupListener == null) {
            mCountDownPopupListener = new FunctionPopup.Listener() {
                @Override
                public void onPopupWindowDismissListener() {
                    showUIForCountDown();
                }

                @Override
                public void onItemClickListener(View v) {

                }
            };
        }
        if(mCountDownPopup == null) {
            mCountDownPopup = new FunctionPopup(mActivity);
            mCountDownPopup.setBackgroundDrawable(null);
            mCountDownPopup.setOutsideTouchable(true);
        }
        LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View contentView = inflater.inflate(R.layout.function_in_main,null);
        mCountDownPopup.setContentView(contentView);
        mCountDownPopup.setFUnctionsListener(mCountDownPopupListener);

        mImageViewDisforCountDown = mCountDownPopup.getContentView().findViewById(R.id.count_dismiss);
        mTextViewOffforCountDown = mCountDownPopup.getContentView().findViewById(R.id.count_off);
        mTextView3sforCountDown = mCountDownPopup.getContentView().findViewById(R.id.count_three);
        mTextView7sforCountDown = mCountDownPopup.getContentView().findViewById(R.id.count_seven);
        mTextView10sforCountDown = mCountDownPopup.getContentView().findViewById(R.id.count_ten);
        checkSelected(flag2);

        mCountDownPopView = mCountDownPopup.getContentView().findViewById(R.id.count_down_popup_view);

        mTextViewOffforCountDown.setOnClickListener(this);
        mTextView3sforCountDown.setOnClickListener(this);
        mTextView7sforCountDown.setOnClickListener(this);
        mTextView10sforCountDown.setOnClickListener(this);

        mImageViewDisforCountDown.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCountDownPopup.dismiss();
            }
        });

        mCountDownPopup.showAtLocation(mRootView, Gravity.CENTER,0,-750);
    }

    public void showFilterPop(){
        hideTopUIForFIlter();
        if(mFilterPopupListener == null) {
            mFilterPopupListener = new FunctionPopup.Listener() {
                @Override
                public void onPopupWindowDismissListener() {
                    showUIForFilter();
                }

                @Override
                public void onItemClickListener(View v) {

                }
            };
        }
        if(mFilterPopup == null) {
            mFilterPopup = new FunctionPopup(mActivity);
            mFilterPopup.setBackgroundDrawable(null);
            mFilterPopup.setOutsideTouchable(true);
        }
        LayoutInflater inflater = (LayoutInflater)mActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View contentView = inflater.inflate(R.layout.function_in_main_filter,null);
        mFilterPopup.setContentView(contentView);
        mFilterPopup.setFUnctionsListener(mFilterPopupListener);

        mDissmissFilter = mFilterPopup.getContentView().findViewById(R.id.filter_dismiss);
        mMonochromeFilter = mFilterPopup.getContentView().findViewById(R.id.filter_monochrome);
        mSepiaFilter = mFilterPopup.getContentView().findViewById(R.id.filter_sepia);
        mNegativeFilter = mFilterPopup.getContentView().findViewById(R.id.filter_negative);
        mSolarizeFilter = mFilterPopup.getContentView().findViewById(R.id.filter_solarize);
        mPosterizeFilter = mFilterPopup.getContentView().findViewById(R.id.filter_posterize);
        mAquaFilter = mFilterPopup.getContentView().findViewById(R.id.filter_aqua);
        mDissmissFilterText = mFilterPopup.getContentView().findViewById(R.id.filter_text_dismiss);
        mMonochromeFilterText = mFilterPopup.getContentView().findViewById(R.id.filter_text_monochrome);
        mSepiaFilterText = mFilterPopup.getContentView().findViewById(R.id.filter_text_sepia);
        mNegativeFilterText = mFilterPopup.getContentView().findViewById(R.id.filter_text_negative);
        mSolarizeFilterText = mFilterPopup.getContentView().findViewById(R.id.filter_text_solarize);
        mPosterizeFilterText = mFilterPopup.getContentView().findViewById(R.id.filter_text_posterize);
        mAquaFilterText = mFilterPopup.getContentView().findViewById(R.id.filter_text_aqua);

        mDissmissFilterText.setTextColor(Color.parseColor("#F8E71C"));
        checkSelected(flag3);

        mFilterPopView = mFilterPopup.getContentView().findViewById(R.id.filter_popup_view);

        mDissmissFilter.setOnClickListener(this);
        mMonochromeFilter.setOnClickListener(this);
        mSepiaFilter.setOnClickListener(this);
        mNegativeFilter.setOnClickListener(this);
        mSolarizeFilter.setOnClickListener(this);
        mPosterizeFilter.setOnClickListener(this);
        mAquaFilter.setOnClickListener(this);

        mFilterPopup.showAtLocation(mRootView, Gravity.CENTER,0,520);
    }

    public void checkSelected(int flag){
        switch (flag){
            case 1:
                mTextViewOffforFlash.setTextColor(Color.parseColor("#F8E71C"));
                mTextViewAutoforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewOnforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewKeepforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 2:
                mTextViewAutoforFlash.setTextColor(Color.parseColor("#F8E71C"));
                mTextViewOffforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewOnforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewKeepforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 3:
                mTextViewOnforFlash.setTextColor(Color.parseColor("#F8E71C"));
                mTextViewAutoforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewKeepforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewOffforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 4:
                mTextViewKeepforFlash.setTextColor(Color.parseColor("#F8E71C"));
                mTextViewAutoforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewOnforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewOffforFlash.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 5:
                mTextViewOffforCountDown.setTextColor(Color.parseColor("#F8E71C"));
                mTextView3sforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                mTextView7sforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                mTextView10sforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 6:
                mTextView3sforCountDown.setTextColor(Color.parseColor("#F8E71C"));
                mTextView7sforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                mTextView10sforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewOffforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 7:
                mTextView7sforCountDown.setTextColor(Color.parseColor("#F8E71C"));
                mTextView3sforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                mTextView10sforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewOffforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 8:
                mTextView10sforCountDown.setTextColor(mContext.getColor(R.color.selected));
                mTextView3sforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                mTextView7sforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                mTextViewOffforCountDown.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 9:
                mDissmissFilterText.setTextColor(Color.parseColor("#F8E71C"));
                mSepiaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mMonochromeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mNegativeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSolarizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mPosterizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mAquaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                break;
            case 10:
                mDissmissFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSepiaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mNegativeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSolarizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mPosterizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mAquaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mMonochromeFilterText.setTextColor(Color.parseColor("#F8E71C"));
                break;
            case 11:
                mDissmissFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mNegativeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSolarizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mPosterizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mAquaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mMonochromeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSepiaFilterText.setTextColor(Color.parseColor("#F8E71C"));
                break;
            case 12:
                mDissmissFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSepiaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSolarizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mPosterizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mAquaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mMonochromeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mNegativeFilterText.setTextColor(Color.parseColor("#F8E71C"));
                break;
            case 13:
                mDissmissFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSepiaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mPosterizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mAquaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mMonochromeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mNegativeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSolarizeFilterText.setTextColor(Color.parseColor("#F8E71C"));
                break;
            case 14:
                mDissmissFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSepiaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mAquaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mMonochromeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mNegativeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSolarizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mPosterizeFilterText.setTextColor(Color.parseColor("#F8E71C"));
                break;
            case 15:
                mDissmissFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSepiaFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mMonochromeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mNegativeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mSolarizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mPosterizeFilterText.setTextColor(Color.parseColor("#FFFFFF"));
                mAquaFilterText.setTextColor(Color.parseColor("#F8E71C"));
                break;
        }
    }
}
