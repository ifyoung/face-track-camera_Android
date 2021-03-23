package face.camera.beans;

import android.app.Application;
import android.content.Context;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.util.DisplayMetrics;

import com.danikula.videocache.HttpProxyCacheServer;
import com.umeng.analytics.MobclickAgent;
import com.umeng.commonsdk.UMConfigure;
import com.zhy.http.okhttp.OkHttpUtils;
import com.zhy.http.okhttp.log.LoggerInterceptor;

import java.util.concurrent.TimeUnit;

import face.camera.beans.arc.ActiveInstance;
import face.camera.beans.ble.L;
import face.camera.beans.net.modelCom.NetWorkChangReceiver;
import face.camera.beans.record.RecorderActivity;
import okhttp3.OkHttpClient;

/**
 * description:
 * Created by LC.
 */


public class MyApplication extends Application {
    public static boolean DEBUG = true;
    private static Context mContext;
    public static int screenWidth;
    public static int screenHeight;
    private NetWorkChangReceiver netWorkChangReceiver;
    private boolean isRegistered = false;

    @Override
    public void onCreate() {
        super.onCreate();
        mContext = this;
        DisplayMetrics mDisplayMetrics = getApplicationContext().getResources()
                .getDisplayMetrics();
        screenWidth = mDisplayMetrics.widthPixels;
        screenHeight = mDisplayMetrics.heightPixels;


        //注册网络状态监听广播
        netWorkChangReceiver = new NetWorkChangReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(netWorkChangReceiver, filter);
        isRegistered = true;


//        FileUtils.createAppFolder();//创建文件夹
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
//                .addInterceptor(new LoggerInterceptor("TAG"))
                .connectTimeout(10000L, TimeUnit.MILLISECONDS)
                .readTimeout(10000L, TimeUnit.MILLISECONDS)
                .addInterceptor(new LoggerInterceptor("网络"))
                //其他配置
                .build();

        OkHttpUtils.initClient(okHttpClient);

        ActiveInstance.getInstance().activeEngine(this);


/**
 * 注意：如果您已经在AndroidManifest.xml中配置过appkey和channel值，可以调用此版本初始化函数。
 */
//        UMConfigure.init(this, UMConfigure.DEVICE_TYPE_PHONE, null);
//        UMConfigure.setLogEnabled(true);
//// 选用AUTO页面采集模式
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);//自动采集页面使用信息，不用手动埋点
//        // 打开统计SDK调试模式
//        UMConfigure.setLogEnabled(true);
        app = this;
    }

    @Override
    public void onTerminate() {
        L.i("MyApplication", "onTerminate");
        super.onTerminate();
        //解绑
        if (isRegistered) {
            unregisterReceiver(netWorkChangReceiver);
        }
        ActiveInstance.getInstance().unInitEngine();
    }

    public static Context getContext() {
        return mContext;
    }

    public static MyApplication app;

    public static MyApplication getInstance() {
        return app;
    }

    //=====================================================缓存区
    private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy() {
        MyApplication app = getInstance();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer(this);
    }

}
