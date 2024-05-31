package de.anbeli.dronedelivery.activities;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import de.anbeli.dronedelivery.IMenu;
import de.anbeli.dronedelivery.R;
import de.anbeli.dronedelivery.databinding.ActivityMainBinding;
import de.anbeli.dronedelivery.fragment.ContactFragment;
import de.anbeli.dronedelivery.fragment.DeliveryFragment;
import de.anbeli.dronedelivery.fragment.DroneFragment;
import de.anbeli.dronedelivery.fragment.SettingsFragment;

public class MainActivity extends AppCompatActivity implements IMenu {

    ActivityMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        binding.bottomNavigationView.setOnItemSelectedListener(menuItem -> {
            if(menuItem.getItemId() == R.id.nav_deliveries)
                replace_fragment(new DeliveryFragment());
            else if(menuItem.getItemId() == R.id.nav_contacts)
                replace_fragment(new ContactFragment());
            else if(menuItem.getItemId() == R.id.nav_drones)
                replace_fragment(new DroneFragment());
            else if(menuItem.getItemId() == R.id.nav_settings)
                replace_fragment(new SettingsFragment());

            return true;
        });

        update_menu();
    }

    public void update_menu() {
        boolean drone_management = getSharedPreferences("save_data", MODE_PRIVATE).getBoolean("drone_management", false);

        if(drone_management) {
            binding.bottomNavigationView.getMenu().getItem(0).setVisible(true);
            binding.bottomNavigationView.getMenu().getItem(2).setVisible(true);
        } else {
            binding.bottomNavigationView.getMenu().getItem(0).setVisible(false);
            binding.bottomNavigationView.getMenu().getItem(2).setVisible(false);
        }
    }

    private void replace_fragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frameLayout, fragment);
        fragmentTransaction.commit();
    }
}