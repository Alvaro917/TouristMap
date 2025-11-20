package com.example.touristmap.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.example.touristmap.R;
import com.example.touristmap.map.Review;
import com.example.touristmap.map.ReviewsAdapter;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import org.json.JSONArray;
import org.json.JSONObject;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ReviewsFragment extends Fragment {

    private RecyclerView rvReviews;
    private ReviewsAdapter adapter;
    private DatabaseReference dbRef;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_reviews, container, false);
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        rvReviews = view.findViewById(R.id.rvReviews);
        FloatingActionButton fab = view.findViewById(R.id.fabAddReview);


        adapter = new ReviewsAdapter();
        rvReviews.setLayoutManager(new LinearLayoutManager(getContext()));
        rvReviews.setAdapter(adapter);


        dbRef = FirebaseDatabase.getInstance().getReference().child("public_reviews");

        loadReviews();

        fab.setOnClickListener(v -> showAddReviewDialog());
    }
    private void loadReviews() {
        dbRef.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Review> list = new ArrayList<>();
                for (DataSnapshot data : snapshot.getChildren()) {
                    Review r = data.getValue(Review.class);
                    if (r != null) list.add(r);
                }
                Collections.reverse(list);
                adapter.setList(list);
            }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }
    private void showAddReviewDialog() {
        if (getContext() == null) return;
        BottomSheetDialog dialog = new BottomSheetDialog(getContext());
        View view = getLayoutInflater().inflate(R.layout.layout_add_review, null);
        dialog.setContentView(view);

        Spinner spinnerPlaces = view.findViewById(R.id.spinnerPlaces);
        EditText etComment = view.findViewById(R.id.etComment);
        RatingBar ratingBar = view.findViewById(R.id.ratingBar);
        Button btnPost = view.findViewById(R.id.btnPost);

        List<String> placeNames = getPlaceNamesFromJSON();

        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_item, placeNames);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerPlaces.setAdapter(spinnerAdapter);

        btnPost.setOnClickListener(v -> {
            String comment = etComment.getText().toString().trim();
            float rating = ratingBar.getRating();
            if (spinnerPlaces.getSelectedItem() == null) {
                Toast.makeText(getContext(), "No hay lugares cargados", Toast.LENGTH_SHORT).show();
                return;
            }
            String placeName = spinnerPlaces.getSelectedItem().toString();
            if (comment.isEmpty()) {
                etComment.setError("Escribe algo");
                return;
            }
            String author = "Anónimo";
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                author = FirebaseAuth.getInstance().getCurrentUser().getEmail();
            }
            Review newReview = new Review(author, comment, rating, placeName);
            dbRef.push().setValue(newReview);
            Toast.makeText(getContext(), "Publicado en el muro", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
        });
        dialog.show();
    }
    private List<String> getPlaceNamesFromJSON() {
        List<String> names = new ArrayList<>();
        try {
            InputStream is = requireContext().getAssets().open("lugares.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);
            JSONArray jsonArray = new JSONArray(json);
            for (int i = 0; i < jsonArray.length(); i++) {
                JSONObject obj = jsonArray.getJSONObject(i);
                names.add(obj.getString("nombre"));
            }
        } catch (Exception e) {
            e.printStackTrace();
            names.add("General");
        }
        return names;
    }
}