package face.camera.beans.ble.EnumEx;



import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * 最关键广播BROADCAST_USED_DEVICE_ADDED
 */

@IntDef({StartBleRequestCodeType.STARTBLE, StartBleRequestCodeType.STARTBLE_SEC})
//@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface StartBleRequestCodeType {
     int STARTBLE = 11;
     int STARTBLE_SEC = 12;
}







