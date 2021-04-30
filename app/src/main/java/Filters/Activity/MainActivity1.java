package Filters.Activity;

import android.Manifest;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.android.xzwcamera.R;

import static android.content.pm.PackageManager.PERMISSION_GRANTED;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-29 下午3:39
 */
public class MainActivity1 extends BaseActivity implements View.OnClickListener{

    public final String TAG = "MainActivity1";

    private static final int REQUEST_PERMISSION = 1;
    private final String[] PERMISSIONS = new String[] {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d("xzw filter", "onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main1);
        initView();
        checkPermission();
    }

    private void initView() {
        findViewById(R.id.main_camera_btn).setOnClickListener(this);
        findViewById(R.id.main_gallery_btn).setOnClickListener(this);
    }

    private boolean checkPermission() {
        for (int i = 0; i < PERMISSIONS.length; i++) {
            int state = ContextCompat.checkSelfPermission(this, PERMISSIONS[i]);
            if (state != PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, PERMISSIONS, REQUEST_PERMISSION);
                return false;
            }
        }
        return true;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_PERMISSION) {
            if (grantResults[0] != PERMISSION_GRANTED) {
                Toast.makeText(this, R.string.main_permission_hint, Toast.LENGTH_SHORT).show();
                Intent intent = new Intent();
                intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
                Uri uri = Uri.fromParts("package", getPackageName(), null);
                intent.setData(uri);
                startActivityForResult(intent, REQUEST_PERMISSION);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == REQUEST_PERMISSION) {
            //checkPermission();
        }
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.main_camera_btn:
                startActivity(new Intent(this, CameraActivity.class));
                break;
            case R.id.main_gallery_btn:
                startActivity(new Intent(this, GalleryActivity.class));
                break;
        }
    }
}
