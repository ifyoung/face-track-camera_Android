package face.camera.beans.net;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface SavedKV {

    @interface H5Path {
        String H5PathName = "H5PathName";
        String H5PathKey = "H5PathKey";
        String H5PathDef = "1.2";
    }

    @interface VER {
        String VerSharedName = "Ver_SharedName";
        String VerValueKey = "Ver_ValueKey";
        String VerValueDef = "1.2";
    }

    @interface UserInfo {
        String InfoSharedName = "UserInfo_SharedName";
        String InfoValueKey = "UserInfo_ValueKey";
        String InfoValueDef = "";
    }

    @interface CacheData {
        String CacheSharedName = "CacheData_SharedName";
        String CacheValueKey = "CacheData_ValueKey";
        String CacheValueDef = "";
    }


    @interface ResultType {
        String SUCCESS = "success";
        String FAILED = "failed";
        String ERROR = "error";
        String EMPTY = "本地数据为空";
        String EMPTYSer = "传送数据为空";
    }

}







