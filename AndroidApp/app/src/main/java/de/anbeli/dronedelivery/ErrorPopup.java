package de.anbeli.dronedelivery;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.widget.TextView;

public class ErrorPopup {

    public enum error_code {
        NO_ERROR,
        SIGNUP_EMAIL_INVALID,
        SIGNUP_EMAIL_TAKEN,
        SIGNUP_FIELD_EMPTY,
        SIGNUP_PASSWORD_NO_MATCH

    }

    Dialog dialog;
    TextView error;
    String code;
    int time = -1;

    public ErrorPopup(Context c, String text) {
        dialog = new Dialog(c);
        dialog.setContentView(R.layout.error_popup);
        error = dialog.findViewById(R.id.error_text);
        this.code = text;
    }

    public ErrorPopup(Context c, String text, int time) {
        dialog = new Dialog(c);
        dialog.setContentView(R.layout.error_popup);
        error = dialog.findViewById(R.id.error_text);
        this.time = time;
        this.code = text;
    }

    public void show() {
        error.setText(code);
        dialog.getWindow().setBackgroundDrawable(null);
        dialog.show();
        if(time != -1) {
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    dialog.dismiss();
                }

            }, time);
        }
    }

    public void dismiss() {
        dialog.dismiss();
    }
}
