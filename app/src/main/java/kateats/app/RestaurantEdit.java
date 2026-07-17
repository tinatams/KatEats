package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.textfield.TextInputEditText;
import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import io.realm.Realm;
import io.realm.RealmResults;

public class RestaurantEdit extends AppCompatActivity {
    EditText restoName, pass, conPass;
    Spinner cuisinePicker;
    EditText menuEdit, descEdit;
    Button saveButton;
    Realm realm;
    String restoID;
    ImageView image;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_restaurant_edit);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        init();
    }

    public void init(){
        image = findViewById(R.id.imageRestView);
        pass = findViewById(R.id.passEdit);
        conPass = findViewById(R.id.passConEdit);
        restoName = findViewById(R.id.restoNameEditor);
        cuisinePicker = findViewById(R.id.cuisinePicker);
        menuEdit = findViewById(R.id.editMenu);
        saveButton = findViewById(R.id.saveBtn);
        descEdit = findViewById(R.id.editDesc);

        saveButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onSave();
            }
        });

        String[] options = Restaurant.CUISINE_OPTIONS;
        ArrayAdapter<String> cuisineAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, options);
        cuisineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisinePicker.setAdapter(cuisineAdapter);

        realm = Realm.getDefaultInstance();

        Intent i = getIntent();

        if (i != null && i.hasExtra("restoID")){
            restoID = i.getStringExtra("restoID");
            Restaurant editR = realm.where(Restaurant.class).equalTo("uuid", restoID).findFirst();

            if (editR != null){
                restoName.setText(editR.getName());
                pass.setText(editR.getPassword());
                menuEdit.setText(editR.getMenu());
                descEdit.setText(editR.getDescription());
            } else {
                Toast toast = Toast.makeText(this, "Error", Toast.LENGTH_SHORT);
                toast.show();
            }
        }

        image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                takePhoto();
            }
        });

        // Photo

        File getImageDir = getExternalCacheDir();  // this method is in the Activity class

        // just a sample, normally you have a diff image name each time
        File file = new File(getImageDir, restoID+"resto.jpeg");

        if (file.exists()) {
            // this will put the image saved to the file system to the imageview
            Picasso.get()
                    .load(file)
                    .networkPolicy(NetworkPolicy.NO_CACHE)
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(image);
        }
        else {
            // use a default picture
            image.setImageResource(R.mipmap.ic_launcher);
        }
    }
    public static int REQUEST_CODE_IMAGE_SCREEN = 0;

    public void takePhoto()
    {
        Intent i = new Intent(this, ImageActivity.class);
        startActivityForResult(i, REQUEST_CODE_IMAGE_SCREEN);
    }

    private String password, confirmPassword, username;
    private void onSave(){
        username = restoName.getText().toString();
        password = pass.getText().toString();
        confirmPassword = conPass.getText().toString();

        if (username.isEmpty()|| password.isEmpty()|| confirmPassword.isEmpty()) {
            Toast toast = Toast.makeText(this, "Fields cannot be empty", Toast.LENGTH_SHORT);
            toast.show();
        }
        else if (!password.equals(confirmPassword)) {
            Toast toast = Toast.makeText(this, "Confirm password does not match", Toast.LENGTH_SHORT);
            toast.show();
        } else {
            boolean repeatedUser = false;
            RealmResults<Restaurant> restos = realm.where(Restaurant.class).equalTo("name", username).findAll();

            if (restos.size() > 1){
                repeatedUser = true;
            } else if (restos.size() == 1 && restos.get(0) != null) {
                if (!restoID.equals(restos.get(0).getUuid())){
                    repeatedUser = true;
                }
            }
            if (repeatedUser){
                Toast newToast = Toast.makeText(this, "Username Taken", Toast.LENGTH_SHORT);
                newToast.show();
            } else {
                Restaurant editResto = realm.where(Restaurant.class)
                        .equalTo("uuid", restoID)
                        .findFirst();

                if (editResto == null){
                    Toast toast = Toast.makeText(this, "User does not exist", Toast.LENGTH_SHORT);
                    toast.show();
                } else {
                    try {
                        realm.beginTransaction();
                        editResto.setName(username);
                        editResto.setPassword(password);
                        editResto.setCuisine(cuisinePicker.getSelectedItem().toString());
                        editResto.setMenu(menuEdit.getText().toString());
                        editResto.setDescription(descEdit.getText().toString());
                        realm.copyToRealmOrUpdate(editResto);
                        realm.commitTransaction();

                        Toast toast = Toast.makeText(this, "Resto Info has been updated", Toast.LENGTH_SHORT);
                        toast.show();

                    } catch(Exception e) {
                        Toast toast = Toast.makeText(this, "Error Saving", Toast.LENGTH_SHORT);
                        toast.show();
                    }
                    //closes view
                    finish();
                }
            }
        }
    }

    // SINCE WE USE startForResult(), code will trigger this once the next screen calls finish()
    public void onActivityResult(int requestCode, int responseCode, Intent data)
    {
        super.onActivityResult(requestCode, responseCode, data);

        if (requestCode==REQUEST_CODE_IMAGE_SCREEN)
        {
            if (responseCode==ImageActivity.RESULT_CODE_IMAGE_TAKEN)
            {
                // receieve the raw JPEG data from ImageActivity
                // this can be saved to a file or save elsewhere like Realm or online
                byte[] jpeg = data.getByteArrayExtra("rawJpeg");

                try {
                    //SAVE
                    // save rawImage to file
                    File savedImage = saveFile(jpeg, restoID+"resto.jpeg");

                    File getImageDir = getExternalCacheDir();  // this method is in the Activity class

                    // just a sample, normally you have a diff image name each time
                    File file = new File(getImageDir, restoID+"resto.jpeg");


                    if (file.exists()) {
                        // this will put the image saved to the file system to the imageview
                        Picasso.get()
                                .load(file)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .into(image);
                    }
                    else {
                        // use a default picture
                        image.setImageResource(R.mipmap.ic_launcher);
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }

    private File saveFile(byte[] jpeg, String filename) throws IOException
    {
        // this is the root directory for the images
        File getImageDir = getExternalCacheDir();

        // just a sample, normally you have a diff image name each time
        File savedImage = new File(getImageDir, filename);


        FileOutputStream fos = new FileOutputStream(savedImage);
        fos.write(jpeg);
        fos.close();
        return savedImage;
    }


    public void onDestroy(){
        super.onDestroy();
        if (!realm.isClosed()){
            realm.close();
        }
    }
}