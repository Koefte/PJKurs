package de.anbeli.dronedelivery.fragment.drone;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.activities.MainActivity;

public class DroneFragment extends Fragment {

    Button add_drone_btn;
    RecyclerView drones;

    View v;

    Context c;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_drone, container, false);
        c = v.getContext();

        add_drone_btn = v.findViewById(R.id.new_drone_button);
        drones = v.findViewById(R.id.drones_recycler_view);

        set_listener();

        return v;
    }

    private void set_listener() {
        add_drone_btn.setOnClickListener(view -> {
            ((MainActivity) getActivity()).replace_fragment(new DroneAddFragment());
        });
    }
}
