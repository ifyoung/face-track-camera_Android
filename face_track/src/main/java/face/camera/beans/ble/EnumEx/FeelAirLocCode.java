package face.camera.beans.ble.EnumEx;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.FIELD, ElementType.METHOD})
@Retention(RetentionPolicy.SOURCE) //表示注解所存活的时间,在运行时,而不会存在. class 文件.
public @interface FeelAirLocCode {

    @interface Code {
        String Global = "EA";
        String ZH_main = "E9";
        String Zh_gat = "E8";
        String MaLai_Xinjia = "E7";
        String Tai = "E6";
        String Riben = "E5";
        String MeiGuo = "E4";
        String Dibai = "E3";
        String Feilv = "E2";
        String Def = "Default";


         final class FeelAirLocCodeCls {


            public static String fromLocCode(String c) {
                switch (c) {
                    case Code.Global:

                        return Country.Global;
                    case Code.ZH_main:

                        return Country.ZH_main;
                    case Code.Zh_gat:

                        return Country.Zh_gat;
                    case Code.MaLai_Xinjia:

                        return Country.MaLai_Xinjia;
                    case Code.Tai:

                        return Country.Tai;
                    case Code.Riben:

                        return Country.Riben;
                    case Code.MeiGuo:

                        return Country.MeiGuo;
                    case Code.Dibai:

                        return Country.Dibai;
                    case Code.Feilv:

                        return Country.Feilv;
                }

                return Country.Def;
            }


        }


        @interface Country {

            String Global = "全球";
            String ZH_main = "中国大陆";
            String Zh_gat = "中国香港澳门台湾";
            String MaLai_Xinjia = "马来西亚/新加坡";
            String Tai = "泰国";
            String Riben = "日本";
            String MeiGuo = "美国";
            String Dibai = "阿拉伯联合酋长国迪拜";
            String Feilv = "菲律宾";
            String Def = "默认";
        }


    }


}










