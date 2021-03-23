package com.luck.picture.lib.tools;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;


import java.util.Locale;

public class StaticFunction {

    private static final String TAG = "StaticFunction-Tag";

    public static boolean SYSTEM_CHANGE_LANGUAGE_FLAG = false;


    public static final String SYSTEM_LOCALE_LANGUAGUE_STRING = "system_locale_languague_string";
    private static final String SYSTEM_LOCALE_COUNTRY_STRING = "system_locale_country_string";

    private static final String[] ENAME = {"zh", "en", "fr", "de", "ja", "ko",
            "es", "pt", "ar"};

    public static Locale getSystemLacate(Context context) {
        SharedPreferences sharedPreferences = getCurrentSharedPreferences(context);
        String str = sharedPreferences.getString(
                SYSTEM_LOCALE_LANGUAGUE_STRING, "no_languague");
        String strc = sharedPreferences.getString(SYSTEM_LOCALE_COUNTRY_STRING,
                "");
        if ("no_languague".equals(str)) {

//			Resources resources = context.getResources();//获得res资源对象
//			Configuration config = resources.getConfiguration();//获得设置对象
//			Locale l = config.locale;
            Locale l = Resources.getSystem().getConfiguration().locale;
            String def = "en";
            for (String aENAME : ENAME) {
                if (aENAME.equals(l.getLanguage())) {
                    def = aENAME;
                    break;
                }
            }

            Locale nLocale = null;


            if ("zh".equals(def)) {
                nLocale = Locale.CHINA;
            } else if ("fr".equals(def)) {
                nLocale = Locale.FRANCE;
            }else if ("ko".equals(def)) {
                nLocale = Locale.KOREA;
            }else if ("ja".equals(def)) {
                nLocale = Locale.JAPAN;
            }
            else {
                nLocale = new Locale("en");
            }

//			setSystemLacate(context, nLocale);
            Log.e("结果", nLocale.getLanguage() + "默认语言" + l.getLanguage() + "系统语言新" + Resources.getSystem().getConfiguration().locale.getLanguage());

            return nLocale;
        }

        return new Locale(str, strc);
    }

    public static void setSystemLacate(Context context, Locale locale) {
        SharedPreferences sharedPreferences = getCurrentSharedPreferences(context);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        if (locale != null) {
            editor.putString(SYSTEM_LOCALE_LANGUAGUE_STRING, locale.getLanguage());
            editor.putString(SYSTEM_LOCALE_COUNTRY_STRING, locale.getCountry());
        } else {
//            L.e("语言跟随系统");
            editor.putString(SYSTEM_LOCALE_LANGUAGUE_STRING, "no_languague");
            editor.putString(SYSTEM_LOCALE_COUNTRY_STRING, "");
        }

        editor.apply();

    }

    public static SharedPreferences getCurrentSharedPreferences(Context context) {

        return context.getSharedPreferences(TAG, Context.MODE_PRIVATE);
    }

    public static void reloadLanguageAction(Context context) {
        Locale locale = StaticFunction.getSystemLacate(context);
        Log.i("语言reloadLanguageAction", locale.getLanguage());
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, null);
        context.getResources().flushLayoutCache();

    }


    //全面屏
    public static void fullScreenDisplay(Activity aty) {
        //全屏显示
        aty.getWindow().getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);

        WindowManager.LayoutParams lp = aty.getWindow().getAttributes();

        //下面图1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        }
        //下面图2
//        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES;
        //下面图3
//        lp.layoutInDisplayCutoutMode = WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_DEFAULT;
        aty.getWindow().setAttributes(lp);

    }


}
