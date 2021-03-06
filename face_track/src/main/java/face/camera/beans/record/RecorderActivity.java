package face.camera.beans.record;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.hardware.Camera;
import android.hardware.SensorManager;
import android.media.MediaPlayer;
import android.media.SoundPool;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.MotionEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.luck.picture.lib.PictureSelectionModel;
import com.luck.picture.lib.PictureSelector;
import com.luck.picture.lib.config.PictureMimeType;
import com.luck.picture.lib.config.PictureSelectionConfig;
import com.luck.picture.lib.language.LanguageConfig;
import com.luck.picture.lib.model.LocalMediaPageLoader;
import com.luck.picture.lib.tools.ToastUtils;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import butterknife.BindView;
import butterknife.OnClick;
import co.ceryle.segmentedbutton.SegmentedButtonGroup;
import face.camera.beans.MyApplication;
import face.camera.beans.R;
import face.camera.beans.SettingActivity;
import face.camera.beans.arc.ActiveInstance;
import face.camera.beans.arc.common.Constants;
import face.camera.beans.arc.widget.FaceRectView;
import face.camera.beans.base.activity.BaseActivity;
import face.camera.beans.base.pop.PopupManager;
import face.camera.beans.base.utils.FileUtils;
import face.camera.beans.base.utils.StaticFinalValues;
import face.camera.beans.ble.BLEService;
import face.camera.beans.ble.EnumEx.BleActionType;
import face.camera.beans.ble.EnumEx.BleHandleDataType;
import face.camera.beans.ble.EnumEx.DataProtocol;
import face.camera.beans.ble.EnumEx.StartBleRequestCodeType;
import face.camera.beans.ble.L;
import face.camera.beans.ble.Preferences;
import face.camera.beans.counter.CountupView;
import face.camera.beans.dialog.DialogFilter;
import face.camera.beans.net.UpdateManager;
import face.camera.beans.record.beans.MediaObject;
import face.camera.beans.record.beans.VideoFile;
import face.camera.beans.record.encoder.GlideCacheEngine;
import face.camera.beans.record.encoder.GlideEngine;
import face.camera.beans.record.other.MagicFilterType;
import face.camera.beans.record.ui.CameraView;
import face.camera.beans.record.ui.CustomRecordImageView;
import face.camera.beans.record.ui.FocusImageView;
import face.camera.beans.record.ui.ProgressView;
import face.camera.beans.record.ui.SlideGpuFilterGroup;

import static face.camera.beans.base.utils.StaticFinalValues.CHANGE_IMAGE;
import static face.camera.beans.base.utils.StaticFinalValues.DELAY_DETAL;
import static face.camera.beans.base.utils.StaticFinalValues.DELAY_DETAL_refresh;
import static face.camera.beans.base.utils.StaticFinalValues.OVER_CLICK;
import static face.camera.beans.base.utils.StaticFinalValues.RECORD_MIN_TIME;
import static face.camera.beans.base.utils.StaticFinalValues.REQUEST_CODE_PERMISSION;
import static face.camera.beans.ble.GlobalStatic.ISARCACTIVE;
import static face.camera.beans.ble.GlobalStatic.ISDeviceRight;


public class RecorderActivity extends BaseActivity implements View.OnTouchListener, SlideGpuFilterGroup.OnFilterChangeListener {
    private static final String TAG = "RecorderActivity";

    private static final int VIDEO_MAX_TIME = 30 * 1000;
    @BindView(R.id.record_camera_view)
    CameraViewARC mRecordCameraView;
    @BindView(R.id.face_rect_view)
    FaceRectView faceRectView;
    //    @BindView(R.id.video_record_progress_view)
//    ProgressView mVideoRecordProgressView;
    @BindView(R.id.ble_btn)
    ImageView bleState;
    @BindView(R.id.switch_camera)
    ImageView mMeetCamera;
    @BindView(R.id.index_delete)
    LinearLayout mIndexDelete;
    @BindView(R.id.index_album)
    TextView mIndexAlbum;
    @BindView(R.id.custom_record_image_view)
    CustomRecordImageView mCustomRecordImageView;
    @BindView(R.id.record_btn_ll)
    FrameLayout mRecordBtnLl;

//    @BindView(R.id.switch_ll)
//    LinearLayout mSwitchll;

    @BindView(R.id.video_filter)
    ImageView mVideoFilter;

    @BindView(R.id.flash_btn)
    ImageView mFlashBtn;

