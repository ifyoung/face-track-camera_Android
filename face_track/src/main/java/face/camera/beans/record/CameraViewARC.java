package face.camera.beans.record;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.media.ExifInterface;
import android.opengl.GLES20;
import android.opengl.GLException;
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.ViewTreeObserver;

import com.arcsoft.face.AgeInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.Face3DAngle;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.FaceInfo;
import com.arcsoft.face.GenderInfo;
import com.arcsoft.face.LivenessInfo;
import com.arcsoft.face.enums.DetectMode;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.List;

import javax.microedition.khronos.egl.EGL10;
import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.egl.EGLContext;
import javax.microedition.khronos.opengles.GL10;

import face.camera.beans.arc.model.DrawInfo;
import face.camera.beans.arc.util.ConfigUtil;
import face.camera.beans.arc.util.DrawHelper;
import face.camera.beans.arc.util.camera.CameraHelper;
import face.camera.beans.arc.util.camera.CameraListener;
import face.camera.beans.arc.util.face.RecognizeColor;
import face.camera.beans.arc.widget.FaceRectView;
import face.camera.beans.ble.EnumEx.DataProtocol;
import face.camera.beans.record.camera.CameraController;
import face.camera.beans.record.draw.CameraDrawer;
import face.camera.beans.record.ui.CameraView;
import face.camera.beans.record.ui.SlideGpuFilterGroup;

//import static face.camera.beans.arc.ActiveInstance.faceEngine;
import static face.camera.beans.arc.ActiveInstance.faceEngine;
import static face.camera.beans.arc.ActiveInstance.processMask;
import static face.camera.beans.base.activity.BaseActivity.myDevice;


/**
 * description:
 * Created by aserbao on 2018/5/15.
 */


