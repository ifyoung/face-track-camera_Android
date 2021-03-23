package face.camera.beans.net;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ProgressBar;

import androidx.core.content.FileProvider;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.luck.picture.lib.tools.ToastUtils;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.callback.StringCallback;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;

import face.camera.beans.BuildConfig;
import face.camera.beans.R;
import face.camera.beans.arc.common.Constants;
import face.camera.beans.ble.GlobalStatic;
import face.camera.beans.ble.L;
import face.camera.beans.ble.Preferences;
import face.camera.beans.dialog.CustomDialog;
import face.camera.beans.record.utils.AesUtil;
import okhttp3.Call;

import static face.camera.beans.ble.GlobalStatic.MAIN_ENTER;
import static face.camera.beans.ble.GlobalStatic.UPDateApkName;


//下载60，解压20，web加载20；
public class UpdateManager {
    /* 下载中 */
    private static final int DOWNLOAD = 1;
    /* 下载结束 */
    private static final int DOWNLOAD_FINISH = 2;
    private static final int DOWNLOAD_PERMISSION = 3;


    /* 下载中 */
    private static final int DOWNLOADAPK = 4;
    /* 下载结束 */
    private static final int DOWNLOAD_APK_FINISH = 5;
    private static final int DOWNLOAD_APK_PERMISSION = 6;

    /* 保存解析的XML信息 */
//    HashMap<String, String> mHashMap = new HashMap<>();
    /* 下载保存路径 */
    private String mSavePath;
    /* 记录进度条数量 */
    private int progress;
    /* 是否取消更新 */
    private boolean cancelUpdate = false;

    private Context mContext;
    //    private boolean isManual;
    private int updateCase;//1:静默,小圆点;2:主页;3:设置;
    /* 更新进度条 */
    private ProgressBar mProgress;
    private Dialog mDownloadDialog;

    File apkFile;

    public boolean isHasNet;
    public int part1Progress = 80;


    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                // 正在下载
                case DOWNLOAD:
                    // 设置进度条位置
//                    mProgress.setProgress(progress);
                    L.d("下载进度" + progress);
                    if (updateFinishListener != null) {
                        updateFinishListener.onProgress(progress);
                    }
                    break;
                case DOWNLOAD_PERMISSION:
                    // 设置进度条位置

                    break;
                case DOWNLOAD_FINISH:
                    // 安装文件
//                    installApk();
                    L.d("下载完成");
//                    UnZipUtils.unpackOne(apkFile, mSavePath);

                    break;

                case DOWNLOADAPK:
                    L.d("下载进度apk" + progress);
                    mProgress.setProgress(progress);
                    break;
                case DOWNLOAD_APK_FINISH:
                    L.d("DOWNLOAD_APK_FINISH", "DOWNLOAD_APK_FINISH");
                    // 安装文件
                    installApk();
                    break;

                default:
                    break;
            }
        }

        ;
    };

    public UpdateManager(Context context, int updateCase) {
        this.mContext = context;
        this.updateCase = updateCase;
    }

//    private volatile static UpdateManager updateManager;
//    private UpdateManager (){}
//    public static UpdateManager getUpdateManager(Context context, boolean isManual) {
//        if (updateManager == null) {
//            synchronized (UpdateManager.class) {
//                if (updateManager == null) {
//                    updateManager = new UpdateManager();
//                }
//                updateManager.mContext=context;
//                updateManager.isManual=isManual;
//            }
//        }
//        return updateManager;
//    }


    public void getActiveCode() {

        if (NetUtils.isConnected(mContext)) {
//			progressW("检测中，请稍后。。。");
            isHasNet = true;
            toActive(mContext);
//toUpdateApk();
        } else {
            isHasNet = false;
//            if (updateCase == 3)
//            ToastUtils.showMidToast(mContext, "网络未连接，继续使用请打开网络链接");
            ToastUtils.s(mContext, "网络未连接，继续使用请打开网络链接");
        }

    }

    /**
     * 检测软件更新
     */
    public void checkUpdate() {

        if (NetUtils.isConnected(mContext)) {
//			progressW("检测中，请稍后。。。");
            isHasNet = true;
//            toActive();
            toUpdateApk();
        } else {
            isHasNet = false;
//            if (updateCase == 3)
//            ToastUtils.showMidToast(mContext, "网络未连接，继续使用请打开网络链接");
            ToastUtils.s(mContext, "网络未连接，继续使用请打开网络链接");
        }

    }

    public interface UpdateFinish {
        void done();

        void onProgress(int progress);
    }

    public interface NoUpdate {

        void done();
    }

    private UpdateFinish updateFinishListener;
    private NoUpdate noUpdateListener;


    public void setUpdateFinishListener(UpdateFinish updateFinishListener) {
        this.updateFinishListener = updateFinishListener;
    }

    public void setNoUpdateListener(NoUpdate noUpdateListener) {
        this.noUpdateListener = noUpdateListener;
    }

