package face.camera.beans.record.other;

import android.content.res.Resources;

import androidx.annotation.StringDef;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ResourceBundle;

import face.camera.beans.R;
import face.camera.beans.ble.EnumEx.BleDATAType;

//
//@StringDef({MagicFilterType.NONE,
//        MagicFilterType.WARM,
//        MagicFilterType.ANTIQUE})
////@Target({ElementType.FIELD})
//@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.

//public @interface MagicFilterType {
//    String NONE = Resources.getSystem().getString(R.string.str_filter_none);
//    String WARM = "WARM";
//    String ANTIQUE = "ANTIQUE";
//    String COOL = "COOL";
//    String BRANNAN = Resources.getSystem().getString(R.string.str_filter_brannan);
//    String FREUD = "FREUD";
//    String HEFE = Resources.getSystem().getString(R.string.str_filter_hefe);
//    String HUDSON = Resources.getSystem().getString(R.string.str_filter_hudson);
//    String INKWELL = Resources.getSystem().getString(R.string.str_filter_inkwell);
//    String N1977 = Resources.getSystem().getString(R.string.str_filter_n1977);
//    String NASHVILLE = Resources.getSystem().getString(R.string.str_filter_nashville);
//
//
//}

//@StringDef({MagicFilterType.NONE,
//        MagicFilterType.WARM,
//        MagicFilterType.ANTIQUE})
//@Target({ElementType.FIELD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface MagicFilterType {
    int NONE = R.string.str_filter_none;
    int WARM = R.string.str_filter_warm;
    int ANTIQUE = R.string.str_filter_antique;
    int COOL = R.string.str_filter_cool;
    int BRANNAN = R.string.str_filter_brannan;
    int FREUD = R.string.str_filter_freud;
    int HEFE = R.string.str_filter_hefe;
    int HUDSON = R.string.str_filter_hudson;
    int INKWELL = R.string.str_filter_inkwell;
    int N1977 = R.string.str_filter_n1977;
    int NASHVILLE = R.string.str_filter_nashville;
}
