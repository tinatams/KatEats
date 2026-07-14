package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import io.realm.Realm;
import io.realm.RealmResults;

public class TinderSwipe extends AppCompatActivity {
    Button yay;
    Button nay;
    Restaurant currentRestaurant;
    Realm realm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tinder_swipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    private void init(){
        yay = findViewById(R.id.yayButton);
        nay = findViewById(R.id.nayButton);

        yay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yayResto(currentRestaurant);
            }
        });

        nay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nayResto(currentRestaurant);
            }
        });


        //REALM STUFF
        realm = Realm.getDefaultInstance();

        RealmResults<Restaurant> datalist = realm.where(Restaurant.class).findAll();
    }

    private void yayResto(Restaurant r){
        r.setLiked(true);
    }

    private void nayResto(Restaurant r){
        r.setLiked(false);
    }

    public void allDone(){
        Intent i = new Intent(this, LikedRestaurants.class);
        startActivity(i);
    }

    public void onDestroy(){
        super.onDestroy();
        if (!realm.isClosed()){
            realm.close();
        }
    }
}