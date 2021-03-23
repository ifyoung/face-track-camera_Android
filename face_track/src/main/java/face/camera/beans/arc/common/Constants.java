package face.camera.beans.arc.common;

import android.content.Context;
import android.content.SharedPreferences;


public class Constants {

    //虹软Key,有次数限制
    public static String APP_ID = "8rpGZPJoGMY4VM62aRpYYMZDD7Gi8x9iVvcwN4roecyJ";
    public static String SDK_KEY = "F2oMFXpFkb9pbKR5nFpgTpvnwscsGzw8ugnm1PRReWhe";

    /**
     * IR预览数据相对于RGB预览数据的横向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    public static final int HORIZONTAL_OFFSET = 0;
    /**
     * IR预览数据相对于RGB预览数据的纵向偏移量，注意：是预览数据，一般的摄像头的预览数据都是 width > height
     */
    public static final int VERTICAL_OFFSET = 0;

    //0,是首次进入
    public static boolean setIsFirstIn(Context context, int isFirst) {
        if (context == null) {
            return false;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("Constants", Context.MODE_PRIVATE);
        return sharedPreferences.edit()
                .putInt("setIsFirstIn", isFirst)
                .commit();
    }

    public static boolean getIsFirstIn(Context context) {
        if (context == null) {
            return true;
        }
        SharedPreferences sharedPreferences = context.getSharedPreferences("Constants", Context.MODE_PRIVATE);
        return sharedPreferences.getInt("setIsFirstIn", 0) == 0;
    }


}

