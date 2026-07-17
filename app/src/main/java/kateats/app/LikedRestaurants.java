package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageButton;
import android.widget.RatingBar;
import android.widget.SearchView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.viewpager2.widget.ViewPager2;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmQuery;
import io.realm.RealmResults;

public class LikedRestaurants extends AppCompatActivity {
    SearchView searchBar;
    ViewPager2 restoViewer;
    Realm realm;

    RealmResults<Restaurant> restoSet;
    RestaurantAdaptor adaptor;
    RealmResults<Restaurant> filtered;


    Button submitFilters;
    RatingBar filterRating;
    Spinner filterCuisine;
    CheckBox filterAlphabetical;


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

    private void init(){
        searchBar = findViewById(R.id.likedRestoSearch);
        restoViewer = findViewById(R.id.likedRestoViewer);

        submitFilters = findViewById(R.id.submitLikeFilters);
        filterRating = findViewById(R.id.filterLikedRating);
        filterAlphabetical = findViewById(R.id.alphabeticalLiked);

        filterCuisine = findViewById(R.id.filterLikedCuisine);
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
                //filterResto();
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
        restoSet = realm.where(Restaurant.class).equalTo("liked", true).findAll();
        adaptor = new RestaurantAdaptor(this, restoSet);
        restoViewer.setAdapter(adaptor);

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener(){
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

    public void selectResto(){
        int position = restoViewer.getCurrentItem();

        Intent i = new Intent(this, Restaurant.class);
        String restoUUID = restoSet.get(position).getUuid();

        i.putExtra("restoID", restoUUID);
        startActivity(i);
        finish();
    }

    public void refreshList(){
        float rating = filterRating.getRating();
        String cuisine =  filterCuisine.getSelectedItem().toString();
        boolean alpha = filterAlphabetical.isChecked();

        RealmQuery<Restaurant> query = realm
                .where(Restaurant.class)
                .greaterThanOrEqualTo("rating", rating)
                .equalTo("liked", true);

        if (!cuisine.equals("All")) {
            query = query.equalTo("cuisine", cuisine);
        }

        String search = searchBar.getQuery().toString();
        if (search != null && !search.trim().isEmpty()) {
            query = query.contains("name", search, Case.INSENSITIVE);
        }

        filtered = filterAlphabetical.isChecked() ? query.sort("name").findAll() : query.findAll();

        adaptor.updateData(filtered);
    }

    public void onDestroy(){
        super.onDestroy();
        if (!realm.isClosed()){
            realm.close();
        }
    }
}