public class CameraViewARC extends GLSurfaceView implements GLSurfaceView.Renderer,
        SurfaceTexture.OnFrameAvailableListener, ViewTreeObserver.OnGlobalLayoutListener {

    interface FaceMove {
        void faceMoved(byte direction, int dis);
    }

    private FaceMove faceMoveVar;

    public void setFaceMoveVar(FaceMove faceMoveVar) {
        this.faceMoveVar = faceMoveVar;
    }

    CameraListener cameraListener = new CameraListener() {
        //先与Surface Create 回调
        @Override
        public void onCameraOpened(Camera camera, int cameraId, int displayOrientation, boolean isMirror) {
            Log.i(TAG, "Open Camera-onCameraOpened: " + cameraId + "  " + displayOrientation + " " + isSetParm);
            previewSize = camera.getParameters().getPreviewSize();

            drawHelper = new DrawHelper(previewSize.width, previewSize.height, getPWidth(), getPHeight(), displayOrientation
                    , cameraId, isMirror, false, false);
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (tempGL10 != null && tempGLW > 0) {
                        onSurfaceChanged(tempGL10, tempGLW, tempGLH);//告诉我墙在哪儿？？？？
                    }
                }
            });
        }


        @Override
        public void onPreview(byte[] nv21, Camera camera) {

            if (faceEngine == null) {
                return;
            }

            if (faceRectView != null) {
                faceRectView.clearFaceInfo();
            }
            List<FaceInfo> faceInfoList = new ArrayList<>();
//                long start = System.currentTimeMillis();
            int code = faceEngine.detectFaces(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList);
            if (code == ErrorInfo.MOK && faceInfoList.size() > 0) {
                code = faceEngine.process(nv21, previewSize.width, previewSize.height, FaceEngine.CP_PAF_NV21, faceInfoList, processMask);
                if (code != ErrorInfo.MOK) {
                    return;
                }
            } else {
//                return;
            }

            List<AgeInfo> ageInfoList = new ArrayList<>();
            List<GenderInfo> genderInfoList = new ArrayList<>();
            List<Face3DAngle> face3DAngleList = new ArrayList<>();
            List<LivenessInfo> faceLivenessInfoList = new ArrayList<>();
            int ageCode = faceEngine.getAge(ageInfoList);
            int genderCode = faceEngine.getGender(genderInfoList);
            int face3DAngleCode = faceEngine.getFace3DAngle(face3DAngleList);
            int livenessCode = faceEngine.getLiveness(faceLivenessInfoList);

            // Rect(int left, int top, int right, int bottom)

//           3->CY->正，前置：大左，小右
//           1->CX->右HOME，前置：大左，小右
//           4->CX->左HOME，前置：小左，大右
            int screenCX = previewSize.width / 2;
            int screenCY = previewSize.height / 2;

            if (faceInfoList.size() > 0) {
                Log.i(TAG, "Open faceEngine: " + faceInfoList.get(0).getRect() + ">>" + faceInfoList.get(0).getOrient());
                Log.i(TAG, "Open faceEngine:X " + faceInfoList.get(0).getRect().centerX() + ">>Y" + faceInfoList.get(0).getRect().centerY() + ">>" + faceInfoList.get(0).getOrient());
                Log.i(TAG, "Open faceEngine:EX " + faceInfoList.get(0).getRect().exactCenterX() + ">>EY" + faceInfoList.get(0).getRect().exactCenterY());
                Log.i(TAG, "Open faceEngine:XSC " + screenCX + ">>Y" + screenCY);


                int faceY = faceInfoList.get(0).getRect().centerY();
                int faceX = faceInfoList.get(0).getRect().centerX();
                int phoneDirection = faceInfoList.get(0).getOrient();
                switch (phoneDirection) {
                    case 3://3->CY->正，前置：大左，小右
                        FACE_moveDistance = Math.abs(screenCY - faceY);
                        if (screenCY - faceY > 5) {
                            FACE_direction = DataProtocol.BYTE_direction.HOLD_LEFT;
                        } else {
                            FACE_direction = DataProtocol.BYTE_direction.RIGHT;
                        }

                        break;
                    case 1://1->CX->右HOME，前置：大左，小右
                        FACE_moveDistance = Math.abs(screenCX - faceX);
                        if (screenCX - faceX > 5) {
                            FACE_direction = DataProtocol.BYTE_direction.HOLD_LEFT;
                        } else {
                            FACE_direction = DataProtocol.BYTE_direction.RIGHT;
                        }
                        break;
                    case 4://4->CX->左HOME，前置：小左，大右
                        FACE_moveDistance = Math.abs(screenCX - faceX);
                        if (screenCX - faceX > 5) {
                            FACE_direction = DataProtocol.BYTE_direction.RIGHT;
                        } else {
                            FACE_direction = DataProtocol.BYTE_direction.HOLD_LEFT;
                        }
                        break;
                }
                if (FACE_moveDistance < 57) {//移动阙值，满足表示居中了
                    FACE_moveDistance = 0;
                }
                Log.i(TAG, "Open faceEngine:MOVE " + FACE_direction + ">>Dis" + FACE_moveDistance);

                if (faceMoveVar != null) {
                    faceMoveVar.faceMoved(FACE_direction, FACE_moveDistance);
                }

            } else {
                if (faceMoveVar != null) {//超界
                    faceMoveVar.faceMoved(DataProtocol.BYTE_direction.HOLD_LEFT, -1);
                }
            }


            // 有其中一个的错误码不为ErrorInfo.MOK，return
            if ((ageCode | genderCode | face3DAngleCode | livenessCode) != ErrorInfo.MOK) {
                return;
            }
            if (faceRectView != null && drawHelper != null) {
                List<DrawInfo> drawInfoList = new ArrayList<>();
                for (int i = 0; i < faceInfoList.size(); i++) {

                    drawInfoList.add(new DrawInfo(drawHelper.adjustRect(faceInfoList.get(i).getRect()),
                            genderInfoList.get(i).getGender(), ageInfoList.get(i).getAge(),
                            faceLivenessInfoList.get(i).getLiveness(),
                            RecognizeColor.COLOR_UNKNOWN, null));
                }
                drawHelper.draw(faceRectView, drawInfoList);
            }
        }

        @Override
        public void onCameraClosed() {
            Log.i(TAG, "Open Camera-onCameraClosed: ");
        }

        @Override
        public void onCameraError(Exception e) {
            Log.i(TAG, "Open Camera-onCameraError: " + e.getMessage());
            queueEvent(new Runnable() {
                @Override
                public void run() {
                    if (tempGL10 != null && tempGLW > 0) {
                        onSurfaceChanged(tempGL10, tempGLW, tempGLH);//告诉我墙在哪儿？？？？
                    }
                }
            });
        }

        @Override
        public void onCameraConfigurationChanged(int cameraID, int displayOrientation) {
            if (drawHelper != null) {
                drawHelper.setCameraDisplayOrientation(displayOrientation);
            }
            Log.i(TAG, "onCameraConfigurationChanged: " + cameraID + "  " + displayOrientation);
        }
    };

    private int FACE_moveDistance = 0;
    //    private static int FACE_deltDistance = 20;//移动位置阙值
    private static byte FACE_direction = DataProtocol.BYTE_direction.HOLD_LEFT;//移动位置阙值


    private static final String TAG = "CameraViewARC";

    public CameraDrawer mCameraDrawer;
    private int dataWidth = 0, dataHeight = 0;
    private boolean isSetParm = false;

    private DrawHelper drawHelper;

    public CameraHelper cameraHelper;
    //    private DrawHelper drawHelper;
    private Camera.Size previewSize;
    private Integer rgbCameraId = Camera.CameraInfo.CAMERA_FACING_BACK;

