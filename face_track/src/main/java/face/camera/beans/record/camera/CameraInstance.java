package face.camera.beans.record.camera;

import android.graphics.PixelFormat;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Build;
import android.util.Log;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import face.camera.beans.MyApplication;
import face.camera.beans.record.interfaces.ICamera;

/**
 * Created by wangyang on 15/7/27.
 */


// Camera 仅适用单例
public class CameraInstance {
    public static final String LOG_TA = "CameraInstance>>";

    private static final String ASSERT_MSG = "检测到CameraDevice 为 null! 请检查";

    private Camera mCameraDevice;
    private Camera.Parameters mParams;

    public static final int DEFAULT_PREVIEW_RATE = 30;


    private boolean mIsPreviewing = false;

    private int mDefaultCameraID = -1;

    private static CameraInstance mThisInstance;

    /**
     * 预览的尺寸
     */
    private Camera.Size preSize;
    /**
     * 实际的尺寸
     */
    private Camera.Size picSize;

    private int mFacing = 0;

    private CameraInstance() {
    }

    public static synchronized CameraInstance getInstance() {
        if (mThisInstance == null) {
            mThisInstance = new CameraInstance();
        }
        return mThisInstance;
    }

    public boolean isPreviewing() {
        return mIsPreviewing;
    }

    public interface CameraOpenCallback {
        void cameraReady();
    }

    public boolean tryOpenCamera(ICamera.Config config, CameraOpenCallback callback) {
        return tryOpenCamera(config, callback, Camera.CameraInfo.CAMERA_FACING_BACK);
    }

    public int getFacing() {
        return mFacing;
    }

    public synchronized boolean tryOpenCamera(ICamera.Config mConfig, CameraOpenCallback callback, int facing) {
        Log.i(LOG_TA, "try open camera...");

        try {
            if (Build.VERSION.SDK_INT > Build.VERSION_CODES.FROYO) {
                int numberOfCameras = Camera.getNumberOfCameras();

                Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
                for (int i = 0; i < numberOfCameras; i++) {
                    Camera.getCameraInfo(i, cameraInfo);
                    if (cameraInfo.facing == facing) {
                        mDefaultCameraID = i;
                        mFacing = facing;
                    }
                }
            }
            stopPreview();
            if (mCameraDevice != null)
                mCameraDevice.release();

            if (mDefaultCameraID >= 0) {
                mCameraDevice = Camera.open(mDefaultCameraID);
            } else {
                mCameraDevice = Camera.open();
                mFacing = Camera.CameraInfo.CAMERA_FACING_BACK; //default: back facing
            }
        } catch (Exception e) {
            Log.e(LOG_TA, "Open Camera Failed!");
            e.printStackTrace();
            mCameraDevice = null;
            return false;
        }

        if (mCameraDevice != null) {
            Log.i(LOG_TA, "Camera opened!");

            try {
                initCamera(mConfig, DEFAULT_PREVIEW_RATE);
            } catch (Exception e) {
                mCameraDevice.release();
                mCameraDevice = null;
                return false;
            }

            if (callback != null) {
                callback.cameraReady();
            }

            return true;
        }

        return false;
    }

    public synchronized void stopCamera() {
        if (mCameraDevice != null) {
            mIsPreviewing = false;
            mCameraDevice.stopPreview();
            mCameraDevice.setPreviewCallback(null);
            mCameraDevice.release();
            mCameraDevice = null;
        }
    }

    public synchronized boolean isCameraOpened() {

        try {
            mCameraDevice = Camera.open();
            if (mCameraDevice != null) {
                return true;
            } else {
                return false;
            }

        } catch (Exception e) {
            Log.e(LOG_TA, "Open Camera Failed!");
            e.printStackTrace();
            mCameraDevice = null;
            return false;
        }
    }

