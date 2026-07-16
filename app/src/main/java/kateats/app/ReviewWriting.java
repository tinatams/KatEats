package kateats.app;

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.UUID;

import io.realm.Realm;
import io.realm.RealmResults;

public class ReviewWriting extends AppCompatActivity {

    String uuid;
    AutoCompleteTextView restaurant;
    EditText comments;
    RatingBar overall;
    RatingBar ambiance;
    RatingBar food;
    RatingBar value;
    RatingBar service;
    ImageView photo;
    File getImageDir;
    File file;
    Realm realm;
    Button submit;
    RealmResults<Restaurant> restaurantArrayList;
    ArrayList<String> names;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_writing);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkPermissions();
    }

    public void init() {
        uuid = UUID.randomUUID().toString();
        restaurant = findViewById(R.id.reviewRestaurant);

        restaurantArrayList = realm.where(Restaurant.class).findAll();

        names = new ArrayList<>();
        for (Restaurant r : restaurantArrayList) {
            names.add(r.getName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_dropdown_item_1line,
                names
        );

        restaurant.setAdapter(adapter);
        restaurant.setThreshold(1);

        comments = findViewById(R.id.reviewComments);
        overall = findViewById(R.id.reviewOverall);
        ambiance = findViewById(R.id.reviewAmbiance);
        food = findViewById(R.id.reviewFood);
        value = findViewById(R.id.reviewValue);
        service = findViewById(R.id.reviewService);
        submit = findViewById(R.id.reviewSubmit);
        realm = Realm.getDefaultInstance();
        photo.setImageResource(R.mipmap.ic_launcher);

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check the resto name, check overall, ambiance, food, value, service
                String chosenRestaurantName = restaurant.getText().toString();
                if (!names.contains(chosenRestaurantName)){
                    Toast.makeText(ReviewWriting.this, "Please choose a restaurant among the options", Toast.LENGTH_SHORT).show();
                } else {
                    Restaurant chosenRestaurant = realm.where(Restaurant.class).equalTo("name", chosenRestaurantName).findFirst();
                    if (overall.getRating() == 0.0f || ambiance.getRating() == 0.0f || food.getRating() == 0.0f || value.getRating() == 0.0f || service.getRating() == 0.0f) {
                        Toast.makeText(ReviewWriting.this, "Please fill up the ratings", Toast.LENGTH_SHORT).show();
                    } else {
                        Review r = new Review();
                        r.setUuid(uuid);
                        r.setRestaurant(chosenRestaurant);
                        r.setCuisine(chosenRestaurant.getCuisine());
                        r.setOverall(overall.getRating());
                        r.setAmbiance(ambiance.getRating());
                        r.setFood(food.getRating());
                        r.setValue(value.getRating());
                        r.setService(service.getRating());
                        r.setComment(comments.getText().toString());
                        r.setCreatedAt(System.currentTimeMillis());
                        realm.beginTransaction();
                        realm.copyToRealmOrUpdate(r);
                        realm.commitTransaction();
                        goToReviewDisplay();
                    }
                }
            }
        });

        photo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });
    }

    protected void onDestroy() {
        super.onDestroy();
        if(!realm.isClosed()) {
            realm.close();
        }
    }

    protected void goToReviewDisplay(){
        Intent i = new Intent(this, ReviewDisplay.class);
        startActivity(i);
    }


    public static int REQUEST_CODE_IMAGE_SCREEN = 0;

    public void takePhoto()
    {
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

                    // save the photo
                    File savedImage = saveFile(jpeg, uuid + ".jpeg");

                    // load photo file for instant updating
                    getImageDir = getExternalCacheDir();
                    file = new File(getImageDir, uuid + ".jpeg");
                    if (file.exists()) {
                        Picasso.get()
                                .load(file)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .into(photo);
                    }
                    else {
                        photo.setImageResource(R.mipmap.ic_launcher);
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

    public void checkPermissions()
    {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA

                )

                .withListener(new BaseMultiplePermissionsListener()
                {
                    public void onPermissionsChecked(MultiplePermissionsReport report)
                    {
                        if (report.areAllPermissionsGranted())
                        {
                            // all permissions accepted proceed
                            init();
                        }
                        else
                        {
                            // notify about permissions
                            toastRequirePermissions();
                        }
                    }
                })
                .check();
    }

    public void toastRequirePermissions() {
        Toast.makeText(this, "You must provide permissions for app to run", Toast.LENGTH_LONG).show();
        finish();
    }

}