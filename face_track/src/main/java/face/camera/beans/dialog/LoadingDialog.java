package face.camera.beans.dialog;


import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.LayoutInflater;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.luck.picture.lib.R;
import com.luck.picture.lib.dialog.PictureLoadingDialog;

import face.camera.beans.ble.L;

public class LoadingDialog extends Dialog {

    TextView tipTxtView;
    int DISSMISS = 0;
    Context mContext;

    public LoadingDialog(Context context) {
        super(context, R.style.Picture_Theme_AlertDialog);
        setCancelable(true);
        setCanceledOnTouchOutside(false);
        Window window = getWindow();
        window.setWindowAnimations(R.style.PictureThemeDialogWindowStyle);
//        setContentView();
        mContext = context;
        LayoutInflater inflater = (LayoutInflater)
                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        LinearLayout layout = (LinearLayout) inflater.inflate(face.camera.beans.R.layout.picture_alert_dialog, null);
        setContentView(layout);
        tipTxtView = layout.findViewById(R.id.load_txt);
        L.d("LoadingDialog>>init");

    }

    public void setLoadTxt(String txt) {
        tipTxtView.setText(txt);
        L.d("LoadingDialog>>txt" + txt);

    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        LayoutInflater inflater = (LayoutInflater)
//                mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
//        LinearLayout layout = (LinearLayout) inflater.inflate(face.camera.beans.R.layout.picture_alert_dialog, null);
//        setContentView(layout);
//        tipTxtView = layout.findViewById(R.id.load_txt);

//        setContentView(R.layout.picture_alert_dialog);
        L.d("LoadingDialog>>onCreate");
    }

    /**
     * loading dialog
     */
    public void showPleaseDialog() {
        try {
//            if (mLoadingDialog == null) {
//                mLoadingDialog = new PictureLoadingDialog(getContext());
//            }
            if (this.isShowing()) {
                this.dismiss();
            }
            this.show();
            handlerDismiss.postDelayed(new Runnable() {
                @Override
                public void run() {
                    handlerDismiss.sendEmptyMessage(DISSMISS);
                }
            }, 7000);//七秒后自动取消异常弹框
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    Handler handlerDismiss = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.what == DISSMISS) {
                dismissDialog();
            }
        }
    };

    /**
     * dismiss dialog
     */
    public void dismissDialog() {
        try {
            if (this.isShowing()) {
                this.dismiss();
            }
        } catch (Exception e) {
//            this = null;
            e.printStackTrace();
        }

    }
}