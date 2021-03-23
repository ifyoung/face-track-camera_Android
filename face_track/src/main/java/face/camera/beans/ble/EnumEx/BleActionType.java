package face.camera.beans.ble.EnumEx;



import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 最关键广播BROADCAST_USED_DEVICE_ADDED
 */

@StringDef({BleActionType.ACTION_DATA_AVAILABLE,
        BleActionType.ACTION_GATT_CONNECTED,
        BleActionType.ACTION_GATT_DISCONNECTED,
        BleActionType.ACTION_GATT_NEED_PAIR,
        BleActionType.ACTION_GATT_DISCONNECTED_NEW,
        BleActionType.BROADCAST_RSSI,
        BleActionType.ACTION_GATT_CONNECTING
//        BleActionType.BROADCAST_DEVICE_ADDED_AND_USING old logic
})
//@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface BleActionType {

    String ACTION_DATA_AVAILABLE = "com.ma.ACTION_DATA_AVAILABLE";
    //    String EXTRA_DATA = "com.ma.ble.EXTRA_DATA";
//    String HEIGHT_DATA = "com.ma.ble.HEIGHT_DATA";
//    String ERROR_DATA = "com.ma.ble.ERROR_DATA";
    String ACTION_GATT_CONNECTED = "com.ma.ACTION_GATT_CONNECTED";
    String ACTION_GATT_DISCONNECTED = "com.ma.ACTION_GATT_DISCONNECTED";
    String ACTION_GATT_NEED_PAIR = "com.ma.ACTION_GATT_NEED_PAIR";
    String ACTION_GATT_DISCONNECTED_NEW = "com.ma.ACTION_GATT_DISCONNECTED_NO_SER";
    String ACTION_GATT_CONNECTING = "com.ma.ACTION_GATT_CONNECTING";


    String ACTION_GATT_CODE_ERROR = "com.ma.ACTION_GATT_CODE_ERROR";

    String BROADCAST_RSSI = "com.ma.BROADCAST_RSSI";
//    String BROADCAST_DEVICE_ADDED_AND_USING = "com.ma.BROADCAST_DEVICE_ADDED_AND_USING";old logic

}