    @BindView(R.id.video_time_label)
    CountupView videoTimeLabel;

    @BindView(R.id.recorder_focus_iv)
    FocusImageView mRecorderFocusIv;
    @BindView(R.id.count_time_down_iv)
    ImageView mCountTimeDownIv;

    @BindView(R.id.album_show)
    ImageView albumShow;


    @BindView(R.id.switch_camera_mode)
    SegmentedButtonGroup switchCameraMode;

    @BindView(R.id.go_set)
    ImageView goSetBtn;

    //????????????,??????0?????????1
    boolean IS_photo_Model = false;


    public int mNum = 0;
    private long mLastTime = 0;
    ExecutorService executorService;
    private MediaObject mMediaObject;

    private MyHandler mMyHandler = new MyHandler(this);
    private boolean isRecording = false;

    private DialogFilter mDialogFilter;
    String LOG_TAG = "RecorderActivity";

    @Override
    protected int setLayoutId() {
        if (updateManager != null) {
            updateManager.checkUpdate();
        }
        return R.layout.activity_recorder;
    }

    @Override
    protected void onResume() {
        super.onResume();
//        Toast.makeText(RecorderActivity.this, "onResume--", Toast.LENGTH_SHORT).show();

//        mVideoRecordProgressView.setData(mMediaObject);
        if (mRecordCameraView.cameraHelper != null && mRecordCameraView.cameraHelper.mCamera != null) {
            freshAlbum();
        }
    }


    void freshAlbum() {
        PictureSelectionConfig config = new PictureSelectionModel().imageEngine(GlideEngine.createGlideEngine()).selectionConfig;
        LocalMediaPageLoader.getInstance(this, config).setLatestCover(this, albumShow);
    }


