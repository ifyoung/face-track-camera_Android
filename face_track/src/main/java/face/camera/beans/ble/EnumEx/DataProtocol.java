package face.camera.beans.ble.EnumEx;


import face.camera.beans.ble.L;

import static face.camera.beans.base.activity.BaseActivity.myDevice;

public class DataProtocol {

    //    public static byte[] S_BYTE1 = new byte[]{(byte) 0xFE};//头部
    public static byte S_BYTE1 = (byte) 0xFE;//头部
    public static byte S_BYTE2 = 0x5A;
    public static byte S_BYTE3 = 0x03;

    public @interface BYTE_direction {
        byte HOLD_LEFT = 0x00;
        byte RIGHT = 0x01;

    }

    public @interface BYTE_speed {
        byte HOLD = 0x00;
        byte SLOW = 0x01;
        byte FAST = 0x01;

    }

    public @interface BYTE_LED {
        byte Shoot = (byte) 0xc5;
        byte RecordStart = 0x55;
        byte RecordStop = (byte) 0xd5;

    }

    public @interface Order_hold {
        final class SET {
            public static byte[] Hold() {
                byte sum = (byte) (S_BYTE3 + BYTE_direction.HOLD_LEFT + BYTE_speed.HOLD);
                return new byte[]{S_BYTE1, S_BYTE2,
                        S_BYTE3, BYTE_direction.HOLD_LEFT, BYTE_speed.HOLD, sum};
            }
        }
    }

    public @interface Order_LED {
        final class SET {
            public static byte[] LED(byte led_action) {
                byte sum = (byte) (S_BYTE3 + led_action + BYTE_speed.HOLD);
                return new byte[]{S_BYTE1, S_BYTE2,
                        S_BYTE3, led_action, BYTE_speed.HOLD, sum};
            }
        }
    }

    public @interface Order_fast {
        final class SET {
            public static byte[] Direction(byte direction) {
                byte sum = (byte) (S_BYTE3 + direction + BYTE_speed.FAST);
                return new byte[]{S_BYTE1, S_BYTE2,
                        S_BYTE3, direction, BYTE_speed.FAST, sum};
            }
        }

    }

    public @interface Order_SLOW {
        final class SET {
            public static byte[] Direction(byte direction) {
                byte sum = (byte) (S_BYTE3 + direction + BYTE_speed.SLOW);
                return new byte[]{S_BYTE1, S_BYTE2,
                        S_BYTE3, direction, BYTE_speed.SLOW, sum};
            }
        }

    }
    //激活校验指令
    public @interface Order_Active {
        final class SET {
            public static byte[] Active() {
                byte sum = (byte) (S_BYTE3 + S_BYTE2 + BYTE_speed.HOLD);
                return new byte[]{S_BYTE1, S_BYTE2,
                        S_BYTE3, S_BYTE2, BYTE_speed.HOLD, sum};
            }
        }

    }

    private void sendWrapper(byte[] order) {

        if (myDevice != null) {
            StringBuilder str = new StringBuilder();
            for (byte aData : order) {
                // System.out.print(Integer.toHexString(b[i]).toUpperCase()
                // + ",");
                str.append(Integer.toHexString(aData & 0xFF).toUpperCase()).append(",");

            }

            L.d("sendWrapper", str.toString());
            myDevice.sendOrder(order);
        }
    }


}
