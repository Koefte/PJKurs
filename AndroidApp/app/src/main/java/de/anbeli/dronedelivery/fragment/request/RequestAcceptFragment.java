package de.anbeli.dronedelivery.fragment.request;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.health.connect.datatypes.ExerciseRoute;
import android.location.Location;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.Task;

import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.activities.MainActivity;
import de.anbeli.dronedelivery.data.Delivery;
import de.anbeli.dronedelivery.util.DatabaseConnector;
import de.anbeli.dronedelivery.util.ErrorPopup;
import de.anbeli.dronedelivery.util.Util;

public class RequestAcceptFragment extends Fragment implements OnMapReadyCallback {

    private final int FINE_PERMISSION_CODE = 1;
    LatLng delivery_location;

    View v;
    Context c;
    Delivery delivery;
    GoogleMap map;
    Button accept_btn;

    public RequestAcceptFragment(Delivery delivery) {
        this.delivery = delivery;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        v = inflater.inflate(R.layout.fragment_delivery_accept, container, false);
        c = v.getContext();

        accept_btn = v.findViewById(R.id.accept_delivery_button);

        set_listeners();

        SupportMapFragment mapFragment = (SupportMapFragment) this.getChildFragmentManager().findFragmentById(R.id.fragment_map);
        mapFragment.getMapAsync(this);

        return v;
    }

    @Override
    public void onMapReady(@NonNull GoogleMap googleMap) {
        this.map = googleMap;

        map.setOnMapClickListener(latLng ->  {
            if(delivery_location != null) return;
            delivery_location = latLng;

            Marker marker = map.addMarker(new MarkerOptions()
                    .position(delivery_location)
                    .title("Drop-off Point for Delivery from" + delivery.get_receiver()));
            marker.setDraggable(true);
        });

    }

    private void set_listeners() {
        accept_btn.setOnClickListener(v -> {
            if(delivery_location == null) {
                ErrorPopup errorPopup = new ErrorPopup(c, getString(R.string.no_delivery_location));
                errorPopup.show();
                return;
            }
            DatabaseConnector.process_async_post_request("requests", Util.build_acceptor_session_id_obj_string(), res -> {
                System.out.println(res);
            });

            DatabaseConnector.process_async_post_request("requests", Util.build_request_b_obj_string(delivery_location, delivery), res -> {
                System.out.println(res);
            });

            ((MainActivity) getActivity()).replace_fragment(new RequestFragment());
        });
    }
}
