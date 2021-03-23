package face.camera.beans.ble;

import android.content.IntentFilter;

import face.camera.beans.ble.EnumEx.BleActionType;


public class MyIntentFilter {

    /**
     * 广播过滤 类名最好与关键字区别开
     */

    public static IntentFilter makeGattUpdateIntentFilter() {

        final IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(BleActionType.ACTION_GATT_CONNECTED);
        intentFilter.addAction(BleActionType.ACTION_GATT_CONNECTING);
        intentFilter.addAction(BleActionType.ACTION_GATT_DISCONNECTED);

        intentFilter.addAction(BleActionType.ACTION_GATT_DISCONNECTED_NEW);
        // intentFilter
        // .addAction(BLEService.ACTION_GATT_SERVICES_DISCOVERED);
        intentFilter.addAction(BleActionType.ACTION_GATT_NEED_PAIR);
        intentFilter.addAction(BleActionType.ACTION_DATA_AVAILABLE);

        intentFilter.addAction(BleActionType.BROADCAST_RSSI);
        intentFilter.addAction(BleActionType.ACTION_GATT_CODE_ERROR);
        //intentFilter.addAction(BleActionType.BROADCAST_DEVICE_ADDED_AND_USING);old logic
//蓝牙状态监听
//        intentFilter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);


        return intentFilter;
    }
}
