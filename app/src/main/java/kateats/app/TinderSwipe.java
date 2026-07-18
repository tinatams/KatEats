package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
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

import io.realm.Realm;
import io.realm.RealmResults;

public class TinderSwipe extends AppCompatActivity {
    Button yay;
    Button nay;
    int currentRestaurant;
    Realm realm;
    TextView restoName;
    TextView description;
    RatingBar rating;
    ImageView restoImage;
    ImageButton back;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_tinder_swipe);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cancelTinder), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }
    RealmResults<Restaurant> restoList;
    private void init(){
        back = findViewById(R.id.cancelTinderButton);
        restoName = findViewById(R.id.restoNamePlaceholder);
        description = findViewById(R.id.descriptionTinder);
        rating = findViewById(R.id.ratingTinder);
        restoImage = findViewById(R.id.imageTinder);

        yay = findViewById(R.id.yayButton);
        nay = findViewById(R.id.nayButton);

        yay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                yayResto();
            }
        });

        nay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nayResto();
            }
        });


        //REALM STUFF
        realm = Realm.getDefaultInstance();

        restoList = realm.where(Restaurant.class).findAll();
        for (Restaurant r : restoList){
            realm.beginTransaction();
            r.setLiked(true);
            realm.commitTransaction();
        }
        currentRestaurant = 0;
        setResto(currentRestaurant);

        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onCancel();
            }
        });
    }

    private void onCancel() {
        finish();
    }

    private void setResto(int restoNum){
        if (restoNum >= restoList.size()){
            allDone();
            return;
        }
        Restaurant r = restoList.get(restoNum);
        restoName.setText(r.getName());
        description.setText(r.getDescription());
        rating.setRating(r.getRating());
    }

    private void yayResto(){
        realm.beginTransaction();
        restoList.get(currentRestaurant).setLiked(true);
        realm.commitTransaction();
        setResto(++currentRestaurant);
        Toast toast = Toast.makeText(this, "Restaurant Liked", Toast.LENGTH_SHORT);
        toast.show();
    }

    private void nayResto(){
        realm.beginTransaction();
        restoList.get(currentRestaurant).setLiked(false);
        realm.commitTransaction();
        setResto(++currentRestaurant);
        Toast toast = Toast.makeText(this, "Restaurant Disliked", Toast.LENGTH_SHORT);
        toast.show();
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