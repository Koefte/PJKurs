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

public class RequestFragment extends Fragment {
    ArrayList<Delivery> deliveries_list = new ArrayList<>();
    RecyclerView deliveries;
    View v;
    Context c;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_delivery, container, false);
        c = v.getContext();

        deliveries = v.findViewById(R.id.deliveries_recycler_view);

        fetch_deliveries();

        DeliveryAdapter adapter = new DeliveryAdapter(deliveries_list, c);
        deliveries.setAdapter(adapter);
        deliveries.setLayoutManager(new LinearLayoutManager(c));

        set_listeners();

        return v;
    }

    private void set_listeners() {

    }

    private void fetch_deliveries() {
        DatabaseConnector.process_async_post_request("requests", Util.build_session_id_obj_string(), res -> {
            System.out.println(res);
            System.out.println(Util.parse_fetch_deliveries_outgoing(res));
            deliveries_list.addAll(Util.parse_fetch_deliveries_outgoing(res));
        });
    }
}
