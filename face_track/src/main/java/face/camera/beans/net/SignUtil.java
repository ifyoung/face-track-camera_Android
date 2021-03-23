package face.camera.beans.net;



import java.util.Date;

import face.camera.beans.record.utils.AesUtil;
import face.camera.beans.record.utils.Base64Com.DigestUtils;


/**
 * @author stone
 * 2018年01月29日13:07:19
 * 添加
 * 签名类
 */
public class SignUtil {


    static String SIGN_KEY = "0EC28E604767C487AE293F5A7854522F";

    /**
     * Stone.Cai
     * 2019年07月20日21:34:43
     * 添加
     *
     * @param key
     * @return
     */
    public static String createSing(String key) {
        StringBuffer signStr = new StringBuffer();
        signStr.append(key.replaceAll("_", "\\$")).append("_");
        signStr.append(new Date().getTime()).append("_");
        String keyStr = signStr.toString() + SIGN_KEY;
//        String sign = DigestUtils.md5Hex(keyStr);
        String sign = DigestUtils.md5Hex(keyStr);
        signStr.append(sign);
        System.out.println(signStr.toString());
        String keys = null;
        try {
            keys = AesUtil.aesPKCS7PaddingEncrypt(signStr.toString());
        } catch (Exception e) {
        }
        return keys;
    }

    /**
     * Stone.Cai
     * 2018年01月29日13:19:55
     * 添加
     * 校验签名
     *
     * @param signStr
     * @return
     */
    public static boolean signChecking(String signStr) {
        if (signStr.isEmpty()) {
            return false;
        }
        try {
            signStr = AesUtil.aesPKCS7PaddingDecrypt(signStr);
        } catch (Exception e) {
            return false;
        }
        String[] strs = signStr.split("_");
        if (strs.length != 3) {
            return false;
        }
        StringBuffer signStrTem = new StringBuffer();
        signStrTem.append(strs[0]).append("_");
        signStrTem.append(strs[1]).append("_");
        signStrTem.append(SIGN_KEY);
        Long nowDate = new Date().getTime();
        if ((nowDate - Long.valueOf(strs[1])) / 1000 > 7200) {
            return false;
        }
        String str = signStrTem.toString();
        String lls = DigestUtils.md5Hex(str);
        if (lls.equals(strs[2])) {
            return true;
        }
        return false;
    }


    /**
     * Stone.Cai
     * 2019年07月25日16:38:27
     * 添加解码
     *
     * @param sign
     * @return
     */
    public static String signDecode(String sign) {

        try {
            return AesUtil.aesPKCS7PaddingDecrypt(sign);
        } catch (Exception e) {
        }
        return null;
    }
    //加密
    public static String signEncode(String text) {

        try {
            return AesUtil.aesPKCS7PaddingEncrypt(text);
        } catch (Exception e) {
        }
        return null;
    }
//
//	public static void main(String[] args) {
//		System.out.println(createSing("admin"));
//	}

}
