package face.camera.beans.ble.EnumEx;



import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;


@IntDef({BleCurrentStateType.Connected,
        BleCurrentStateType.Connecting,
        BleCurrentStateType.Disconnected,
        BleCurrentStateType.Disconnecting,
        BleCurrentStateType.Searching,
        BleCurrentStateType.UnKnown
})
//@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface BleCurrentStateType {
    int Connected = 1;
    int Connecting = 0;
    int Disconnecting = -1;
    int Disconnected = -2;
    int UnKnown = -3;
    int Searching = -4;

}







