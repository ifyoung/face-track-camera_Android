package face.camera.beans.base.activity;

import android.Manifest;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.RuntimeABI;
import com.luck.picture.lib.tools.StaticFunction;
import com.luck.picture.lib.tools.ToastUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import butterknife.ButterKnife;
import face.camera.beans.R;
import face.camera.beans.arc.common.Constants;
import face.camera.beans.base.utils.L;
import face.camera.beans.ble.BLEService;
import face.camera.beans.ble.EnumEx.BleActionType;
import face.camera.beans.ble.EnumEx.BleCurrentStateType;
import face.camera.beans.ble.EnumEx.BleDATAType;
import face.camera.beans.ble.EnumEx.BleFakeAddressType;
import face.camera.beans.ble.EnumEx.BleHandleDataType;
import face.camera.beans.ble.EnumEx.BleSavedType;
import face.camera.beans.ble.EnumEx.DataProtocol;
import face.camera.beans.ble.GlobalStatic;
import face.camera.beans.ble.Preferences;
import face.camera.beans.ble.SendMessage;
import face.camera.beans.dialog.DialogPrivacy.DialogPrivacy;
import face.camera.beans.net.UpdateManager;
import face.camera.beans.record.RecorderActivity;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static face.camera.beans.ble.GlobalStatic.BleCurrentState;
import static face.camera.beans.ble.GlobalStatic.ISDeviceRight;

/**
 * description:
 * Created by aserbao on 2018/5/5.
 */


public abstract class BaseActivity extends AppCompatActivity {


    public final static int QR_CAMERA_PERMISSIN = 200;
    public final static int RECORD_PERMISSION = 206;
    public final static int ALERT_PERMISSION = 207;
    public final static int CAMERA_PERMISSION_REQUEST_CODE = 231;
    public final static int STORAGE_PERMISSION_REQUEST_CODE = 232;

    private final String[] BASIC_PERMISSIONS = new String[]{
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.RECORD_AUDIO,
            Manifest.permission.READ_PHONE_STATE,//????????????
            Manifest.permission.ACCESS_COARSE_LOCATION,//????????????
            Manifest.permission.CAMERA,

    };
    //    // ??????
    public static BLEService bleService;


    public static boolean threadAlive = false;
    public static boolean isReCon = false;

    public static BLEService.SerBleGattCallbackAndContext myDevice;

//    private static List<DeviceAndRISS> deviceNames_ls = new ArrayList<>();

    public static List<BluetoothDevice> unusedDevices = Collections.synchronizedList(new ArrayList<BluetoothDevice>());
    public static Map<String, String> mapRISS = new HashMap<>();

    public Handler hostDataHandler;
    public SoundPool soundPool;
    public int voiceId = 0;

    public interface OnCameraOkListener {
        void cameraOk();
    }

    public OnCameraOkListener cameraOkListenerVar;

    private static final String TAG = "BaseActivity";

    public UpdateManager updateManager;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        startRequestPermission();

//        updateManager = new UpdateManager(getApplicationContext(), 2);
        updateManager = new UpdateManager(BaseActivity.this, 2);
//        StatusBarUtil.transparencyBar(this);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);//??????????????????
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            WindowManager.LayoutParams attributes = getWindow().getAttributes();
            attributes.systemUiVisibility = View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
            getWindow().setAttributes(attributes);
        }

        // Activity???????????????????????????????????????
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LOCKED);

        setContentView(setLayoutId());
        ButterKnife.bind(this);
        initView();
        if (isPermission(BASIC_PERMISSIONS)) {
//            initView();


        }
