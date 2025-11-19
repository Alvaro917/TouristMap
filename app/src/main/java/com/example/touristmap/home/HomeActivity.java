package com.example.touristmap.home;

import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.example.touristmap.R;
import com.example.touristmap.databinding.ActivityHomeBinding;
import com.example.touristmap.map.MapFragment;

public class HomeActivity extends AppCompatActivity {

    public static final String KEY_EMAIL = "email";
    public static final String KEY_PROVIDER = "provider";

    private ActivityHomeBinding binding;
    private String email;
    private String provider;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Recuperar datos del Login
        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            email = bundle.getString(KEY_EMAIL, "");
            provider = bundle.getString(KEY_PROVIDER, "");
        }

        setupNavigation();
    }

    private void setupNavigation() {
        // Cargar InfoFragment por defecto al abrir
        loadFragment(new InfoFragment(email, provider));

        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            Fragment fragment = null;
            int itemId = item.getItemId();

            if (itemId == R.id.nav_home) {
                fragment = new InfoFragment(email, provider);
            } else if (itemId == R.id.nav_map) {
                fragment = new MapFragment();
            }

            if (fragment != null) {
                loadFragment(fragment);
                return true;
            }
            return false;
        });
    }

    private void loadFragment(Fragment fragment) {
        getSupportFragmentManager()
                .beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .commit();
    }
}