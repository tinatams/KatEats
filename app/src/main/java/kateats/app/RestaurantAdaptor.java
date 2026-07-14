package kateats.app;

import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.RatingBar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class RestaurantAdaptor extends RealmRecyclerViewAdapter<Restaurant, RestaurantAdaptor.ViewHolder> {
    LikedRestaurants activity;
    public RestaurantAdaptor(LikedRestaurants activity, @Nullable OrderedRealmCollection<Restaurant> data) {
        super(data, true);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create the raw view for this ViewHolder
        View v = activity.getLayoutInflater().inflate(R.layout.restaurant_layout, parent, false);  // VERY IMPORTANT TO USE THIS STYLE

        // assign view to the viewholder
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant resto = getItem(position);
        if (resto == null){
            return;
        }
        holder.restaurantName.setText(resto.getName());
        holder.rating.setRating(resto.getRating());
        holder.description.setText(resto.getDescription());
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView restaurantName;
        RatingBar rating;
        ImageView image;
        TextView description;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            restaurantName = itemView.findViewById(R.id.RestaurantNamePlaceholder);
            rating = itemView.findViewById(R.id.ratingPlaceholder);
            image = itemView.findViewById(R.id.restaurantPlaceholder);
            description = itemView.findViewById(R.id.descriptionPlaceholder);
        }
    }
}
