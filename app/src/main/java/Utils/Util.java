package Utils;

import android.app.Application;
import android.util.Log;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-20 下午4:20
 */
public class Util extends Application {

    private static final String TAG = "MyApp";

    private static Util util;

    @Override
    public void onCreate() {
        super.onCreate();
        util = this;

    }

    public static Util getInstance() {
        return util;
    }

}