    public synchronized void startPreview(SurfaceTexture texture, Camera.PreviewCallback callback) {
        Log.i(LOG_TA, "Open Camera Camera startPreview...");
        if (mIsPreviewing) {
            Log.e(LOG_TA, "Err: Open Camera is previewing...");
            return;
        }

        if (mCameraDevice != null) {
            try {
                mCameraDevice.setPreviewTexture(texture);
//                mCameraDevice.addCallbackBuffer(callbackBuffer);
//                mCameraDevice.setPreviewCallbackWithBuffer(callback);
                mCameraDevice.setPreviewCallbackWithBuffer(callback);
            } catch (IOException e) {
                e.printStackTrace();
            }

            mCameraDevice.startPreview();
            mIsPreviewing = true;
            Log.i(LOG_TA, "Open Camera Camera startedPreview");

        }
    }

    public void startPreview(SurfaceTexture texture) {
        startPreview(texture, null);
    }

    public void startPreview(Camera.PreviewCallback callback) {
        startPreview(null, callback);
    }

    public synchronized void stopPreview() {
        if (mIsPreviewing && mCameraDevice != null) {
            Log.i(LOG_TA, "Camera stopPreview...");
            mIsPreviewing = false;
            mCameraDevice.stopPreview();
            mCameraDevice.release();
            mCameraDevice = null;

        }
    }

    public synchronized Camera.Parameters getParams() {
        if (mCameraDevice != null)
            return mCameraDevice.getParameters();
        assert mCameraDevice != null : ASSERT_MSG;
        return null;
    }

    public synchronized void setParams(Camera.Parameters param) {
        if (mCameraDevice != null) {
            mParams = param;
            mCameraDevice.setParameters(mParams);
        }
        assert mCameraDevice != null : ASSERT_MSG;
    }

    public Camera getCameraDevice() {
        return mCameraDevice;
    }

    //保证从大到小排列
    private Comparator<Camera.Size> comparatorBigger = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            int w = rhs.width - lhs.width;
            if (w == 0)
                return rhs.height - lhs.height;
            return w;
        }
    };

    //保证从小到大排列
    private Comparator<Camera.Size> comparatorSmaller = new Comparator<Camera.Size>() {
        @Override
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            int w = lhs.width - rhs.width;
            if (w == 0)
                return lhs.height - rhs.height;
            return w;
        }
    };

    public void initCamera(ICamera.Config mConfig, int previewRate) {
        if (mCameraDevice == null) {
            Log.e(LOG_TA, "initCamera: Camera is not opened!");
            return;
        }

        mParams = mCameraDevice.getParameters();
        List<Integer> supportedPictureFormats = mParams.getSupportedPictureFormats();

        for (int fmt : supportedPictureFormats) {
            Log.i(LOG_TA, String.format("Picture Format: %x", fmt));
        }

        mParams.setPictureFormat(PixelFormat.JPEG);

        List<Camera.Size> picSizes = mParams.getSupportedPictureSizes();
        Camera.Size picSz = null;

        preSize = getPropPreviewSize(mParams.getSupportedPreviewSizes(), mConfig.rate,
                mConfig.minPreviewWidth);
        picSize = getPropPictureSize(mParams.getSupportedPictureSizes(), mConfig.rate,
                mConfig.minPictureWidth);
        mParams.setPictureSize(picSize.width, picSize.height);
        mParams.setPreviewSize(preSize.width, preSize.height);

//            mCamera.setParameters(param);
//            mCamera.setParams(param);
//            Camera.Size pre = param.getPreviewSize();
//            Camera.Size pic = param.getPictureSize();

        List<Integer> frameRates = mParams.getSupportedPreviewFrameRates();

        int fpsMax = 0;

        for (Integer n : frameRates) {
            Log.i(LOG_TA, "Supported frame rate: " + n);
            if (fpsMax < n) {
                fpsMax = n;
            }
        }

//        mParams.setPreviewSize(prevSz.width, prevSz.height);
//        mParams.setPictureSize(picSz.width, picSz.height);

        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO)) {
            mParams.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_VIDEO);
        }

        previewRate = fpsMax;
        mParams.setPreviewFrameRate(previewRate); //设置相机预览帧率
