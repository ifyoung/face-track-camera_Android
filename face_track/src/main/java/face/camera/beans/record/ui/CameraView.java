package face.camera.beans.record.ui;

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
import android.opengl.GLSurfaceView;
import android.os.Handler;
import android.os.Looper;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;

import face.camera.beans.record.camera.CameraController;
import face.camera.beans.record.draw.CameraDrawer;


/**
 * description:
 * Created by aserbao on 2018/5/15.
 */


public class CameraView extends GLSurfaceView implements GLSurfaceView.Renderer, SurfaceTexture.OnFrameAvailableListener {

    public CameraDrawer mCameraDrawer;
    public CameraController mCameraController;
    private int dataWidth = 0, dataHeight = 0;
    private int cameraId;
    private boolean isSetParm = false;
    String LOG_TAG = "CameraView";

    public CameraView(Context context) {
        this(context, null);
    }

    public CameraView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        Log.e(LOG_TAG, "Open Camera to onSurface-init");

        /**初始化OpenGL的相关信息*/
        setEGLContextClientVersion(2);//设置版本
        setRenderer(this);//设置Renderer
        setRenderMode(RENDERMODE_WHEN_DIRTY);//主动调用渲染
//        setRenderMode(RENDERMODE_CONTINUOUSLY);//主动调用渲染
        setPreserveEGLContextOnPause(true);//保存Context当pause时
        setCameraDistance(100);//相机距离
        /**初始化Camera的绘制类*/
        mCameraDrawer = new CameraDrawer(getResources());
        /**初始化相机的管理类*/
        mCameraController = new CameraController();
    }

    public void renderStart() {
//     setRenderer(this);//设置Renderer
//       setRenderMode(RENDERMODE_WHEN_DIRTY);//主动调用渲染
//       if (!isSetParm) {
//            open(cameraId);
//       }
        handler.postDelayed(showRunnable, 1000);//在这里设置延时时间，单位是毫秒

    }

    Handler handler = new Handler(Looper.getMainLooper());


    Runnable showRunnable = new Runnable() {
        @Override
        public void run() {
//            handler.postDelayed(showRunnable, 300);//在这里设置延时时间，单位是毫秒
            //在这里进行想做的事
            if (!isSetParm) {
                Log.e(LOG_TAG, "Open Camera to Render-handler");
                open(cameraId);
                queueEvent(new Runnable() {
                    @Override
                    public void run() {
                        if (tempGL10 != null && tempGLW > 0) {
//                            mCameraDrawer.onSurfaceCreated(tempGL10, tempEGLConfig);
//                            mCameraDrawer.onSurfaceChanged(tempGL10, tempGLW, tempGLH);
//                            mCameraDrawer.onDrawFrame(tempGL10);
//                            requestRender();

                            onSurfaceChanged(tempGL10, tempGLW, tempGLH);//告诉我墙在哪儿？？？？

                        }
                    }
                });


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
        Log.e(LOG_TAG, "Open Camera to onSurfaceCreated");
        tempEGLConfig = config;
        mCameraDrawer.onSurfaceCreated(gl, config);
        //在RecorderActivity->onResume后面？？
        tempGL10 = gl;
        if (!isSetParm) {
            int texSize[] = new int[1];
            GLES20.glGetIntegerv(GLES20.GL_MAX_TEXTURE_SIZE, texSize, 0);
            mMaxTextureSize = texSize[0];
            Log.e(LOG_TAG, "Open Camera to Render");

            open(cameraId);
        }
//        mCameraDrawer.setPreviewSize(dataWidth,dataHeight);
    }


    @Override
    public void onSurfaceChanged(GL10 gl, int width, int height) {

        tempGL10 = gl;
        tempGLW = width;
        tempGLH = height;
        Log.e(LOG_TAG, "Open Camera to onSurfaceChanged" + tempGLW + ">>" + tempGLH);

        mCameraDrawer.onSurfaceChanged(gl, width, height);

    }

    @Override
    public void onDrawFrame(GL10 gl) {
        Log.e(LOG_TAG, "Open Camera to onDrawFrame" + gl + tempGLW + ">>" + tempGLH);

        if (isSetParm) {
            mCameraDrawer.onDrawFrame(gl);
        }
    }

    @Override
    public void onFrameAvailable(SurfaceTexture surfaceTexture) {
        this.requestRender();
    }

    private void open(int cameraId) {

//        mCameraController.close();//释放相机

        //打开相机
        mCameraController.open(cameraId, new CameraController.CameraIsReady() {
            @Override
            public void ready() {

                SurfaceTexture texture = mCameraDrawer.getTexture();
                if (texture == null) {
                    return;
                }

                mCameraDrawer.setCameraId(cameraId);
                final Point previewSize = mCameraController.getPreviewSize();
                dataWidth = previewSize.x;
                dataHeight = previewSize.y;

                mCameraDrawer.setPreviewSize(dataWidth, dataHeight);


                texture.setOnFrameAvailableListener(CameraView.this);
                mCameraController.setPreviewTexture(texture);
                Log.e(LOG_TAG, "Open Camera to Render-finish");

                stickerInit();
            }
        });
//        mCameraController.preview();
    }

    public void switchCamera() {
        cameraId = cameraId == 0 ? 1 : 0;
//        open(cameraId);
        queueEvent(new Runnable() {
            @Override
            public void run() {

                mCameraController.mCamera.stopCamera();
//                onSwitchCamera();
                open(cameraId);

                requestRender();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();

//        resumePreview();
//        if (isSetParm) {
//            open(cameraId);
//        }
    }

    public void onDestroy() {
        if (mCameraController != null) {
            mCameraController.close();
        }
    }

    public int getCameraId() {
        return cameraId;
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
        mCameraController.onFocus(point, callback);
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

    public synchronized void takePicture(final TakePictureCallback photoCallback, Camera.ShutterCallback shutterCallback, final String config, final float intensity, final boolean isFrontMirror) {

        Camera.Parameters params = mCameraController.mCamera.getParams();

        if (photoCallback == null || params == null || mCameraController.mCamera.getCameraDevice() == null) {
            Log.e(LOG_TAG, "takePicture after release!");
            if (photoCallback != null) {
                photoCallback.takePictureOK(null);
            }
            return;
        }

        try {
            params.setRotation(90);
            mCameraController.mCamera.setParams(params);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Error when takePicture: " + e.toString());
            if (photoCallback != null) {
                photoCallback.takePictureOK(null);
            }
            return;
        }

        mCameraController.mCamera.getCameraDevice().takePicture(shutterCallback, null, new Camera.PictureCallback() {
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
                    Log.i(LOG_TAG, "Cache image to get exif.");

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
                        Log.e(LOG_TAG, "Err when saving bitmap...");
                        e.printStackTrace();
                        return;
                    }
                }


                if (width > mMaxTextureSize || height > mMaxTextureSize) {
                    float scaling = Math.max(width / (float) mMaxTextureSize, height / (float) mMaxTextureSize);
                    Log.i(LOG_TAG, String.format("目标尺寸(%d x %d)超过当前设备OpenGL 能够处理的最大范围(%d x %d)， 现在将图片压缩至合理大小!", width, height, mMaxTextureSize, mMaxTextureSize));

                    bmp = Bitmap.createScaledBitmap(bmp, (int) (width / scaling), (int) (height / scaling), false);

                    width = bmp.getWidth();
                    height = bmp.getHeight();
                }

                Bitmap bmp2;

                if (shouldRotate) {
                    bmp2 = Bitmap.createBitmap(height, width, Bitmap.Config.ARGB_8888);

                    Canvas canvas = new Canvas(bmp2);

                    if (mCameraController.mCamera.getFacing() == Camera.CameraInfo.CAMERA_FACING_BACK) {
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
                    if (mCameraController.mCamera.getFacing() == Camera.CameraInfo.CAMERA_FACING_BACK) {
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

                mCameraController.mCamera.getCameraDevice().startPreview();
            }
        });
    }


}
