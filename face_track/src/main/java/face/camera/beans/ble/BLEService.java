package face.camera.beans.ble;

import android.app.Activity;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGatt;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattDescriptor;
import android.bluetooth.BluetoothManager;
import android.bluetooth.BluetoothProfile;
import android.bluetooth.le.ScanCallback;
import android.bluetooth.le.ScanFilter;
import android.bluetooth.le.ScanResult;
import android.bluetooth.le.ScanSettings;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.IBinder;
import android.text.TextUtils;
import android.util.Log;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import face.camera.beans.ble.EnumEx.BleActionType;
import face.camera.beans.ble.EnumEx.BleCurrentStateType;
import face.camera.beans.ble.EnumEx.BleDATAType;
import face.camera.beans.ble.EnumEx.BleSavedType;
import face.camera.beans.ble.EnumEx.StartBleRequestCodeType;

import static face.camera.beans.ble.GlobalStatic.tempCode;


/**
 * @author MagicAir-LCh
 * @time 2018/11/23 6:32 PM
 */
public class BLEService extends Service {

    // 蓝牙
    public BluetoothAdapter serBluetoothAdapter;
    public BluetoothManager serBluetoothManager;


    public static final String RSSI_VALUE = "device_rssi";

    private static final String LOG_TAG = "BLENAMEAA";//BLEService.class.getSimpleName();

    public static boolean isNeedPairing = false;

//    private final IBinder binder = new LocalBinder();

    private ExecutorService generalWorker = Executors.newSingleThreadExecutor();
    private ExecutorService generalWorkerGATT = Executors.newSingleThreadExecutor();

    // 用户已使用的设备
//    private Map<String, BLEDeviceContext> usedDevices = new ConcurrentHashMap<>();

    // private Set<String> usedDeviceAddresses;
    private boolean isDeviceClose = false;

    public SerBleGattCallbackAndContext bleDeviceInfoContext;//NOTE 可在绑定后赋值，先占上内存？？

    public BLEService() {//NOTE 服务启动最开始,必执行，所以前面有参构造传的参数在binder取得的实例里被覆盖为空，覆盖前面的有参构造!!!
        L.i(LOG_TAG, "BLEService()");

    }

    private Context contextAct;
//    private ServiceConnection conn;
//    private BroadcastReceiver mGattUpdateReceiver;

    // 非启动服务,实例化占位---
    // NOTE 服务启动后插值，防止serBluetoothAdapter、serBluetoothManager为空(null)？？
    public void initBLEService(Context contextAct) {//实例化？？
        if (this.contextAct == null) {
            this.contextAct = contextAct;
            bleDeviceInfoContext.serContext = contextAct;
        }
        registerBleSwitchBroast();
        serBluetoothManager = (BluetoothManager) contextAct.getSystemService(Context.BLUETOOTH_SERVICE);
        bleDeviceInfoContext.serBluetoothManager = serBluetoothManager;
        serBluetoothAdapter = serBluetoothManager.getAdapter();
        L.i(LOG_TAG, "initBLEService(cccc)" + serBluetoothManager + serBluetoothAdapter);
        if (serBluetoothAdapter == null || !serBluetoothAdapter.isEnabled()) {//蓝牙没打开或不可用
            requestBleON(contextAct);
        } else {
//            Intent serviceIntent = new Intent(this, BLEService.class);
//            Intent serviceIntent = new Intent(contextAct,BLEService.class);
            bleDeviceInfoContext.serBluetoothAdapter = serBluetoothAdapter;
            // NOTE 开始扫描
            startBleScan();
        }

        GlobalStatic.BleCurrentState = BleCurrentStateType.Searching;

        // NOTE this-传服务实例
//        bleDeviceInfoContext = new SerBleGattCallbackAndContext(this,serBluetoothManager,serBluetoothAdapter);


    }

