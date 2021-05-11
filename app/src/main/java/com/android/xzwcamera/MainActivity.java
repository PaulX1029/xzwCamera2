package com.android.xzwcamera;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;
import androidx.viewpager2.widget.ViewPager2;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.icu.util.LocaleData;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.android.xzwcamera.UI.AutoFitTextureView;

import java.util.ArrayList;
import java.util.List;

import Utils.ImageUtils;

public class MainActivity extends FragmentActivity implements RadioGroup.OnCheckedChangeListener {

    private static String TAG = "MainActivity";

    private ViewPager mPager;
    private FragmentManager mFragmentManager;
//    private AutoFitTextureView autoFitTextureView;

    private Camera2Proxy mCameraProxy;

    private RadioGroup mModuleSelectMain;
    private RadioButton mModuleSelectCapture;
    private RadioButton mModuleSelectVideo;

    private FragmentAdapter mFragmentAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        checkPermission();
        mCameraProxy = new Camera2Proxy(this);
//        autoFitTextureView = (AutoFitTextureView) findViewById(R.id.Texture_view);
        mPager = (ViewPager) findViewById(R.id.pager);
        mModuleSelectMain = (RadioGroup) findViewById(R.id.module_select_main);
        mModuleSelectMain.setOnCheckedChangeListener(this);
        mModuleSelectCapture = (RadioButton) findViewById(R.id.module_select_capture);
        mModuleSelectVideo = (RadioButton) findViewById(R.id.module_select_video);
        mFragmentManager = getSupportFragmentManager();
        List<Fragment> mFragments = new ArrayList<>();
        mFragments.add(new VideoFragment());
        mFragments.add(new CaptureFragment());
        mFragmentAdapter = new FragmentAdapter(mFragmentManager);
        mFragmentAdapter.setFragments(mFragments);
        mPager.setAdapter(mFragmentAdapter);
        mPager.setCurrentItem(1);
        mModuleSelectMain.check(R.id.module_select_capture);
        mPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        mModuleSelectVideo.setChecked(true);
                        mModuleSelectCapture.setChecked(false);
                        break;
                    case 1:
                        mModuleSelectCapture.setChecked(true);
                        mModuleSelectVideo.setChecked(false);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return false;
    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        int currentItem = mPager.getCurrentItem();
        Log.d("xzw1","currentItem:" + currentItem);
        switch (checkedId) {
            case R.id.module_select_capture:
                if(currentItem != 1) {
                    mPager.setCurrentItem(1);
                }
                break;
            case R.id.module_select_video:
                if(currentItem != 0) {
                    mPager.setCurrentItem(0);
                }
                break;
        }
    }



    private void checkPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            String[] permissions = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA};
            for (String permission : permissions) {
                if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, permissions, 200);
                    return;
                }
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[]
            grantResults) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && requestCode == 200) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "请在设置中打开摄像头和存储权限", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent();
                    intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                    Uri uri = Uri.fromParts("package", getPackageName(), null);
                    intent.setData(uri);
                    startActivityForResult(intent, 200);
                    return;
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == RESULT_OK && requestCode == 200) {
            checkPermission();
        }
    }

    @Override
    public void onResume() {
        Log.d(TAG,"onResume");
        mPager.setAdapter(mFragmentAdapter);
        mPager.setCurrentItem(1);
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mCameraProxy.releaseCamera();
        Log.d(TAG,"onPause");
    }

    @Override
    public void onStop() {
//        mCameraProxy.releaseCamera();
        Log.d(TAG,"onStop");
        super.onStop();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG,"onDestroy");
        super.onDestroy();
//        mCameraProxy.releaseCamera();
    }

}
