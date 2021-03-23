package face.camera.beans.net;


import android.text.TextUtils;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.regex.Pattern;

/**
 * User DELL
 * Date 2015/2/4.
 */
public class MyTextUtil {

    public static final String NICK_NAME_REGEX = "";

    public static boolean isPhoneNumber(String phone) {
        // 匹配手机号
        Pattern pmobile = Pattern.compile("^13[0-9]{9}$|14[0-9]{9}|15[0-9]{9}|17[0-9]{9}$|18[0-9]{9}$");
        return pmobile.matcher(phone).matches();
    }

    public static String md5(String string) {
        byte[] hash;
        try {
            hash = MessageDigest.getInstance("MD5").digest(string.getBytes("UTF-8"));
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Huh, MD5 should be supported?", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("Huh, UTF-8 should be supported?", e);
        }
        StringBuilder hex = new StringBuilder(hash.length * 2);
        for (byte b : hash) {
            if ((b & 0xFF) < 0x10) hex.append("0");
            hex.append(Integer.toHexString(b & 0xFF));
        }
        return hex.toString();

    }

    public static String read(File file) {
        StringBuilder str = new StringBuilder();
        BufferedReader in = null;
        try {
            in = new BufferedReader(new FileReader(file));
            String s;
            try {
                while ((s = in.readLine()) != null)
                    str.append(s).append('\n');
            } finally {
                in.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return str.toString();
    }

    // 写入指定的文本文件，append为true表示追加，false表示重头开始写，
    //text是要写入的文本字符串，text为null时直接返回
    public static void write(File cacheFile, boolean append, String text) {
        if (text == null)
            return;
        try {
            BufferedWriter out = new BufferedWriter(new FileWriter(cacheFile,
                    append));
            try {
                out.write(text);
            } finally {
                out.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static String millFormat(int millisecond) {
        if (millisecond <= 0) {
            return "00:00";
        }
        int second = 1000;
        int minute = second * 60;
        int hour = minute * 60;
        String hh = "";
        String mm = "";
        String ss = "";

        if (millisecond >= hour) {
            int h = millisecond / hour;
            hh = intToString(h);
            int m = millisecond % (h * hour) / minute;
            mm = intToString(m);
            int s = millisecond % (h * hour + m * minute) / second;
            ss = intToString(s);
            return hh + ":" + mm + ":" + ss;
        } else {
            if (millisecond > minute) {
                int m = millisecond / minute;
                mm = intToString(m);
                int s = millisecond % (m * minute) / second;
                ss = intToString(s);
                return mm + ":" + ss;
            } else {
                int s = millisecond / second;
                return "00:" + intToString(s);
            }
        }

    }

    private static String intToString(int time) {
        if (time < 10) {
            return "0" + time;
        } else {
            return String.valueOf(time);
        }
    }

    public static void setText(TextView tv, CharSequence text) {
        if (null == tv)
            return;
        if (!TextUtils.isEmpty(text)) {
            tv.setText(text);
        }
    }

    /**
     * 将1-100之间的数字转换成汉字
     *
     * @param number 需要转化的数字
     * @return 转化后的汉字
     */
    public static String formatNumber(int number) {
        if (number >= 100 || number < 1) {
            throw new IllegalArgumentException("只支持[1-100)之间");
        }
        String[] str = {"十", "一", "二", "三", "四", "五", "六", "七", "八", "九"};


        if (number < 10) {
            return str[number];
        } else {

            if (number == 10) {
                return "十";
            } else {
                StringBuilder sb = new StringBuilder();
                String s = String.valueOf(number);
                String s0 = String.valueOf(s.charAt(0));
                String s1 = String.valueOf(s.charAt(1));
                if(number % 10 == 0){
                    sb.append((str[Integer.parseInt(s0) + 1]));
                    sb.append("十");
                }else{
                    if(number < 20){
                        sb.append("十");
                        sb.append(str[number % 10]);
                    }else{
                        sb.append((str[Integer.parseInt(s0) + 1]));
                        sb.append("十");
                        sb.append(str[number % 10]);
                    }
                }
                return sb.toString();
            }
        }
    }

    public static boolean checkNickName(String nickName) {
        return Pattern.matches("[\\u4E00-\\u9FA5\\w~!@#$%^&*()+-=\\[\\]\\{\\}|:]{1,20}", nickName);
    }

    public static boolean checkPassword(String password) {
        return Pattern.matches("[\\w~!@#$%^&*()+-=\\[\\]\\{\\}|:]{6,16}", password);
    }

}
