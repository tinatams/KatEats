package kateats.app;

import android.content.Intent;
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

public class AdminUserAdapter extends RealmRecyclerViewAdapter<User, AdminUserAdapter.ViewHolder> {
    AdminActivity activity;


    public class ViewHolder extends RecyclerView.ViewHolder{
        ImageView pfp;
        TextView accName, accPassword;
        ImageButton photo, edit, delete;
        public ViewHolder(@NonNull View itemView){
            super(itemView);
            pfp = itemView.findViewById(R.id.imgRowProfile);
            accName = itemView.findViewById(R.id.tvRowName);
            accPassword = itemView.findViewById(R.id.tvRowPassword);
            photo = itemView.findViewById(R.id.btnRowCamera);
            edit = itemView.findViewById(R.id.btnRowEdit);
            delete = itemView.findViewById(R.id.btnRowDelete);
        }
    }

    public AdminUserAdapter(AdminActivity activity, @Nullable OrderedRealmCollection<User> data){
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

    User user;
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        user = getItem(position);
        holder.accName.setText(user.getName());
        holder.accPassword.setText(user.getPassword());

        // gives you the data object at the given position
        File getImageDir = activity.getExternalCacheDir();  // this method is in the Activity class

        // just a sample, normally you have a diff image name each time
        File file = new File(getImageDir, user.getUuid()+"user.jpeg");

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
                Realm realm = user.getRealm();

                realm.beginTransaction();
                user.deleteFromRealm();
                realm.commitTransaction();
            }
        });

        holder.edit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(activity, UserEdit.class);
                i.putExtra("userUuid", user.getUuid());
                activity.startActivity(i);
            }
        });

        holder.pfp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.takePhoto(user, null, "USER");
            }
        });

        holder.photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activity.takePhoto(user, null, "USER");
            }
        });
    }

}