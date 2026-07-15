package kateats.app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class ReviewAdaptor extends RealmRecyclerViewAdapter<Review, ReviewAdaptor.ViewHolder> {

    public class ViewHolder extends RecyclerView.ViewHolder {
        TextView restaurant;
        RatingBar score;
        ImageView photo;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurant = itemView.findViewById(R.id.layout_review_name);
            score = itemView.findViewById(R.id.layout_review_score);
            photo = itemView.findViewById(R.id.layout_review_photo);
        }
    }

    ReviewDisplay activity;

    public ReviewAdaptor(ReviewDisplay activity, @NonNull OrderedRealmCollection<Review> data) {
        super(data, true);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.review_layout, parent, false);  // VERY IMPORTANT TO USE THIS STYLE

        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Review r = getItem(position);
        holder.restaurant.setText(r.getRestaurant().getName());
        holder.score.setRating(r.getOverall());
        File image = new File(
                activity.getExternalCacheDir(),
                r.getUuid() + ".jpeg"
        );
        if (image.exists()) {
            Picasso.get()
                    .load(image)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(holder.photo);
        } else {
            holder.photo.setImageResource(R.mipmap.ic_launcher);
        }
    }
}
