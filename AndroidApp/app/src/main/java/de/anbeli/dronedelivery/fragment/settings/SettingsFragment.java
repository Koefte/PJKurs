package de.anbeli.dronedelivery.fragment.settings;

import static android.content.Context.MODE_PRIVATE;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

import org.json.JSONException;
import org.json.JSONObject;

import de.anbeli.dronedelivery.activities.LoginActivity;
import de.anbeli.dronedelivery.activities.MainActivity;
import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.activities.SignUpActivity;
import de.anbeli.dronedelivery.util.DatabaseConnector;
import de.anbeli.dronedelivery.util.ErrorPopup;
import de.anbeli.dronedelivery.util.Util;

public class SettingsFragment extends Fragment {

    SwitchMaterial drone_switch;
    Button sign_off_button;
    View v;
    Context c;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_settings, container, false);
        c = v.getContext();

        drone_switch = v.findViewById(R.id.settings_drone_switch);
        sign_off_button = v.findViewById(R.id.sign_off_button);

        set_listeners();

        return v;
    }

    private void set_listeners() {
        drone_switch.setChecked(c.getSharedPreferences("save_data", MODE_PRIVATE).getBoolean("drone_management", false));
        drone_switch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            SharedPreferences.Editor e = c.getSharedPreferences("save_data", MODE_PRIVATE).edit();
            e.putBoolean("drone_management", isChecked);
            e.apply();

            ((MainActivity) getActivity()).update_menu();
        });

        sign_off_button.setOnClickListener(v -> {
            sign_off();
        });
    }

    private void sign_off() {
        DatabaseConnector.process_async_post_request("users", Util.build_session_id_obj_string(), res -> {
            if(res.getString("message").equals("Logged out")) {
                DatabaseConnector.session_id = -1;
                DatabaseConnector.save_session_id(c);

                Intent myIntent = new Intent(c, LoginActivity.class);
                startActivity(myIntent);
            } else {
                ErrorPopup errorPopup = new ErrorPopup(c, getString(R.string.sign_off_failed));
                errorPopup.show();
            }
        });
    }
}
