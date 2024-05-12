package de.anbeli.dronedelivery;

import android.content.Intent;
import android.os.Bundle;
import android.util.JsonReader;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import org.json.JSONException;

public class LoginActivity extends AppCompatActivity {

    EditText email_inp;
    EditText password_inp;
    TextView link_signup;
    TextView link_reset;
    Button login_btn;
    String db_access;
    int session_id;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        db_access = getString(R.string.datbase_url);
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {

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

    }

    void onLogin() {
        String login_post_data = "";
        try {
            login_post_data = Util.build_login_obj_string(email_inp.getText().toString(), password_inp.getText().toString());
        } catch (JSONException e) {
            throw new RuntimeException(e);
        }

        DatabaseConnector.process_async_post_request(db_access, login_post_data, res -> {
            if(res.getString("message").equals("Doesnt exist")) {

            } else if(res.getString("message").equals("Exists")) {
                session_id = res.getInt("sessionID");
                Intent myIntent = new Intent(this, MainActivity.class);
                startActivity(myIntent);
            }
        });
    }
    void set_listeners() {
        login_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onLogin();
            }
        });

        link_reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        link_signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent myIntent = new Intent(v.getContext(), SignUpActivity.class);
                startActivity(myIntent);
            }
        });
    }

    
}