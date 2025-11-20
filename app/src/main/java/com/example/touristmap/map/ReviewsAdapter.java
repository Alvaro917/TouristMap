package com.example.touristmap.map;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RatingBar;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.example.touristmap.R;
import java.util.ArrayList;
import java.util.List;

public class ReviewsAdapter extends RecyclerView.Adapter<ReviewsAdapter.ViewHolder> {
    private List<Review> list = new ArrayList<>();
    public void setList(List<Review> list) {
        this.list = list;
        notifyDataSetChanged();
    }
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_review, parent, false);
        return new ViewHolder(view);
    }
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review review = list.get(position);
        if (review.getAuthor() != null) {
            holder.tvAuthor.setText(review.getAuthor());
        } else {
            holder.tvAuthor.setText("Anónimo");
        }
        holder.tvComment.setText(review.getComment());
        holder.rbRating.setRating(review.getRating());
        if (review.getPlaceName() != null && !review.getPlaceName().isEmpty()) {
            holder.tvPlaceName.setText("en " + review.getPlaceName());
            holder.tvPlaceName.setVisibility(View.VISIBLE);
        } else {
            holder.tvPlaceName.setVisibility(View.GONE);
        }
    }
    @Override
    public int getItemCount() {
        return list.size();
    }
    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView tvAuthor, tvComment, tvPlaceName;
        RatingBar rbRating;
        ViewHolder(View v) {
            super(v);
            tvAuthor = v.findViewById(R.id.tvAuthor);
            tvComment = v.findViewById(R.id.tvComment);
            tvPlaceName = v.findViewById(R.id.tvPlaceName);
            rbRating = v.findViewById(R.id.rbRating);
        }
    }
}