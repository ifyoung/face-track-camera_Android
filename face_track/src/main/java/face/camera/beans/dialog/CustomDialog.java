package face.camera.beans.dialog;

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.text.SpannableStringBuilder;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;


import face.camera.beans.R;

import static android.view.View.TEXT_ALIGNMENT_CENTER;

/**
 * Create custom Dialog windows for your application Custom dialogs rely on
 * custom layouts wich allow you to create and use your own look & feel.
 * <p/>
 * Under GPL v3 : http://www.gnu.org/licenses/gpl-3.0.html
 * <p/>
 * <a href="http://my.oschina.net/arthor" target="_blank"
 * rel="nofollow">@author</a> antoine vianey
 */
public class CustomDialog extends Dialog {

    public CustomDialog(Context context, int theme) {
        super(context, theme);
    }

    public CustomDialog(Context context) {
        super(context);
    }

    /**
     * Helper class for creating a custom dialog
     */
    public static class Builder {

        private Context context;
        private String title;
        private String message;
        private SpannableStringBuilder spannableString;
        private int txtGravity;
        private String positiveButtonText;
        private String[] mItems;
        private String negativeButtonText;
        private String settingButtonText;

        private String cancleButtonText;
        private View contentView;
        private CustomDialog dialog;
        private View layout;
        private Boolean isBotShow;
        private LinearLayout permissionTip, permissionLine, dialogMainView;
        private Button settingBtn, leftBtn, rightBtn;

        private OnClickListener positiveButtonClickListener,
                negativeButtonClickListener, cancleButtonClickListener;

        private resetNameonClickListener mListener;
        private settingClickListener settingListener;


        public void setOnResettListener(resetNameonClickListener listener) {
            mListener = listener;
        }

        public interface resetNameonClickListener {
            public void onClick(DialogInterface d);
        }

        void setSettingListener(settingClickListener settingListener) {
            this.settingListener = settingListener;
        }

        public interface settingClickListener {
            public void onClick(DialogInterface d);
        }


        public Builder(Context context) {
            this.context = context;
        }

        /**
         * Set the Dialog message from String
         *
         * @param message
         * @return
         */
        public Builder setMessage(String message) {
            this.message = message;
            return this;
        }

        Builder setSpannableString(SpannableStringBuilder message, int gravity) {
            this.spannableString = message;
            this.txtGravity = gravity;
            return this;
        }

        /**
         * Set the Dialog message from resource
         *
         * @param message
         * @return
         */
        public Builder setMessage(int message) {
            this.message = (String) context.getText(message);
            return this;
        }

        /**
         * Set the Dialog title from resource
         *
         * @param title
         * @return
         */
        public Builder setTitle(int title) {
            this.title = (String) context.getText(title);
            return this;
        }

        /**
         * Set the Dialog title from String
         *
         * @param title
         * @return
         */
        public Builder setTitle(String title) {
            this.title = title;
            return this;
        }

        /**
         * Set a custom content view for the Dialog. If a message is set, the
         * contentView is not added to the Dialog...
         *
         * @param view
         * @return
         */
        public Builder setContentView(View view) {
            this.contentView = view;
            return this;
        }

        public Builder setBotShow(Boolean botShow) {
            this.isBotShow = botShow;
            return this;
        }

        /**
         * Set the positive button resource and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(int positiveButtonText,
                                         OnClickListener listener) {
            this.positiveButtonText = (String) context
                    .getText(positiveButtonText);
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the positive button text and it's listener
         *
         * @param positiveButtonText
         * @param listener
         * @return
         */
        public Builder setPositiveButton(String positiveButtonText,
                                         OnClickListener listener) {
            this.positiveButtonText = positiveButtonText;
            this.positiveButtonClickListener = listener;
            return this;
        }

        /**
         * Set the negative button resource and it's listener
         *
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(int negativeButtonText,
                                         OnClickListener listener) {
            this.negativeButtonText = (String) context
                    .getText(negativeButtonText);
            this.negativeButtonClickListener = listener;
            return this;
        }

        /**
         * Set the negative button text and it's listener
         *
         * @param negativeButtonText
         * @param listener
         * @return
         */
        public Builder setNegativeButton(String negativeButtonText,
                                         OnClickListener listener) {
            this.negativeButtonText = negativeButtonText;
            this.negativeButtonClickListener = listener;
            return this;
        }

        public Builder setCancleButton(int cancleButtonText,
                                       OnClickListener listener) {
            this.cancleButtonText = (String) context
                    .getText(cancleButtonText);
            this.cancleButtonClickListener = listener;
            return this;
        }

        public Builder setSettingButton(String settingButtonText,
                                        OnClickListener listener) {
            this.settingButtonText = settingButtonText;
            this.cancleButtonClickListener = listener;
            return this;
        }

        /**
         * Set the positive button text and it's listener
         *
         * @param cancleButtonText
         * @param listener
         * @return
         */
        public Builder setCancleButtonText(String cancleButtonText,
                                           OnClickListener listener) {
            this.cancleButtonText = cancleButtonText;
            this.cancleButtonClickListener = listener;
            return this;
        }