//        setCameraOkListenerVar(new OnCameraOkListener() {//??????????????????
//            @Override
//            public void cameraOk() {
//
//            }
//        });

        //sdk??????21???SoundPool ??????????????????
        if (Build.VERSION.SDK_INT >= 21) {
            SoundPool.Builder builder = new SoundPool.Builder();
            //??????????????????????????????,
            builder.setMaxStreams(1);
            //AudioAttributes??????????????????????????????????????????
            AudioAttributes.Builder attrBuilder = new AudioAttributes.Builder();
            //?????????????????????????????????
            attrBuilder.setLegacyStreamType(AudioManager.STREAM_MUSIC);
            //????????????AudioAttributes
            builder.setAudioAttributes(attrBuilder.build());
            soundPool = builder.build();
        } else {
            /**
             * ??????????????????int maxStreams???SoundPool???????????????????????????
             * ??????????????????int streamType???AudioManager???????????????????????????
             *??????????????????int srcQuality????????????????????????????????? ????????????????????? ??????0??????????????????
             */
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        }

        voiceId = soundPool.load(getApplicationContext(), R.raw.di, 1);
        //?????????????????????????????????????????????????????????
        soundPool.setOnLoadCompleteListener(new SoundPool.OnLoadCompleteListener() {
            @Override
            public void onLoadComplete(SoundPool soundPool, int sampleId, int status) {
                if (status == 0) {
                    if (Preferences.getPreferences(getApplicationContext(), "sound", "sound") > 0) {
                        isSoundLoaded = true;
                    }
                    //???????????????soundID
                    //???????????????leftVolume???????????????????????????= 0.0???1.0???
                    //???????????????rightVolume???????????????????????????= 0.0???1.0???
                    //???????????????priority ???????????????????????????????????????????????????????????????????????????????????????????????????SoundPool??????????????????
                    //???????????????loop ??????????????????????????????0?????????????????????-1????????????????????????????????????loop+1???
                    //??????????????? rate???????????????????????????0.5-2.0(0.5??????????????????1.0??????????????????2.0???????????????)
//                    soundPool.play(voiceId, 1, 1, 1, 0, 1);
                }
            }
        });

        if (Preferences.getPreferences(getApplicationContext(), "privacy", "privacy") != 0) {
            DialogPrivacy.showPrivacy(this, false);
        }
        GlobalStatic.ISARCACTIVE = Preferences.getPreferences(getApplicationContext(), "ARC", "ARC") != 1;
    }

    @Override
    protected void onResume() {
        super.onResume();
        StaticFunction.reloadLanguageAction(this);

        if (Preferences.getPreferences(getApplicationContext(), "sound", "sound") == 0) {
            isSoundLoaded = false;
        } else {
            isSoundLoaded = true;
        }

    }

    // NOTE:  doSearchBle
  public   void doSearchBle() {
        unusedDevices.clear();
        runStopScanTimerVoid();//NOTE
        if (bleService != null) {
            bleService.initBLEService(this);
        }
    }

    public boolean isSoundLoaded = false;
    // NOTE: ???????????????
    private Timer stopScanTimer;

    void cleanScanTimer() {

        if (stopScanTimer != null) {
            stopScanTimer.cancel();
            stopScanTimer = null;
        }
    }


    // NOTE:  toConnectBle
    void toConnectBle(BluetoothDevice device) {
//        DeviceListDialog.dismissDialog();
        if (bleService != null) {
            bleService.toConnectDevice(device);
        }
    }


    private int flagNoneTwice = 0;


    void runStopScanTimerVoid() {

        cleanScanTimer();
        stopScanTimer = new Timer();
        stopScanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                //NOTE ????????????
                if (BleCurrentState == BleCurrentStateType.Connecting
                        || BleCurrentState == BleCurrentStateType.Connected) {
                    cleanScanTimer();//????????????
                    return;
                }

                final String usedAddress = Preferences.getPreferences(
                        getApplicationContext(),
                        BleSavedType.ADDRESS.SharedKey,
                        BleSavedType.ADDRESS.ValueKey,
                        BleFakeAddressType.B);

//                Looper.prepareMainLooper();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        for (int i = 0; i < unusedDevices.size(); i++) {
                            if (usedAddress.equals(unusedDevices.get(i).getAddress())) {

                                return;
                            }
                        }

                        if (unusedDevices.size() > 0) {
                            flagNoneTwice = 0;
                        } else {
                            flagNoneTwice = flagNoneTwice + 1;
                        }

                        List<BluetoothDevice> t = unusedDevices;
                        if ((flagNoneTwice > 2 || flagNoneTwice <= 0)) {//??????????????????????????????
//                            showDeviceChooser();
                            flagNoneTwice = 0;
                        } else {
                            runStopScanTimerVoid();
                        }

                    }
                });
