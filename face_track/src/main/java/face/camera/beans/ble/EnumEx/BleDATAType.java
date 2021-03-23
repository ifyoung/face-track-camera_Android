package face.camera.beans.ble.EnumEx;


import androidx.annotation.StringDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@StringDef({BleDATAType.EXTRA_DATA,
        BleDATAType.HEIGHT_DATA,
        BleDATAType.ERROR_DATA})
//@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface BleDATAType {

    String EXTRA_DATA = "com.feelair.ble.EXTRA_DATA";
    String HEIGHT_DATA = "com.feelair.ble.HEIGHT_DATA";
    String ERROR_DATA = "com.feelair.ble.ERROR_DATA";
}







