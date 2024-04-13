package de.anbeli.dronedelivery;

import android.app.Dialog;
import android.content.Context;
import android.os.Handler;
import android.widget.TextView;

public class ErrorPopup {

    Dialog dialog;
    TextView error;
    String text;
    int time = -1;

    public ErrorPopup(Context c, String text) {
        dialog = new Dialog(c);
        dialog.setContentView(R.layout.error_popup);
        error = dialog.findViewById(R.id.error_text);
        this.text = text;
    }

    public ErrorPopup(Context c, String text, int time) {
        dialog = new Dialog(c);
        dialog.setContentView(R.layout.error_popup);
        error = dialog.findViewById(R.id.error_text);
        this.text = text;
        this.time = time;
    }

    public void show() {
        error.setText(text);
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
