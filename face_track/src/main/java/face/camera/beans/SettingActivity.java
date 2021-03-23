package face.camera.beans;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.luck.picture.lib.camera.listener.ClickListener;
import com.luck.picture.lib.tools.StaticFunction;
import com.luck.picture.lib.tools.ToastUtils;

import cn.bingoogolapple.bgabanner.BGABanner;
import cn.bingoogolapple.bgabanner.BGALocalImageSize;
import face.camera.beans.arc.common.Constants;
import face.camera.beans.ble.Preferences;
import face.camera.beans.dialog.DialogPrivacy.DialogPrivacy;
import face.camera.beans.net.UpdateManager;
import face.camera.beans.record.RecorderActivity;

public class SettingActivity extends Activity implements View.OnClickListener {
    private static final String TAG = SettingActivity.class.getSimpleName();
    ImageView switchSound;
    RelativeLayout relativeLayoutPrivacy;
    TextView verTxt;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        initView();
    }

    private void initView() {
        setContentView(R.layout.setting_activity);
        StaticFunction.reloadLanguageAction(this);

        switchSound = findViewById(R.id.sound_switch);
//        if (Preferences.getPreferences(this, "sound").equals("off")) {
        if (Preferences.getPreferences(getApplicationContext(), "sound", "sound") == 0) {
            switchSound.setSelected(false);
            switchSound.setImageResource(R.drawable.switch_off);
        } else {
            switchSound.setSelected(true);
            switchSound.setImageResource(R.drawable.switch_on);


        }
        relativeLayoutPrivacy = findViewById(R.id.privacy);
        verTxt = findViewById(R.id.ver_app);
        findViewById(R.id.btn_back).setOnClickListener(this);
        switchSound.setOnClickListener(this);
        relativeLayoutPrivacy.setOnClickListener(this);
        verTxt.setText(UpdateManager.getVersionName(this) +"."+ UpdateManager.getVersionCode(this));

    }


    @Override
    protected void onResume() {
        super.onResume();

    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.sound_switch:
                if (switchSound.isSelected()) {
                    switchSound.setImageResource(R.drawable.switch_off);
                    switchSound.setSelected(false);

//                    Preferences.savePreferences(this, "sound", "off");
                    Preferences.savePreferences(getApplicationContext(), "sound", "sound", 0);
                } else {
                    switchSound.setImageResource(R.drawable.switch_on);
                    switchSound.setSelected(true);
//                    Preferences.savePreferences(this, "sound", "on");
                    Preferences.savePreferences(getApplicationContext(), "sound", "sound", 1);

                }


                break;
            case R.id.privacy:
                DialogPrivacy.showPrivacy(this, true);
                break;

            case R.id.btn_back:
                finish();
                break;
        }
    }
}