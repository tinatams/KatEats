package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.squareup.picasso.MemoryPolicy;
import com.squareup.picasso.NetworkPolicy;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

import io.realm.Realm;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegPassword, etRegConfirmPassword;
    private String uuid;
    private EditText etRegDescription;
    private RadioGroup rgUserType;
    private RadioButton rbUser, rbRestaurant;
    private LinearLayout layoutRestaurantFields;
    private Button btnSave, btnCancel;
    private ImageView imgRegisterProfile;
    private Realm realm;
    private Spinner cuisineChoose;
    File getImageDir;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_register);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Bind layouts
        etRegUsername = findViewById(R.id.etRegisterUsername);
        uuid = UUID.randomUUID().toString();
        etRegPassword = findViewById(R.id.etRegisterPassword);
        etRegConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        etRegDescription = findViewById(R.id.etRegisterDescription);
        rgUserType = findViewById(R.id.rgUserType);
        rbUser = findViewById(R.id.rbUser);
        rbRestaurant = findViewById(R.id.rbRestaurant);
        layoutRestaurantFields = findViewById(R.id.layoutRestaurantFields);
        btnSave = findViewById(R.id.btnRegisterSave);
        btnCancel = findViewById(R.id.btnRegisterCancel);
        imgRegisterProfile = findViewById(R.id.imgRegisterProfile);
        imgRegisterProfile.setImageResource(R.mipmap.ic_launcher);
        cuisineChoose = findViewById(R.id.registerCuisine);

        String[] options = Restaurant.CUISINE_OPTIONS;
        ArrayAdapter<String> cuisineAdapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, options);
        cuisineAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        cuisineChoose.setAdapter(cuisineAdapter);


        realm = Realm.getDefaultInstance();

        // Dynamically toggle Restaurant specific input views
        rgUserType.setOnCheckedChangeListener((group, checkedId) -> {
            if (checkedId == R.id.rbRestaurant) {
                layoutRestaurantFields.setVisibility(View.VISIBLE);
            } else {
                layoutRestaurantFields.setVisibility(View.GONE);
            }
        });

        // Click profile picture to trigger photo taking activity
        imgRegisterProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                takePhoto();
            }
        });

        btnCancel.setOnClickListener(v -> finish());

        btnSave.setOnClickListener(v -> {
            final String usernameInput = etRegUsername.getText().toString().trim();
            final String passwordInput = etRegPassword.getText().toString().trim();
            final String confirmPasswordInput = etRegConfirmPassword.getText().toString().trim();
            final boolean isRestaurant = rbRestaurant.isChecked();

            if (usernameInput.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Name must not be blank", Toast.LENGTH_SHORT).show();
                return;
            }
            if (passwordInput.isEmpty() || confirmPasswordInput.isEmpty()) {
                Toast.makeText(RegisterActivity.this, "Password fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passwordInput.equals(confirmPasswordInput)) {
                Toast.makeText(RegisterActivity.this, "Confirm password does not match", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRestaurant) {
                final String descInput = etRegDescription.getText().toString().trim();

                registerRestaurant(usernameInput, passwordInput, descInput, cuisineChoose.getSelectedItem().toString());
            } else {
                registerRegularUser(usernameInput, passwordInput);
            }
        });
    }

    private void registerRegularUser(String name, String password) {
        User existing = realm.where(User.class).equalTo("name", name).findFirst();

        if (existing == null) {
            User u = new User();
            u.setUuid(uuid);
            u.setName(name);
            u.setPassword(password);
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(u);
            realm.commitTransaction();
            Toast.makeText(RegisterActivity.this, "Regular User registered successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
        }

    }

    private void registerRestaurant(String name, String password, String description, String cuisine) {
        Restaurant existing = realm.where(Restaurant.class).equalTo("name", name).findFirst();

        if (existing == null) {
            Restaurant r = new Restaurant();
            r.setName(name);
            r.setPassword(password);
            r.setDescription(description);
            r.setCuisine(cuisine);
            realm.beginTransaction();
            realm.copyToRealmOrUpdate(r);
            realm.commitTransaction();
            Toast.makeText(RegisterActivity.this, "Restaurant registered successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(RegisterActivity.this, "Restaurant already exists", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }

    public static int REQUEST_CODE_IMAGE_SCREEN = 0;

    public void takePhoto()
    {
        Intent i = new Intent(this, ImageActivity.class);
        startActivityForResult(i, REQUEST_CODE_IMAGE_SCREEN);
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

                    // save the photo
                    File savedImage = saveFile(jpeg, uuid + ".jpeg");

                    // load photo file for instant updating
                    getImageDir = getExternalCacheDir();
                    file = new File(getImageDir, uuid + ".jpeg");
                    if (file.exists()) {
                        Picasso.get()
                                .load(file)
                                .networkPolicy(NetworkPolicy.NO_CACHE)
                                .memoryPolicy(MemoryPolicy.NO_CACHE)
                                .into(imgRegisterProfile);
                    }
                    else {
                        imgRegisterProfile.setImageResource(R.mipmap.ic_launcher);
                    }
                }
                catch(Exception e)
                {
                    e.printStackTrace();
                }

            }
        }
    }
}
