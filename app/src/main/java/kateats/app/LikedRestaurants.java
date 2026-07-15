package kateats.app;

import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.Spinner;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import io.realm.Realm;

public class LikedRestaurants extends AppCompatActivity {
    Realm realm;
    Spinner cuisinePicker;

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
        cuisinePicker = findViewById(R.id.TypePicker);
        String[] options = {"All", "Italian", "Japanese", "Filipino", "Thai"};

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                options
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisinePicker.setAdapter(adapter);

        //picker can have onNothingSelected and onItemSelected Listeners

        //REALM STUFF
        realm = Realm.getDefaultInstance();
    }

    public void onDestroy(){
        super.onDestroy();
        if (!realm.isClosed()){
            realm.close();
        }
    }
}