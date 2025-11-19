package com.example.touristmap.home;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.example.touristmap.R;
import com.example.touristmap.auth.LoginActivity;
import com.google.firebase.auth.FirebaseAuth;

public class InfoFragment extends Fragment {

    private String email;
    private String provider;

    public InfoFragment() {} // Constructor vacío necesario

    public InfoFragment(String email, String provider) {
        this.email = email;
        this.provider = provider;
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_info, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        TextView txtEmail = view.findViewById(R.id.emailView);
        TextView txtProvider = view.findViewById(R.id.providerView);
        Button btnLogout = view.findViewById(R.id.btnLogout);

        if(email != null) txtEmail.setText(email);
        if(provider != null) txtProvider.setText(provider);

        btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            if (getActivity() != null) getActivity().finish();
        });
    }
}