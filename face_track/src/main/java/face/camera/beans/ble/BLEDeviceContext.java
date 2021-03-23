package face.camera.beans.ble;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCallback;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothGattService;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.annotation.MainThread;

import java.lang.reflect.Method;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


import face.camera.beans.ble.EnumEx.BleCurrentStateType;
import face.camera.beans.ble.EnumEx.BleUUIDType;


/**
 * 供外部调用的方法集内部类（连接、断开连接、发送数据等）
 *
 * @author MagicAir-LCh
 * @time 2018/11/23 10:27 AM
 */
public class BLEDeviceContext extends BluetoothGattCallback {

    //        public volatile String address; // 设备MAC
//        public volatile String name; // 备注名称
    public volatile BluetoothDevice bluetoothDevice;
    volatile BluetoothManager serBluetoothManager;
    volatile BluetoothAdapter serBluetoothAdapter;
    volatile Context serContext;

    private String LOG_TAG = "BLEDeviceContext";

    public volatile boolean busy = false;

    private volatile BluetoothGatt gatt;//蓝牙事务代理
    private volatile BluetoothGattCharacteristic characteristicMydeviceR;
    private volatile BluetoothGattCharacteristic characteristicMydeviceW;

    //串行
    private ExecutorService writeWorker = Executors.newSingleThreadExecutor();

    // TODO ？多个体设备使用一个回调还是多个回调
//    private BluetoothGattCallback callback = new BLEService.MyBluetoothGattCallback(this);

//    private BluetoothGattCallback callback;


    public BLEDeviceContext() {
    }

    /**
     * @param context
     * @param bleManager
     * @param bleAdapter
     */

    public BLEDeviceContext(Context context,
                            BluetoothManager bleManager,
                            BluetoothAdapter bleAdapter) {
        this.serBluetoothManager = serBluetoothManager;
        this.serBluetoothAdapter = serBluetoothAdapter;
        this.serContext = serContext;
    }

    /**
     * @param context
     * @param bluetoothDevice
     * @param bleManager
     * @param bleAdapter
     */
    public BLEDeviceContext(Context context, BluetoothDevice bluetoothDevice,
                            BluetoothManager bleManager,
                            BluetoothAdapter bleAdapter) {
        this.bluetoothDevice = bluetoothDevice;
        this.serBluetoothManager = bleManager;
        this.serBluetoothAdapter = bleAdapter;
        this.serContext = context;
//            connectBle(true);
//            if(gatt == null){
//               new Handler().post(new Runnable() {
//                   @Override
//                   public void run() {
//                       gatt = bluetoothDevice.connectGatt(BLEService.this, false, callback);
//
//                   }
//               });
//            }
    }

    // NOTE: 连接
    void connectBle() {
//        Looper.prepare();
        Handler conHandler = new Handler(Looper.getMainLooper());//NOTE 不绑到主线，容易炸??

        //连接前刷新一下
        internalRefreshDeviceCache("connectBle");

//        Looper.loop();
        //NOTE 连接前，查询一下要连接设备的状态
        int connectionState = serBluetoothManager.getConnectionState(bluetoothDevice, BluetoothProfile.GATT);
        Log.i(LOG_TAG, "connectBle连接前状态" + connectionState);
        if (gatt != null) {
            switch (connectionState) {

                case BluetoothProfile.STATE_CONNECTED:

//                        break;
                case BluetoothProfile.STATE_CONNECTING:

//                        break;
                case BluetoothProfile.STATE_DISCONNECTING:
                    gatt.disconnect();
                    gatt.close();
                    break;
            }
        }
        // NOTE Auto-generated method stub,依附与服务上-BLEService---持久化？
//        gatt = bluetoothDevice.connectGatt(serContext, false, BLEDeviceContext.this);
        conHandler.postDelayed(new Runnable() {

            @Override
            public void run() {
//

                // NOTE Auto-generated method stub,依附与服务上-BLEService---持久化？
                gatt = bluetoothDevice.connectGatt(serContext, false, BLEDeviceContext.this);
            }
        }, 100);
    }

