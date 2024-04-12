package de.anbeli.dronedelivery;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class LoginActivity extends AppCompatActivity {

    EditText username_inp;
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

        username_inp = findViewById(R.id.login_input_username);
        password_inp = findViewById(R.id.login_input_password);
        link_signup = findViewById(R.id.login_problem_account);
        link_reset = findViewById(R.id.login_problem_password);
        login_btn = findViewById(R.id.login_input_button);




    }

    void onSignup() {

    }

    
}