//    private FaceEngine faceEngine;


    public FaceRectView faceRectView;

    public CameraViewARC(Context context) {
        this(context, null);
    }

    public CameraViewARC(Context context, AttributeSet attrs) {
        super(context, attrs);

//        faceRectView = new FaceRectView(context,attrs);
        getHolder().addCallback(this);
        init();
    }

    //    第一次布局完成后，去除该监听，并且进行引擎和相机的初始化
    @Override
    public void onGlobalLayout() {
        getViewTreeObserver().removeOnGlobalLayoutListener(this);

//        initEngine();
        initCamera();
    }


    private void init() {
        Log.e(TAG, "Open Camera to onSurface-init");
        getViewTreeObserver().addOnGlobalLayoutListener(this);
        /**初始化OpenGL的相关信息*/
        setEGLContextClientVersion(2);//设置版本
        setRenderer(this);//设置Renderer
        setRenderMode(RENDERMODE_WHEN_DIRTY);//主动调用渲染
//        setRenderMode(RENDERMODE_CONTINUOUSLY);//主动调用渲染
        setPreserveEGLContextOnPause(true);//保存Context当pause时
//        setCameraDistance(100);//相机距离
        /**初始化Camera的绘制类*/
        mCameraDrawer = new CameraDrawer(getResources());
//        mCameraDrawer.getTexture();
        /**初始化相机的管理类*/
//        mCameraController = new CameraController();
//        initCamera();
    }


    void initCamera() {
        cameraHelper = new CameraHelper.Builder()
                .previewViewSize(new Point(this.getMeasuredWidth(), this.getMeasuredHeight()))
                .rotation(getDisplay().getRotation())
                .specificCameraId(rgbCameraId != null ? rgbCameraId : Camera.CameraInfo.CAMERA_FACING_FRONT)
                .isMirror(false)
                .previewOn(this)
                .cameraListener(cameraListener)
                .build();
        cameraHelper.init();
        Log.e(TAG, "Open Camera--initCamera");
        cameraHelper.start();
        cameraHelper.switchCamera();
//switchCamera();
//        initEngine();
    }

    public void renderStart() {
        handler.postDelayed(showRunnable, 1300);//在这里设置延时时间，单位是毫秒
//        handler.postDelayed(showRunnable, 2600);//在这里设置延时时间，单位是毫秒

    }

    Handler handler = new Handler(Looper.getMainLooper());


    Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
//            handler.postDelayed(showRunnable, 300);//在这里设置延时时间，单位是毫秒
            //在这里进行想做的事

            Log.e(TAG, "Open Camera to Render-handler");
