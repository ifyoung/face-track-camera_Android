package face.camera.beans.net;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

import java.util.Date;

import face.camera.beans.record.utils.AesUtil;


/**
 * Created by huangjinlong on 2016/2/24.
 * 网络相关工具类
 */
public class NetUtils {
    private NetUtils() {
        /* cannot be instantiated */
        throw new UnsupportedOperationException("cannot be instantiated");
    }

    /**
     * 是否网络连接
     *
     * @param context
     * @return
     */
    public static boolean isConnected(Context context) {

        ConnectivityManager connectivity = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);

        if (null != connectivity) {

            NetworkInfo info = connectivity.getActiveNetworkInfo();
            if (null != info && info.isConnected()) {
                if (info.getState() == NetworkInfo.State.CONNECTED) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 是否是wifi环境下
     */
    public static boolean isWifi(Context context) {


        ConnectivityManager connectivityManager = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetInfo = connectivityManager.getActiveNetworkInfo();
        if (activeNetInfo != null
                && activeNetInfo.getType() == ConnectivityManager.TYPE_WIFI) {
            return true;
        }
        return false;

    }

    /**
     * 打开网络连接设置页面
     */
    public static void openSetting(Activity activity) {
        Intent intent = new Intent("/");
        ComponentName cm = new ComponentName("com.android.settings",
                "com.android.settings.WirelessSettings");
        intent.setComponent(cm);
        intent.setAction("android.intent.action.VIEW");
        activity.startActivityForResult(intent, 0);
    }


    /**
     * token生成
     * tokenPrim六位数字
     */
    public static String genToken(String tokenPrim) {
        StringBuffer signStr = new StringBuffer();
        signStr.append(tokenPrim.replaceAll("_", "\\$")).append("_");


        long t = new Date().getTime();
        signStr.append(new Date().getTime()).append("_");
        String keyStr = signStr.toString() + "0EC28E604767C487AE293F5A7854522F";
//        String sign= DigestUtils.md5Hex(keyStr);
        String sign = MyTextUtil.md5(keyStr);
        signStr.append(sign);

        String test = "admin_1563882792706_bfaa19b5e4712d28464b4687afeb9196";
        try {
            test = AesUtil.aesPKCS7PaddingEncrypt(test);
        } catch (Exception e) {
            e.printStackTrace();
        }

//String base64Key = KeyPairGenUtilAn.encryption(signStr.toString());
//        String base64Key = KeyPairGenUtil.encryption(signStr.append(sign).toString());
//        String base64Key = KeyPairGenUtil.encryption(test);
//        String jiemi1 = KeyPairGenUtil.deciphering(base64Key);
//        String jiemi = KeyPairGenUtil.deciphering("pZmuX+XA/JGUNdax39XVWGRrbyoKXVoSzGKLbE5VaqhojOdeyIY/6EEtyYz5Er95nMjk0qZaH3rG4ZWZifoOYYXVfmyQCxEBwZyQzWohBkf9vUpvzgaqi5owz9aTJO6hIsSb30kvfHtwYyxIkJngGsmzgBkeCVzYivGIzCnfXS0=");
        String aesToken = signStr.append(sign).toString();

        try {
            aesToken = AesUtil.aesPKCS7PaddingEncrypt(aesToken);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return aesToken;
    }


}