    public static void bleServiceStartAndBind(Context contextAct,
                                              Intent serviceIntent,
                                              ServiceConnection conn,
                                              BroadcastReceiver mGattUpdateReceiver) {
//        serviceIntent = new Intent(contextAct, this.getClass());
        contextAct.startService(serviceIntent);//NOTE 持久化
        contextAct.registerReceiver(mGattUpdateReceiver, MyIntentFilter.makeGattUpdateIntentFilter());
//            contextAct.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);
        contextAct.bindService(serviceIntent, conn, Context.BIND_AUTO_CREATE);//NOTE 在这儿绑定
    }

    public static void bleServiceDestroy(Context contextAct,
                                         ServiceConnection conn,
                                         BroadcastReceiver mGattUpdateReceiver) {
//        serviceIntent = new Intent(contextAct, this.getClass());
        contextAct.unregisterReceiver(mGattUpdateReceiver);
        contextAct.unbindService(conn);
//        contextAct.stopService(serviceIntent);
    }

    private boolean mStateChangeReceiver = false; //广播接受者标识位
    public boolean mBleRequest = false; //蓝牙打开请求标识位

    // NOTE: 监听蓝牙连接状态,只有在过程中改变才能监听到
    private void registerBleSwitchBroast() {
        if (!mStateChangeReceiver) {
            IntentFilter connectedFilter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
            registerReceiver(stateChangeReceiver, connectedFilter);
            mStateChangeReceiver = true;
        }
    }

    // NOTE: 监听蓝牙连接状态,只有在过程中改变才能监听到,连接后取消监听，以断开连接监听代替，避免重复弹框
    private void unregisterBleSwitchBroast() {
        if (mStateChangeReceiver) {
            unregisterReceiver(stateChangeReceiver);
            mStateChangeReceiver = false;
        }
    }

    private BroadcastReceiver stateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            int action = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE,
                    BluetoothAdapter.ERROR);
