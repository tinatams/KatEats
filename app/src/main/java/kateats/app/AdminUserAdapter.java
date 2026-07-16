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

public class AdminUserAdapter extends RealmRecyclerViewAdapter<User, AdminUserAdapter.ViewHolder> {

    private AdminActivity activity;
    private OnUserActionListener listener;

    public interface OnUserActionListener {
        void onDelete(User user);
        void onEdit(User user);
        void onCapturePhoto(User user);
    }

    public AdminUserAdapter(AdminActivity activity, @Nullable OrderedRealmCollection<User> data, OnUserActionListener listener) {
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
        User user = getItem(position);
        if (user == null) return;

        holder.tvName.setText(user.getName());
        holder.tvUuid.setText("UUID: " + user.getUuid());

        // Decode byte[] photo
        if (user.getPhoto() != null && user.getPhoto().length > 0) {
            Bitmap bitmap = BitmapFactory.decodeByteArray(user.getPhoto(), 0, user.getPhoto().length);
            holder.imgProfile.setImageBitmap(bitmap);
        } else {
            holder.imgProfile.setImageResource(R.mipmap.ic_launcher);
        }

        holder.btnDelete.setOnClickListener(v -> listener.onDelete(user));
        holder.btnEdit.setOnClickListener(v -> listener.onEdit(user));
        holder.btnCamera.setOnClickListener(v -> listener.onCapturePhoto(user));
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
