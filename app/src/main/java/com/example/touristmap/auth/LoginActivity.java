package com.example.touristmap.auth;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import androidx.appcompat.app.AppCompatActivity;
import com.example.touristmap.home.HomeActivity;
import com.example.touristmap.R;
import com.example.touristmap.databinding.ActivityLoginBinding;
import com.google.firebase.analytics.FirebaseAnalytics;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {
    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseAnalytics analytics;

    public enum ProviderType {
        BASIC
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.Base_Theme_TouristMap);
        super.onCreate(savedInstanceState);

        binding = ActivityLoginBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        auth = FirebaseAuth.getInstance();
        analytics = FirebaseAnalytics.getInstance(this);

        // Analytics Event
        Bundle bundle = new Bundle();
        bundle.putString("message", "Integracion de Firebase completa");
        analytics.logEvent("InitScreen", bundle);

        // Setup
        setup();
    }
    @Override
    protected void onStart(){
        super.onStart();
        FirebaseUser currentUser = auth.getCurrentUser();
        if (currentUser !=null) {
            showHome(currentUser.getEmail(), ProviderType.BASIC);
        }
    }
    private void setup() {
        setTitle("Login");
        binding.btnRegister.setOnClickListener(v -> handleRegistration());
        binding.btnLogin.setOnClickListener(v -> handleLogin());
    }
    private void handleRegistration() {
        // En Java se usa getText().toString().trim()
        final String email = binding.editEmail.getText().toString().trim();
        final String password = binding.editPassword.getText().toString().trim();

        if (validateInput(email, password)) return;

        setLoading(true);

        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {
                        analytics.logEvent("register_success", null);

                        String userEmail = (task.getResult().getUser() != null)
                                ? task.getResult().getUser().getEmail() : "";

                        showHome(userEmail, ProviderType.BASIC);
                    } else {
                        String errorMessage = (task.getException() != null)
                                ? task.getException().getMessage() : "Error en el registro";
                        showAlert(errorMessage);
                    }
                });
    }
    private void handleLogin() {
        final String email = binding.editEmail.getText().toString().trim();
        final String password = binding.editPassword.getText().toString().trim();

        if (validateInput(email, password)) return;

        setLoading(true);

        auth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    setLoading(false);
                    if (task.isSuccessful()) {

                        analytics.logEvent("login_success", null);

                        String userEmail = (task.getResult().getUser() != null)
                                ? task.getResult().getUser().getEmail() : "";
                        showHome(userEmail, ProviderType.BASIC);
                    } else {
                        String errorMessage = (task.getException() != null)
                                ? task.getException().getMessage() : "Email o contraseña incorrectos";
                        showAlert(errorMessage);
                    }
                });
    }
    private boolean validateInput(String email, String pass) {
        binding.editEmail.setError(null);
        binding.editPassword.setError(null);

        if (email.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.editEmail.setError("Por favor, introduce un email válido");
            return true;
        }

        if (pass.isEmpty() || pass.length() < 6) {
            binding.editPassword.setError("La contraseña debe tener al menos 6 caracteres");
            return true;
        }

        return false;
    }

    private void setLoading(boolean isLoading) {
        if (isLoading) {
            binding.progressBar.setVisibility(View.VISIBLE);
            binding.btnLogin.setEnabled(false);
            binding.btnRegister.setEnabled(false);
        } else {
            binding.progressBar.setVisibility(View.GONE);
            binding.btnLogin.setEnabled(true);
            binding.btnRegister.setEnabled(true);
        }
    }
    private void showAlert(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Error");
        builder.setMessage(message);
        builder.setPositiveButton("Aceptar", null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showHome(String email, ProviderType provider) {
        Intent homeIntent = new Intent(this, HomeActivity.class);
        homeIntent.putExtra("email", email);
        homeIntent.putExtra("provider", provider.name());
        startActivity(homeIntent);
        finish();
    }
}