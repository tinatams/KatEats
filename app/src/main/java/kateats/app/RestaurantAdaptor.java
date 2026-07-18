package kateats.app;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
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
    Context context;
    int layoutID;
    public RestaurantAdaptor(Context context, int layoutID, @Nullable OrderedRealmCollection<Restaurant> data) {
        super(data, true);
        this.context = context;
        this.layoutID = layoutID;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // create the raw view for this ViewHolder
        View v = LayoutInflater.from(context).inflate(layoutID, parent, false);  // VERY IMPORTANT TO USE THIS STYLE

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

        holder.cuisine.setText(resto.getCuisine());
        holder.restaurantName.setText(resto.getName());
        holder.rating.setRating(resto.getRating());
        holder.rating.setIsIndicator(true);
        holder.description.setText(resto.getDescription());
        holder.image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(context, RestaurantView.class);
                i.putExtra("restoID", resto.getUuid());
                context.startActivity(i);
            }
        });
    }

    public class ViewHolder extends RecyclerView.ViewHolder{
        TextView restaurantName, cuisine;
        RatingBar rating;
        ImageView image;
        TextView description;

        public ViewHolder(@NonNull View itemView){
            super(itemView);

            cuisine = itemView.findViewById(R.id.layout_cuisine_view);
            restaurantName = itemView.findViewById(R.id.RestaurantNamePlaceholder);
            rating = itemView.findViewById(R.id.ratingPlaceholder);
            image = itemView.findViewById(R.id.restaurantPlaceholder);
            description = itemView.findViewById(R.id.descriptionPlaceholder);
        }
    }
}
