package de.anbeli.dronedelivery.fragment.delivery;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import de.anbeli.dronedelivery.R;

public class DeliveryCreationFragment extends Fragment {
    View v;
    Context c;
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_settings, container, false);
        c = v.getContext();

        return v;
    }
}