//    private UpdateModel updateModel;
//    private UpdateModel upApkdateModel;

    private int reConnectTimes = 0;

    /**
     * 检查Zip是否有更新版本
     *
     * @return
     */
    private void toActive(Context context) {
        HashMap<String, String> upHashMap = new HashMap<>();

//        这个DEVICE_ID可以同通过下面的方法获取：
        TelephonyManager tm = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission") String DEVICE_ID = tm.getDeviceId();
//        String DEVICE_ID = tm.getMeid();
        String ts = "aaaaaaaaaaaaaaaaaaqqqqqqqqqqqqq";
        if (DEVICE_ID == null || DEVICE_ID.length() < 0) {
            DEVICE_ID = ts;
        }

//        上面的方法在api26已经放弃。官方建议使用getImei()和getMeid()这两个方法得到相应的值。

        String test = "admin_1563882792706_bfaa19b5e4712d28464b4687afeb9196";
//        try {
//            ts = AesUtil.aesPKCS7PaddingEncrypt(ts);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

        String codeUp = "{\"type\":\"android\",\"appid\":\"" + DEVICE_ID + "\"}";

        String code = SignUtil.signEncode(codeUp);
        codeUp = code == null ? "" : code;
        upHashMap.put("code", codeUp);
        reConnectTimes = reConnectTimes + 1;
        L.e("toActive" + "isHasNet" + isHasNet, upHashMap.toString());

        OkHttpUtils.get().url(GlobalStatic.activeUrl).params(upHashMap).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
//                if (updateCase == 3)
//                    ToastUtils.showToast(mContext, e.toString());
                L.e("toActive-onError" + "isHasNet" + isHasNet + e.getMessage());
                if (reConnectTimes < 2) {
//                    toUpdate();
                } else {
                    ToastUtils.s(mContext, e.toString());
                    reConnectTimes = 0;
                }


            }

            @Override
            public void onResponse(String response, int id) {
                reConnectTimes = 0;
                try {
                    L.e("升级接口" + response + "本地版本");

                    JSONObject jsonObject = new JSONObject(response);
                    if (jsonObject.getInt("status") == 200) {
                        JSONObject jsonObjectData = (JSONObject) jsonObject.get("data");
                        Constants.APP_ID = jsonObjectData.getString("appId");
                        Constants.SDK_KEY = jsonObjectData.getString("appSdkkey");
                        L.e("ARC——APPID" + Constants.APP_ID + ">>" + Constants.SDK_KEY + "本地版本");

                    } else {

                        if (noUpdateListener != null) {
                            noUpdateListener.done();
                        }
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void toUpdateApk() {

        // 获取当前软件版本
        final String versionName = getVersionName(mContext) + "." + getVersionCode(mContext);

        //    http://47.105.34.210/ydcp/app/findZipNewVersion?
//    tokens=admin_1563462121870_9901c0e76d79b384a40a415d8b0564e3
//    &appVersion=1.2
//    &pt=android&dev=true
//        HashMap upHashMap = new HashMap();
        HashMap<String, String> upHashMap = new HashMap<>();
//        upHashMap.put("tokens", "admin_1563462121870_9901c0e76d79b384a40a415d8b0564e3");

//        String test = "CEIu034A2MXL7YMo9UwPE7EXUtXidaKWNXmX2KRqwVc%3D";
        String verCode = "{\"type\":\"android\",\"version\":\"" + versionName + "\"}";
        String code = SignUtil.signEncode(verCode);
        String codeDes = SignUtil.signDecode(code);

//        String codeDesTest = SignUtil.signDecode(test);

        verCode = code == null ? "" : code;
//        upHashMap.put("code", URLEncoder.encode(verCode));
        upHashMap.put("code", verCode);
//        reConnectTimes = reConnectTimes + 1;

        OkHttpUtils.get().url(GlobalStatic.updateUrl).params(upHashMap).build().execute(new StringCallback() {
            @Override
            public void onError(Call call, Exception e, int id) {
//                if (updateCase == 3)
//                    ToastUtils.showToast(mContext, e.toString());
                L.e("toUpdateApk-onError" + "isHasNet" + isHasNet + e.getMessage());
//                if (reConnectTimes < 2) {
//                    toUpdate();
//                } else {
//                    ToastUtils.showToast(mContext, e.toString());
//                    reConnectTimes = 0;
//                }


            }

            @Override
            public void onResponse(String response, int id) {
//                reConnectTimes = 0;
//                showNoticeDialog();

                try {

//                    "id": 6,
//                            "appType": "ios", app类型 ios android
//                    "appVersion": "10.1", 当前可更新最高版本
//                    "appUrl": null, #下载地址
//                    "createDate": 1593893200000

                    JSONObject jsonObject = new JSONObject(response);
                    L.e("toUpdateApk-升级接口APK" + response + ">>" + jsonObject.get("data"));

                    Object ob = jsonObject.get("data");
                    if (jsonObject.getInt("status") == 200 && ob != null) {
//                        versionName = "0";

//User user = gson.fromJson(jsonString, User.class);
//                            upApkdateModel = gson.fromJson(jsonObject.get("data").toString(), UpdateModel.class);
//                        JSONObject jsonObjectData = (JSONObject) jsonObject.get("data");
                        JSONObject jsonObjectVer = jsonObject.getJSONObject("data");
                        String appVer = jsonObjectVer.getString("appVersion");
                        String downLoadURl = jsonObjectVer.getString("appUrl");

                        L.e("升级接口-updateModel-" + jsonObject.get("data") + ">>" + appVer + ">>" + downLoadURl);
                        if (!TextUtils.isEmpty(downLoadURl) && appVer.compareTo(versionName) > 0) {
//                                downloadZip();
                            // 启动新线程下载软件
//                                new downloadApkThread().start();
                            staticDownloadUrl = downLoadURl;
                            showNoticeDialog();

                        }


                    } else {

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        });
    }


    /**
     * 获取版本号的名称
     *
     * @param context
     * @return
     */
    public static String getVersionName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
//            return packageInfo.versionCode;//看不见
            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "1.0";
    }

    public static int getVersionCode(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.versionCode;//看不见
//            return packageInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 1;
    }

    /**
     * 显示软件更新对话框
     */
    private void showNoticeDialog() {

        CustomDialog.Builder noticeDialog = new CustomDialog.Builder(mContext);

        // 构造对话框
        noticeDialog.setTitle(mContext.getString(R.string.str_update_tip));
        noticeDialog.setMessage(mContext.getString(R.string.str_update_txt));
        // 更新
        noticeDialog.setPositiveButton(mContext.getString(R.string.str_update),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        dialog.dismiss();
                        // 显示下载对话框
                        showDownloadDialog();
                    }
                });
        // 稍后更新
        noticeDialog.setNegativeButton(mContext.getString(R.string.str_later),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
        Dialog myNoticeDialog = noticeDialog.create();
        myNoticeDialog.setCancelable(false);
        if (!myNoticeDialog.isShowing())
            myNoticeDialog.show();
    }

    /**
     * 显示软件下载对话框
     */
    private void showDownloadDialog() {
        if (staticDownloadUrl == null) {
            return;
        }
        CustomDialog.Builder upDialog = new CustomDialog.Builder(mContext);
        upDialog.setTitle(mContext.getString(R.string.str_updating));

        // 给下载对话框增加进度条
        final LayoutInflater inflater = LayoutInflater.from(mContext);
        View v = inflater.inflate(R.layout.softupdate_progress, null);
        mProgress = (ProgressBar) v.findViewById(R.id.update_progress);
        upDialog.setContentView(v);

        // 取消更新
//        upDialog.setNegativeButton("取消更新",
//                new DialogInterface.OnClickListener() {
//                    @Override
//                    public void onClick(DialogInterface dialog, int which) {
//                        dialog.dismiss();
//                        // 设置取消状态
//                        cancelUpdate = true;
//                    }
//                });
        mDownloadDialog = upDialog.create();
        mDownloadDialog.show();
        mDownloadDialog.setCancelable(false);
        // 现在文件
        downloadAPK();
    }

    /**
     * 下载apk文件
     */
    private void downloadAPK() {
        // 启动新线程下载软件
        if (staticDownloadUrl != null) {
            new downloadApkThread().start();
        }
    }

    private String mSavePathApk;
    private String staticDownloadUrl = null;
//    private String staticDownloadUrl = "http://magicairsuspension.com/MagicAir_v4.9.4_2020-03-07.apk";

    private class downloadApkThread extends Thread {
        @Override
        public void run() {
            try {
                // 判断SD卡是否存在，并且是否具有读写权限
                if (Environment.getExternalStorageState().equals(
                        Environment.MEDIA_MOUNTED)) {
                    // 获得存储卡的路径
                    String sdpath = Environment.getExternalStorageDirectory()
                            + "/";
                    mSavePathApk = sdpath + "download";
//                    URL url = new URL(MAIN_ENTER + "url");
                    URL url = new URL(staticDownloadUrl);
                    L.e("下载链接" + url);
                    // 创建连接
                    HttpURLConnection conn = (HttpURLConnection) url
                            .openConnection();
                    conn.connect();
                    // 获取文件大小
                    int length = conn.getContentLength();
                    // 创建输入流
                    InputStream is = conn.getInputStream();

                    File file = new File(mSavePathApk);
                    // 判断文件目录是否存在
                    if (!file.exists()) {
                        file.mkdir();
                    }
                    File apkFile = new File(mSavePathApk, UPDateApkName);
                    FileOutputStream fos = null;
                    try {
                        fos = new FileOutputStream(apkFile);
                        int count = 0;
                        // 缓存
                        byte buf[] = new byte[1024];
                        // 写入到文件中
                        do {
                            int numread = is.read(buf);
                            count += numread;
                            // 计算进度条位置
                            progress = (int) (((float) count / length) * 100);
                            // 更新进度
                            mHandler.sendEmptyMessage(DOWNLOADAPK);
                            if (numread <= 0) {
                                // 下载完成
                                mHandler.sendEmptyMessage(DOWNLOAD_APK_FINISH);
                                break;
                            }
                            // 写入文件
                            if (fos != null)
                                fos.write(buf, 0, numread);
                        } while (!cancelUpdate);// 点击取消就停止下载.
                        fos.close();
                        is.close();
                    } catch (FileNotFoundException e) {
                        mHandler.sendEmptyMessage(DOWNLOAD_PERMISSION);
                        e.printStackTrace();
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            // 取消下载对话框显示
            mDownloadDialog.dismiss();
        }
    }


    /**
     * 安装APK文件
     */
    private void installApk() {
        File apkfile = new File(mSavePathApk, UPDateApkName);
        if (!apkfile.exists()) {
            return;
        }
        // 通过Intent安装APK文件
//        Intent intent = new Intent(Intent.ACTION_VIEW);
        Intent intent = new Intent();
//        intent.setDataAndType(Uri.parse("file://" + apkfile.toString()),
//                "application/vnd.android.package-archive");

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            intent.setAction(Intent.ACTION_INSTALL_PACKAGE);
            intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//            Uri uri = FileProvider.getUriForFile(mContext, mContext.getPackageName() + ".provider", apkfile);
            Uri uri = FileProvider.getUriForFile(mContext, BuildConfig.APPLICATION_ID + ".provider", apkfile);
            intent.setDataAndType(uri, "application/vnd.android.package-archive");
        } else {
            intent.setAction(Intent.ACTION_VIEW);
            intent.setDataAndType(Uri.parse("file://" + apkfile.toString()), "application/vnd.android.package-archive");
        }


        mContext.startActivity(intent);
    }

}
