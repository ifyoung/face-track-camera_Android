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

    //相机模式,视频0，照片1
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
//                Toast.makeText(RecorderActivity.this, "当前选择" + position + "", Toast.LENGTH_SHORT).show();


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

                        if (dis == 0) {//居中
                            myDevice.sendOrder(DataProtocol.Order_hold.SET.Hold());
//                            mMyHandler.
                            if (countHold[0] == null && !isRecording) {
                                countHold[0] = new Timer();
                                countHold[0].schedule(new TimerTask() {
                                    @Override
                                    public void run() {
//                                        hideAllView();
                                        Log.e(LOG_TAG, "CHANGE_IMAGE" + mNum + "" + "居中计时");

                                        if (countHold[0] != null) {
                                            mNum = 0;
                                            mMyHandler.sendEmptyMessage(CHANGE_IMAGE);
                                            //倒计时开始
                                            if(myDevice != null){
                                                myDevice.sendOrder(DataProtocol.Order_LED.SET.LED(DataProtocol.BYTE_LED.Shoot));
                                            }
                                        }
                                    }
                                }, 2000);//2秒
                            }

                        } else {
                            Log.e(LOG_TAG, "CHANGE_IMAGE" + mNum + "" + "移动");

                            if (countHold[0] != null) {
                                countHold[0].cancel();
                                countHold[0] = null;
                                mNum = -1;
                                mMyHandler.sendEmptyMessage(CHANGE_IMAGE);
                            }
                            myDevice.sendOrder(DataProtocol.Order_fast.SET.Direction(direction));
                        }

                    } else {//超界，停止计时拍照
//                    myDevice.sendOrder(DataProtocol.Order_hold.SET.Hold());//让转一会儿自动停
                        Log.e(LOG_TAG, "CHANGE_IMAGE" + mNum + "" + "超界");

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
 * 蓝牙
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
                        if (reconnectTimes[0] < 3) {//重连三次
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
                        reconnectTimes[0] = 0;//重置
                        List<String> tempArray = Arrays.asList(msg.obj.toString().split(","));
                        Log.e(LOG_TAG, "BLE-接收数据" + tempArray);
//[FE, 5A, 3, CF, AF, 81]
                        List<String> address = Arrays.asList(myDevice.bluetoothDevice.getAddress().split(":"));
                        if (!ISDeviceRight) {
                            if (tempArray.size() == 6 && address.contains(tempArray.get(3)) && address.contains(tempArray.get(4))) {
                                ISDeviceRight = true;//设备匹配
                                //设备匹配
                                //执行激活
                                if (!ISARCACTIVE) {
//                                    ActiveInstance.getInstance().activeEngine(RecorderActivity.this);

                                }

                            }

                        }

                        break;

                }


            }
        };
        //屏幕方向监听
        OrientationEventListener mOrientationListener = new OrientationEventListener(this,
                SensorManager.SENSOR_DELAY_NORMAL) {
            @Override
            public void onOrientationChanged(int orientation) {

                if (orientation == OrientationEventListener.ORIENTATION_UNKNOWN) {
                    return;  //手机平放时，检测不到有效的角度
                }
                //只检测是否有四个角度的改变
                if (orientation > 350 || orientation < 10) { //0度
                    orientation = 0;
                } else if (orientation > 80 && orientation < 100) { //90度
//                    orientation = 90;
                    orientation = 270;
                } else if (orientation > 170 && orientation < 190) { //180度
                    orientation = 180;
                } else if (orientation > 260 && orientation < 280) { //270度
//                    orientation = 270;
                    orientation = 90;
                } else {
                    return;
                }
                Log.e(LOG_TAG, "屏幕方向-mOrientationListener" + orientation);


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
//————————————————
//                版权声明：本文为CSDN博主「hipeboy」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//                原文链接：https://blog.csdn.net/haiping1224746757/article/details/107150390/


            }
        };
        if (mOrientationListener.canDetectOrientation()) {
            Log.v(LOG_TAG, "屏幕方向-Can detect orientation");
            mOrientationListener.enable();
        } else {
            Log.v(LOG_TAG, "屏幕方向-Cannot detect orientation");
            mOrientationListener.disable();
        }
