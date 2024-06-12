package de.anbeli.dronedelivery.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import static de.anbeli.dronedelivery.util.ErrorPopup.error_code;

import de.anbeli.dronedelivery.util.ErrorPopup;
import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.util.Util;
import de.anbeli.dronedelivery.util.DatabaseConnector;

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
        signup_btn.setOnClickListener(v -> {
            String username = username_inp.getText().toString();
            String email = email_inp.getText().toString();
            String password_initial = password_inp.getText().toString();
            String password_confirm = password_inp_2.getText().toString();

            error_code error = error_code.NO_ERROR;
            String error_text = "";


            if(!password_initial.equals(password_confirm)) error = error_code.SIGNUP_PASSWORD_NO_MATCH;
            if(!(email.split("@").length==2 && email.contains("."))) error = error_code.SIGNUP_EMAIL_INVALID;
            if(username.equals("") || email.equals("") || password_initial.equals("")) error = error_code.SIGNUP_FIELD_EMPTY;

            if(error != error_code.NO_ERROR) {
                switch (error) {
                    case SIGNUP_EMAIL_INVALID:
                        error_text = getString(R.string.signup_email_invalid);
                        break;
                    case SIGNUP_EMAIL_TAKEN:
                        error_text = getString(R.string.signup_email_taken);
                        break;
                    case SIGNUP_FIELD_EMPTY:
                        error_text = getString(R.string.signup_field_empty);
                        break;
                    case SIGNUP_PASSWORD_NO_MATCH:
                        error_text = getString(R.string.signup_password_no_match);
                        break;
                }

                ErrorPopup errorPopup = new ErrorPopup(v.getContext(), error_text);
                errorPopup.show();
            } else {
                String post_data = Util.build_user_obj_string(username, email, password_initial);

                DatabaseConnector.process_async_post_request("users",post_data, res -> {
                    Intent myIntent = new Intent(v.getContext(), LoginActivity.class);
                    startActivity(myIntent);
                    System.out.println(res);
                });
            }
        });
    }
}
