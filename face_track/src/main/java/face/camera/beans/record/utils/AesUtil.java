package face.camera.beans.record.utils;

//import org.apache.commons.codec.binary.Base64;


import org.bouncycastle.jce.provider.BouncyCastleProvider;

import java.security.Security;

import javax.crypto.Cipher;
import javax.crypto.spec.SecretKeySpec;

import face.camera.beans.record.utils.Base64Com.Base64;

/**
 * Stone.Cai
 * 2019年07月25日16:59:24
 * 添加
 * AES工具
 */
public class AesUtil {

    private static final String EncryptAlg ="AES";
    private static final String Cipher_Mode="AES/ECB/PKCS7Padding";
    private static final String Encode="UTF-8";
    private static final int Secret_Key_Size=32;
    private static final String Key_Encode="UTF-8";

    private static final String keys="69530C901192ABE596CD10A288D7D327";

    static {
        Security.addProvider(new BouncyCastleProvider());
    }


    /**
     * Stone.Cai
     * 2019年07月25日14:58:20
     * 添加
     * 加密
     * @param content
     * @return
     * @throws
     */
    public static String aesPKCS7PaddingEncrypt(String content) throws Exception{
        return aesPKCS7PaddingEncrypt(content,keys);
    }

    /**
     * Stone.Cai
     * 2019年07月25日11:33:08
     * 添加
     * 加密
     * @param content
     * @param key
     * @return
     * @throws
     */
    public static String aesPKCS7PaddingEncrypt(String content, String key) throws Exception {
        try {

            Cipher cipher = Cipher.getInstance(Cipher_Mode);
            byte[] realKey=getSecretKey(key);
            cipher.init(Cipher.ENCRYPT_MODE, new SecretKeySpec(realKey,EncryptAlg));
            byte[] data=cipher.doFinal(content.getBytes(Encode));
            String result=new Base64().encodeToString(data);
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("AES加密失败：content=" +content +" key="+key);
        }
    }


    /**
     * Stone.Cai
     * 2019年07月25日14:59:02
     * 添加
     * 解密
     * @param content
     * @return
     * @throws
     */
    public static String aesPKCS7PaddingDecrypt(String content) throws Exception{
        return aesPKCS7PaddingDecrypt(content,keys);
    }



    /**
     * AES/ECB/PKCS7Padding 解密
     * @param content
     * @param key 密钥
     * @return 先转base64 再解密
     * @throws
     */
    private static String aesPKCS7PaddingDecrypt(String content, String key) throws Exception {
        try {
            byte[] decodeBytes=Base64.decodeBase64(content);
            Cipher cipher = Cipher.getInstance(Cipher_Mode);
            byte[] realKey=getSecretKey(key);
            cipher.init(Cipher.DECRYPT_MODE, new SecretKeySpec(realKey,EncryptAlg));
            byte[] realBytes=cipher.doFinal(decodeBytes);

            return new String(realBytes, Encode);
        } catch (Exception e) {
            e.printStackTrace();
            throw new Exception("AES解密失败：Aescontent = " +e.fillInStackTrace(),e);
        }
    }

    /**
     * 对密钥key进行处理：如密钥长度不够位数的则 以指定paddingChar 进行填充；
     * 此处用空格字符填充，也可以 0 填充，具体可根据实际项目需求做变更
     * @param key
     * @return
     * @throws
     */
    public static byte[] getSecretKey(String key) throws Exception{
        final byte paddingChar=' ';
        byte[] realKey = new byte[Secret_Key_Size];
        byte[] byteKey = key.getBytes(Key_Encode);
        for (int i =0;i<realKey.length;i++){
            if (i<byteKey.length){
                realKey[i] = byteKey[i];
            }else {
                realKey[i] = paddingChar;
            }
        }
        return realKey;
    }

}