//            L.i(LOG_TAG, "intent" + intent);

            switch (action) {
                case BluetoothAdapter.STATE_OFF:
                    L.i(LOG_TAG, "BluetoothAdapter.STATE_OFF");
                    requestBleON(contextAct);
                    break;
                case BluetoothAdapter.STATE_TURNING_OFF:
                    L.i(LOG_TAG, "BluetoothAdapter.STATE_TURNING_OFF");

                    break;
                case BluetoothAdapter.STATE_ON:
                    L.i(LOG_TAG, "BluetoothAdapter.STATE_ON");
                    serBluetoothAdapter = serBluetoothManager.getAdapter();
                    bleDeviceInfoContext.serBluetoothAdapter = serBluetoothAdapter;
                    //开始扫描
                    startBleScan();

                    break;
                case BluetoothAdapter.STATE_TURNING_ON:
//                    Log.d("tag", "STATE_TURNING_ON 手机蓝牙正在开启");
                    L.i(LOG_TAG, "BluetoothAdapter.STATE_TURNING_ON");

                    break;
            }
        }
    };


    // NOTE: 扫描蓝牙设备
    private void startBleScan() {

        if (serBluetoothAdapter.isDiscovering()) {//NOTE 多余？？
//            serBluetoothAdapter.getBluetoothLeScanner()
            serBluetoothAdapter.cancelDiscovery();
//            serBluetoothAdapter.stopLeScan(mLeScanCallback);
            serBluetoothAdapter.getBluetoothLeScanner().stopScan(newScanCallback);
//            L.i(LOG_TAG, "newScanCallback-isDiscovering");
        }
//        if (newScanCallback != null) {
//            serBluetoothAdapter.getBluetoothLeScanner().stopScan(newScanCallback);
//            L.i(LOG_TAG, "newScanCallback-stopScan+" + serBluetoothAdapter.getScanMode());
//        }
        new Thread(new Runnable() {
            @Override
            public void run() {
//                serBluetoothAdapter.startLeScan(mLeScanCallback);

//                ScanSettings scanSettings = new ScanSettings.Builder()
//                        .setScanMode(ScanSettings.SCAN_MODE_BALANCED)//NOTE 自带延时
//                        .build();
                ScanSettings scanSettings = new ScanSettings.Builder()
                        .setScanMode(ScanSettings.SCAN_MODE_LOW_LATENCY)
                        .build();
                ScanFilter filter = new ScanFilter.Builder().build();
                List<ScanFilter> list = new ArrayList<ScanFilter>(1);
                list.add(filter);
//                Log.i(LOG_TAG, "newScanCallback-Thread开启" + serBluetoothAdapter + "-newScanCallback-" + newScanCallback);

//                serBluetoothAdapter.getBluetoothLeScanner().startScan(newScanCallback);
//                serBluetoothAdapter.getBluetoothLeScanner().flushPendingScanResults();
                serBluetoothAdapter.getBluetoothLeScanner().startScan(null, scanSettings, newScanCallback);

            }
        }).start();

    }

    private ScanCallback newScanCallback = new ScanCallback() {
        @Override
        public void onScanResult(int callbackType, ScanResult result) {
            super.onScanResult(callbackType, result);
            //  if (result.getDevice().getType() == BluetoothDevice.DEVICE_TYPE_LE
//                    || result.getDevice().getType() == BluetoothDevice.DEVICE_TYPE_CLASSIC)
//            Log.i(LOG_TAG, "BLE-newScanCallback-扫描结果" + result + "-name-" + result.getDevice());
//            Log.i(LOG_TAG, "BLE-newScanCallback-扫描结果名称" + "-name-" + result.getDevice().getName());
//            回调value配对:fe5a03cfaf81>>Optional(Optional(<e7fecfaf 2705e702>))--ios
//            02:E7:05:27:AF:CF
//            指令4位值->cf,5位置最低位：af

            if (result.getDevice() != null) {
                if (!TextUtils.isEmpty(result.getDevice().getName())) {

                    // unusedDevices.clear();
                    //NOTE 只有在未连接状态下才列表弹框，但仍扫描---iOS
                    if (getScanBleDevice != null && GlobalStatic.BleCurrentState <= BleCurrentStateType.Disconnected) {
                        getScanBleDevice.bleScanResults(result.getDevice(), result.getRssi());
                    }

                }

            }

        }

        //批量结果，需ScanSettings设置
        @Override
        public void onBatchScanResults(List<ScanResult> results) {
            super.onBatchScanResults(results);
//            Log.i(LOG_TAG, "newScanCallback-扫描结果列表" + results + "-name-" + results.size());

        }

        @Override
        public void onScanFailed(int errorCode) {
            super.onScanFailed(errorCode);
            if (errorCode == ScanCallback.SCAN_FAILED_ALREADY_STARTED) {
//                serBluetoothAdapter.getBluetoothLeScanner().flushPendingScanResults(this);
                serBluetoothAdapter.getBluetoothLeScanner().stopScan(this);//NOTE 设置权限返回时，不先停止一下，会搜索不到设备
//                serBluetoothAdapter.getBluetoothLeScanner().s(this);
                startBleScan();
            }
//            Log.i(LOG_TAG, "newScanCallback-扫描结果onScanFailed" + "-failed-" + errorCode);

        }
    };


    // NOTE: 停止扫描蓝牙设备
    private void stopBleScan() {
        if (serBluetoothAdapter != null && serBluetoothAdapter.getBluetoothLeScanner() != null) {
//            serBluetoothAdapter.stopLeScan(mLeScanCallback);

            serBluetoothAdapter.getBluetoothLeScanner().stopScan(newScanCallback);
        }
    }

    public interface GetScanBleDevice {

        void bleScanResults(BluetoothDevice device, int rssi);
    }

    private GetScanBleDevice getScanBleDevice;

    public void setGetScanBleDevice(GetScanBleDevice getScanBleDevice) {
        this.getScanBleDevice = getScanBleDevice;
    }

    //NOTE 蓝牙设备扫描回调-api 21 废弃
