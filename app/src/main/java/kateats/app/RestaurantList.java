package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class RestaurantList extends AppCompatActivity {

    SearchView searchBar;
    RecyclerView restaurantList;
    Realm realm;
    Button swipe;
    RealmResults<Restaurant> restaurants;
    RestaurantAdaptor adapter;
    RealmResults<Restaurant> filtered;
    Button submitFilters;
    Button back;
    RatingBar filterRating;
    Spinner filterCuisine;
    CheckBox filterAlphabetical;
    Button reviewButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cancelTinder), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchBar = findViewById(R.id.restaurantSearch);
        realm = Realm.getDefaultInstance();
        restaurantList = findViewById(R.id.restaurantList);

        submitFilters = findViewById(R.id.submitFilters);
        filterRating = findViewById(R.id.filterRating);
        filterAlphabetical = findViewById(R.id.alphabetical);
        filterCuisine = findViewById(R.id.filterCuisine);
        reviewButton = findViewById(R.id.reviewButton);
        back = findViewById(R.id.backRestoList);

        String[] options = Restaurant.CUISINE_OPTIONS;

        ArrayAdapter<String> cuisine_options = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );
        cuisine_options.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterCuisine.setAdapter(cuisine_options);

        submitFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                refreshList();
                Toast.makeText(RestaurantList.this, "Filters applied", Toast.LENGTH_SHORT).show();

            }
        });
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                moveToLogin();
            }
        });

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        restaurantList.setLayoutManager(mLayoutManager);

        restaurants = realm.where(Restaurant.class).findAll();

        adapter = new RestaurantAdaptor(this, R.layout.restaurant_layout, restaurants);
        restaurantList.setAdapter(adapter);

        swipe = findViewById(R.id.cantDecide);

        swipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                moveToTinderSwipe();
            }
        });

        reviewButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                moveToReviewDisplay();
            }
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                refreshList();
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                refreshList();
                return true;
            }
        });
    }

    public void moveToTinderSwipe(){
        Intent i = new Intent(this, TinderSwipe.class);
        startActivity(i);
    }

    public void moveToLogin(){
        Intent i = new Intent(this, LoginActivity.class);
        startActivity(i);
    }

    public void moveToReviewDisplay(){
        Intent i = new Intent(this, ReviewDisplay.class);
        startActivity(i);
    }

    private void refreshList() {
        RealmQuery<Restaurant> query = realm.where(Restaurant.class)
                .greaterThanOrEqualTo("rating", filterRating.getRating());

        String cuisine = filterCuisine.getSelectedItem().toString();
        if (!cuisine.equals("Any")) {
            query = query.equalTo("cuisine", cuisine);
        }

        String search = searchBar.getQuery().toString();
        if (search != null && !search.trim().isEmpty()) {
            query = query.contains("name", search, Case.INSENSITIVE);
        }

        filtered = filterAlphabetical.isChecked() ? query.sort("name").findAll() : query.findAll();

        adapter.updateData(filtered);
        Toast toast = Toast.makeText(this, "Filtered", Toast.LENGTH_SHORT);
        toast.show();
    }

    protected void onDestroy() {
        super.onDestroy();
        if(!realm.isClosed()) {
            realm.close();
        }
    }


}