package face.camera.beans.ble.EnumEx;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({BleHandleDataType.Connected,
        BleHandleDataType.ConnectData,
        BleHandleDataType.Disconnected,
        BleHandleDataType.RssiValue})
//@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface BleHandleDataType {

    int Connected = 1;
    int ConnectData = 2;
    int Disconnected = -1;
    int RssiValue = 3;
}