//    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
//
//        @Override
//        public void onLeScan(final BluetoothDevice myDevice, int rssi, byte[] scanRecord) {
//
//            //myDevice.getType() == BluetoothDevice.DEVICE_TYPE_DUAL、myDevice.getType() == BluetoothDevice.DEVICE_TYPE_LE
//            if (!TextUtils.isEmpty(myDevice.getName())) {
//
//                // unusedDevices.clear();
////                Log.i(LOG_TAG, "mLeScanCallback-扫描结果" + myDevice.getAddress() + "-name-" + myDevice.getName());
//                // 先判断一下设备名再保存地址（多个设备）
//                if (getScanBleDevice != null) {
//                    getScanBleDevice.bleScanResults(myDevice, rssi);
//                }
//
//            }
//        }
//    };


    @Override
    public void onCreate() {//只执行一次
        super.onCreate();
        bleDeviceInfoContext = new SerBleGattCallbackAndContext();// NOTE 实例化占位
        // res=getResources();
        L.i(LOG_TAG, "onCreate");

    }


    public void requestBleON(Context contextAct) {
        if (mBleRequest) {
            mBleRequest = false;
            return;
        }
        Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
        // enableBtIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        // startActivity(enableBtIntent);
//        sendBroadcast(enableBtIntent);
//        contextAct.startActivity(enableBtIntent);
//        contextAct.startActivityForResult(enableBtIntent, StartBleRequestCodeType.STARTBLE);
        ((Activity) contextAct).startActivityForResult(enableBtIntent, StartBleRequestCodeType.STARTBLE, null);
        mBleRequest = true;
//        ((FragmentActivity)contextAct).onActivityReenter();
        L.i(LOG_TAG, "requestBleON-强制打开蓝牙");
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // loadPersistedDevices();
        L.i(LOG_TAG, "onStartCommand" + startId);

        return START_STICKY;
    }


    // NOTE: start - stop，stop一定停止start,高优先级;onBind - onUnbind,成对出现？
// NOTE 绑定服务
    @Override
    public IBinder onBind(Intent intent) {
        // 连接到所有设备
//        EventBus.getDefault().register(this);
        L.i(LOG_TAG, "onBind" + intent);

        return new LocalBinder(this);
    }


    @Override
    public boolean onUnbind(Intent intent) {
        // 断开到所有设备的连接
//        EventBus.getDefault().unregister(this);
        L.i(LOG_TAG, "onUnbind" + intent);
        stopBleScan();
        return super.onUnbind(intent);
    }

    @Override
    public void onDestroy() {
        Log.i(LOG_TAG, "onDestroy");
        // 关闭所有设备
//        closeAllGatt();
//        if (myDevice != null) {
//            myDevice.disconnect();
//        }
        super.onDestroy();
    }

    // NOTE: 连接设备 device,必在服务启动后？
    public void toConnectDevice(BluetoothDevice device) {


//        SerBleGattCallbackAndContext deviceInfo = new SerBleGattCallbackAndContext(device);

//        SerBleGattCallbackAndContext deviceInfo = new SerBleGattCallbackAndContext()
//        LoadingViewUtil.getINS(contextAct).setNewText(getString(R.string.progress_dialog_connecting));
        if (bleDeviceInfoContext != null) {
            bleDeviceInfoContext.bluetoothDevice = device;//NOTE 先赋值
            bleDeviceInfoContext.connectBle();
            // persistUsedDevice(context.address);
//        context.connectBle(true); //

//            if (device.getName() != null) {
//                saveUsedDeviceName(device.getName());
//                Log.i(LOG_TAG, "useDeviceNEWBLEgetName: " + device.getName());
//
//            }
            Log.i(LOG_TAG, "toConnectDevice: " + device.getName());

//            FileUtils.putValueInSharedPreference(DEVICE_NUMBER_KEY, "FFFFFFFF");
//        persistNewDevices(context.address);
            GlobalStatic.BleCurrentState = BleCurrentStateType.Connecting;
            bleDeviceInfoContext.broadcastBleCurrentState(BleActionType.ACTION_GATT_CONNECTING);

        }
        // persistUsedDevice();
    }


    // NOTE: 保存连接设备的地址
    public void persistUsedDevice(String address) {

        Log.i(LOG_TAG, "更新-已使用的设备地址：" + address);
        //上下文是主activity，存取要一致
        Preferences.savePreferences(contextAct,
                BleSavedType.ADDRESS.SharedKey,
                BleSavedType.ADDRESS.ValueKey, address);

        // notifyUsedDeviceChanged();
    }

    public void savePWD(String pwd) {
        Preferences.savePreferences(contextAct,
                BleSavedType.PairPWD.SharedName,
                BleSavedType.PairPWD.SharedKey, pwd);
    }


    // NOTE: 保存新闯入设备的地址
    public void persistNewDevices(String address) {

        //上下文是主activity
        Preferences.savePreferences(contextAct,
                BleSavedType.ADDRESS_New.SharedKey,
                BleSavedType.ADDRESS_New.ValueKey, address);

        Log.i(LOG_TAG, "更新-新设备闯入：" + address);
        // notifyUsedDeviceChanged();
    }

    // NOTE: 保存连接设备的名称
    public void saveUsedDeviceName(String name) {
        Log.i(LOG_TAG, "useDeviceNEWBLE: " + name);
        Preferences.savePreferences(contextAct,
                BleSavedType.Name.SharedKey,
                BleSavedType.Name.ValueKey, name);

    }


