package de.anbeli.dronedelivery;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.google.android.material.switchmaterial.SwitchMaterial;

public class SettingsFragment extends Fragment {

    SwitchMaterial drone_switch;
    View v;
    Context c;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_settings, container, false);
        c = v.getContext();

        drone_switch = v.findViewById(R.id.settings_drone_switch);

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

    }
}
