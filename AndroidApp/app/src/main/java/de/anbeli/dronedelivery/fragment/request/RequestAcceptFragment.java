package de.anbeli.dronedelivery.fragment.request;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.data.Delivery;

public class RequestAcceptFragment extends Fragment {
    View v;
    Context c;
    Delivery delivery;

    public RequestAcceptFragment(Delivery delivery) {
        this.delivery = delivery;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_delivery_accept, container, false);
        c = v.getContext();

        return v;
    }
}
