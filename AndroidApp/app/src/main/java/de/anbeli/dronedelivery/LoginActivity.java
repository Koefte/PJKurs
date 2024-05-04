package de.anbeli.dronedelivery;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

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

        DatabaseConnector.process_async_get_request("http://127.0.0.1:3001/api/users", res -> {
            System.out.println("Haaaaaalo" + res);
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