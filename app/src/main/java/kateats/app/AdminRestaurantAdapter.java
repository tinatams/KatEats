package kateats.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.OrderedRealmCollection;
import io.realm.Realm;
import io.realm.RealmRecyclerViewAdapter;

public class AdminRestaurantAdapter extends RealmRecyclerViewAdapter<Restaurant, AdminRestaurantAdapter.ViewHolder> {
    AdminActivity activity;


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView pfp;
        TextView accName, accPassword;
        ImageButton edit, delete;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            pfp = itemView.findViewById(R.id.imgRowProfile);
            accName = itemView.findViewById(R.id.tvRowName);
            accPassword = itemView.findViewById(R.id.tvRowPassword);
            edit = itemView.findViewById(R.id.btnRowEdit);
            delete = itemView.findViewById(R.id.btnRowDelete);
        }
    }

    public AdminRestaurantAdapter(AdminActivity activity, @Nullable OrderedRealmCollection<Restaurant> data){
        super(data, true);
        this.activity = activity;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = activity.getLayoutInflater().inflate(R.layout.item_admin_row, parent, false);  // VERY IMPORTANT TO USE THIS STYLE

        // assign view to the viewholder
        ViewHolder vh = new ViewHolder(v);
        return vh;
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        final Restaurant resto = getItem(position);
        holder.accName.setText(resto.getName());
        holder.accPassword.setText(resto.getPassword());

        // gives you the data object at the given position
        File getImageDir = activity.getExternalCacheDir();  // this method is in the Activity class

        // just a sample, normally you have a diff image name each time
        File file = new File(getImageDir, resto.getUuid()+".jpeg");

        if (file.exists()) {
            // this will put the image saved to the file system to the imageview
            Picasso.get()
                    .load(file)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(holder.pfp);
        }
        else {
            // use a default picture
            holder.pfp.setImageResource(R.mipmap.ic_launcher);
        }

        holder.delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.deleteResto(resto);
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, RestaurantEdit.class);
                i.putExtra("restoID", resto.getUuid());
                activity.startActivity(i);
            }
        });

    }

}