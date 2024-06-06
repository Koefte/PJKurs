package de.anbeli.dronedelivery.fragment.delivery;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
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
import de.anbeli.dronedelivery.util.listeners.onRequestClickListener;

public class DeliveryFragment extends Fragment {

    Button new_delivery_btn;
    ArrayList<Delivery> deliveries_list = new ArrayList<>();
    RecyclerView deliveries;
    DeliveryAdapter adapter;
    View v;
    Context c;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_delivery, container, false);
        c = v.getContext();

        new_delivery_btn = v.findViewById(R.id.new_delivery_button);
        deliveries = v.findViewById(R.id.deliveries_recycler_view);

        adapter = new DeliveryAdapter(deliveries_list, c, (v, position) -> {});
        deliveries.setAdapter(adapter);
        deliveries.setLayoutManager(new LinearLayoutManager(c));

        fetch_deliveries();

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
            deliveries_list.addAll(Util.parse_fetch_deliveries_outgoing(res));

            c.getMainExecutor().execute(() -> {
                adapter.notifyDataSetChanged();
            });
        });
    }
}
