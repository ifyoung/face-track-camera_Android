package face.camera.beans.record.camera;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Point;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.util.Log;

import face.camera.beans.record.interfaces.ICamera;

//import android.hardware.Camera;

/**
 * description:
 * Created by aserbao on 2018/5/15.
 */


public class CameraController implements ICamera {
    /**
     * 相机的宽高及比例配置
     */
    private ICamera.Config mConfig;
    /**
     * 相机实体
     */
//    private Camera mCamera;
    public CameraInstance mCamera;


    private Point mPreSize;
    private Point mPicSize;

    static String LOG_TAG = "CameraController";

    public CameraController() {
        /**初始化一个默认的格式大小*/
        mConfig = new ICamera.Config();
        mConfig.minPreviewWidth = 720;
        mConfig.minPictureWidth = 720;
        mConfig.rate = 1.778f;
        mCamera = CameraInstance.getInstance();

    }

    public void open(int cameraId, CameraIsReady cameraIsReady) {

        if (mCamera != null) {
            mCamera.tryOpenCamera(mConfig, new CameraInstance.CameraOpenCallback() {
                @Override
                public void cameraReady() {

                    Camera.Size pre = mCamera.getParams().getPreviewSize();
                    Camera.Size pic = mCamera.getParams().getPictureSize();
                    mPicSize = new Point(pic.height, pic.width);
                    mPreSize = new Point(pre.height, pre.width);
                    if (cameraIsReady != null) {
                        cameraIsReady.ready();
                    }
                }
            }, cameraId);
        }
    }

    public interface CameraIsReady {
        void ready();
    }

    @Override
    public void setPreviewTexture(SurfaceTexture texture) {
        if (mCamera != null) {
            mCamera.startPreview(texture);

        }
    }

    @Override
    public void setConfig(ICamera.Config config) {
        this.mConfig = config;
    }

    @Override
    public void setOnPreviewFrameCallback(final ICamera.PreviewFrameCallback callback) {
        if (mCamera != null) {
            mCamera.startPreview(new Camera.PreviewCallback() {
                @Override
                public void onPreviewFrame(byte[] data, Camera camera) {
                    callback.onPreviewFrame(data, mPreSize.x, mPreSize.y);
                }
            });
        }
    }

    @Override
    public void preview() { //setPreviewTexture
//        if (mCamera != null) {
//            mCamera.startPreview();
//        }
    }

    @Override
    public Point getPreviewSize() {
        return mPreSize;
    }

    @Override
    public Point getPictureSize() {
        return mPicSize;
    }

    @Override
    public boolean close() {
        if (mCamera != null) {
//            mCamera.stopPreview();
            mCamera.stopPreview();
//            mCamera = null;
        }
//        try {
//            Thread.sleep(300);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        return false;
    }

    public void onFocus(Point point, Camera.AutoFocusCallback callback) {
//        Camera.Parameters parameters = mCamera.getParameters();
        mCamera.focusAtPoint(point, callback);

    }

    // mode value should be:
    //    Camera.Parameters.FLASH_MODE_AUTO;
    //    Camera.Parameters.FLASH_MODE_OFF;
    //    Camera.Parameters.FLASH_MODE_ON;
    //    Camera.Parameters.FLASH_MODE_RED_EYE
    //    Camera.Parameters.FLASH_MODE_TORCH 等
    public synchronized boolean setFlashLightMode(Context context, boolean mIsCameraBackForward, String mode) {

        if (!context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA_FLASH)) {
            Log.e(LOG_TAG, "No flash light is supported by current device!");
            return false;
        }

        if (!mIsCameraBackForward) {
            return false;
        }

        Camera.Parameters parameters = mCamera.getParams();

        if (parameters == null)
            return false;

        try {

            if (!parameters.getSupportedFlashModes().contains(mode)) {
                Log.e(LOG_TAG, "Invalid Flash Light Mode!!!");
                return false;
            }

            parameters.setFlashMode(mode);
            mCamera.setParams(parameters);
        } catch (Exception e) {
            Log.e(LOG_TAG, "Switch flash light failed, check if you're using front camera.");
            return false;
        }

        return true;
    }


}