    @Override
    public void initView() {


        if (mMediaObject == null) {
            mMediaObject = new MediaObject();
        }
        Log.e(LOG_TAG, "Open Camera to initView()");
        mDialogFilter = new DialogFilter(this);
        mRecordCameraView.faceRectView = faceRectView;
        this.setCameraOkListenerVar(new OnCameraOkListener() {
            @Override
            public void cameraOk() {

                if (ISARCACTIVE) {
                    ActiveInstance.getInstance().initEngine(RecorderActivity.this);
                }
                Log.e(LOG_TAG, "Open Camera to setCameraOkListenerVar");
                mRecordCameraView.renderStart();
                if (updateManager != null) {
                    updateManager.getActiveCode();
                }

            }
        });
        mRecordCameraView.setOnTouchListener(RecorderActivity.this);
        mRecordCameraView.setOnFilterChangeListener(RecorderActivity.this);

        executorService = Executors.newSingleThreadExecutor();
        switchCameraMode.setOnClickedButtonListener(new SegmentedButtonGroup.OnClickedButtonListener() {
            @Override
            public void onClickedButton(int position) {
//                Toast.makeText(RecorderActivity.this, "????????????" + position + "", Toast.LENGTH_SHORT).show();


                IS_photo_Model = position == 1;
                if (IS_photo_Model && isRecording) {
                    videoTimeLabel.setVisibility(View.INVISIBLE);
                    videoTimeLabel.stop();
                    onStopRecording();

                }

                mCustomRecordImageView.setModelColor(IS_photo_Model);


            }
        });

        mRecordCameraView.setFaceMoveVar(new CameraViewARC.FaceMove() {
            @Override
            public void faceMoved(byte direction, int dis) {
                if (myDevice != null) {

                    if (dis > -1) {

                        if (dis == 0) {//??????
                            myDevice.sendOrder(DataProtocol.Order_hold.SET.Hold());
//                            mMyHandler.
                            if (countHold[0] == null && !isRecording) {
                                countHold[0] = new Timer();
                                countHold[0].schedule(new TimerTask() {
                                    @Override
                                    public void run() {
//                                        hideAllView();
                                        Log.e(LOG_TAG, "CHANGE_IMAGE" + mNum + "" + "????????????");

                                        if (countHold[0] != null) {
                                            mNum = 0;
                                            mMyHandler.sendEmptyMessage(CHANGE_IMAGE);
                                            //???????????????
                                            if(myDevice != null){
                                                myDevice.sendOrder(DataProtocol.Order_LED.SET.LED(DataProtocol.BYTE_LED.Shoot));
                                            }
                                        }
                                    }
                                }, 2000);//2???
                            }

                        } else {
                            Log.e(LOG_TAG, "CHANGE_IMAGE" + mNum + "" + "??????");

                            if (countHold[0] != null) {
                                countHold[0].cancel();
                                countHold[0] = null;
                                mNum = -1;
                                mMyHandler.sendEmptyMessage(CHANGE_IMAGE);
                            }
                            myDevice.sendOrder(DataProtocol.Order_fast.SET.Direction(direction));
                        }

                    } else {//???????????????????????????
//                    myDevice.sendOrder(DataProtocol.Order_hold.SET.Hold());//????????????????????????
                        Log.e(LOG_TAG, "CHANGE_IMAGE" + mNum + "" + "??????");

                        if (countHold[0] != null) {
                            countHold[0].cancel();
                            countHold[0] = null;
                            mNum = -1;
                            mMyHandler.sendEmptyMessage(CHANGE_IMAGE);
                        }
                    }
                }


            }
        });
/**
 * ??????
 */
        final int[] reconnectTimes = {0};
        hostDataHandler = new Handler(Looper.getMainLooper()) {
            @Override
            public void handleMessage(@NonNull Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case BleHandleDataType.RssiValue:

                        break;
                    case BleHandleDataType.Disconnected:
                        bleState.setImageDrawable(getDrawable(R.drawable.ble_off));
                        if (reconnectTimes[0] < 3) {//????????????
                            doSearchBle();
                            reconnectTimes[0] = reconnectTimes[0] + 1;
                        } else {
                            ToastUtils.s(RecorderActivity.this, getString(R.string.str_ble_time_out));
                        }

                        break;
                    case BleHandleDataType.Connected:
                        bleState.setImageDrawable(getDrawable(R.drawable.ble_on));
                        if (null == msg.obj) {
                            // showMessage(message.obj.toString());
                            return;
                        }
                        if (BleActionType.ACTION_GATT_CONNECTED.equals(msg.obj.toString())) {
                            runHoldConnect();

//                            setSignalColor(Color.GREEN);
                        }

                        break;
                    case BleHandleDataType.ConnectData:
//                    bleState.setImageDrawable();

                        if (null == msg.obj) {
                            // showMessage(message.obj.toString());
                            return;
                        }
                        reconnectTimes[0] = 0;//??????
                        List<String> tempArray = Arrays.asList(msg.obj.toString().split(","));
                        Log.e(LOG_TAG, "BLE-????????????" + tempArray);
//[FE, 5A, 3, CF, AF, 81]
                        List<String> address = Arrays.asList(myDevice.bluetoothDevice.getAddress().split(":"));
                        if (!ISDeviceRight) {
                            if (tempArray.size() == 6 && address.contains(tempArray.get(3)) && address.contains(tempArray.get(4))) {
                                ISDeviceRight = true;//????????????
                                //????????????
                                //????????????
                                if (!ISARCACTIVE) {
//                                    ActiveInstance.getInstance().activeEngine(RecorderActivity.this);

                                }

                            }

                        }

                        break;

                }


            }
        };
        //??????????????????
        OrientationEventListener mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {

                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;  //?????????????????????????????????????????????
                }
                //???????????????????????????????????????
                if (orientation > 350 || orientation < 10) { //0???
                    orientation = 0;
                } else if (orientation > 80 && orientation < 100) { //90???
//                    orientation = 90;
                    orientation = 270;
                } else if (orientation > 170 && orientation < 190) { //180???
                    orientation = 180;
                } else if (orientation > 260 && orientation < 280) { //270???
//                    orientation = 270;
                    orientation = 90;
                } else {
                    return;
                }
                Log.e(LOG_TAG, "????????????-mOrientationListener" + orientation);


//                an.setFillAfter(true);
//                ivArrow.startAnimation(an);

                int finalOrientation = orientation;

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        mFlashBtn.setRotation(finalOrientation);
                        bleState.setRotation(finalOrientation);
                        mVideoFilter.setRotation(finalOrientation);
                        albumShow.setRotation(finalOrientation);
                        mCountTimeDownIv.setRotation(finalOrientation);
                        mMeetCamera.setRotation(finalOrientation);

                        tempOrientation = finalOrientation;
                    }
                });
//????????????????????????????????????????????????
//                ????????????????????????CSDN?????????hipeboy???????????????????????????CC 4.0 BY-SA???????????????????????????????????????????????????????????????
//                ???????????????https://blog.csdn.net/haiping1224746757/article/details/107150390/


            }
        };
        if (mOrientationListener.canDetectOrientation()) {
            Log.v(LOG_TAG, "????????????-Can detect orientation");
            mOrientationListener.enable();
        } else {
            Log.v(LOG_TAG, "????????????-Cannot detect orientation");
            mOrientationListener.disable();
        }
