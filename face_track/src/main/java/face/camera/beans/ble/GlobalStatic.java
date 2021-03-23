package face.camera.beans.ble;


import face.camera.beans.ble.EnumEx.BleCurrentStateType;
import face.camera.beans.ble.EnumEx.BleSavedType;

public class GlobalStatic {
    // 连接过的的地址与设备名
    public static String myUsedAddress;
    public static String myUsedName;
    //是否正式版标志

    public static boolean isRealease = true;

    @BleCurrentStateType
    public static int BleCurrentState = BleCurrentStateType.Disconnected;



    public static final String APPFOLDERPATH = "/MagicAir/";
    public static final String DATAS_FOLDER_PATH = APPFOLDERPATH + "datas/";

    public static String tempCode = BleSavedType.PairPWD.DefaultValue;

    public static String activeUrl = "http://snp-us.top";
    //    code
    public static String updateUrl = "http://snp-us.top";
//    public static String UPDateApkName = "sygjApk.apk";
    public static String UPDateApkName = "ApaiGo.apk";
    public static String MAIN_ENTER = "http://snp-us.top";

    public static boolean ISDeviceRight = false;
    public static boolean ISARCACTIVE = false;


}
