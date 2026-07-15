package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.realm.Realm;

public class RestaurantView extends AppCompatActivity {
    TextView restoName;
    RatingBar overallRating;
    RatingBar foodRating, ambianceRating, serviceRating, valueRating;
    TextView description;
    TextView menu;
    Realm realm;
    Restaurant resto;

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

    private void init(){
        Intent i = getIntent();

        restoName = findViewById(R.id.restaurantName);
        overallRating = findViewById(R.id.overallRating);
        foodRating = findViewById(R.id.foodDrinkRating);
        ambianceRating = findViewById(R.id.ambianceRating);
        serviceRating = findViewById(R.id.serviceRating);
        valueRating = findViewById(R.id.valueRating);
        description = findViewById(R.id.descriptionView);
        menu = findViewById(R.id.menuView);

        realm = Realm.getDefaultInstance();

        String restoID = i.getStringExtra("restoID");

        resto = realm.where(Restaurant.class)
                .equalTo("uuid", restoID)
                .findFirst();

        restoName.setText(resto.getName());
        overallRating.setRating(resto.getRating());
        description.setText(resto.getDescription());
        String menuString = "";

        for (String m : resto.getMenu()){
            menuString += m + '\n';
        }
        menu.setText(menuString);

        //Add the other ratings here

    }

    public void onDestroy(){
        super.onDestroy();
        if (!realm.isClosed()){
            realm.close();
        }
    }

}