//    public Map<String, BLEDeviceContext> getUsedDevices() {
//
//        return usedDevices;
//    }


//    private void closeAllGatt() {
//
//        for (BLEDeviceContext dc : usedDevices.values()) {
//            dc.closeGatt();
//        }
//    }


    private long t1;

    // NOTE:  蓝牙响应的各种回调（数据发送成功、数据读取成功、蓝牙连接成功或失败、获取蓝牙service、UUID成功。。。）,回调都是异步的？
    public class SerBleGattCallbackAndContext extends BLEDeviceContext {

        private Timer deviceCloseTimer = null;


        //NOTE 无参构造默认实现

//        public SerBleGattCallbackAndContext(Context context,
//                                            BluetoothManager bleManager,
//                                            BluetoothAdapter bleAdapter) {
//            super(context, bleManager, bleAdapter);
//        }

        //        public SerBleGattCallbackAndContext(Context context,
//                                            BluetoothDevice bluetoothDevice,
//                                            BluetoothManager bleManager,
//                                            BluetoothAdapter bleAdapter) {
//            super(context, bluetoothDevice, bleManager, bleAdapter);
//        }

        // NOTE: 蓝牙连接状态改变
        @Override
        public void onConnectionStateChange(final BluetoothGatt gatt, int status, int newState) {

            if (newState == BluetoothProfile.STATE_CONNECTED) {
                unregisterBleSwitchBroast();//NOTE 取消蓝牙打开与否状态监听
                System.out.println("BLE-onConnectionStateChange: connected | " + bluetoothDevice);
                t1 = System.currentTimeMillis();
                Log.e("BLE-连接成功", t1 + "");

                // 不在回调线程中调用
                generalWorker.submit(new Runnable() {

                    @Override
                    public void run() {
                        //NOTE 连接成功去发现服务
                        boolean discoverServicesResult = gatt.discoverServices();
                        // 发现服务失败，判定为连接失败，仍在扫描
                        if (!discoverServicesResult) {

                            // broadcastUpdateA(ACTION_GATT_NOT_CLEAR_PAIR);
                            GlobalStatic.BleCurrentState = BleCurrentStateType.Disconnected;

                            broadcastBleCurrentState(BleActionType.ACTION_GATT_DISCONNECTED);

                        } else {
                            GlobalStatic.BleCurrentState = BleCurrentStateType.Connected;
                            //NOTE 连接成功就记录为原设备
//                            persistNewDevices(gatt.getDevice().getAddress());
                            persistUsedDevice(gatt.getDevice().getAddress());

                            //NOTE 记录密码
//                            savePWD(tempCode);

//                            broadcastBleCurrentState(BleActionType.ACTION_GATT_CONNECTED);//拿到读写接口才真正连接成功
                            stopBleScan();
                        }

                    }
                });
//                broadcastUpdateA(BleActionType.ACTION_GATT_CONNECTED);
            } else if (newState == BluetoothProfile.STATE_DISCONNECTED) {
                System.out.println("BLE-onConnectionStateChange: disconnected | " + bluetoothDevice);
                long t2 = System.currentTimeMillis() - t1;
                GlobalStatic.BleCurrentState = BleCurrentStateType.Disconnected;
                //NOTE 密码错误

                if ((t2 / 1000) < 16 && (t2 / 1000) >= 9) {
                    tempCode = BleSavedType.PairPWD.DefaultValue;
                    savePWD(tempCode);
                    Log.e("BLE-连接断开密码重置", t2 + "");
//                    ToastUtils.showMidToast(getApplicationContext(), "密码错误");
                    broadcastBleCurrentState(BleActionType.ACTION_GATT_CODE_ERROR);
                }

                Log.e("BLE-连接断开", t2 + "");
                // 不在回调线程中调用
                generalWorker.submit(new Runnable() {

                    @Override
                    public void run() {
                        disconnect();
                        //NOTE 需配对手动断开，也走断开连接，综合判断
                        if (isNeedPairing) {
                            System.out.println("配对特征需要配对2" + "dd");
//                    broadcastUpdateA(ACTION_GATT_NOT_CLEAR_PAIR);
                            broadcastBleCurrentState(BleActionType.ACTION_GATT_NEED_PAIR);
                        } else {
//                   cancelDeviceCloseTimer();

//                            if (isDeviceClose) {//NOTE 延时主动断开后，过滤掉系统30s延时自带回调
//                                isDeviceClose = false;
//                                Log.e(LOG_TAG, "onConnectionStateChange: 系统30s延时自带回调: " + " | " + bluetoothDevice);
//
//                                return;
//                            }
                            broadcastBleCurrentState(BleActionType.ACTION_GATT_DISCONNECTED);
                        }
                    }
                });
                // 连接失败消息
//                BluetoothDevice device = serBluetoothAdapter.getRemoteDevice(deviceContext.address);


            } else {
                // unknown
                // broadcastUpdateA(ACTION_GATT_NOT_CLEAR_PAIR);
                Log.e(LOG_TAG, "onConnectionStateChange: unknown: " + newState + " | " + bluetoothDevice);
            }

        }

        @Override
        public void onDescriptorRead(BluetoothGatt gatt, BluetoothGattDescriptor descriptor, int status) {
            super.onDescriptorRead(gatt, descriptor, status);

            L.d("onDescriptorRead", "BluetoothGattDescriptor" + descriptor);
        }

        @Override
        public void onServicesDiscovered(final BluetoothGatt gatt, int status) {

            if (status == BluetoothGatt.GATT_SUCCESS) {
                 Log.i(LOG_TAG, "onServicesDiscovered： success | "
                 + status);

                Runnable writeRun = new Runnable() {
                    @Override
                    public void run() {
                        getCharacteristicAndSetNotification_WRITE(gatt);
                        Log.i(LOG_TAG, "onServicesDiscovered： writeRun");
                    }
                };
                Runnable readRun = new Runnable() {
                    @Override
                    public void run() {

                        try {

                            Thread.sleep(300);
                            getCharacteristicAndSetNotification_READ(gatt);//NOTE 获取到特征值真正意义上的连接成功
                            broadcastBleCurrentState(BleActionType.ACTION_GATT_CONNECTED);
                            Log.i(LOG_TAG, "onServicesDiscovered： readRun");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                };


                //NOTE 不在回调线程中调用
                generalWorker.submit(writeRun);
                generalWorker.submit(readRun);

            } else {

                //NOTE 连接失败消息,原因：配对信息没清除---最先回调.。。。HTC不回调
                // broadcastUpdateA(ACTION_GATT_NOT_CLEAR_PAIR);
                Log.e(LOG_TAG, "onServicesDiscovered： fail " + status + "| " + bluetoothDevice);
            }

        }

        /**
         * * Callback reporting the result of a characteristic read operation.
         * *
         * * @param gatt GATT client invoked {@link BluetoothGatt#readCharacteristic}
         * * @param characteristic Characteristic that was read from the associated
         * *                       remote device.
         * * @param status {@link BluetoothGatt#GATT_SUCCESS} if the read operation
         * *               was completed successfully.
         */

        @Override
        public void onCharacteristicRead(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                         int status) {


            if (status == BluetoothGatt.GATT_SUCCESS) {
                // Log.i(LOG_TAG, characteristic.toString());

//                broadcastUpdateOpen(ACTION_DATA_AVAILABLE, characteristic, deviceContext.name, deviceContext.address,
//                        deviceContext.gatt);
                Log.e("返回", "onCharacteristicRead");

                broadcastBleByteData(characteristic, gatt);
            }
        }

        //NOTE 信号
        @Override
        public void onReadRemoteRssi(BluetoothGatt gatt, int rssi, int status) {

            L.i("信号回调", "值" + rssi);
            Intent intent_rssi = new Intent(BleActionType.BROADCAST_RSSI);
            intent_rssi.putExtra(BLEService.RSSI_VALUE, rssi);
            if (status == BluetoothGatt.GATT_SUCCESS) {
                sendBroadcast(intent_rssi);
            }
//            busy = false;// ?

        }

        @Override
        public void onCharacteristicWrite(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic,
                                          int status) {
            if (status == BluetoothGatt.GATT_SUCCESS) {
                Log.i(LOG_TAG, "BLE-onCharacteristicWrite: success | " +
                        status);
//                busy = false;
                if (bleDeviceInfoContext != null) {
                    bleDeviceInfoContext.busy = false;//写出去了，但没有成功与否的回调，需硬件设置
                }

                String co = new String(characteristic.getValue(), StandardCharsets.US_ASCII);
//                if (co.equals(tempCode)) {
//                    savePWD(co);
//                    try {
//                        Thread.sleep(300);
//                        sendOrder(S_IN);//进入时指令
//                        Log.i(LOG_TAG, "sendOrder(S_IN):" + co);
//
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }


            } else {
                // 连接失败消息
                // intentActionW = ACTION_GATT_NOT_CLEAR_PAIR;
                //
                // broadcastUpdateA(intentActionW);
                // deviceContext.usable = false;
                Log.e(LOG_TAG, "onCharacteristicWrite: fail " + status + " | " + bluetoothDevice);
            }

//            busy = false;

        }

        /**
         * * <p>Once notifications are enabled for a characteristic, a
         * * {NOTE BluetoothGattCallback#onCharacteristicChanged} callback will be triggered
         * *  if the remote device indicates that the given characteristic
         * * has changed.
         *
         * @param gatt
         * @param characteristic 有通知的口，此为主数据口？？
         */

        @Override
        public void onCharacteristicChanged(final BluetoothGatt gatt, BluetoothGattCharacteristic characteristic) {
            if (gatt != null) {
                gatt.readRemoteRssi();// 监听信号强度
//                broadcastUpdateOpen(ACTION_DATA_AVAILABLE, characteristic, deviceContext.name, deviceContext.address,
//                        deviceContext.gatt);
                Log.e("返回", "onCharacteristicChanged");
                broadcastBleByteData(characteristic, gatt);
            }
        }

        //NOTE 广播连接状态
        void broadcastBleCurrentState(final String action) {
//            if(action.equals(BleActionType.ACTION_GATT_DISCONNECTED)){
//                cancelDeviceCloseTimer();
//            }
//            if(action.equals(BleActionType.ACTION_GATT_CONNECTED)){//NOTE 在这儿开启心跳包监听
            if (action.equals(BleActionType.ACTION_GATT_CONNECTING) || action.equals(BleActionType.ACTION_GATT_CONNECTED)) {//NOTE 在这儿开启心跳包监听

                updateDeviceCloseTimer();
            } else {
                cancelDeviceCloseTimer();
            }
            final Intent intent = new Intent(action);
            sendBroadcast(intent);
        }

        public void cancelDeviceCloseTimer() {
            if (deviceCloseTimer != null) {
                deviceCloseTimer.cancel();
                deviceCloseTimer = null;
            }
        }

        public void updateDeviceCloseTimer() {
            if (deviceCloseTimer != null) {
                cancelDeviceCloseTimer();
                isDeviceClose = false;
            }
            deviceCloseTimer = new Timer();

            deviceCloseTimer.schedule(new TimerTask() {
                @Override
                public void run() {
//                    Log.e("as_receive_data心跳包有否", BleCurrentState + "");
//                    isDeviceClose = false;

                    if (isDeviceClose) {
                        Log.e("BLE-as_receive心跳包有否", "无心跳");
//                            broadcastBleCurrentState(BleActionType.ACTION_GATT_DISCONNECTED);
                        disconnect();
                        broadcastBleCurrentState(BleActionType.ACTION_GATT_DISCONNECTED);
                        cancelDeviceCloseTimer();
                    } else {
                        Log.e("BLE-as_receive心跳包有否", "有心跳");
//                            broadcastUpdateA(ACTION_GATT_DISCONNECTED);

                    }

//                    isDeviceClose = true;
                }
            }, 5000, 5000);//10s检测一次,period上下5s


        }

        private void broadcastBleByteData(final BluetoothGattCharacteristic characteristic,
                                          final BluetoothGatt gatt) {

            //NOTE 获取数据
            final byte[] data = characteristic.getValue();

            StringBuilder str = new StringBuilder();
            if (data != null && data.length > 0) {
                for (byte aData : data) {
                    // System.out.print(Integer.toHexString(b[i]).toUpperCase()
                    // + ",");
                    str.append(Integer.toHexString(aData & 0xFF).toUpperCase()).append(",");

                }
                isDeviceClose = false;
//                    updateDeviceCloseTimer(); //old logic
                Log.e("BLE-bleGet心跳包", str.toString());
                Intent intent = new Intent(BleActionType.ACTION_DATA_AVAILABLE);

//                intent.putExtra(BleDATAType.EXTRA_DATA, data);
                intent.putExtra(BleDATAType.EXTRA_DATA, str.toString());

                // BtnMainActivity.sendMessage(data, 5);
                sendBroadcast(intent);
            }


        }

    }


    // 清除蓝牙配对信息（一个）
//    public void unpairDevice(BluetoothDevice device) {
//        try {
//            Method m = device.getClass().getMethod("removeBond", (Class[]) null);
//            m.invoke(device, (Object[]) null);
//        } catch (Exception e) {
//            Log.e("反射", e.getMessage());
//        }
//    }


//    // 中间人，NOTE 因为这个所以绑定时会再区调无餐构造？？？
//    public class LocalBinder extends Binder {
//
//        public BLEService getService() {
//            // 返回Service实例，让客户端直接调用Service的方法
//            return BLEService.this;
//        }
//    }

    public class LocalBinder extends Binder {
        private BLEService self;

        public LocalBinder(BLEService self) {
            this.self = self;
        }

        public BLEService getService() {
            //NOTE 返回Service实例，让客户端直接调用Service的方法
            return self;
        }
    }


}
