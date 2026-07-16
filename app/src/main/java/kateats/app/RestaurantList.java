package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.SearchView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class RestaurantList extends AppCompatActivity {

    SearchView searchBar;
    RecyclerView restaurantList;
    Realm realm;
    Button swipe;
    RealmResults<Restaurant> restaurants;
    RestaurantAdaptor adapter;
    RealmResults<Restaurant> filtered;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_list);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        searchBar = findViewById(R.id.restaurantSearch);
        realm = Realm.getDefaultInstance();


        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);
        restaurantList.setLayoutManager(mLayoutManager);

        restaurants = realm.where(Restaurant.class).findAll();

        adapter = new RestaurantAdaptor(this, restaurants);
        restaurantList.setAdapter(adapter);

        swipe = findViewById(R.id.cantDecide);

        swipe.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                moveToTinderSwipe();
            }
        });

        searchBar.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextChange(String newText) {
                filterList(newText);
                return true;
            }

            @Override
            public boolean onQueryTextSubmit(String query) {
                filterList(query);
                return true;
            }
        });
    }

    public void moveToTinderSwipe(){
        Intent i = new Intent(this, TinderSwipe.class);
        startActivity(i);
    }

    private void filterList(String search) {
        if (search == null || search.trim().isEmpty()) {
            filtered = realm.where(Restaurant.class).findAll();
        } else {
            filtered = realm.where(Restaurant.class).contains("name", search, Case.INSENSITIVE).findAll();
        }

        adapter.updateData(filtered);
    }
}