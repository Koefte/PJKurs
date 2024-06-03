package de.anbeli.dronedelivery.fragment.delivery;

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

import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.activities.MainActivity;
import de.anbeli.dronedelivery.data.Delivery;
import de.anbeli.dronedelivery.data.DeliveryAdapter;
import de.anbeli.dronedelivery.util.DatabaseConnector;
import de.anbeli.dronedelivery.util.Util;

public class DeliveryFragment extends Fragment {

    Button new_delivery_btn;


    ArrayList<Delivery> deliveries_list;
    RecyclerView deliveries;
    View v;
    Context c;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_delivery, container, false);
        c = v.getContext();


        new_delivery_btn = v.findViewById(R.id.new_delivery_button);
        deliveries = v.findViewById(R.id.deliveries_recycler_view);

        deliveries_list = new ArrayList<>();

        fetch_deliveries();

        DeliveryAdapter adapter = new DeliveryAdapter(deliveries_list, c);
        deliveries.setAdapter(adapter);
        deliveries.setLayoutManager(new LinearLayoutManager(c));

        set_listeners();

        return v;
    }

    private void set_listeners() {
        new_delivery_btn.setOnClickListener(v -> {
            ((MainActivity) getActivity()).replace_fragment(new DeliveryCreationFragment());
        });
    }

    private void fetch_deliveries() {
        DatabaseConnector.process_async_post_request("requests", Util.build_session_id_obj_string(), res -> {
            System.out.println(res);
            deliveries_list = Util.parse_fetch_deliveries_outgoing(res);
        });
    }
}