    // NOTE 断开连接
    public void disconnect() {

//        int connectionState = serBluetoothManager.getConnectionState(bluetoothDevice, BluetoothProfile.GATT);
//        if (connectionState == BluetoothProfile.STATE_DISCONNECTED) {
//            Log.i(LOG_TAG, "disconnect: GATT already disconnected");
//        } else {
//
//        }
        if (gatt != null) {
            gatt.disconnect();
            gatt.close();
            internalRefreshDeviceCache("disconnect");
            gatt = null;
            characteristicMydeviceR = null;
            characteristicMydeviceW = null;

        } else {
            characteristicMydeviceR = null;
            characteristicMydeviceW = null;
        }
        GlobalStatic.BleCurrentState = BleCurrentStateType.Disconnected;

    }


    /**
     * Clears the device cache.
     */
    @SuppressWarnings("JavaReflectionMemberAccess")
    @MainThread
    private boolean internalRefreshDeviceCache(String fromFunc) {
//        final BluetoothGatt gatt = mBluetoothGatt;
        if (gatt == null) // no need to be connected
            return false;

        Log.w("RefreshCache", "Refreshing device cache..." + fromFunc);
        Log.w("RefreshCache", "gatt.refresh() (hidden)");

        /*
         * There is a refresh() method in BluetoothGatt class but for now it's hidden.
         * We will call it using reflections.
         */
        try {
            final Method refresh = gatt.getClass().getMethod("refresh");
            return (Boolean) refresh.invoke(gatt);
        } catch (final Exception e) {
            Log.w("RefreshCache", "An exception occurred while refreshing device", e);
//            log(Log.WARN, "gatt.refresh() method not found");
        }
        return false;
    }

//        public void closeGatt() {
//            Log.e(LOG_TAG, "close: closing gatt");
//            if (gatt == null) {
//                return;
//            }
//            unpairDevice(bluetoothDevice);
//            gatt.close();
//            gatt = null;
//            characteristicMydeviceRW = null;
//            characteristicMydeviceRW_2 = null;
//        }

    // NOTE: 获取特征通信口，并打开通知(有通知的口)
    void getCharacteristicAndSetNotification_READ(BluetoothGatt gatt) {
        if (gatt != null) {
            //NOTE 服务
            BluetoothGattService gattSer = gatt.getService(BleUUIDType.SERVER);
//            if (gattSer == null) {
//                Log.e(LOG_TAG, "setCharacteristics: cannot find service");//新版刚上电,约3秒没数据
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        connectBle();
//                    }
//                }, 3000);
//                return;
//            }
            //NOTE 读特征口-主要是配对，放前面
            characteristicMydeviceR = gattSer.getCharacteristic(BleUUIDType.READ);
            if (characteristicMydeviceR == null) {
                Log.i(LOG_TAG, "setCharacteristics: cannot find characteristic" + BleUUIDType.READ);
                connectBle();
            }

            if (characteristicMydeviceR.getDescriptors().size() != 0) {
                BluetoothGattDescriptor des = characteristicMydeviceR.getDescriptors().get(0);
                des.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(des);
            }

            gatt.readCharacteristic(characteristicMydeviceR);//NOTE 读取配对值
            gatt.setCharacteristicNotification(characteristicMydeviceR, true);

//            BluetoothGattDescriptor dsc = characteristicMydeviceRW.getDescriptor(BleUUIDType.CCCD);//NOTE 必须？？？！！！
//            if (dsc != null) {
//                dsc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
//            }
        }
    }

