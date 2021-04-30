package Utils;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.ImageFormat;
import android.graphics.Matrix;
import android.graphics.Rect;
import android.media.Image;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import com.android.xzwcamera.CaptureFragment;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-21 上午9:27
 */
public class ImageUtils {

    private static final String TAG = "ImageUtils";
    private static Context sContext = CaptureFragment.getmContext();

    private static final String GALLERY_PATH = Environment.getExternalStoragePublicDirectory(Environment
            .DIRECTORY_DCIM) + File.separator + "Camera";

    private static final String[] STORE_IMAGES = {
            MediaStore.Video.Thumbnails._ID,
    };
    private static final SimpleDateFormat DATE_FORMAT = new SimpleDateFormat("yyyyMMdd_HHmmss");

    private static Bitmap mThumbnailBitmap;
    private static CircularDrawable mThumbnailDrawable;

    public static Bitmap rotateBitmap(Bitmap source, int degree, boolean flipHorizontal, boolean recycle) {
        if (degree == 0 && !flipHorizontal) {
            return source;
        }
        Matrix matrix = new Matrix();
        matrix.postRotate(degree);
        if (flipHorizontal) {
            matrix.postScale(-1, 1);
        }
        Log.d(TAG, "source width: " + source.getWidth() + ", height: " + source.getHeight());
        Log.d(TAG, "rotateBitmap: degree: " + degree);
        Bitmap rotateBitmap = Bitmap.createBitmap(source, 0, 0, source.getWidth(), source.getHeight(), matrix, false);
        Log.d(TAG, "rotate width: " + rotateBitmap.getWidth() + ", height: " + rotateBitmap.getHeight());
        if (recycle) {
            source.recycle();
        }
        return rotateBitmap;
    }

    public static void saveImage(byte[] jpeg) {
        String fileName = DATE_FORMAT.format(new Date(System.currentTimeMillis())) + ".jpg";
        File outFile = new File(GALLERY_PATH, fileName);
        Log.d(TAG, "saveImage. filepath: " + outFile.getAbsolutePath());
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outFile);
            os.write(jpeg);
            os.flush();
            os.close();
            insertToDB(outFile.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        refreshAlbum(outFile);
    }

    public static void saveBitmap(Bitmap bitmap) {
        String fileName = DATE_FORMAT.format(new Date(System.currentTimeMillis())) + ".jpg";
        File outFile = new File(GALLERY_PATH, fileName);
        Log.d(TAG, "saveBitmap. filepath: " + outFile.getAbsolutePath());
        FileOutputStream os = null;
        try {
            os = new FileOutputStream(outFile);
            boolean success = bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            if (success) {
                insertToDB(outFile.getAbsolutePath());
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        refreshAlbum(outFile);
    }

    public static void refreshAlbum(File outFile){
//        MediaStore.Images.Media.insertImage(sContext.getContentResolver(), BitmapFactory.decodeFile(outFile.getAbsolutePath()), outFile.getName(), null);
        Intent intent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        Uri uri = Uri.fromFile(outFile);
        intent.setData(uri);
        sContext.sendBroadcast(intent);
        Bitmap ThumbnailBitmap = ThumbnailUtils.createVideoThumbnail(outFile.getAbsolutePath(), MediaStore.Video.Thumbnails.MINI_KIND);
    }


    public static void insertToDB(String picturePath) {
        ContentValues values = new ContentValues();
        ContentResolver resolver = sContext.getContentResolver();
        values.put(MediaStore.Images.ImageColumns.DATA, picturePath);
        values.put(MediaStore.Images.ImageColumns.TITLE, picturePath.substring(picturePath.lastIndexOf("/") + 1));
        values.put(MediaStore.Images.ImageColumns.DATE_TAKEN, System.currentTimeMillis());
        values.put(MediaStore.Images.ImageColumns.MIME_TYPE, "image/jpeg");
        resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
    }

    public static Bitmap getLatestThumbBitmap() {
        Bitmap bitmap = null;
        // 按照时间顺序降序查询
        Cursor cursor = MediaStore.Images.Media.query(sContext.getContentResolver(), MediaStore.Images.Media
                .EXTERNAL_CONTENT_URI, STORE_IMAGES, null, null, MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC");
        boolean first = cursor.moveToFirst();
        if (first) {
            long id = cursor.getLong(0);
            bitmap = MediaStore.Images.Thumbnails.getThumbnail(sContext.getContentResolver(), id, MediaStore.Images
                    .Thumbnails.MICRO_KIND, null);
        }
        cursor.close();
        return bitmap;
    }

    public static CircularDrawable updateThumbnail(Bitmap bitmap, Activity activity){
        if(bitmap == null)
            return null;
        if (mThumbnailBitmap != null)
            mThumbnailBitmap.recycle();
        mThumbnailBitmap = bitmap;
        mThumbnailDrawable = new CircularDrawable(bitmap , activity);
        return mThumbnailDrawable;
    }

    public static byte[] generateNV21Data(Image image) {
        Rect crop = image.getCropRect();
        int format = image.getFormat();
        int width = crop.width();
        int height = crop.height();
        Image.Plane[] planes = image.getPlanes();
        byte[] data = new byte[width * height * ImageFormat.getBitsPerPixel(format) / 8];
        byte[] rowData = new byte[planes[0].getRowStride()];
        int channelOffset = 0;
        int outputStride = 1;
        for (int i = 0; i < planes.length; i++) {
            switch (i) {
                case 0:
                    channelOffset = 0;
                    outputStride = 1;
                    break;
                case 1:
                    channelOffset = width * height + 1;
                    outputStride = 2;
                    break;
                case 2:
                    channelOffset = width * height;
                    outputStride = 2;
                    break;
            }
            ByteBuffer buffer = planes[i].getBuffer();
            int rowStride = planes[i].getRowStride();
            int pixelStride = planes[i].getPixelStride();
            int shift = i == 0 ? 0 : 1;
            int w = width >> shift;
            int h = height >> shift;
            buffer.position(rowStride * (crop.top >> shift) + pixelStride * (crop.left >> shift));
            for (int row = 0; row < h; row++) {
                int length;
                if (pixelStride == 1 && outputStride == 1) {
                    length = w;
                    buffer.get(data, channelOffset, length);
                    channelOffset += length;
                } else {
                    length = (w - 1) * pixelStride + 1;
                    buffer.get(rowData, 0, length);
                    for (int col = 0; col < w; col++) {
                        data[channelOffset] = rowData[col * pixelStride];
                        channelOffset += outputStride;
                    }
                }
                if (row < h - 1) {
                    buffer.position(buffer.position() + rowStride - length);
                }
            }
        }
        return data;
    }
}
