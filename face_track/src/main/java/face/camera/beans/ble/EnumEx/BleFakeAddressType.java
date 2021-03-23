package face.camera.beans.ble.EnumEx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface BleFakeAddressType {//假的蓝牙地址，以达到断开连接的目的

     String A = "01:09:5B:00:15:10";
     String B = "01:09:5B:00:15:12";
     String C = "01:09:5B:00:15:13";
}







