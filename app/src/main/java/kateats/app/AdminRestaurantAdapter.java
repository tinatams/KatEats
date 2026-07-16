package kateats.app;

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

import io.realm.OrderedRealmCollection;
import io.realm.RealmRecyclerViewAdapter;

public class AdminRestaurantAdapter extends RealmRecyclerViewAdapter<Restaurant, AdminRestaurantAdapter.ViewHolder> {

    private AdminActivity activity;
    private OnRestaurantActionListener listener;

    public interface OnRestaurantActionListener {
        void onDelete(Restaurant restaurant);
        void onEdit(Restaurant restaurant);
        void onCapturePhoto(Restaurant restaurant);
    }

    public AdminRestaurantAdapter(AdminActivity activity, @Nullable OrderedRealmCollection<Restaurant> data, OnRestaurantActionListener listener) {
        super(data, true); // true enables automatic UI updates when the database updates
        this.activity = activity;
        this.listener = listener;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_admin_row, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Restaurant restaurant = getItem(position);
        if (restaurant == null) return;

        holder.tvName.setText(restaurant.getName());
        holder.tvUuid.setText("UUID: " + restaurant.getUuid());

        // Decode byte[] photo
        if (restaurant.getPhoto() != null && restaurant.getPhoto().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(restaurant.getPhoto(), 0, restaurant.getPhoto().length);
            holder.imgProfile.setImageBitmap(bitmap);
        } else {
            holder.imgProfile.setImageResource(R.mipmap.ic_launcher);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(restaurant));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(restaurant));
        holder.btnCamera.setOnClickListener(v -> listener.onCapturePhoto(restaurant));
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imgProfile;
        TextView tvName, tvUuid;
        ImageButton btnCamera, btnEdit, btnDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imgProfile = itemView.findViewById(R.id.imgRowProfile);
            tvName = itemView.findViewById(R.id.tvRowName);
            tvUuid = itemView.findViewById(R.id.tvRowUuid);
            btnCamera = itemView.findViewById(R.id.btnRowCamera);
            btnEdit = itemView.findViewById(R.id.btnRowEdit);
            btnDelete = itemView.findViewById(R.id.btnRowDelete);
        }
    }
}
