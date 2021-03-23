package face.camera.beans.ble.EnumEx;


import androidx.annotation.IntDef;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@IntDef({ActiveStateBleType.ACTIVE,
        ActiveStateBleType.INACTIVE,
        ActiveStateBleType.LOCK,
        ActiveStateBleType.OUTFACTORY,
        ActiveStateBleType.TD_IN_CHINA,
        ActiveStateBleType.LOC_PERMIT})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface ActiveStateBleType {
    int ACTIVE = 1;//激活
    int INACTIVE = 0;//未激活
    int LOCK = 2;//黑名单
    int OUTFACTORY =3;//出厂
    int TD_IN_CHINA =4;//国外的不能在国内使用
    int LOC_PERMIT = 5;//TD 校验允许激活

    int LOC_FAIL = 6;
    int LOC_FAIL_NO_NET = 7;
    int LOC_FAIL_NO_PHONE_PERMIT = 8;
}
