package face.camera.beans.ble.EnumEx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface BleSavedType {

    @interface ADDRESS {
        String SharedKey = "Address_SharedKey";
        String ValueKey = "Address_ValueKey";
    }

    @interface ADDRESS_New {
        String SharedKey = "Address_SharedKey_New";
        String ValueKey = "Address_ValueKey_New";
    }

    @interface Name {
        String SharedKey = "Name_SharedKey";
        String ValueKey = "Name_ValueKey";
    }

    @interface PairPWD {
        String SharedName = "PairPWD_SharedName";
        String SharedKey = "PairPWD_SharedKey";
        String DefaultValue = "";
    }

}







