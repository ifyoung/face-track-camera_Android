package face.camera.beans.ble;

/**
 * @author wu
 */

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;

import face.camera.beans.ble.EnumEx.BleSavedType;


public class Preferences {
    /**
     * 保存Preferences文件
     *
     * @param context 上下文
     * @param name    Preferences名字
     * @param key     key
     * @param value   value
     * @return
     */
    public static void savePreferences(Context context, String name,
                                       String key, String value) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                name, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    /**
     * 不指定Preferences的名字
     *
     * @param activity 当前的activity
     * @param key      key
     * @param value    values
     * @return
     */
    public static void savePreferences(Activity activity, String key,
                                       String value) {
        SharedPreferences sharedPref = activity
                .getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString(key, value);
        editor.apply();
    }

    // 存颜色
    public static void savePreferences(Context context, String name,
                                       String key, int value) {
        SharedPreferences sharedPref = context.getSharedPreferences(name,
                Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putInt(key, value);
        editor.apply();
    }

    /**
     * 通过上下文获取指定名字的Preferences
     *
     * @param context      上下文
     * @param name         Preferences的名字
     * @param key          key
     * @param defaultValue 默认值
     * @return
     */
    public static String getPreferences(Context context, String name,
                                        String key, String defaultValue) {
        SharedPreferences sharedPreferences = context.getSharedPreferences(
                name, Context.MODE_PRIVATE);
        return sharedPreferences.getString(key, defaultValue);
    }

    public static int getPreferences(Context context, String name, String key) {
        SharedPreferences sharedPref = context.getSharedPreferences(name,
                Context.MODE_PRIVATE);
//        return sharedPref.getInt(key, Color.GREEN);
        return sharedPref.getInt(key,1);
    }

    /**
     * 通过Activity获取默认的Preferences
     *
     * @param activity     当前的Activity
     * @param name         Preferences的名字
     * @param key          key
     * @param defaultValue 默认值
     * @return
     */
    public static String getPreferences(Activity activity, String name,
                                        String key, String defaultValue) {
        SharedPreferences sharedPref = activity
                .getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(name, defaultValue);
    }

    public static String getPreferences(Activity activity, String key) {
        SharedPreferences sharedPref = activity
                .getPreferences(Context.MODE_PRIVATE);
        return sharedPref.getString(key, "无地址");
    }


    public static boolean getBLENAMENew(Context context) {
        String sharedPref = context
                .getSharedPreferences(BleSavedType.Name.SharedKey,
                        Context.MODE_PRIVATE).getString(
                        BleSavedType.Name.ValueKey, " ");//判断新旧版

        if (!sharedPref.contains("MagicAir")) {
            return true;
        }
        return false;
    }

    //是否是1.5版本
    public static boolean isA5Version(Context context) {

        String ver = Preferences.getPreferences(context, "MagicVer", "MagicVer", "old_version");

        return ver.contains("A5");
    }

}
