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

import io.realm.Realm;
import io.realm.RealmResults;
import io.realm.Sort;

public class ReviewDisplay extends AppCompatActivity {

    RecyclerView reviewView;
    Realm realm;
    RealmResults<Review> reviews;
    Button makeReview;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_review_display);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        reviewView = findViewById(R.id.reviewView);
        realm = Realm.getDefaultInstance();

        LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        mLayoutManager.setOrientation(RecyclerView.VERTICAL);

        reviewView.setLayoutManager(mLayoutManager);

        reviews = realm.where(Review.class).sort("createdAt", Sort.DESCENDING).findAll();

        ReviewAdaptor adapter = new ReviewAdaptor(this, reviews);
        reviewView.setAdapter(adapter);

        makeReview = findViewById(R.id.makeReview);

        makeReview.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view){
                moveToReviewWriting();
            }
        });
    }

    public void moveToReviewWriting(){
        Intent i = new Intent(this, ReviewWriting.class);
        startActivity(i);
    }

    protected void onDestroy() {
        super.onDestroy();
        if(!realm.isClosed()) {
            realm.close();
        }
    }
}