//????????????????????????????????????????????????
//        ????????????????????????CSDN?????????hipeboy???????????????????????????CC 4.0 BY-SA???????????????????????????????????????????????????????????????
//        ???????????????https://blog.csdn.net/haiping1224746757/article/details/107150390/
    }

    private int tempOrientation = 0;
    final Timer[] countHold = {null};
    private boolean mIsFlashOpened = false;

    @OnClick({R.id.ble_btn,
            R.id.switch_camera, R.id.index_delete,
            R.id.index_album, R.id.custom_record_image_view,
            R.id.video_filter,
            R.id.flash_btn,
            R.id.go_set,
            R.id.album_show})
    public void onViewClicked(View view) {
        if (System.currentTimeMillis() - mLastTime < 500) {
            return;
        }
        mLastTime = System.currentTimeMillis();
        if (view.getId() != R.id.index_delete) {
            if (mMediaObject != null) {
                MediaObject.MediaPart part = mMediaObject.getCurrentPart();
                if (part != null) {
                    if (part.remove) {
                        part.remove = false;

                    }
                }
            }
        }
        switch (view.getId()) {
            case R.id.ble_btn:
//                onBackPressed();
                break;
//            case R.id.video_record_finish_iv:
//                onStopRecording();
//                if (mMediaObject != null) {
//                    videoFileName = mMediaObject.mergeVideo();
//                }
//                VideoPlayerActivity2.launch(RecorderActivity.this,videoFileName);//????????????
//                break;
            case R.id.switch_camera:
//                mRecordCameraView.setAlpha(0.3f);
                mRecordCameraView.switchCamera();
//                mRecordCameraView.setAlpha(1.0f);
                if (mRecordCameraView.getCameraId() == 1) {
                    mRecordCameraView.changeBeautyLevel(3);
                } else {
                    mRecordCameraView.changeBeautyLevel(0);
                }
                break;
            case R.id.index_delete:
                MediaObject.MediaPart part = mMediaObject.getCurrentPart();
                if (part != null) {
                    if (part.remove) {
                        part.remove = false;
                        mMediaObject.removePart(part, true);
                        if (mMediaObject.getMedaParts().size() == 0) {
                            mIndexDelete.setVisibility(View.GONE);
                            mIndexAlbum.setVisibility(View.VISIBLE);
                        }

                    } else {
                        part.remove = true;
                    }
                }
                break;
            case R.id.album_show://??????
                // ???????????? ??????????????????????????????api????????????
                PictureSelector.create(this)
                        .openGallery(PictureMimeType.ofAll())// ??????.PictureMimeType.ofAll()?????????.ofImage()?????????.ofVideo()?????????.ofAudio()
                        .imageEngine(GlideEngine.createGlideEngine())// ??????????????????????????????????????????
                        .isWeChatStyle(false)// ????????????????????????????????????
                        .isUseCustomCamera(false)// ???????????????????????????
//                        .setLanguage(LanguageConfig.CHINESE)// ???????????????????????????
                        .isWithVideoImage(true)// ?????????????????????????????????
                        .maxSelectNum(1)// ????????????????????????
                        .maxVideoSelectNum(1) // ???????????????????????????????????????????????????????????????????????????????????????maxSelectNum??????
                        .imageSpanCount(4)// ??????????????????
                        .isReturnEmpty(false)// ????????????????????????????????????????????????
                        //.isAndroidQTransform(false)// ??????????????????Android Q ??????????????????????????????????????????.isCompress(false); && .isEnableCrop(false);??????,????????????
                        .loadCacheResourcesCallback(GlideCacheEngine.createCacheEngine())// ????????????????????????????????????????????????10??????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)// ????????????Activity????????????????????????????????????
                        .isOriginalImageControl(true)// ????????????????????????????????????????????????true?????????????????????????????????????????????????????????????????????????????????
                        //.cameraFileName("test.png")    // ???????????????????????????????????????????????????????????????????????????????????????????????????????????????????????? ????????????????????????????????????api
                        //.renameCompressFile("test.png")// ??????????????????????????? ????????????????????????????????????????????????????????????
                        //.renameCropFileName("test.png")// ??????????????????????????? ????????????????????????????????????????????????????????????
                        .isPreviewImage(true)// ?????????????????????
                        .isPreviewVideo(true)// ?????????????????????
                        //.querySpecifiedFormatSuffix(PictureMimeType.ofJPEG())// ??????????????????????????????
                        .isCamera(false)// ????????????????????????
                        //.isMultipleSkipCrop(false)// ????????????????????????????????????????????????
                        .isZoomAnim(true)// ?????????????????? ???????????? ??????true
                        //.imageFormat(PictureMimeType.PNG)// ??????????????????????????????,??????jpeg
                        .isEnableCrop(false)// ????????????
                        .isCompress(false)// ????????????
                        .compressQuality(80)// ??????????????????????????? 0~ 100
                        .synOrAsy(true)//??????false?????????true ?????? ????????????
                        //.queryMaxFileSize(10)// ????????????M?????????????????????????????????  ??????M
                        //.compressSavePath(getPath())//????????????????????????
                        //.sizeMultiplier(0.5f)// glide ?????????????????? 0~1?????? ????????? .glideOverride()?????? ???????????????
                        //.glideOverride(160, 160)// glide ??????????????????????????????????????????????????????????????????????????????????????? ???????????????
                        //.setCircleDimmedColor(ContextCompat.getColor(this, R.color.app_color_white))// ??????????????????????????????
                        //.setCircleDimmedBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.app_color_white))// ??????????????????????????????
                        //.setCircleStrokeWidth(3)// ??????????????????????????????
                        //.isDragFrame(false)// ????????????????????????(??????)
                        //.videoMaxSecond(15)
                        //.videoMinSecond(10)
                        .isPreviewEggs(false)// ??????????????? ????????????????????????????????????(???????????????????????????????????????????????????)
                        //.cropCompressQuality(90)// ??????????????? ??????cutOutQuality()
                        .cutOutQuality(90)// ?????????????????? ??????100
                        .minimumCompressSize(100)// ??????100kb??????????????????
                        //.cropWH()// ???????????????????????????????????????????????????????????????
                        //.rotateEnabled(true) // ???????????????????????????
                        //.scaleEnabled(true)// ?????????????????????????????????
                        //.videoQuality()// ?????????????????? 0 or 1
                        //.recordVideoSecond()//?????????????????? ??????60s
                        //.setOutputCameraPath("/CustomPath")// ???????????????????????????  ???????????????
                        //.forResult(PictureConfig.CHOOSE_REQUEST);//????????????onActivityResult code
                        .forResult(null);


//                Intent intent2 = new Intent(this, VideoPickActivity.class);
//                intent2.putExtra(IS_NEED_CAMERA, false);
//                intent2.putExtra(MAX_NUMBER, 1);
//                intent2.putExtra(IS_NEED_FOLDER_LIST, true);
//                startActivityForResult(intent2, StaticFinalValues.REQUEST_CODE_PICK_VIDEO);
                break;
            case R.id.custom_record_image_view:
                if (IS_photo_Model) {
//                    mCustomRecordImageView.setModelColor();
//                    mRecordCameraView.takePicture(new CameraViewARC.TakePictureCallback() {
//                        @Override
//                        public void takePictureOK(Bitmap bmp) {
//                            if (bmp != null) {
//                                String s = FileUtils.saveBitmap(bmp);
//                                bmp.recycle();
//                                tempRecordFilePath = s;
////                                RecorderActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + s)));
//                                mMyHandler.sendEmptyMessageDelayed(DELAY_DETAL, 250);
//                                mMyHandler.sendEmptyMessageDelayed(DELAY_DETAL_refresh, 1000);
//                            } else {
//                                Toast.makeText(RecorderActivity.this, "????????????", Toast.LENGTH_SHORT).show();
//                            }
//                        }
//                    }, null, null, 1.0f, true);

                    mRecordCameraView.captureBitmap(new CameraViewARC.BitmapReadyCallbacks() {
                        @Override
                        public void onBitmapReady(Bitmap bmp) {
                            if (bmp != null) {
                                String s = FileUtils.saveBitmap(bmp);
                                bmp.recycle();
                                tempRecordFilePath = s;
//                                RecorderActivity.this.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, Uri.parse("file://" + s)));
                                mMyHandler.sendEmptyMessageDelayed(DELAY_DETAL, 250);
                                mMyHandler.sendEmptyMessageDelayed(DELAY_DETAL_refresh, 1000);
                            } else {
                                Toast.makeText(RecorderActivity.this, R.string.str_shoot_fail, Toast.LENGTH_SHORT).show();
                            }
                        }
                    }, RecorderActivity.this);

                } else {
                    if (!isRecording) {
                        long time = (long) 24 * 60 * 60 * 60 * 1000;
                        videoTimeLabel.setVisibility(View.VISIBLE);
                        videoTimeLabel.start(time);
                        onStartRecording();
                    } else {
                        videoTimeLabel.setVisibility(View.INVISIBLE);
                        videoTimeLabel.stop();
                        onStopRecording();
                    }
                }


                break;
            case R.id.flash_btn:
                if (mRecordCameraView.cameraHelper != null) {
                    if (mIsFlashOpened) {
                        mRecordCameraView.cameraHelper.setFlashLightMode(this, mRecordCameraView.getCameraId() == 0, Camera.Parameters.FLASH_MODE_OFF);
                        mFlashBtn.setImageResource(R.drawable.flash_off);
                        mIsFlashOpened = false;
                    } else {
//                        mCameraView.setFlashLightMode(Camera.Parameters.FLASH_MODE_TORCH);
                        if (IS_photo_Model) {
                            mRecordCameraView.cameraHelper.setFlashLightMode(this, mRecordCameraView.getCameraId() == 0, Camera.Parameters.FLASH_MODE_ON);

                        } else {
                            mRecordCameraView.cameraHelper.setFlashLightMode(this, mRecordCameraView.getCameraId() == 0, Camera.Parameters.FLASH_MODE_TORCH);

                        }
                        mFlashBtn.setImageResource(R.drawable.flash_on);

                        mIsFlashOpened = true;
                    }
                }

                break;

            case R.id.video_filter:
//                if (mRecordCameraView.getCameraId() == 0) {
//                    Toast.makeText(this, "??????????????? ???????????????????????????", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                mDialogFilter.show();
                hideOtherView();
                /*????????????????????????????????????*/
                mDialogFilter.setOnFilterChangedListener(new DialogFilter.OnFilterChangedListener() {
                    @Override
                    public void onFilterChangedListener(final int position) {
//                        showOtherView();
                        mRecordCameraView.changeFilter(position);
//                        mCameraView.setFilterWithConfig(ConstantFilters.FILTERS[position]);
//                        mCurrentFilter = ConstantFilters.FILTERS[position];
                    }
                });

                /*??????????????????????????????*/
                mDialogFilter.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        hideOtherView();
                    }
                });
                /*??????????????????????????????*/
                mDialogFilter.setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        showOtherView();
                    }
                });

                break;

            case R.id.go_set:
                Intent i = new Intent();
                i.setClass(this, SettingActivity.class);
                startActivity(i);
                break;
        }
    }

    String tempRecordFilePath = null;

    private void onStartRecording() {
        isRecording = true;
        File videoFile = FileUtils.getStorageMp4(String.valueOf(System.currentTimeMillis()));
        String storageMp4 = tempRecordFilePath = videoFile.getPath();
        MediaObject.MediaPart mediaPart = mMediaObject.buildMediaPart(storageMp4);
        mRecordCameraView.setSavePath(storageMp4);
        mRecordCameraView.startRecord();
        mCustomRecordImageView.startRecord();
        alterStatus();
        if(myDevice != null){
            myDevice.sendOrder(DataProtocol.Order_LED.SET.LED(DataProtocol.BYTE_LED.RecordStart));
        }
    }

    private void onStopRecording() {
        isRecording = false;
        mRecordCameraView.stopRecord();
        //todo:????????????????????????????????????
        mMyHandler.sendEmptyMessageDelayed(DELAY_DETAL, 500);

        mMyHandler.sendEmptyMessageDelayed(DELAY_DETAL_refresh, 1000);
        mCustomRecordImageView.stopRecord();
        alterStatus();
        if(myDevice != null){
            myDevice.sendOrder(DataProtocol.Order_LED.SET.LED(DataProtocol.BYTE_LED.RecordStop));
        }



//        freshAlbum();
    }

    private void setBackAlpha(Button view, int alpha) {
        if (alpha > 127) {
            view.setClickable(true);
        } else {
            view.setClickable(false);
        }
        view.getBackground().setAlpha(alpha);
    }

    private void showOtherView() {
        if (mMediaObject != null && mMediaObject.getMedaParts().size() == 0) {
            mIndexDelete.setVisibility(View.GONE);
            mIndexAlbum.setVisibility(View.VISIBLE);
        } else {
            mIndexDelete.setVisibility(View.VISIBLE);
            mIndexAlbum.setVisibility(View.GONE);
        }

        mRecordBtnLl.setVisibility(View.VISIBLE);

//        mMeetMask.setVisibility(View.VISIBLE);
        mCustomRecordImageView.setVisibility(View.VISIBLE);
    }

    private void hideOtherView() {

        mRecordBtnLl.setVisibility(View.INVISIBLE);

        mIndexAlbum.setVisibility(View.INVISIBLE);
        mIndexDelete.setVisibility(View.INVISIBLE);
//        mMeetMask.setVisibility(View.INVISIBLE);
        mCustomRecordImageView.setVisibility(View.INVISIBLE);
    }

    //???????????????
    public void alterStatus() {
        if (isRecording) {
            mIndexAlbum.setVisibility(View.INVISIBLE);
            mIndexDelete.setVisibility(View.INVISIBLE);
//            mMeetMask.setVisibility(View.INVISIBLE);
//            mVideoFilter.setVisibility(View.INVISIBLE);
        } else {
            if (mMediaObject != null && mMediaObject.getMedaParts().size() == 0) {
                mIndexDelete.setVisibility(View.GONE);
                mIndexAlbum.setVisibility(View.VISIBLE);
            } else {
                mIndexDelete.setVisibility(View.VISIBLE);
                mIndexAlbum.setVisibility(View.GONE);
            }
//            mMeetMask.setVisibility(View.VISIBLE);
//            mVideoFilter.setVisibility(View.VISIBLE);
            mMeetCamera.setVisibility(View.VISIBLE);
        }
    }

    private void hideAllView() {
        hideOtherView();
        mMeetCamera.setVisibility(View.GONE);
    }


    @Override
    public boolean onTouch(View v, MotionEvent event) {
        mRecordCameraView.onTouch(event);
        if (mRecordCameraView.getCameraId() == 1) {
            return false;
        }
        switch (event.getAction()) {
            case MotionEvent.ACTION_UP:
                float sRawX = event.getRawX();
                float sRawY = event.getRawY();
                float rawY = sRawY * MyApplication.screenWidth / MyApplication.screenHeight;
                float temp = sRawX;
                float rawX = rawY;
                rawY = (MyApplication.screenWidth - temp) * MyApplication.screenHeight / MyApplication.screenWidth;

                Point point = new Point((int) rawX, (int) rawY);
                mRecordCameraView.onFocus(point, new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            mRecorderFocusIv.onFocusSuccess();
                        } else {
                            mRecorderFocusIv.onFocusFailed();
                        }
                    }
                });
                mRecorderFocusIv.startFocus(new Point((int) sRawX, (int) sRawY));
        }
        return true;
    }

    @Override
    public void onFilterChange(final int type) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(RecorderActivity.this,  getString(type), Toast.LENGTH_SHORT).show();
            }
        });
    }


    private static class MyHandler extends Handler {

        private WeakReference<RecorderActivity> mVideoRecordActivity;

        public MyHandler(RecorderActivity videoRecordActivity) {
            mVideoRecordActivity = new WeakReference<>(videoRecordActivity);
        }

        @Override
        public void handleMessage(Message msg) {
            RecorderActivity activity = mVideoRecordActivity.get();
            if (activity != null) {
                switch (msg.what) {
                    case DELAY_DETAL:
                        //BUG
                        if (activity.tempRecordFilePath.endsWith("mp4")) {
                            Log.e("tempRecordFilePath", "handleMessage" + activity.tempRecordFilePath);
                            activity.mMediaObject.stopRecord(activity, activity.mMediaObject);
                        }
                        if (activity.tempRecordFilePath != null) {
                            //???????????????????????????????????????
                            activity.mMediaObject.refreshGallery(activity, activity.tempRecordFilePath);

                        }
                        break;

                    case DELAY_DETAL_refresh:
                        if (activity.tempRecordFilePath != null) {
                            //???????????????????????????????????????
//                            activity.mMediaObject.saveVideoToGallery(activity, activity.tempRecordFile);
                            activity.freshAlbum();
                        }
                        break;

                    case CHANGE_IMAGE:

                        switch (activity.mNum) {
                            case 0:
                                if (activity.countHold[0] == null) {
                                    break;
                                }
                                if (activity.isSoundLoaded && activity.voiceId > 0) {
                                    activity.soundPool.play(activity.voiceId, 1, 1, 1, 0, 1);
                                }
                                activity.mCountTimeDownIv.setVisibility(View.VISIBLE);
                                activity.mCountTimeDownIv.setImageResource(R.drawable.bigicon_3);
                                activity.mMyHandler.sendEmptyMessageDelayed(CHANGE_IMAGE, 1000);
                                break;
                            case 1:
                                if (activity.countHold[0] == null) {
                                    break;
                                }
                                if (activity.isSoundLoaded && activity.voiceId > 0) {
                                    activity.soundPool.play(activity.voiceId, 1, 1, 1, 0, 1);
                                }
                                activity.mCountTimeDownIv.setImageResource(R.drawable.bigicon_2);
                                activity.mMyHandler.sendEmptyMessageDelayed(CHANGE_IMAGE, 1000);
                                break;
                            case 2:
                                if (activity.countHold[0] == null) {

                                    break;
                                }
                                if (activity.isSoundLoaded && activity.voiceId > 0) {
                                    activity.soundPool.play(activity.voiceId, 1, 1, 1, 0, 1);
                                }
                                activity.mCountTimeDownIv.setImageResource(R.drawable.bigicon_1);
                                activity.mMyHandler.sendEmptyMessageDelayed(CHANGE_IMAGE, 1000);
                                activity.mCustomRecordImageView.performClick();
                                break;
                            case -1:
                                activity.mMyHandler.removeCallbacks(null);
                                activity.mCountTimeDownIv.setVisibility(View.GONE);
                                activity.mCustomRecordImageView.setVisibility(View.VISIBLE);
//                                activity.mCustomRecordImageView.performClick();
                                break;
                        }
                        if (activity.mNum >= 3) {
                            activity.mMyHandler.sendEmptyMessage(CHANGE_IMAGE);
                            activity.mNum = -1;
                        } else {
                            activity.mNum++;
                        }
                        break;
                    case OVER_CLICK:
                        activity.mCustomRecordImageView.performClick(); //????????????
                        break;
                }
            }
        }
    }


    String videoFileName;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case StaticFinalValues.REQUEST_CODE_PICK_VIDEO:
                if (resultCode == RESULT_OK) {
                    ArrayList<VideoFile> list = data.getParcelableArrayListExtra(StaticFinalValues.RESULT_PICK_VIDEO);
                    for (VideoFile file : list) {
                        videoFileName = file.getPath();
                    }


                    //????????????????????????????????????
                    try {
                        MediaPlayer player = new MediaPlayer();
                        player.setDataSource(videoFileName);
                        player.prepare();
                        int duration = player.getDuration();
                        player.release();
                        int s = duration / 1000;
                        int hour = s / 3600;
                        int minute = s % 3600 / 60;
                        int second = s % 60;
                        Log.e(TAG, "??????????????????,??????: " + minute + "?????????" + s + "???");
                        if (s >= 120) {
                            Toast.makeText(this, "????????????????????????2??????", Toast.LENGTH_LONG).show();
                            return;
                        } else if (s < 5) {
                            Toast.makeText(this, "????????????????????????5???", Toast.LENGTH_LONG).show();
                            return;
                        } else {

//                            Intent intent = new Intent(RecorderActivity.this, LocalVideoActivity.class);
//                            Bundle bundle = new Bundle();
//                            bundle.putString(StaticFinalValues.VIDEOFILEPATH, videoFileName);
//                            bundle.putInt(StaticFinalValues.MISNOTCOMELOCAL, 0);
//                            intent.putExtra(StaticFinalValues.BUNDLE, bundle);
//                            startActivity(intent);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }


                }
                break;
            case REQUEST_CODE_PERMISSION:
                if (resultCode == RESULT_OK) {
                    Toast.makeText(RecorderActivity.this, "????????????" + REQUEST_CODE_PERMISSION + "", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RecorderActivity.this, "??????????????????" + REQUEST_CODE_PERMISSION + "", Toast.LENGTH_SHORT).show();

                }
            case StartBleRequestCodeType.STARTBLE:
                if (resultCode != Activity.RESULT_OK) {
//                    Toast.makeText(this, "??????????????????????????????", Toast.LENGTH_SHORT).show();
//                    finish();
                    bleService.mBleRequest = false;
//                    bleService.requestBleON(this);
                    doSearchBle();
                    // System.exit(0);
                } else {

//                    startServiceAndBroadcast();
                }

        }
    }
}
