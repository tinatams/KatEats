package kateats.app;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;
import io.realm.Sort;

public class ReviewDisplay extends AppCompatActivity {

    RecyclerView reviewView;
    Realm realm;
    RealmResults<Review> reviews;
    Button makeReview;
    Button submitFilters;
    Spinner filterRestaurant;
    RatingBar filterRating;
    Spinner filterCuisine;
    CheckBox filterEarliest;
    ArrayList<String> restaurantNames = new ArrayList<>();
    RealmResults<Review> filtered;
    ReviewAdaptor adapter;
    Button back;
    SharedPreferences sharedPrefs;
    String currentUserUuid;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_display);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cancelTinder), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        reviewView = findViewById(R.id.reviewView);
        realm = Realm.getDefaultInstance();
        sharedPrefs = getSharedPreferences("KatEatsPrefs", MODE_PRIVATE);
        currentUserUuid = sharedPrefs.getString("logged_user_uuid", null);


        submitFilters = findViewById(R.id.submitReviewFilters);
        filterRestaurant = findViewById(R.id.filterReviewRestaurant);
        filterRating = findViewById(R.id.filterReviewRating);
        filterCuisine = findViewById(R.id.filterReviewCuisine);
        filterEarliest = findViewById(R.id.earliestReview);
        back = findViewById(R.id.backReviewDisplay);

        String[] options = Restaurant.CUISINE_OPTIONS;

        ArrayAdapter<String> cuisine_options = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );
        cuisine_options.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterCuisine.setAdapter(cuisine_options);

        RealmResults<Restaurant> restaurantList = realm.where(Restaurant.class).findAll();
        restaurantNames.add("Any");
        for (Restaurant r : restaurantList) {
            restaurantNames.add(r.getName());
        }

        ArrayAdapter<String> restaurant_options = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                restaurantNames
        );
        restaurant_options.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterRestaurant.setAdapter(restaurant_options);

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);

        reviewView.setLayoutManager(mLayoutManager);

        reviews = realm.where(Review.class).sort("createdAt", Sort.DESCENDING).findAll();

        adapter = new ReviewAdaptor(this, currentUserUuid, reviews);
        reviewView.setAdapter(adapter);

        makeReview = findViewById(R.id.makeReview);

        makeReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                moveToReviewWriting();
            }
        });

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                moveToRestaurantList();
            }
        });

        submitFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshList();
                Toast.makeText(ReviewDisplay.this, "Filters applied", Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void moveToReviewWriting(){
        Intent i = new Intent(this, ReviewWriting.class);
        startActivity(i);
    }

    public void moveToRestaurantList(){
        Intent i = new Intent(this, RestaurantList.class);
        startActivity(i);
    }

    protected void onDestroy() {
        super.onDestroy();
        if(!realm.isClosed()) {
            realm.close();
        }
    }

    private void refreshList() {
        RealmQuery<Review> query = realm.where(Review.class).greaterThanOrEqualTo("overall", filterRating.getRating());

        String cuisine = filterCuisine.getSelectedItem().toString();
        if (!cuisine.equals("Any")) {
            query = query.equalTo("cuisine", cuisine);
        }

        String restoName = filterRestaurant.getSelectedItem().toString();
        if (!restoName.equals("Any")) {
            Restaurant chosenResto = realm.where(Restaurant.class).equalTo("name", restoName).findFirst();
            query = query.equalTo("restaurant.name", chosenResto.getName());
        }

        filtered = filterEarliest.isChecked() ? query.sort("createdAt", Sort.ASCENDING).findAll() : query.sort("createdAt", Sort.DESCENDING).findAll();

        adapter.updateData(filtered);
    }
}