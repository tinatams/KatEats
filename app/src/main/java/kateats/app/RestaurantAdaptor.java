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

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

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

        // gives you the data object at the given position
        File getImageDir = context.getExternalCacheDir();  // this method is in the Activity class

        // just a sample, normally you have a diff image name each time
        File file = new File(getImageDir, resto.getUuid()+".jpeg");

        if (file.exists()) {
            // this will put the image saved to the file system to the imageview
            Picasso.get()
                    .load(file)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(holder.image);
        }
        else {
            // use a default picture
            holder.image.setImageResource(R.mipmap.ic_launcher);
        }
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