//                Looper.loop();// ??????loop?????????????????????????????????

            }
        }, 2000);//NOTE ??????2??????????????????


    }


    private Timer holdConnectOrder;

    protected void runHoldConnect() {
        if (holdConnectOrder != null) {
            holdConnectOrder.cancel();
            holdConnectOrder = null;
        }
        holdConnectOrder = new Timer();
        holdConnectOrder.schedule(new TimerTask() {
            @Override
            public void run() {
                if (myDevice != null) {
                    myDevice.sendOrder(DataProtocol.Order_Active.SET.Active());
                }

            }
        }, 0, 3000);//3???


    }


    private final BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action != null) {
                L.d("BLE-????????????", action);

            }

            if (BleActionType.ACTION_DATA_AVAILABLE.equals(action)) {

//                SendMessage.sendMessage(hostDataHandler, intent.getByteArrayExtra(BleDATAType.EXTRA_DATA), BleHandleDataType.ConnectData);

                SendMessage.sendMessage(hostDataHandler, intent.getStringExtra(BleDATAType.EXTRA_DATA), BleHandleDataType.ConnectData);

            } else if (BleActionType.ACTION_GATT_CONNECTED.equals(action)) {
                threadAlive = true;
                SendMessage.sendMessage(hostDataHandler, BleActionType.ACTION_GATT_CONNECTED, BleHandleDataType.Connected);

            } else if (BleActionType.ACTION_GATT_DISCONNECTED.equals(action)) {
                threadAlive = false;
//                isSendPaired = false;//??????
//                bleService.initBLEService(BtnMainActivity.this);//NOTE ??????????????????
                SendMessage.sendMessage(hostDataHandler, BleActionType.ACTION_GATT_DISCONNECTED, BleHandleDataType.Disconnected);

            } else if (BleActionType.ACTION_GATT_DISCONNECTED_NEW.equals(intent.getAction()) && Preferences.getBLENAMENew(BaseActivity.this)) {

            } else if (BleActionType.ACTION_GATT_NEED_PAIR.equals(action)) {

            } else if (BleActionType.BROADCAST_RSSI.endsWith(intent.getAction())) {

                SendMessage.sendMessage(hostDataHandler, intent.getIntExtra(BLEService.RSSI_VALUE, 0), BleHandleDataType.RssiValue);
            } else if (BleActionType.ACTION_GATT_CODE_ERROR.equals(action)) {

            }

        }
    };

    //NOTE ??????APP???????????????????????????????????????
    private ServiceConnection serviceConnection = new ServiceConnection() {

        @Override
        public void onServiceConnected(ComponentName className, IBinder iBinder) {
            BLEService.LocalBinder binder = (BLEService.LocalBinder) iBinder;
            bleService = binder.getService(); // ??????Binder???????????????
//            bleService.initBLEService(BtnMainActivity.this);
            myDevice = bleService.bleDeviceInfoContext;
            doSearchBle();
//            saveGuideFlag();
            bleService.setGetScanBleDevice(new BLEService.GetScanBleDevice() {
                @Override
                public void bleScanResults(final BluetoothDevice device, int rssi) {
                    Log.e("BLE-????????????", "??????????????????" + device.getAddress() + "-" + device.getName() + "-" + rssi);
                    String usedAddress = Preferences.getPreferences(
                            getApplicationContext(),
                            BleSavedType.ADDRESS.SharedKey,
                            BleSavedType.ADDRESS.ValueKey,
                            BleFakeAddressType.B);
                    String newAddress = Preferences.getPreferences(
                            getApplicationContext(),
                            BleSavedType.ADDRESS_New.SharedKey,
                            BleSavedType.ADDRESS_New.ValueKey,
                            BleFakeAddressType.A);

                    //NOTE ????????????????????????????????????????????????
                    if (rssi > -89) {


                        // ??????
                        if (!unusedDevices.contains(device)) {
//                        if (!unusedDevices.contains(device)) {
                            unusedDevices.add(device);
                            if (device.getName().toLowerCase().contains("x06")) {
                                toConnectBle(device);//??????
                            }

                        }
                    }


                    if (TextUtils.isEmpty(device.getName())) {
                        mapRISS.put(device.getAddress(), String.valueOf(rssi));
                    } else {
                        mapRISS.put(device.getName(), String.valueOf(rssi));
                    }


                }
            });

//            autoConnect();
            Log.e("BLE-????????????", "??????????????????!");
        }

        @Override
        public void onServiceDisconnected(ComponentName className) {
            Log.e("BLE-????????????", "????????????????????????!");
        }
    };

    public void setCameraOkListenerVar(OnCameraOkListener cameraOkListenerVar) {
        this.cameraOkListenerVar = cameraOkListenerVar;
    }


    public boolean isPermission(String... parameter) {
        boolean isGRANTED = true;

        for (String s : parameter) {
            if (ContextCompat.checkSelfPermission(this, s) != PackageManager.PERMISSION_GRANTED) {
                isGRANTED = false;
                break;
            }
        }
        ActivityCompat.requestPermissions(this,
                parameter,
                CAMERA_PERMISSION_REQUEST_CODE);

        return isGRANTED;
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        doNext(requestCode, grantResults);
    }

    //    ????????????????????????requestCode???grantResults(????????????)????????????????????????
    private void doNext(int requestCode, int[] grantResults) {

        boolean isP = true;
        for (int p : grantResults) {
            if (p != PackageManager.PERMISSION_GRANTED) {
                L.e("????????????" + p);
                isP = false;
            }
        }
        if (isP) {
//                    callCamera();
            if (cameraOkListenerVar != null) {
                cameraOkListenerVar.cameraOk();
                //??????????????????
                BLEService.bleServiceStartAndBind(BaseActivity.this,
                        new Intent(BaseActivity.this, BLEService.class),
                        serviceConnection, mGattUpdateReceiver);
//                        activeEngine();
            }
        } else {
            Toast.makeText(this, R.string.str_permission_tip, Toast.LENGTH_LONG).show();
        }
        switch (requestCode) {

            case CAMERA_PERMISSION_REQUEST_CODE:

                break;
            case STORAGE_PERMISSION_REQUEST_CODE:
                if (isP) {
                    L.e("????????????/??????");
//                    callAlbum();
//                    checkUpdateSer();
                } else {
                    Toast.makeText(this, "??????????????????????????????", Toast.LENGTH_LONG).show();
                }
                break;
            case QR_CAMERA_PERMISSIN:
                L.e("?????????");
                if (isP) {
//                    toQR();

//                    mainWebFragment.toQR();
                } else {
                    Toast.makeText(this, "???????????????????????????????????????->??????->???????????????????????????", Toast.LENGTH_LONG).show();
                }
                break;
            case RECORD_PERMISSION:
                L.e("??????");
                if (isP) {
//                    toRecord();
                } else {
                    Toast.makeText(this, "??????????????????", Toast.LENGTH_LONG).show();
                }
                break;
            case ALERT_PERMISSION:
                L.e("ALERT_PERMISSION");
                if (isP) {
//                    toRecord();
                } else {
                    Toast.makeText(this, "?????????????????????", Toast.LENGTH_LONG).show();
                }
                break;


        }

    }

    private void startRequestPermission() {

//        ActivityCompat.requestPermissions(this, BASIC_PERMISSIONS, 123);


    }

    public void initView() {

    }

    protected abstract int setLayoutId();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        if (keyCode == KeyEvent.KEYCODE_BACK) {
            exitBy2Click();

        }
        return false;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (myDevice != null) {
            myDevice.disconnect();
        }
        threadAlive = false;
        try {
            BLEService.bleServiceDestroy(this, serviceConnection, mGattUpdateReceiver);
        } catch (Exception e) {
            e.printStackTrace();
        }
        System.exit(0);//???????????????????????????????????????????????????
    }

    /**
     * ??????
     */
    private static Boolean isExit = false;

    protected void exitBy2Click() {
        Timer tExit;
        if (!isExit) {
            isExit = true;

            Toast.makeText(this,getString(R.string.str_exit_app) , Toast.LENGTH_SHORT).show();
            tExit = new Timer();
            tExit.schedule(new TimerTask() {
                @Override
                public void run() {
                    isExit = false; // ??????
                }
            }, 2000); // ??????2?????????????????????????????????????????????????????????????????????????????????

        } else {
            // Toast.makeText(this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
//            ToastUtils.showToast(MainBaseActivity.this, getString(R.string.manual_to_shut_down_bluetooth));
            if (myDevice != null) {
                myDevice.disconnect();
            }
            onDestroy();
            finish();
            System.exit(0);//???????????????????????????????????????????????????
        }
    }
}
