package de.anbeli.dronedelivery;

import static android.app.PendingIntent.getActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class SignUpActivity  extends AppCompatActivity {
    EditText username_inp;
    EditText email_inp;
    EditText password_inp;
    EditText password_inp_2;
    Button signup_btn;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_signup);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.signup), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        username_inp = findViewById(R.id.signup_input_username);
        email_inp = findViewById(R.id.signup_input_email);
        password_inp = findViewById(R.id.signup_input_password);
        password_inp_2 = findViewById(R.id.signup_input_repeat_password);
        signup_btn = findViewById(R.id.signup_input_button);

        set_listeners();
    }

    void set_listeners() {
        signup_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!password_inp.equals(password_inp_2)) {
                    ErrorPopup error = new ErrorPopup(v.getContext(), getString(R.string.error_non_matching_password), 1000);
                    error.show();
                }
            }
        });
    }
}
