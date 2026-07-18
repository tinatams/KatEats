package kateats.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;

import io.realm.Realm;

public class RestaurantView extends AppCompatActivity {
    TextView restoName, cuisine;
    RatingBar overallRating;
    RatingBar foodRating, ambianceRating, serviceRating, valueRating;
    TextView description;
    Realm realm;
    Restaurant resto;
    ImageButton editResto, back;
    ImageView photo;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cancelTinder), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    String restoID;
    public void onBack(){
        finish();
    }
    private void init(){
        Intent i = getIntent();

        back = findViewById(R.id.cancelRestoView);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBack();
            }
        });
        cuisine = findViewById(R.id.cuisineText);
        restoName = findViewById(R.id.restaurantName);
        overallRating = findViewById(R.id.overallRating);
        foodRating = findViewById(R.id.foodDrinkRating);
        ambianceRating = findViewById(R.id.ambianceRating);
        serviceRating = findViewById(R.id.serviceRating);
        valueRating = findViewById(R.id.valueRating);
        description = findViewById(R.id.descriptionView);
        editResto = findViewById(R.id.editRestaurantPage);
        photo = findViewById(R.id.restoImage);

        realm = Realm.getDefaultInstance();

        if (!i.hasExtra("restoID")){
            finish();
        }

        restoID = i.getStringExtra("restoID");

        resto = realm.where(Restaurant.class)
                .equalTo("uuid", restoID)
                .findFirst();

        bindData();
        resto.addChangeListener(updatedResto -> bindData());

        editResto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goToEditPage();
            }
        });
    }

    private void bindData() {
        if (resto == null || !resto.isValid()) return;
        restoName.setText(resto.getName());
        overallRating.setRating(resto.getRating());
        description.setText(resto.getDescription());
        cuisine.setText(resto.getCuisine());

        Number foodavg = realm.where(Review.class).equalTo("restaurant.uuid", resto.getUuid()).average("food");
        Number ambianceavg = realm.where(Review.class).equalTo("restaurant.uuid", resto.getUuid()).average("ambiance");
        Number serviceavg = realm.where(Review.class).equalTo("restaurant.uuid", resto.getUuid()).average("service");
        Number valueavg = realm.where(Review.class).equalTo("restaurant.uuid", resto.getUuid()).average("value");
        foodRating.setRating(foodavg == null ? 0f : foodavg.floatValue());
        ambianceRating.setRating(ambianceavg == null ? 0f : ambianceavg.floatValue());
        serviceRating.setRating(serviceavg == null ? 0f : serviceavg.floatValue());
        valueRating.setRating(valueavg == null ? 0f : valueavg.floatValue());
    }

    public void goToEditPage(){
        SharedPreferences prefs = getSharedPreferences("KatEatsPrefs", 0);
        String userType = prefs.getString("logged_user_type", "null");
        String userID = prefs.getString("logged_user_uuid", "null");
        if (userType.equals("restaurant") && userID.equals(resto.getUuid())){
            Intent i = new Intent(this, RestaurantEdit.class);
            String restoUUID = resto.getUuid();

            i.putExtra("restoID", restoUUID);
            startActivity(i);
        } else {
            Toast toast = Toast.makeText(this, "You shalt not pass", Toast.LENGTH_SHORT);
            toast.show();
        }

    }

    public void onDestroy(){
        super.onDestroy();
        if (resto != null && resto.isValid()) resto.removeAllChangeListeners();
        if (!realm.isClosed()){
            realm.close();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        File image = new File(getExternalCacheDir(), restoID + ".jpeg");
        if (image.exists()) {
            Picasso.get().invalidate(image);
            Picasso.get().load(image)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(photo);
        } else {
            photo.setImageResource(R.mipmap.ic_launcher);
        }
    }

}