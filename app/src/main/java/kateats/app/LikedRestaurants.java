package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class LikedRestaurants extends AppCompatActivity {
    Realm realm;
    TextView searchBar;
    ImageButton submitSearch;
    Button submitFilters;
    RatingBar filterRating;
    Spinner filterCuisine;
    CheckBox filterAlphabetical;
    ViewPager2 restoViewer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_liked_restaurants);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();

    }

    RealmResults<Restaurant> restoSet;
    private void init(){
        searchBar = findViewById(R.id.searchLiked);
        submitSearch = findViewById(R.id.submitSearchLike);
        submitFilters = findViewById(R.id.submitLikeFilters);
        filterRating = findViewById(R.id.filterLikedRating);
        filterAlphabetical = findViewById(R.id.alphabeticalLiked);
        restoViewer = findViewById(R.id.likedRestoViewer);

        filterCuisine = findViewById(R.id.filterLikedCuisine);
        String[] options = Restaurant.CUISINE_OPTIONS;

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        filterCuisine.setAdapter(adapter);

        //picker can have onNothingSelected and onItemSelected Listeners

        submitSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchResto();
            }
        });

        submitFilters.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                filterResto();
            }
        });

        restoViewer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectResto();
            }
        });


        //REALM STUFF
        realm = Realm.getDefaultInstance();

        //INSTANTIATE restoviewer
        restoSet = realm.where(Restaurant.class).containsValue("liked", true).findAll();
        RestaurantAdaptor adaptor = new RestaurantAdaptor(this, restoSet);
        restoViewer.setAdapter(adaptor);


    }

    public void selectResto(){
        int position = restoViewer.getCurrentItem();

        Intent i = new Intent(this, LikedRestaurants.class);
        String restoUUID = restoSet.get(position).getUuid();

        i.putExtra("restoID", restoUUID);
        startActivity(i);
        finish();
    }

    public void searchResto(){
        String searchThing = searchBar.getText().toString();
        restoSet = realm.where(Restaurant.class).beginsWith("name",searchThing).findAll();
        RestaurantAdaptor adaptor = new RestaurantAdaptor(this, restoSet);
        restoViewer.setAdapter(adaptor);
    }

    public void filterResto(){
        String searchThing = searchBar.getText().toString();
        float rating = filterRating.getRating();
        String cuisine =  filterCuisine.getSelectedItem().toString();
        boolean alpha = filterAlphabetical.isChecked();

        RealmQuery<Restaurant> query = realm
                .where(Restaurant.class)
                .beginsWith("name", searchThing)
                .greaterThanOrEqualTo("rating", rating);

        if (!cuisine.equals("All")) {
            query = query.equalTo("cuisine", cuisine);
        }

        if (alpha) {
            restoSet = query.sort("name").findAll();
        } else {
            restoSet = query.findAll();
        }


        RestaurantAdaptor adaptor = new RestaurantAdaptor(this, restoSet);
        restoViewer.setAdapter(adaptor);
    }

    public void onDestroy(){
        super.onDestroy();
        if (!realm.isClosed()){
            realm.close();
        }
    }
}