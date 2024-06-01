package de.anbeli.dronedelivery.fragment.delivery;

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
import de.anbeli.dronedelivery.data.Delivery;
import de.anbeli.dronedelivery.data.DeliveryAdapter;

public class DeliveryFragment extends Fragment {

    ArrayList<Delivery> deliveries_list;
    RecyclerView deliveries;
    View v;
    Context c;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_delivery, container, false);
        c = v.getContext();

        deliveries = (RecyclerView) v.findViewById(R.id.deliveries_recycler_view);

        deliveries_list = Delivery.createContactsList(10);
        DeliveryAdapter adapter = new DeliveryAdapter(deliveries_list);
        deliveries.setAdapter(adapter);
        deliveries.setLayoutManager(new LinearLayoutManager(c));

        return v;
    }


}
