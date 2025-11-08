package com.example.touristmap.home;

import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import com.example.touristmap.map.MapActivity;
import com.example.touristmap.databinding.ActivityHomeBinding;
import com.google.firebase.auth.FirebaseAuth;

public class HomeActivity extends AppCompatActivity {
    private ActivityHomeBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityHomeBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Bundle bundle = getIntent().getExtras();

        String email = "";
        String provider = "";

        if (bundle != null) {
            email = bundle.getString("email", ""); // El segundo argumento es el valor por defecto si es nulo
            provider = bundle.getString("provider", "");
        }
        setup(email, provider);
    }
    private void setup(String email, String provider) {
        setTitle("Inicio");

        binding.emailView.setText(email);
        binding.providerView.setText(provider);

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finish();
        });

        binding.btnGoToMap.setOnClickListener(v -> {
            Intent mapIntent = new Intent(HomeActivity.this, MapActivity.class);
            startActivity(mapIntent);
        });
    }
}