//                open(cameraId);
            if (cameraHelper != null) {
                if (!isSetParm) {
                    cameraHelper.start();
                    cameraHelper.switchCamera();
                }
//                    cameraHelper.switchCamera();
//                    cameraHelper.switchCamera();
//                    switchCamera();
//                    if(cameraHelper.mCamera == null){
//                    }else {
//                        setPreviewTexture();
//                    }
            }


        }
    };

    private GL10 tempGL10;
    private EGLConfig tempEGLConfig;
    private int tempGLW;
    private int tempGLH;
    public int mMaxTextureSize = 0;


    @Override
    public void onSurfaceCreated(GL10 gl, EGLConfig config) {
        Log.e(TAG, "Open Camera to onSurfaceCreated");
        tempEGLConfig = config;
        mCameraDrawer.onSurfaceCreated(gl, config);
        //在RecorderActivity->onResume后面？？
        tempGL10 = gl;
        if (!isSetParm) {
            int texSize[] = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, texSize, 0);
            mMaxTextureSize = texSize[0];
            Log.e(TAG, "Open Camera to Render");
//            open(cameraId);

        }
    }


    void setPreviewTexture() {
        if (cameraHelper.mCamera != null) {
            if (previewSize == null) {
                previewSize = cameraHelper.mCamera.getParameters().getPreviewSize();
            }
//                cameraHelper.start();
            SurfaceTexture texture = mCameraDrawer.getTexture();
            if (texture == null) {
                return;
            }

            mCameraDrawer.setCameraId(cameraHelper.mCameraId);
//            final Point previewSize =  new Point(previewSize.height, previewSize.width);;
            dataWidth = previewSize.height;
            dataHeight = previewSize.width;
            mCameraDrawer.setPreviewSize(dataWidth, dataHeight);
            texture.setOnFrameAvailableListener(CameraViewARC.this);
            cameraHelper.startPreview(texture);
            Log.e("CameraViewARC", "Open Camera to Render-finish");
            stickerInit();
        }
    }

    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        tempGL10 = gl;
        tempGLW = width;
        tempGLH = height;
        Log.e(TAG, "Open Camera to onSurfaceChanged" + tempGLW + ">>" + tempGLH);
        if (!isSetParm) {

        }
        setPreviewTexture();

        mCameraDrawer.onSurfaceChanged(gl, width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
//        Log.e(TAG, "Open Camera to onDrawFrame" + gl + tempGLW + ">>" + tempGLH);

        if (isSetParm) {
            mCameraDrawer.onDrawFrame(gl);
            //清楚深度缓存和颜色缓存
//            GLES20.glClear(GLES20.GL_DEPTH_BUFFER_BIT | GLES20.GL_COLOR_BUFFER_BIT);
            if (mNeedReadPixel) {
//                Bitmap bmp = readBufferPixelToBitmap(getPWidth(), getPHeight());
                Bitmap bmp = createBitmapFromGLSurface(0, 0, getPWidth(), getPHeight(), gl);
                mNeedReadPixel = false;
                if (mListener != null) {
                    mListener.onBitmapReady(bmp);
                }
            }

        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }

    public void switchCamera() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.setSwitch(true);
                cameraHelper.switchCamera();
//                mCameraDrawer.onPause(false);
                requestRender();
            }
        });
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.setSwitch(false);
            }
        }, 700);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    public void onDestroy() {

        if (cameraHelper != null) {
            cameraHelper.release();
            cameraHelper = null;
        }
//        unInitEngine();
    }

    public int getCameraId() {
        return cameraHelper.mCameraId;
    }

    int getPWidth() {
        return getWidth();
    }

    int getPHeight() {
        return getHeight();
    }

    public int getBeautyLevel() {
        return mCameraDrawer.getBeautyLevel();
    }

    public void changeBeautyLevel(final int level) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.changeBeautyLevel(level);
            }
        });
    }

    public void changeFilter(final int pos) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.changeFilter(pos);
                requestRender();
            }
        });
    }

    public void startRecord() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.startRecord();
            }
        });
    }

    public void stopRecord() {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.stopRecord();
            }
        });
    }

    public void setSavePath(String path) {
        mCameraDrawer.setSavePath(path);
    }

    public void resume(final boolean auto) {
        queueEvent(new Runnable() {
            @Override

            public void run() {
                mCameraDrawer.onResume(auto);
            }
        });
    }

    public void pause(final boolean auto) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.onPause(auto);
            }
        });
    }

    public void onTouch(final MotionEvent event) {
        queueEvent(new Runnable() {
            @Override
            public void run() {
                mCameraDrawer.onTouch(event);
            }
        });
    }

    public void setOnFilterChangeListener(SlideGpuFilterGroup.OnFilterChangeListener listener) {
        mCameraDrawer.setOnFilterChangeListener(listener);
    }

    /**
     * 摄像头聚焦
     */
    public void onFocus(Point point, Camera.AutoFocusCallback callback) {
        cameraHelper.focusAtPoint(point, callback);
    }


    private void stickerInit() {
        if (!isSetParm && dataWidth > 0 && dataHeight > 0) {
            isSetParm = true;
        }
    }


    public interface TakePictureCallback {
        //You can recycle the bitmap.
        void takePictureOK(Bitmap bmp);
    }

    public void takeShot(final TakePictureCallback callback) {
    }


    public synchronized void takeShoot(final TakePictureCallback photoCallback) {
        if (photoCallback == null || cameraHelper.mCamera == null) {
            Log.e(TAG, "takePicture after release!");
            if (photoCallback != null) {
                photoCallback.takePictureOK(null);
            }
            return;
        }
        // 方式二:
        Bitmap bitmap2 = Bitmap.createBitmap(this.getWidth(), this.getHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas();
        canvas.setBitmap(bitmap2);
        this.getHolder().unlockCanvasAndPost(canvas);
        photoCallback.takePictureOK(bitmap2);

    }

    //https://www.jianshu.com/p/4d4702530180,从GLSurfaceView中拿到图片
    private Bitmap createBitmapFromGLSurface(int x, int y, int w, int h, GL10 gl)
            throws OutOfMemoryError {
        int bitmapBuffer[] = new int[w * h];
        int bitmapSource[] = new int[w * h];
        IntBuffer intBuffer = IntBuffer.wrap(bitmapBuffer);
        intBuffer.position(0);

        try {
            gl.glReadPixels(x, y, w, h, GL10.GL_RGBA, GL10.GL_UNSIGNED_BYTE, intBuffer);
            int offset1, offset2;
            for (int i = 0; i < h; i++) {
                offset1 = i * w;
                offset2 = (h - i - 1) * w;
                for (int j = 0; j < w; j++) {
                    int texturePixel = bitmapBuffer[offset1 + j];
                    int blue = (texturePixel >> 16) & 0xff;
                    int red = (texturePixel << 16) & 0x00ff0000;
                    int pixel = (texturePixel & 0xff00ff00) | red | blue;
                    bitmapSource[offset2 + j] = pixel;
                }
            }
        } catch (GLException e) {
            return null;
        }

        return Bitmap.createBitmap(bitmapSource, w, h, Bitmap.Config.ARGB_8888);
    }

    public interface BitmapReadyCallbacks {
        void onBitmapReady(Bitmap bitmap);
    }
    private boolean mNeedReadPixel = false;
    private BitmapReadyCallbacks mListener;

    // supporting methods
    public void captureBitmap(final BitmapReadyCallbacks bitmapReadyCallbacks, Activity activity) {
//        queueEvent(new Runnable() {
//            @Override
//            public void run() {
//                EGL10 egl = (EGL10) EGLContext.getEGL();
//                GL10 gl = (GL10) egl.eglGetCurrentContext().getGL();
////                Bitmap snapshotBitmap = createBitmapFromGLSurface(0, 0, getPWidth(), getPHeight(), gl);
//                Bitmap snapshotBitmap = readBufferPixelToBitmap(getPWidth(), getPHeight());
//
//                activity.runOnUiThread(new Runnable() {
//                    @Override
//                    public void run() {
//                        bitmapReadyCallbacks.onBitmapReady(snapshotBitmap);
//                    }
//                });
//
//            }
//        });

        mListener = bitmapReadyCallbacks;
        mNeedReadPixel = true;

    }

    private Bitmap readBufferPixelToBitmap(int width, int height) {
        ByteBuffer buf = ByteBuffer.allocateDirect(width * height * 4);
        buf.order(ByteOrder.LITTLE_ENDIAN);
        GLES20.glReadPixels(0, 0, width, height, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);
        buf.rewind();

        Bitmap bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
        bmp.copyPixelsFromBuffer(buf);
        return bmp;
    }


    public synchronized void takePicture(final TakePictureCallback photoCallback, Camera.ShutterCallback shutterCallback, final String config, final float intensity, final boolean isFrontMirror) {

        Camera.Parameters params = cameraHelper.mCamera.getParameters();

        if (photoCallback == null || params == null || cameraHelper.mCamera == null) {
            Log.e(TAG, "takePicture after release!");
            if (photoCallback != null) {
                photoCallback.takePictureOK(null);
            }
            return;
        }

        try {
            params.setRotation(90);
            cameraHelper.mCamera.setParameters(params);
        } catch (Exception e) {
            Log.e(TAG, "Error when takePicture: " + e.toString());
            if (photoCallback != null) {
                photoCallback.takePictureOK(null);
            }
            return;
        }

        cameraHelper.mCamera.takePicture(shutterCallback, null, new Camera.PictureCallback() {
            @Override
            public void onPictureTaken(final byte[] data, Camera camera) {

                Camera.Parameters params = camera.getParameters();
                Camera.Size sz = params.getPictureSize();

                boolean shouldRotate;

                Bitmap bmp;
                int width, height;

                //当拍出相片不为正方形时， 可以判断图片是否旋转
                if (sz.width != sz.height) {
                    //默认数据格式已经设置为 JPEG
                    bmp = BitmapFactory.decodeByteArray(data, 0, data.length);
                    width = bmp.getWidth();
                    height = bmp.getHeight();
                    shouldRotate = (sz.width > sz.height && width > height) || (sz.width < sz.height && width < height);
                } else {
                    Log.i(TAG, "Cache image to get exif.");

                    try {
                        String tmpFilename = getContext().getExternalCacheDir() + "/picture_cache000.jpg";
                        FileOutputStream fileout = new FileOutputStream(tmpFilename);
                        BufferedOutputStream bufferOutStream = new BufferedOutputStream(fileout);
                        bufferOutStream.write(data);
                        bufferOutStream.flush();
                        bufferOutStream.close();

                        ExifInterface exifInterface = new ExifInterface(tmpFilename);
                        int orientation = exifInterface.getAttributeInt(ExifInterface.TAG_ORIENTATION, ExifInterface.ORIENTATION_NORMAL);

                        switch (orientation) {
                            //被保存图片exif记录只有旋转90度， 和不旋转两种情况
                            case ExifInterface.ORIENTATION_ROTATE_90:
                                shouldRotate = true;
                                break;
                            default:
                                shouldRotate = false;
                                break;
                        }

                        bmp = BitmapFactory.decodeFile(tmpFilename);
                        width = bmp.getWidth();
                        height = bmp.getHeight();

                    } catch (IOException e) {
                        Log.e(TAG, "Err when saving bitmap...");
                        e.printStackTrace();
                        return;
                    }
                }


                if (width > mMaxTextureSize || height > mMaxTextureSize) {
                    float scaling = Math.max(width / (float) mMaxTextureSize, height / (float) mMaxTextureSize);
                    Log.i(TAG, String.format("目标尺寸(%d x %d)超过当前设备OpenGL 能够处理的最大范围(%d x %d)， 现在将图片压缩至合理大小!", width, height, mMaxTextureSize, mMaxTextureSize));
//bug     java.lang.IllegalArgumentException: width and height must be > 0
                    bmp = Bitmap.createScaledBitmap(bmp, (int) (width / scaling), (int) (height / scaling), false);

                    width = bmp.getWidth();
                    height = bmp.getHeight();
                }

                Bitmap bmp2;

                if (shouldRotate) {
                    bmp2 = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(bmp2);

                    if (cameraHelper.mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        Matrix mat = new Matrix();
                        int halfLen = Math.min(width, height) / 2;
                        mat.setRotate(90, halfLen, halfLen);
                        canvas.drawBitmap(bmp, mat, null);
                    } else {
                        Matrix mat = new Matrix();

                        if (isFrontMirror) {
                            mat.postTranslate(-width / 2, -height / 2);
                            mat.postScale(-1.0f, 1.0f);
                            mat.postTranslate(width / 2, height / 2);
                            int halfLen = Math.min(width, height) / 2;
                            mat.postRotate(90, halfLen, halfLen);
                        } else {
                            int halfLen = Math.max(width, height) / 2;
                            mat.postRotate(-90, halfLen, halfLen);
                        }

                        canvas.drawBitmap(bmp, mat, null);
                    }

                    bmp.recycle();
                } else {
                    if (cameraHelper.mCameraId == Camera.CameraInfo.CAMERA_FACING_BACK) {
                        bmp2 = bmp;
                    } else {

                        bmp2 = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
                        Canvas canvas = new Canvas(bmp2);
                        Matrix mat = new Matrix();
                        if (isFrontMirror) {
                            mat.postTranslate(-width / 2, -height / 2);
                            mat.postScale(1.0f, -1.0f);
                            mat.postTranslate(width / 2, height / 2);
                        } else {
                            mat.postTranslate(-width / 2, -height / 2);
                            mat.postScale(-1.0f, -1.0f);
                            mat.postTranslate(width / 2, height / 2);
                        }

                        canvas.drawBitmap(bmp, mat, null);
                    }

                }

                if (config != null) {
//                    CGENativeLibrary.filterImage_MultipleEffectsWriteBack(bmp2, config, intensity);
                }

                photoCallback.takePictureOK(bmp2);

                cameraHelper.mCamera.startPreview();
            }
        });
    }


}
