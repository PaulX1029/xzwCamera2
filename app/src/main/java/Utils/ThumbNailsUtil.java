package Utils;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;

import java.io.File;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-20 下午4:16
 */
public class ThumbNailsUtil {

    private static final String TAG = "ThumbNailsUtil";

    private static Context mContext;

    private static Uri mThumbnailUri;

    private static final  String[] STORE_IMAGES = {MediaStore.Images.Thumbnails._ID ,MediaStore.Images.Thumbnails.DATA};

    public static Bitmap getLatestThumbBitmap(Context context) {
        Bitmap bitmap = null;
        ContentResolver contentResolver = context.getContentResolver();
        Log.d("xzw111","ThumbNailsUtils contentResolver: " + contentResolver);
        // 按照时间顺序降序查询
        Cursor cursor = MediaStore.Images.Media.query(contentResolver, MediaStore.Images.Media
                .EXTERNAL_CONTENT_URI, STORE_IMAGES, null, null, MediaStore.Files.FileColumns.DATE_MODIFIED + " DESC");
        Log.d("xzw111","ThumbNailsUtils cursor: " + cursor);
        boolean first = cursor.moveToFirst();
        if (first) {
            long id = cursor.getLong(1);
            bitmap = MediaStore.Images.Thumbnails.getThumbnail(contentResolver, id, MediaStore.Images
                    .Thumbnails.MICRO_KIND, null);
        }
        cursor.close();
        Log.d("xzw111","ThumbNailsUtils bitmap:" + bitmap);
        return bitmap;
    }
}