    void getCharacteristicAndSetNotification_WRITE(BluetoothGatt gatt) {
        if (gatt != null) {
            //NOTE 服务
            BluetoothGattService gattSer = gatt.getService(BleUUIDType.SERVER);
//            if (gattSer == null) {
//                Log.e(LOG_TAG, "setCharacteristics: cannot find service");//新版刚上电,约3秒没数据
//                new Handler().postDelayed(new Runnable() {
//                    @Override
//                    public void run() {
//                        connectBle();
//                    }
//                }, 3000);
//                return;
//            }
            //NOTE 写/读特征口
            characteristicMydeviceW = gattSer.getCharacteristic(BleUUIDType.WRITE);
            if (characteristicMydeviceW == null) {

                Log.i(LOG_TAG, "setCharacteristics: cannot find characteristic" + BleUUIDType.WRITE);
                connectBle();
                return;
            }

//            gatt.setCharacteristicNotification(characteristicMydeviceRW, true);//NOTE 打开主要数据口的通知-magicair
            BluetoothGattDescriptor dsc = characteristicMydeviceW.getDescriptor(BleUUIDType.CCCD);//NOTE 必须？？？！！！
            if (dsc != null) {
                dsc.setValue(BluetoothGattDescriptor.ENABLE_NOTIFICATION_VALUE);
                gatt.writeDescriptor(dsc);//NOTE 写
            }
        }

    }


    /**
     * NOTE 发送数据
     *
     * @param order
     * testSendOptional(fe5a035a005d)->active
     */
    public void sendOrder(byte[] order) {
        // characteristicOrder = gatt.getService(BleUUIDType.SERVER)
        // .getCharacteristic(BleUUIDType.WRITE);
        if (characteristicMydeviceW != null) {
            StringBuilder ss = new StringBuilder();
            for (byte aData : order) {
                // System.out.print(Integer.toHexString(b[i]).toUpperCase()
                // + ",");
                ss.append(Integer.toHexString(aData & 0xFF).toUpperCase()).append(",");

            }
            Log.e("BLE-send", ss.toString());

            // val = order;
            writeWorker.submit(new Task_w(order, characteristicMydeviceW, gatt));
        }
    }

    // NOTE: 发送数据进程
    private class Task_w implements Runnable {

        private final byte[] value;
        private final BluetoothGattCharacteristic characteristicMydeviceRW;
        private final BluetoothGatt gatt;

        public Task_w(byte[] value, BluetoothGattCharacteristic characteristicMydeviceRW, BluetoothGatt gatt) {
            super();
            this.value = value;
            this.characteristicMydeviceRW = characteristicMydeviceRW;
            this.gatt = gatt;
        }

        @Override
        public void run() {
            int count = 0;
            StringBuilder ss = new StringBuilder();
            for (byte anOrder : value) {
                ss.append(Integer.toHexString(anOrder));
            }

            if (!busy) {
                try {

//                    if (ss.toString().startsWith("50531") || ss.toString().startsWith("50632")) {//按键不延时
//                        Thread.sleep(0);
//                        busy = false;
//
//                    } else {
////                        Thread.sleep(0);
//                        Thread.sleep(100);
//                        busy = true;
//
//                    }
                    busy = true;
                    Thread.sleep(100);
                    characteristicMydeviceRW.setValue(value);
                    gatt.writeCharacteristic(characteristicMydeviceRW);
                } catch (InterruptedException ignored) {

                }
                /*   //蓝牙硬件设置为写入无结果返回，会始终返回false
                boolean isSendOut = gatt.writeCharacteristic(characteristicMydeviceRW);
                busy =  !isSendOut;*/
                Log.i(LOG_TAG, "isBusy " + ss);

//                while (busy) {
//                    try {
//                        Thread.sleep(100);
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                    if (count > 3) {
//                        characteristicMydeviceRW.setValue(value);
//                        gatt.writeCharacteristic(characteristicMydeviceRW);
//                        Log.e(LOG_TAG, "发送指令延时了" + ss);
//                        busy = false;
//                    }
//
//                    Log.e(LOG_TAG, "发送指令延时了等待");
//
//                    count++;
//                }

            }
        }

    }

}


