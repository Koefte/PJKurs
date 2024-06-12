package de.anbeli.dronedelivery.fragment.request;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.activities.MainActivity;
import de.anbeli.dronedelivery.data.Delivery;
import de.anbeli.dronedelivery.data.DeliveryAdapter;
import de.anbeli.dronedelivery.fragment.delivery.DeliveryCreationFragment;
import de.anbeli.dronedelivery.util.DatabaseConnector;
import de.anbeli.dronedelivery.util.Util;
import de.anbeli.dronedelivery.util.listeners.onRequestClickListener;

public class RequestFragment extends Fragment {
    ArrayList<Delivery> deliveries_list = new ArrayList<>();
    RecyclerView deliveries;
    DeliveryAdapter adapter;
    View v;
    Context c;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_request, container, false);
        c = v.getContext();

        deliveries = v.findViewById(R.id.deliveries_recycler_view);

        adapter = new DeliveryAdapter(deliveries_list, c, (v, position) -> {
            if(deliveries_list.get(position).get_state() != Delivery.delivery_state.TO_BE_CONFIRMED) return;
            ((MainActivity) getActivity()).replace_fragment(new RequestAcceptFragment(deliveries_list.get(position)));
        });
        deliveries.setAdapter(adapter);
        deliveries.setLayoutManager(new LinearLayoutManager(c));

        fetch_deliveries();

        set_listeners();

        return v;
    }

    private void set_listeners() {

    }

    private void fetch_deliveries() {
        DatabaseConnector.process_async_post_request("requests", Util.build_session_id_obj_string(), res -> {
            deliveries_list.addAll(Util.parse_fetch_deliveries_incoming(res));

            c.getMainExecutor().execute(() -> {
                adapter.notifyDataSetChanged();
            });
        });
    }
}
