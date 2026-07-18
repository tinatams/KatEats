package kateats.app;

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

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

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
    User remeberedUser;
    Restaurant rememberedResto;
    public void takePhoto(@Nullable User u, @Nullable Restaurant r, String type)
    {
        if (type.equals("USER")) remeberedUser = u;
        else if (type.equals("RESTO")) rememberedResto = r;

        this.type = type;
        Intent i = new Intent(this, ImageActivity.class);
        startActivityForResult(i, REQUEST_CODE_IMAGE_SCREEN);
    }


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
                        File savedImage = saveFile(jpeg, remeberedUser.getUuid()+"user.jpeg");
                    } else if (type.equals("RESTO")){
                        File savedImage = saveFile(jpeg, remeberedUser.getUuid()+"resto.jpeg");
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
}
