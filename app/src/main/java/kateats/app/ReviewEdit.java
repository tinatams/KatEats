package kateats.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
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

public class ReviewEdit extends AppCompatActivity {

    String uuid;
    Review review;
    TextView restaurant;
    Restaurant restoObject;
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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkPermissions();
    }

    public void init() {
        uuid = getIntent().getStringExtra("reviewUuid");
        realm = Realm.getDefaultInstance();
        review = realm.where(Review.class).equalTo("uuid", uuid).findFirst();

        restaurant = findViewById(R.id.editReviewRestaurant);
        comments = findViewById(R.id.editReviewComments);
        overall = findViewById(R.id.editReviewOverall);
        ambiance = findViewById(R.id.editReviewAmbiance);
        food = findViewById(R.id.editReviewFood);
        value = findViewById(R.id.editReviewValue);
        service = findViewById(R.id.editReviewService);
        submit = findViewById(R.id.editReviewSubmit);
        photo = findViewById(R.id.editReviewPhoto);

        restaurant.setText(review.getRestaurant().getName());
        comments.setText(review.getComment());
        overall.setRating(review.getOverall());
        ambiance.setRating(review.getAmbiance());
        food.setRating(review.getFood());
        value.setRating(review.getValue());
        service.setRating(review.getService());

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

        submit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // check the resto name, check overall, ambiance, food, value, service
                if (overall.getRating() == 0.0f || ambiance.getRating() == 0.0f || food.getRating() == 0.0f || value.getRating() == 0.0f || service.getRating() == 0.0f) {
                    Toast.makeText(ReviewEdit.this, "Please fill up the ratings", Toast.LENGTH_SHORT).show();
                } else {
                    realm.beginTransaction();
                    review.setComment(comments.getText().toString());
                    review.setOverall(overall.getRating());
                    review.setAmbiance(ambiance.getRating());
                    review.setFood(food.getRating());
                    review.setService(service.getRating());
                    review.setValue(value.getRating());


                    realm.copyToRealmOrUpdate(review);
                    Number avg = realm.where(Review.class).equalTo("restaurant.uuid", review.getRestaurant().getUuid()).average("overall");
                    review.getRestaurant().setRating(avg.floatValue());
                    realm.commitTransaction();


                    goToReviewDisplay();
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