        /**
         * Create the custom dialog
         */
        public CustomDialog create() {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            // instantiate the dialog with the custom Theme
            dialog = new CustomDialog(context,
                    R.style.Dialog);
            dialog.setCanceledOnTouchOutside(false);
            layout = inflater.inflate(R.layout.dialog, null);
            dialog.addContentView(layout, new LayoutParams(
                    LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT));
            // set the dialog title
            ((TextView) layout.findViewById(R.id.title)).setText(title);

            TextView resetName = layout.findViewById(R.id.resetName);

            permissionTip = layout.findViewById(R.id.locationTip);
            permissionLine = layout.findViewById(R.id.tipLine);
            settingBtn = layout.findViewById(R.id.goSettingButton);

            dialogMainView = layout.findViewById(R.id.dialogMain);

            permissionLine.setVisibility(View.GONE);
            permissionTip.setVisibility(View.GONE);
            leftBtn = layout.findViewById(R.id.positiveButton);
            rightBtn = layout.findViewById(R.id.negativeButton);

//            if (title.contains(context.getString(R.string.str_device_alias)) && Preferences.getBLENAMENew(context)) {
//                resetName.setVisibility(View.VISIBLE);
//
//            }
            resetName.setVisibility(View.VISIBLE);

            settingBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != settingListener) {
                        settingListener.onClick(dialog);
                    }
                }
            });

            resetName.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (null != mListener) {
                        mListener.onClick(dialog);
                    }
                }
            });


            // set the confirm button
            if (positiveButtonText != null) {
                leftBtn.setText(positiveButtonText);


                if (positiveButtonClickListener != null) {

                    leftBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            positiveButtonClickListener.onClick(dialog,
                                    DialogInterface.BUTTON_POSITIVE);
                        }
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                leftBtn.setVisibility(
                        View.GONE);
            }

            // set the cancel button
            if (negativeButtonText != null) {
                rightBtn.setText(negativeButtonText);
                if (negativeButtonClickListener != null) {
                    rightBtn.setOnClickListener(new View.OnClickListener() {
                        public void onClick(View v) {
                            negativeButtonClickListener.onClick(dialog,
                                    DialogInterface.BUTTON_NEGATIVE);
                        }
                    });
                }
            } else {
                // if no confirm button just set the visibility to GONE
                rightBtn.setVisibility(
                        View.GONE);
//				layout.findViewById(R.id.bottomBtn).setVisibility(
//						View.GONE);
            }

            // set the content message
            if (message != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(message);
            } else if (contentView != null) {
                // if no message set
                // add the contentView to the dialog body
                ((LinearLayout) layout.findViewById(R.id.content))
                        .removeAllViews();
                ((LinearLayout) layout.findViewById(R.id.content)).addView(
                        contentView, new LayoutParams(
                                LayoutParams.MATCH_PARENT,
                                LayoutParams.WRAP_CONTENT));
            } else if (spannableString != null) {
                ((TextView) layout.findViewById(R.id.message)).setText(spannableString);
                //txtGravity
                ((TextView) layout.findViewById(R.id.message)).setGravity(txtGravity);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
                    ((TextView) layout.findViewById(R.id.message)).setTextAlignment(TEXT_ALIGNMENT_CENTER);
                }

            }

            // dialog.setContentView(layout);
            return dialog;
        }

        void showBot(boolean isBotShow) {
            Message message = handlerNo.obtainMessage();

            message.what = 0x1213;
            message.obj = isBotShow;
            handlerNo.sendMessage(message);
        }

        void setNone(final String txt) {

//        ((TextView) mProgressDialog.findViewById(R.id.dp_text)).setText(txt);
            Message message = handlerNo.obtainMessage();

            message.what = 0x1211;
            message.obj = txt;
            handlerNo.sendMessage(message);


        }

        Handler handlerNo = new Handler(Looper.getMainLooper()) {//绑到主线程，在 Thread里new Handler会报错
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what) {
                    case 0x1211:
//                        View layout= dialog.findViewById(R.id.message);
                        LayoutInflater inflater1 = (LayoutInflater) context
                                .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                        LinearLayout tv = (LinearLayout) inflater1.inflate(R.layout.content_textview, null);

//                        tv.setText(msg.obj.toString());
                        tv.setGravity(Gravity.CENTER);
                        ((LinearLayout) layout.findViewById(R.id.content))
                                .removeAllViews();
                        ((LinearLayout) layout.findViewById(R.id.content)).addView(
                                tv, new LayoutParams(
                                        LayoutParams.MATCH_PARENT,
                                        LayoutParams.WRAP_CONTENT));
                        break;
                    case 0x1213:


                        break;
                }

            }
        };


    }




    /*
     * 对话框大小
     * */

    public static void ShowDialog(CustomDialog dialog, int mScreenWidth) {
        //        myDialog.setCancelable(false);
        Window dialogWindow = dialog
                .getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        dialogWindow.setGravity(Gravity.CENTER);
        lp.width = mScreenWidth * 9 / 10; // 宽度
        // lp.height = mScreenHeight / 3;
        dialogWindow.setAttributes(lp);
    }

}