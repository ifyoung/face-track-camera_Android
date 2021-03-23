package face.camera.beans.ble.EnumEx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.UUID;

//@StringDef({BleUUIDType.SERVER, BleUUIDType.READ, BleUUIDType.WRITE})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface BleUUIDType {
    //    public static final UUID CCCD = UUID.fromString("00002902-0000-1000-8000-00805f9b34fb");
//    private static final UUID MY_UUID = UUID.fromString("00005501-d102-11e1-9b23-00025b00a5a5");
//    private static final UUID MY_UUID_SEC = UUID.fromString("00005501-d102-11e1-9b23-00025b00a5a0");
//    private static final UUID SERVICE_UUID = UUID.fromString("00005500-d102-11e1-9b23-00025b00a5a5");


//    String SERVERStr = "0003CDD0-0000-1000-8000-00805F9B0131";//服务
//    String READStr = "0003CDD1-0000-1000-8000-00805F9B0131";//读取特征
//    String WRITEStr = "0003CDD2-0000-1000-8000-00805F9B0131";//写


    //let BLEServiceUUID = "0000FFB0-0000-1000-8000-00805F9B34FB"
//let BLECharacteristicUUID_WRITE  = "0000FFB1-0000-1000-8000-00805F9B34FB"
//let BLECharacteristicUUID_READ  = "00000FFB2-0000-1000-8000-00805F9B34FB"
    String SERVERStr = "0000FFB0-0000-1000-8000-00805F9B34FB";//服务
    String READStr = "0000FFB2-0000-1000-8000-00805F9B34FB";//读取特征
    String WRITEStr = "00000FFB1-0000-1000-8000-00805F9B34FB";//写

    // UUID for the BTLE client characteristic which is necessary for notifications.
    String CCCDStr = "00002902-0000-1000-8000-00805f9b34fb";
    UUID WRITE = UUID.fromString(WRITEStr);
    UUID READ = UUID.fromString(READStr);
    UUID SERVER = UUID.fromString(SERVERStr);
    UUID CCCD = UUID.fromString(CCCDStr);
//    Class GetBleUUID = UUID.fromString(SERVER).getClass();
    ;
}







