package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.realm.Realm;

public class RestaurantView extends AppCompatActivity {
    TextView restoName, cuisine;
    RatingBar overallRating;
    RatingBar foodRating, ambianceRating, serviceRating, valueRating;
    TextView description;
    TextView menu;
    Realm realm;
    Restaurant resto;
    ImageButton editResto;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_view);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    String restoID;
    private void init(){
        Intent i = getIntent();

        cuisine = findViewById(R.id.cuisineText);
        restoName = findViewById(R.id.restaurantName);
        overallRating = findViewById(R.id.overallRating);
        foodRating = findViewById(R.id.foodDrinkRating);
        ambianceRating = findViewById(R.id.ambianceRating);
        serviceRating = findViewById(R.id.serviceRating);
        valueRating = findViewById(R.id.valueRating);
        description = findViewById(R.id.descriptionView);
        menu = findViewById(R.id.menuView);
        editResto = findViewById(R.id.editRestaurantPage);

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
        menu.setText(resto.getMenu());
        cuisine.setText(resto.getCuisine());
    }

    public void goToEditPage(){
        Intent i = new Intent(this, RestaurantEdit.class);
        String restoUUID = resto.getUuid();

        i.putExtra("restoID", restoUUID);
        startActivity(i);
    }

    public void onDestroy(){
        super.onDestroy();
        if (resto != null && resto.isValid()) resto.removeAllChangeListeners();
        if (!realm.isClosed()){
            realm.close();
        }
    }

}