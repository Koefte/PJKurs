package de.anbeli.dronedelivery.util;

import android.app.Dialog;
import android.content.Context;
import android.content.res.Resources;
import android.os.Handler;
import android.widget.TextView;

import de.anbeli.dronedelivery.R;

public class ErrorPopup {

    public enum error_code {
        NO_ERROR,
        SIGNUP_EMAIL_INVALID,
        SIGNUP_EMAIL_TAKEN,
        SIGNUP_FIELD_EMPTY,
        SIGNUP_PASSWORD_NO_MATCH,
        LOGIN_ACCOUNT_NO_EXIST

    }

    Dialog dialog;
    TextView error;
    String code;
    Context con;

    public ErrorPopup(Context c, String text) {
        con = c;
        c.getMainExecutor().execute(() -> {
            dialog = new Dialog(c);
            dialog.setContentView(R.layout.error_popup);
            error = dialog.findViewById(R.id.error_text);
            this.code = text;
        });
    }
    public void show() {
        con.getMainExecutor().execute(() -> {
            error.setText(code);
            dialog.getWindow().setBackgroundDrawable(null);
            dialog.show();
        });
    }
}
