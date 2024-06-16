package de.anbeli.dronedelivery.fragment.drone;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.activities.MainActivity;
import de.anbeli.dronedelivery.data.DeliveryAdapter;
import de.anbeli.dronedelivery.data.Drone;
import de.anbeli.dronedelivery.data.DroneAdapter;
import de.anbeli.dronedelivery.util.DatabaseConnector;
import de.anbeli.dronedelivery.util.Util;

public class DroneFragment extends Fragment {

    ArrayList<Drone> drone_list = new ArrayList<>();
    Button add_drone_btn;
    RecyclerView drones;
    DroneAdapter adapter;

    View v;

    Context c;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_drone, container, false);
        c = v.getContext();

        add_drone_btn = v.findViewById(R.id.new_drone_button);
        drones = v.findViewById(R.id.drones_recycler_view);

        //Creating DroneAdapter with for now empty click listener

        adapter = new DroneAdapter(drone_list, c, (v, position) -> {});
        drones.setAdapter(adapter);
        drones.setLayoutManager(new LinearLayoutManager(c));

        fetch_drones();

        set_listener();

        return v;
    }

    private void set_listener() {
        add_drone_btn.setOnClickListener(view -> {
            ((MainActivity) getActivity()).replace_fragment(new DroneAddFragment());
        });
    }

    //Request the users drones by posting his sessionID to /api/drones

    private void fetch_drones() {
        String post_data = Util.build_owner_session_id_obj_string();
        DatabaseConnector.process_async_post_request("drones", post_data, res -> {
            drone_list.addAll(Util.parse_fetch_drones(res));
            c.getMainExecutor().execute(() -> {
                adapter.notifyDataSetChanged();
            });
        });
    }
}
