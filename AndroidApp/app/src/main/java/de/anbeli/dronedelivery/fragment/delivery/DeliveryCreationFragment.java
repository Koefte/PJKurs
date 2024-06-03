package de.anbeli.dronedelivery.fragment.delivery;

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

public class DeliveryCreationFragment extends Fragment {

    Button new_delivery_btn;
    EditText receiver_email_inp;

    View v;
    Context c;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_delivery_creation, container, false);
        c = v.getContext();

        new_delivery_btn = v.findViewById(R.id.new_delivery_button);
        receiver_email_inp = v.findViewById(R.id.delivery_creation_input_email);

        set_listeners();

        return v;
    }

    private void set_listeners() {
        new_delivery_btn.setOnClickListener(v -> {
            DatabaseConnector.process_async_post_request("requests", Util.build_request_a_obj_string(receiver_email_inp.getText().toString()), res -> {
                if(!res.getString("message").equals("Succesfully created the request A")) {
                    ErrorPopup errorPopup = new ErrorPopup(c, c.getString(R.string.create_delivery_failed));
                    errorPopup.show();
                } else {
                    ((MainActivity) getActivity()).replace_fragment(new DeliveryFragment());
                }
            });
        });
    }
}
