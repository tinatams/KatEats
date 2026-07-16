package kateats.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Realm;
import io.realm.RealmResults;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvUsers, rvRestaurants;
    private Button btnAddAccount, btnClearAll;

    private Realm realm;
    private AdminUserAdapter userAdapter;
    private AdminRestaurantAdapter restaurantAdapter;

    // Temporary trackers to know which object we are updating when we return from the camera activity
    private String pendingPhotoUuid = null;
    private boolean isPendingPhotoRestaurant = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // Bind layout views
        rvUsers = findViewById(R.id.rvUsers);
        rvRestaurants = findViewById(R.id.rvRestaurants);
        btnAddAccount = findViewById(R.id.btnAdminAdd);
        btnClearAll = findViewById(R.id.btnAdminClear);

        realm = Realm.getDefaultInstance();

        setupRecyclerViews();

        // Add Account quickly redirects to the Register screen
        btnAddAccount.setOnClickListener(v -> {
            Intent intent = new Intent(AdminActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        // Clear All Database records
        btnClearAll.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Database")
                    .setMessage("Are you sure you want to delete ALL users and restaurants?")
                    .setPositiveButton("Yes, Clear", (dialog, which) -> {
                        realm.executeTransaction(r -> r.deleteAll());
                        Toast.makeText(this, "Database cleared!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });
    }

    private void setupRecyclerViews() {
        // Query all Users ordered by name
        RealmResults<User> users = realm.where(User.class).findAll().sort("name");
        userAdapter = new AdminUserAdapter(this, users, new AdminUserAdapter.OnUserActionListener() {
            @Override
            public void onDelete(User user) {
                final String uuid = user.getUuid();
                realm.executeTransaction(r -> {
                    User toDelete = r.where(User.class).equalTo("uuid", uuid).findFirst();
                    if (toDelete != null) toDelete.deleteFromRealm();
                });
            }

            @Override
            public void onEdit(User user) {
                showEditNameDialog(user.getUuid(), user.getName(), false);
            }

            @Override
            public void onCapturePhoto(User user) {
                pendingPhotoUuid = user.getUuid();
                isPendingPhotoRestaurant = false;

                Intent intent = new Intent(AdminActivity.this, ImageActivity.class);
                startActivityForResult(intent, 200);
            }
        });

        rvUsers.setLayoutManager(new LinearLayoutManager(this));
        rvUsers.setAdapter(userAdapter);

        // Query all Restaurants ordered by name
        RealmResults<Restaurant> restaurants = realm.where(Restaurant.class).findAll().sort("name");
        restaurantAdapter = new AdminRestaurantAdapter(this, restaurants, new AdminRestaurantAdapter.OnRestaurantActionListener() {
            @Override
            public void onDelete(Restaurant restaurant) {
                final String uuid = restaurant.getUuid();
                realm.executeTransaction(r -> {
                    Restaurant toDelete = r.where(Restaurant.class).equalTo("uuid", uuid).findFirst();
                    if (toDelete != null) toDelete.deleteFromRealm();
                });
            }

            @Override
            public void onEdit(Restaurant restaurant) {
                showEditNameDialog(restaurant.getUuid(), restaurant.getName(), true);
            }

            @Override
            public void onCapturePhoto(Restaurant restaurant) {
                pendingPhotoUuid = restaurant.getUuid();
                isPendingPhotoRestaurant = true;

                Intent intent = new Intent(AdminActivity.this, ImageActivity.class);
                startActivityForResult(intent, 200);
            }
        });

        rvRestaurants.setLayoutManager(new LinearLayoutManager(this));
        rvRestaurants.setAdapter(restaurantAdapter);
    }

    private void showEditNameDialog(String uuid, String currentName, boolean isRestaurant) {
        EditText etNameInput = new EditText(this);
        etNameInput.setText(currentName);

        new AlertDialog.Builder(this)
                .setTitle("Edit Account Name")
                .setView(etNameInput)
                .setPositiveButton("Save", (dialog, which) -> {
                    String newName = etNameInput.getText().toString().trim();
                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Name cannot be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    realm.executeTransaction(r -> {
                        if (isRestaurant) {
                            Restaurant resto = r.where(Restaurant.class).equalTo("uuid", uuid).findFirst();
                            if (resto != null) resto.setName(newName);
                        } else {
                            User u = r.where(User.class).equalTo("uuid", uuid).findFirst();
                            if (u != null) u.setName(newName);
                        }
                    });
                    Toast.makeText(this, "Name updated!", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Code 200 indicates we are updating an image inside the admin list
        if (requestCode == 200 && resultCode == ImageActivity.RESULT_CODE_IMAGE_TAKEN && data != null) {
            byte[] jpeg = data.getByteArrayExtra("rawJpeg");
            if (jpeg != null && pendingPhotoUuid != null) {
                realm.executeTransaction(r -> {
                    if (isPendingPhotoRestaurant) {
                        Restaurant resto = r.where(Restaurant.class).equalTo("uuid", pendingPhotoUuid).findFirst();
                        if (resto != null) resto.setPhoto(jpeg);
                    } else {
                        User user = r.where(User.class).equalTo("uuid", pendingPhotoUuid).findFirst();
                        if (user != null) user.setPhoto(jpeg);
                    }
                });
                Toast.makeText(this, "Photo updated successfully!", Toast.LENGTH_SHORT).show();
            }
            // Reset state trackers
            pendingPhotoUuid = null;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
