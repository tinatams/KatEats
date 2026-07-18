package kateats.app;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import io.realm.Realm;
import io.realm.RealmResults;

public class AdminActivity extends AppCompatActivity {

    private RecyclerView rvUsers, rvRestaurants;
    private Button btnAddAccount, btnClearAll, cancel;

    private Realm realm;
    private AdminUserAdapter userAdapter;
    private AdminRestaurantAdapter restaurantAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_admin);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cancelTinder), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    public void onCancel(){
        finish();
    }

    private void init() {
        cancel = findViewById(R.id.cancelAdmin);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });
        rvUsers = findViewById(R.id.rvUsers);
        rvRestaurants = findViewById(R.id.rvRestaurants);
        btnAddAccount = findViewById(R.id.btnAdminAdd);
        btnClearAll = findViewById(R.id.btnAdminClear);

        realm = Realm.getDefaultInstance();

        setupRecyclerViews();

        btnAddAccount.setOnClickListener(v -> {
            Intent intent = new Intent(this, RegisterActivity.class);
            startActivity(intent);
        });

        btnClearAll.setOnClickListener(v -> {
            new AlertDialog.Builder(this)
                    .setTitle("Clear Database")
                    .setMessage("Are you sure you want to delete ALL users and restaurants?")
                    .setPositiveButton("Yes, Clear", (dialog, which) -> {
                        realm.beginTransaction();
                        realm.deleteAll();
                        realm.commitTransaction();
                        Toast.makeText(this, "Database cleared!", Toast.LENGTH_SHORT).show();
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
        });

    }

    private void setupRecyclerViews() {
        rvUsers = findViewById(R.id.rvUsers);
        RealmResults<User> users = realm.where(User.class).findAll().sort("name");

        rvRestaurants = findViewById(R.id.rvRestaurants);
        RealmResults<Restaurant> restaurants = realm.where(Restaurant.class).findAll().sort("name");

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvUsers.setLayoutManager(mLayoutManager);
        LinearLayoutManager nLayoutManager = new LinearLayoutManager(this);
        nLayoutManager.setOrientation(RecyclerView.VERTICAL);
        rvRestaurants.setLayoutManager(nLayoutManager);

        userAdapter = new AdminUserAdapter(this, users);
        rvUsers.setAdapter(userAdapter);
        restaurantAdapter = new AdminRestaurantAdapter(this, restaurants);
        rvRestaurants.setAdapter(restaurantAdapter);

    }

    //CAMERA STUFF

    public static int REQUEST_CODE_IMAGE_SCREEN = 0;

    String type;
    User rememberedUser;
    Restaurant rememberedResto;


    public void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);

        if (requestCode==REQUEST_CODE_IMAGE_SCREEN)
        {
            if (responseCode==ImageActivity.RESULT_CODE_IMAGE_TAKEN)
            {
                // receieve the raw JPEG data from ImageActivity
                // this can be saved to a file or save elsewhere like Realm or online
                byte[] jpeg = data.getByteArrayExtra("rawJpeg");

                try {
                    //SAVE
                    // save rawImage to file
                    if (type.equals("USER")){
                        File savedImage = saveFile(jpeg, rememberedUser.getUuid()+".jpeg");
                        Picasso.get().invalidate(savedImage);
                        userAdapter.notifyDataSetChanged();
                    } else if (type.equals("RESTO")){
                        File savedImage = saveFile(jpeg, rememberedResto.getUuid()+".jpeg");
                        Picasso.get().invalidate(savedImage);
                        restaurantAdapter.notifyDataSetChanged();
                    }

                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    private File saveFile(byte[] jpeg, String filename) throws IOException
    {
        // this is the root directory for the images
        File getImageDir = getExternalCacheDir();

        // just a sample, normally you have a diff image name each time
        File savedImage = new File(getImageDir, filename);


        FileOutputStream fos = new FileOutputStream(savedImage);
        fos.write(jpeg);
        fos.close();
        return savedImage;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }

    public void deleteResto(Restaurant r) {
        if (r.isValid()){
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
            builder.setMessage("Do you want to delete " + r.getName() +"? This cannot be undone");
            builder.setTitle("Alert!");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    realm.beginTransaction();
                    realm.where(Review.class)
                            .equalTo("restaurant.uuid", r.getUuid())
                            .findAll()
                            .deleteAllFromRealm();
                    r.deleteFromRealm();
                    realm.commitTransaction();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
    }

    public void deleteUser(User u) {
        if (u.isValid()){
            AlertDialog.Builder builder = new AlertDialog.Builder(AdminActivity.this);
            builder.setMessage("Do you want to delete " + u.getName() +"? This cannot be undone");
            builder.setTitle("Alert!");
            builder.setCancelable(false);
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    realm.beginTransaction();
                    RealmResults<Review> theirReviews = realm.where(Review.class)
                            .equalTo("author.uuid", u.getUuid())
                            .findAll();
                    Set<String> affectedRestoUuids = new HashSet<>();
                    for (Review rev : theirReviews) {
                        if (rev.getRestaurant() != null) {
                            affectedRestoUuids.add(rev.getRestaurant().getUuid());
                        }
                    }
                    theirReviews.deleteAllFromRealm();
                    u.deleteFromRealm();
                    for (String restoUuid : affectedRestoUuids) {
                        Restaurant resto = realm.where(Restaurant.class)
                                .equalTo("uuid", restoUuid)
                                .findFirst();
                        if (resto == null) continue;

                        Number avg = realm.where(Review.class)
                                .equalTo("restaurant.uuid", restoUuid)
                                .average("overall");
                        resto.setRating(avg == null ? 0f : avg.floatValue());
                    }
                    realm.commitTransaction();
                }
            });
            builder.setNegativeButton("No", new DialogInterface.OnClickListener(){
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            AlertDialog alertDialog = builder.create();
            alertDialog.show();

        }
    }
}
