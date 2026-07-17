package kateats.app;

import android.content.Intent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
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
        RatingBar ambiance;
        RatingBar food;
        RatingBar value;
        RatingBar service;
        TextView comment;
        ImageView photo;
        TextView author;
        Button edit;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            restaurant = itemView.findViewById(R.id.layout_review_name);
            score = itemView.findViewById(R.id.layout_review_score);
            ambiance = itemView.findViewById(R.id.layout_ambiance_score);
            food = itemView.findViewById(R.id.layout_food_score);
            value = itemView.findViewById(R.id.layout_value_score);
            service = itemView.findViewById(R.id.layout_service_score);
            comment = itemView.findViewById(R.id.layout_comment);
            photo = itemView.findViewById(R.id.layout_review_photo);
            author = itemView.findViewById(R.id.author);
            edit = itemView.findViewById(R.id.editReview);
        }
    }

    ReviewDisplay activity;
    String currentUserUuid;

    public ReviewAdaptor(ReviewDisplay activity, String currentUserUuid, @NonNull OrderedRealmCollection<Review> data) {
        super(data, true);
        this.activity = activity;
        this.currentUserUuid = currentUserUuid;
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
        holder.comment.setText(r.getComment());
        holder.score.setRating(r.getOverall());
        holder.ambiance.setRating(r.getAmbiance());
        holder.food.setRating(r.getFood());
        holder.value.setRating(r.getValue());
        holder.service.setRating(r.getService());

        User authorUser = r.getAuthor();
        holder.author.setText(authorUser != null ? authorUser.getName() : "Unknown");

        boolean isOwner = authorUser != null && currentUserUuid != null && authorUser.getUuid().equals(currentUserUuid);

        holder.edit.setVisibility(isOwner ? View.VISIBLE : View.GONE);

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, ReviewEdit.class);
                i.putExtra("reviewUuid", r.getUuid());
                activity.startActivity(i);
            }
        });

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