//        mParams.setPreviewFpsRange(20, 60);

        try {
            mCameraDevice.setParameters(mParams);
        } catch (Exception e) {
            e.printStackTrace();
        }


        mParams = mCameraDevice.getParameters();

        Camera.Size szPic = mParams.getPictureSize();
        Camera.Size szPrev = mParams.getPreviewSize();


        Log.i(LOG_TA, String.format("Camera Picture Size: %d x %d", szPic.width, szPic.height));
        Log.i(LOG_TA, String.format("Camera Preview Size: %d x %d", szPrev.width, szPrev.height));
    }

    public synchronized void setFocusMode(String focusMode) {

        if (mCameraDevice == null)
            return;

        mParams = mCameraDevice.getParameters();
        List<String> focusModes = mParams.getSupportedFocusModes();
        if (focusModes.contains(focusMode)) {
            mParams.setFocusMode(focusMode);
        }
    }

    public synchronized void setPictureSize(int width, int height, boolean isBigger) {

    }

//    public void focusAtPoint(float x, float y, final Camera.AutoFocusCallback callback) {
//        focusAtPoint(x, y, 0.2f, callback);
//    }


    /**
     * 修正尺寸
     *
     * @param list
     * @param th
     * @param minWidth
     * @return
     */

    private Camera.Size getPropPictureSize(List<Camera.Size> list, float th, int minWidth) {
        Collections.sort(list, sizeComparator);
        int i = 0;
        for (Camera.Size s : list) {
            if ((s.height >= minWidth) && equalRate(s, th)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    private Camera.Size getPropPreviewSize(List<Camera.Size> list, float th, int minWidth) {
        Collections.sort(list, sizeComparator);

        int i = 0;
        for (Camera.Size s : list) {
            if ((s.height >= minWidth) && equalRate(s, th)) {
                break;
            }
            i++;
        }
        if (i == list.size()) {
            i = 0;
        }
        return list.get(i);
    }

    private static boolean equalRate(Camera.Size s, float rate) {
        float r = (float) (s.width) / (float) (s.height);
        if (Math.abs(r - rate) <= 0.03) {
            return true;
        } else {
            return false;
        }
    }

    private Comparator<Camera.Size> sizeComparator = new Comparator<Camera.Size>() {
        public int compare(Camera.Size lhs, Camera.Size rhs) {
            if (lhs.height == rhs.height) {
                return 0;
            } else if (lhs.height > rhs.height) {
                return 1;
            } else {
                return -1;
            }
        }
    };


    public synchronized void focusAtPoint(Point point, final Camera.AutoFocusCallback callback) {
        if (mCameraDevice == null) {
            Log.e(LOG_TA, "Error: focus after release.");
            return;
        }

        Camera.Parameters parameters = mCameraDevice.getParameters();
        boolean supportFocus = true;
        boolean supportMetering = true;
        //不支持设置自定义聚焦，则使用自动聚焦，返回
        if (parameters.getMaxNumFocusAreas() <= 0) {
            supportFocus = false;
        }
        if (parameters.getMaxNumMeteringAreas() <= 0) {
            supportMetering = false;
        }
        List<Camera.Area> areas = new ArrayList<Camera.Area>();
        List<Camera.Area> areas1 = new ArrayList<Camera.Area>();
        //再次进行转换
        point.x = (int) (((float) point.x) / MyApplication.screenWidth * 2000 - 1000);
        point.y = (int) (((float) point.y) / MyApplication.screenHeight * 2000 - 1000);

        int left = point.x - 300;
        int top = point.y - 300;
        int right = point.x + 300;
        int bottom = point.y + 300;
        left = left < -1000 ? -1000 : left;
        top = top < -1000 ? -1000 : top;
        right = right > 1000 ? 1000 : right;
        bottom = bottom > 1000 ? 1000 : bottom;
        areas.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
        areas1.add(new Camera.Area(new Rect(left, top, right, bottom), 100));
        if (supportFocus) {
            parameters.setFocusAreas(areas);
        }
        if (supportMetering) {
            parameters.setMeteringAreas(areas1);
        }

        try {
            mCameraDevice.setParameters(parameters);// 部分手机 会出Exception（红米）
            mCameraDevice.autoFocus(callback);

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
