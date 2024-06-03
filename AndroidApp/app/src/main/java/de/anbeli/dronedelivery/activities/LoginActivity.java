package de.anbeli.dronedelivery.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import de.anbeli.dronedelivery.util.ErrorPopup;
import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.util.Util;
import de.anbeli.dronedelivery.util.DatabaseConnector;

public class LoginActivity extends AppCompatActivity {

    EditText email_inp;
    EditText password_inp;
    TextView link_signup;
    TextView link_reset;
    Button login_btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login), (v, insets) -> {

            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        email_inp = findViewById(R.id.login_input_email);
        password_inp = findViewById(R.id.login_input_password);
        link_signup = findViewById(R.id.login_problem_account);
        link_reset = findViewById(R.id.login_problem_password);
        login_btn = findViewById(R.id.login_input_button);

        set_listeners();

        DatabaseConnector.session_id = getSharedPreferences("save_data", MODE_PRIVATE).getLong("session_id", -1);
        System.out.println("saved session " + DatabaseConnector.session_id);

        if(DatabaseConnector.session_id != -1) {
            Intent myIntent = new Intent(this, MainActivity.class);
            startActivity(myIntent);
        }
    }

    void onLogin() {
        String login_post_data = Util.build_login_obj_string(email_inp.getText().toString(), password_inp.getText().toString());

        System.out.println("HUH?");

        DatabaseConnector.process_async_post_request("users",login_post_data, res -> {
            if(res.getString("message").equals("Doesnt exist")) {
                System.out.println(res.getString("message"));
                ErrorPopup errorPopup = new ErrorPopup(this, getString(R.string.login_account_no_exist));
                errorPopup.show();
            } else if(res.getString("message").equals("Exists")) {
                DatabaseConnector.session_id = res.getLong("sessionID");
                DatabaseConnector.save_session_id(this);
                Intent myIntent = new Intent(this, MainActivity.class);
                startActivity(myIntent);
            }
        });
    }
    void set_listeners() {
        login_btn.setOnClickListener(v -> onLogin());

        link_reset.setOnClickListener(v -> {

        });

        link_signup.setOnClickListener(v -> {
            Intent myIntent = new Intent(v.getContext(), SignUpActivity.class);
            startActivity(myIntent);
        });
    }
}