//————————————————
//        版权声明：本文为CSDN博主「hipeboy」的原创文章，遵循CC 4.0 BY-SA版权协议，转载请附上原文出处链接及本声明。
//        原文链接：https://blog.csdn.net/haiping1224746757/article/details/107150390/
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
//                VideoPlayerActivity2.launch(RecorderActivity.this,videoFileName);//播放界面
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
            case R.id.album_show://相册
                // 进入相册 以下是例子：不需要的api可以不写
                PictureSelector.create(this)
                        .openGallery(PictureMimeType.ofAll())// 全部.PictureMimeType.ofAll()、图片.ofImage()、视频.ofVideo()、音频.ofAudio()
                        .imageEngine(GlideEngine.createGlideEngine())// 外部传入图片加载引擎，必传项
                        .isWeChatStyle(false)// 是否开启微信图片选择风格
                        .isUseCustomCamera(false)// 是否使用自定义相机
//                        .setLanguage(LanguageConfig.CHINESE)// 设置语言，默认中文
                        .isWithVideoImage(true)// 图片和视频是否可以同选
                        .maxSelectNum(1)// 最大图片选择数量
                        .maxVideoSelectNum(1) // 视频最大选择数量，如果没有单独设置的需求则可以不设置，同用maxSelectNum字段
                        .imageSpanCount(4)// 每行显示个数
                        .isReturnEmpty(false)// 未选择数据时点击按钮是否可以返回
                        //.isAndroidQTransform(false)// 是否需要处理Android Q 拷贝至应用沙盒的操作，只针对.isCompress(false); && .isEnableCrop(false);有效,默认处理
                        .loadCacheResourcesCallback(GlideCacheEngine.createCacheEngine())// 获取图片资源缓存，主要是解决华为10部分机型在拷贝文件过多时会出现卡的问题，这里可以判断只在会出现一直转圈问题机型上使用
                        .setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)// 设置相册Activity方向，不设置默认使用系统
                        .isOriginalImageControl(true)// 是否显示原图控制按钮，如果设置为true则用户可以自由选择是否使用原图，压缩、裁剪功能将会失效
                        //.cameraFileName("test.png")    // 重命名拍照文件名、注意这个只在使用相机时可以使用，如果使用相机又开启了压缩或裁剪 需要配合压缩和裁剪文件名api
                        //.renameCompressFile("test.png")// 重命名压缩文件名、 注意这个不要重复，只适用于单张图压缩使用
                        //.renameCropFileName("test.png")// 重命名裁剪文件名、 注意这个不要重复，只适用于单张图裁剪使用
                        .isPreviewImage(true)// 是否可预览图片
                        .isPreviewVideo(true)// 是否可预览视频
                        //.querySpecifiedFormatSuffix(PictureMimeType.ofJPEG())// 查询指定后缀格式资源
                        .isCamera(false)// 是否显示拍照按钮
                        //.isMultipleSkipCrop(false)// 多图裁剪时是否支持跳过，默认支持
                        .isZoomAnim(true)// 图片列表点击 缩放效果 默认true
                        //.imageFormat(PictureMimeType.PNG)// 拍照保存图片格式后缀,默认jpeg
                        .isEnableCrop(false)// 是否裁剪
                        .isCompress(false)// 是否压缩
                        .compressQuality(80)// 图片压缩后输出质量 0~ 100
                        .synOrAsy(true)//同步false或异步true 压缩 默认同步
                        //.queryMaxFileSize(10)// 只查多少M以内的图片、视频、音频  单位M
                        //.compressSavePath(getPath())//压缩图片保存地址
                        //.sizeMultiplier(0.5f)// glide 加载图片大小 0~1之间 如设置 .glideOverride()无效 注：已废弃
                        //.glideOverride(160, 160)// glide 加载宽高，越小图片列表越流畅，但会影响列表图片浏览的清晰度 注：已废弃
                        //.setCircleDimmedColor(ContextCompat.getColor(this, R.color.app_color_white))// 设置圆形裁剪背景色值
                        //.setCircleDimmedBorderColor(ContextCompat.getColor(getApplicationContext(), R.color.app_color_white))// 设置圆形裁剪边框色值
                        //.setCircleStrokeWidth(3)// 设置圆形裁剪边框粗细
                        //.isDragFrame(false)// 是否可拖动裁剪框(固定)
                        //.videoMaxSecond(15)
                        //.videoMinSecond(10)
                        .isPreviewEggs(false)// 预览图片时 是否增强左右滑动图片体验(图片滑动一半即可看到上一张是否选中)
                        //.cropCompressQuality(90)// 注：已废弃 改用cutOutQuality()
                        .cutOutQuality(90)// 裁剪输出质量 默认100
                        .minimumCompressSize(100)// 小于100kb的图片不压缩
                        //.cropWH()// 裁剪宽高比，设置如果大于图片本身宽高则无效
                        //.rotateEnabled(true) // 裁剪是否可旋转图片
                        //.scaleEnabled(true)// 裁剪是否可放大缩小图片
                        //.videoQuality()// 视频录制质量 0 or 1
                        //.recordVideoSecond()//录制视频秒数 默认60s
                        //.setOutputCameraPath("/CustomPath")// 自定义拍照保存路径  注：已废弃
                        //.forResult(PictureConfig.CHOOSE_REQUEST);//结果回调onActivityResult code
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
//                                Toast.makeText(RecorderActivity.this, "拍照失败", Toast.LENGTH_SHORT).show();
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
//                    Toast.makeText(this, "后置摄像头 不使用美白磨皮功能", Toast.LENGTH_SHORT).show();
//                    return;
//                }
                mDialogFilter.show();
                hideOtherView();
                /*滤镜对话框选择滤镜的监听*/
                mDialogFilter.setOnFilterChangedListener(new DialogFilter.OnFilterChangedListener() {
                    @Override
                    public void onFilterChangedListener(final int position) {
//                        showOtherView();
                        mRecordCameraView.changeFilter(position);
//                        mCameraView.setFilterWithConfig(ConstantFilters.FILTERS[position]);
//                        mCurrentFilter = ConstantFilters.FILTERS[position];
                    }
                });

                /*过滤对话框显示的监听*/
                mDialogFilter.setOnShowListener(new DialogInterface.OnShowListener() {
                    @Override
                    public void onShow(DialogInterface dialog) {
                        hideOtherView();
                    }
                });
                /*过滤对话框隐藏的监听*/
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
        //todo:录制释放有延时，稍后处理
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

    //正在录制中
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
                            //通知相册刷新，写入相册索引
                            activity.mMediaObject.refreshGallery(activity, activity.tempRecordFilePath);

                        }
                        break;

                    case DELAY_DETAL_refresh:
                        if (activity.tempRecordFilePath != null) {
                            //通知相册刷新，写入相册索引
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
                        activity.mCustomRecordImageView.performClick(); //定时结束
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


                    //这一段用来判断视频时间的
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
                        Log.e(TAG, "视频文件长度,分钟: " + minute + "视频有" + s + "秒");
                        if (s >= 120) {
                            Toast.makeText(this, "视频剪辑不能超过2分钟", Toast.LENGTH_LONG).show();
                            return;
                        } else if (s < 5) {
                            Toast.makeText(this, "视频剪辑不能少于5秒", Toast.LENGTH_LONG).show();
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
                    Toast.makeText(RecorderActivity.this, "权限授予" + REQUEST_CODE_PERMISSION + "", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(RecorderActivity.this, "权限没有授予" + REQUEST_CODE_PERMISSION + "", Toast.LENGTH_SHORT).show();

                }
            case StartBleRequestCodeType.STARTBLE:
                if (resultCode != Activity.RESULT_OK) {
//                    Toast.makeText(this, "设备工作需要链接蓝牙", Toast.LENGTH_SHORT).show();
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
