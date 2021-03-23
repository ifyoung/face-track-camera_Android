package face.camera.beans.arc;

import android.app.Dialog;
import android.content.Context;
import android.util.Log;

import com.arcsoft.face.ActiveFileInfo;
import com.arcsoft.face.ErrorInfo;
import com.arcsoft.face.FaceEngine;
import com.arcsoft.face.enums.DetectMode;
import com.arcsoft.face.enums.RuntimeABI;
import com.luck.picture.lib.tools.ToastUtils;
//import com.yanzhenjie.album.widget.LoadingDialog;

import face.camera.beans.R;
import face.camera.beans.arc.common.Constants;
import face.camera.beans.arc.util.ConfigUtil;
import face.camera.beans.base.activity.BaseActivity;
import face.camera.beans.ble.Preferences;
import face.camera.beans.dialog.LoadingDialog;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

import static face.camera.beans.ble.GlobalStatic.ISARCACTIVE;

public class ActiveInstance {
    static String TAG = "ActiveInstance";

    static final ActiveInstance ourInstance = new ActiveInstance();
    LoadingDialog dialog;

    public static ActiveInstance getInstance() {
        return ourInstance;
    }

    private ActiveInstance() {

    }

    /**
     * 激活引擎
     *
     * @param
     */
    public void activeEngine(Context context) {
//        if (!libraryExists) {
//            showToast(getString(R.string.library_not_found));
//            return;
//        }
//        if (!checkPermissions(NEEDED_PERMISSIONS)) {
//            ActivityCompat.requestPermissions(this, NEEDED_PERMISSIONS, ACTION_REQUEST_PERMISSIONS);
//            return;
//        }
//        if (view != null) {
//            view.setClickable(false);
//        }
        if (dialog == null) {
            dialog = new LoadingDialog(context);
            dialog.setLoadTxt("激活中...");
            dialog.showPleaseDialog();
        }

        Observable.create(new ObservableOnSubscribe<Integer>() {
            @Override
            public void subscribe(ObservableEmitter<Integer> emitter) {
                RuntimeABI runtimeABI = FaceEngine.getRuntimeABI();
                Log.i(TAG, "subscribe: getRuntimeABI() " + runtimeABI);

                long start = System.currentTimeMillis();
                int activeCode = FaceEngine.activeOnline(context, Constants.APP_ID, Constants.SDK_KEY);
                Log.i(TAG, "subscribe cost: " + (System.currentTimeMillis() - start));
                emitter.onNext(activeCode);
            }
        })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Integer>() {
                    @Override
                    public void onSubscribe(Disposable d) {
                        Log.i(TAG, "subscribe Disposable: " + d.toString());
                        if (dialog != null) {
//                            dialog.setLoadTxt("激活失败");

                        }
                    }

                    @Override
                    public void onNext(Integer activeCode) {
                        Log.i(TAG, "subscribe Disposable: " + activeCode.toString());

                        if (activeCode == ErrorInfo.MOK) {
                            Log.i(TAG, context.getString(R.string.active_success));
                            Preferences.savePreferences(context.getApplicationContext(), "ARC", "ARC", 0);
                            ISARCACTIVE = true;
                        } else if (activeCode == ErrorInfo.MERR_ASF_ALREADY_ACTIVATED) {
                            Log.i(TAG, context.getString(R.string.already_activated));
                            Preferences.savePreferences(context.getApplicationContext(), "ARC", "ARC", 0);
                            ISARCACTIVE = true;
                        } else {
                            ISARCACTIVE = false;

                            Log.i(TAG, context.getString(R.string.active_failed, activeCode));
                            if (dialog != null) {
                                dialog.setLoadTxt(context.getString(R.string.active_failed, activeCode));
                            }
                        }

                        if (ISARCACTIVE) {
                            initEngine(context);
                        }

//                        ActiveFileInfo activeFileInfo = new ActiveFileInfo();
//                        int res = FaceEngine.getActiveFileInfo(ChooseFunctionActivity.this, activeFileInfo);
//                        if (res == ErrorInfo.MOK) {
//                            Log.i(TAG, activeFileInfo.toString());
//                        }

                    }

                    @Override
                    public void onError(Throwable e) {
//                        showToast(e.getMessage());
//                        if (view != null) {
//                            view.setClickable(true);
//                        }
                        if (dialog != null) {
                            dialog.setLoadTxt(e.getMessage());
                        }
                    }

                    @Override
                    public void onComplete() {
                        if (dialog != null) {
                            dialog.dismissDialog();
                            dialog = null;
                        }
                    }
                });

    }

    private int afCode = -1;
    public static int processMask = FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS;
    public static FaceEngine faceEngine;

    public void initEngine(Context context) {
        faceEngine = new FaceEngine();
        afCode = faceEngine.init(context.getApplicationContext(), DetectMode.ASF_DETECT_MODE_VIDEO, ConfigUtil.getFtOrient(context.getApplicationContext()),
                16, 20, FaceEngine.ASF_FACE_DETECT | FaceEngine.ASF_AGE | FaceEngine.ASF_FACE3DANGLE | FaceEngine.ASF_GENDER | FaceEngine.ASF_LIVENESS);
        Log.i(TAG, "initEngine:  init: " + afCode);
        if (afCode != ErrorInfo.MOK) {
//            showToast( getString(R.string.init_failed, afCode));
            Log.i(TAG, "initEngine:  init_failed: " + afCode);
            if (dialog != null) {
                dialog.setLoadTxt("激活失败");
            }
        } else {
            if (dialog != null) {
                dialog.setLoadTxt("激活成功");
                try {
                    Thread.sleep(300);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                dialog.dismissDialog();
            }
        }
    }

    public void unInitEngine() {

        if (afCode == 0 && faceEngine != null) {
            afCode = faceEngine.unInit();
            Log.i(TAG, "unInitEngine: " + afCode);
        }
    }


}
