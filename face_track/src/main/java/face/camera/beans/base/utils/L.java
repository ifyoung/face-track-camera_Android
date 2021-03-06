package face.camera.beans.base.utils;

import android.util.Log;

/**
 * Created by huangjinlong on 2016/2/24.
 * 日至打印相关辅助类
 */
public class L {
    private L()
    {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    public static boolean isDebug = true;// 是否为debug模式，关闭后上线
    private static final String TAG = "智拍";

    //默认tag
    public static void i(String msg)
    {
        if (isDebug)
            Log.i(TAG, msg);
    }

    public static void d(String msg)
    {
        if (isDebug)
            Log.d(TAG, msg);
    }

    public static void e(String msg)
    {
        if (isDebug)
            Log.e(TAG, msg);
    }

    public static void v(String msg)
    {
        if (isDebug)
            Log.v(TAG, msg);
    }
    public static void w(String msg)
    {
        if (isDebug)
            Log.w(TAG, msg);
    }

    // 自定义tag
    public static void i(String tag, String msg)
    {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void d(String tag, String msg)
    {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void e(String tag, String msg)
    {
        if (isDebug)
            Log.i(tag, msg);
    }

    public static void v(String tag, String msg)
    {
        if (isDebug)
            Log.i(tag, msg);
    }
    public static void w(String tag, String msg)
    {
        if (isDebug)
            Log.w(tag, msg);
    }
}
