package face.camera.beans.dialog.DialogPrivacy;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.luck.picture.lib.tools.ToastUtils;
import com.orhanobut.dialogplus.DialogPlus;
import com.orhanobut.dialogplus.OnClickListener;
import com.orhanobut.dialogplus.OnItemClickListener;
import com.orhanobut.dialogplus.ViewHolder;

import face.camera.beans.R;
import face.camera.beans.ble.L;
import face.camera.beans.ble.Preferences;
import face.camera.beans.dialog.DialogFilterAdapter;


public final class DialogPrivacy {

    private static DialogPlus dialogPlus;

    public DialogPrivacy() {
    }

    public static void showPrivacy(Context context, boolean isFromSet) {
        if (dialogPlus != null) {
            dialogPlus.dismiss();
        }
        String lang = Resources.getSystem().getConfiguration().locale.getLanguage();
        L.e("initView-system-lang" + lang);

        LayoutInflater inflater = (LayoutInflater) context
                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View v = inflater.inflate(R.layout.privacy_content, null);

        WebView webView = v.findViewById(R.id.privacy_view);
        webView.getSettings().setJavaScriptEnabled(true);
        String url = "https://0bolo.com";
//        if (lang.contains("zh")) {
//            url = "https://snp-us.top/ydcp/sp/protocol?language=zh";
//        } else if (lang.contains("en")) {
//            url = "https://snp-us.top/ydcp/sp/protocol?language=en";
//        } else if (lang.contains("fr")) {
//            url = "https://snp-us.top/ydcp/sp/protocol?language=fr";
//        }
        webView.loadUrl(url);
        Button okBtn = v.findViewById(R.id.ok_privacy);

        Button exitBtn = v.findViewById(R.id.exit);

        if (isFromSet) {
            exitBtn.setVisibility(View.GONE);
            okBtn.setText(R.string.str_btn_close);
        }

        dialogPlus = DialogPlus.newDialog(context)
                .setContentHolder(new ViewHolder(v))
                .setCancelable(false)
                .setGravity(Gravity.CENTER)
                .setOnClickListener(new OnClickListener() {
                    @Override
                    public void onClick(DialogPlus dialog, View view) {
                        if (view.getId() == R.id.ok_privacy) {
                            //同意

                            Preferences.savePreferences(context.getApplicationContext(), "privacy", "privacy", 0);
                            dialogPlus.dismiss();
                        }
                        if (view.getId() == R.id.exit) {
                            System.exit(0);//完全退出。。。造成前面的吐司不显示

                        }

                    }
                })
//                .setExpanded(true)  // This will enable the expand feature, (similar to android L share dialog)
                .create();
        dialogPlus.show();
    }
}
