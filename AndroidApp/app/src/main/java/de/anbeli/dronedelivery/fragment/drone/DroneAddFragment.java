package de.anbeli.dronedelivery.fragment.drone;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import androidx.fragment.app.Fragment;

import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.activities.MainActivity;
import de.anbeli.dronedelivery.util.DatabaseConnector;
import de.anbeli.dronedelivery.util.ErrorPopup;
import de.anbeli.dronedelivery.util.Util;

public class DroneAddFragment extends Fragment {

    EditText serial_number_inp;
    Button add_drone_btn;

    View v;
    Context c;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_drone_add, container, false);
        c = v.getContext();

        serial_number_inp = v.findViewById(R.id.drone_add_inp_serial);
        add_drone_btn = v.findViewById(R.id.add_drone_button);

        set_listeners();

        return v;
    }

    private void set_listeners() {
        add_drone_btn.setOnClickListener(view -> {

            //Post hardwareID to link drone to user on server

            DatabaseConnector.process_async_post_request(
                    "drones",
                    Util.build_drone_id_obj_string(Integer.parseInt(serial_number_inp.getText().toString())),res -> {

                        //checking for valid response

                        if(!res.getString("message").equals("Succesfully created the drone")) {
                            ErrorPopup errorPopup = new ErrorPopup(c, getString(R.string.drone_creation_failed));
                            errorPopup.show();
                        }
                    });
            ((MainActivity) getActivity()).replace_fragment(new DroneFragment());
        });
    }

}
