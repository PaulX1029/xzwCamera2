package Utils;

import android.app.Application;

/**
 * @Description
 * @Author PaulX
 * @Date 21-4-21 上午9:28
 */
public class MyApp extends Application {

    private static MyApp app;

    @Override
    public void onCreate() {
        super.onCreate();
        app = this;
    }

    public static MyApp getInstance() {
        return app;
    }
}
