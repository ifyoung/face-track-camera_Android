-optimizationpasses 5          # 指定代码的压缩级别
-dontusemixedcaseclassnames   # 是否使用大小写混合
-dontoptimize
# 是否混淆第三方jar
-dontskipnonpubliclibraryclasses
-dontpreverify           # 混淆时是否做预校验
-verbose                # 混淆时是否记录日志

-optimizations !code/simplification/arithmetic,!field/*,!class/merging/*  # 混淆时所采用的算法

-keep public class * extends android.app.Activity      # 保持哪些类不被混淆
-keep public class * extends android.app.Application   # 保持哪些类不被混淆
-keep public class * extends android.app.Service       # 保持哪些类不被混淆
-keep public class * extends android.content.BroadcastReceiver  # 保持哪些类不被混淆
-keep public class * extends android.content.ContentProvider    # 保持哪些类不被混淆
-keep public class * extends android.app.backup.BackupAgentHelper # 保持哪些类不被混淆
-keep public class * extends android.preference.Preference        # 保持哪些类不被混淆
-keep public class com.android.vending.licensing.ILicensingService    # 保持哪些类不被混淆

-keepclasseswithmembernames class * {  # 保持 native 方法不被混淆
    native <methods>;
}
-keepclasseswithmembers class * {   # 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet);
}
-keepclasseswithmembers class * {# 保持自定义控件类不被混淆
    public <init>(android.content.Context, android.util.AttributeSet, int);
}
-keepclassmembers class * extends android.app.Activity { # 保持自定义控件类不被混淆
    public void *(android.view.View);
}
-keepclassmembers enum * {     # 保持枚举 enum 类不被混淆
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class * implements android.os.Parcelable { # 保持 Parcelable 不被混淆
    public static final android.os.Parcelable$Creator *;
}
#okhttputils
-dontwarn com.zhy.http.**
-keep class com.zhy.http.**{*;}
#okhttp
-dontwarn okhttp3.**
-keep class okhttp3.**{*;}
#okio
-dontwarn okio.**
-keep class okio.**{*;}
#activeandroid
-keep class com.activeandroid.** { *; }

-keep class face.camera.beans.ble.** { *; }

-dontwarn face.camera.beans.net.modelCom.**
-keep public class com.sygjkh.android.modelCom.** { *;}
-keep public class com.sygjkh.android.config.** { *;}

-keepattributes *Annotation*

-keep class com.google.**{*;}
-keepclassmembers class * implements java.io.Serializable {
    static final long serialVersionUID;
    private static final java.io.ObjectStreamField[] serialPersistentFields;
    private void writeObject(java.io.ObjectOutputStream);
    private void readObject(java.io.ObjectInputStream);
    java.lang.Object writeReplace();
    java.lang.Object readResolve();
}

#友盟
-keep class com.umeng.** {*;}

-keep class com.uc.** {*;}

-keepclassmembers class * {
   public <init> (org.json.JSONObject);
}
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}
-keep class com.zui.** {*;}
-keep class com.miui.** {*;}
-keep class com.heytap.** {*;}
-keep class a.** {*;}
-keep class com.vivo.** {*;}

##---------------Begin: proguard configuration for Gson  ----------
# Gson uses generic type information stored in a class file when working with fields. Proguard
# removes such information by default, so configure it to keep all of it.
-keepattributes Signature
# Gson specific classes
-keep class sun.misc.Unsafe { *; }
-keep class com.google.gson.stream.** { *; }
# Application classes that will be serialized/deserialized over Gson

#不混淆org.apache.http.legacy.jar
# -dontwarn android.net.compatibility.**
# -dontwarn android.net.http.**
# -dontwarn com.android.internal.http.multipart.**
# -dontwarn org.apache.commons.**
# -dontwarn org.apache.http.**
# -keep class android.net.compatibility.**{*;}
# -keep class android.net.http.**{*;}
# -keep class com.android.internal.http.multipart.**{*;}
# -keep class org.apache.commons.**{*;}
# -keep class org.apache.http.**{*;}
# Eventbus
 -keepattributes *Annotation*
 -keepclassmembers class ** {
     @org.greenrobot.eventbus.Subscribe <methods>;
 }
 -keep enum org.greenrobot.eventbus.ThreadMode { *; }

 # Only required if you use AsyncExecutor
 -keepclassmembers class * extends org.greenrobot.eventbus.util.ThrowableFailureEvent {
     <init>(Java.lang.Throwable);
 }
 -printmapping build/outputs/mapping/release/mapping.txt


# webview + js
-keepattributes *JavascriptInterface*

# keep 使用 webview 的类face.camera.beans.dialog.DialogPrivacy.DialogPrivacy
-keepclassmembers class face.camera.beans.dialog.DialogPrivacy.DialogPrivacy {
   public *;
}
# keep 使用 webview 的类的所有的内部类
#-keepclassmembers   com.sygjkh.android.ui.mainweb.MainWebFragment$*{
#public *;
#}
#-keepclassmembers com.sygjkh.android.ui.mainweb.MainWebFragment$JSInterfacel {
#    <methods>;
#}

#科大讯飞
#-dontwarn com.iflytek.**
#-keep class com.iflytek.**{*;}
#虹软
-keep class com.arcsoft.face.**{*;}
-keep class face.camera.beans.arc.**{*;}


#极光
-dontwarn cn.jpush.**
-keep class cn.jpush.** { *; }
-keep class * extends cn.jpush.android.helpers.JPushMessageReceiver { *; }

-dontwarn cn.jiguang.**
-keep class cn.jiguang.** { *; }

#相机图片
-keep class com.huantansheng.easyphotos.models.** { *; }
#com.luck.picture.lib
#-keep class com.huantansheng.easyphotos.models.** { *; }
# web相关
-dontwarn android.webkit.WebView
-dontwarn android.net.http.SslError
-dontwarn android.webkit.WebViewClient
-keep public class android.net.http.SslError
-keep public class android.webkit.WebViewClient

-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String, android.graphics.Bitmap);
    public boolean *(android.webkit.WebView, java.lang.String);
}
-keepclassmembers class * extends android.webkit.WebViewClient {
    public void *(android.webkit.WebView, java.lang.String);
}
#日志
 -assumenosideeffects class android.util.Log {
    public static *** d(...);
    public static *** v(...);
    public static *** i(...);
    public static *** w(...);
    public static *** e(...);
 }