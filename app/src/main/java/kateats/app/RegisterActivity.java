package kateats.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import java.io.ByteArrayOutputStream;

import io.realm.Realm;
import io.realm.RealmResults;

public class RegisterActivity extends AppCompatActivity {

    private EditText etRegUsername, etRegPassword, etRegConfirmPassword;
    private EditText etRegGmaps, etRegDescription;
    private RadioGroup rgUserType;
    private RadioButton rbUser, rbRestaurant;
    private LinearLayout layoutRestaurantFields;
    private Button btnSave, btnCancel;
    private ImageView imgRegisterProfile;

    private Realm realm;
    private byte[] rawImageBytes = null; // Store image as byte[]

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
        etRegPassword = findViewById(R.id.etRegisterPassword);
        etRegConfirmPassword = findViewById(R.id.etRegisterConfirmPassword);
        etRegGmaps = findViewById(R.id.etRegisterGmaps);
        etRegDescription = findViewById(R.id.etRegisterDescription);
        rgUserType = findViewById(R.id.rgUserType);
        rbUser = findViewById(R.id.rbUser);
        rbRestaurant = findViewById(R.id.rbRestaurant);
        layoutRestaurantFields = findViewById(R.id.layoutRestaurantFields);
        btnSave = findViewById(R.id.btnRegisterSave);
        btnCancel = findViewById(R.id.btnRegisterCancel);
        imgRegisterProfile = findViewById(R.id.imgRegisterProfile);

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
        imgRegisterProfile.setOnClickListener(v -> {
            // Reusing your previous image capture flow
            Intent intent = new Intent(RegisterActivity.this, ImageActivity.class);
            startActivityForResult(intent, 100);
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
                final String gmapsInput = etRegGmaps.getText().toString().trim();
                final String descInput = etRegDescription.getText().toString().trim();

                registerRestaurant(usernameInput, passwordInput, gmapsInput, descInput);
            } else {
                registerRegularUser(usernameInput, passwordInput);
            }
        });
    }

    private void registerRegularUser(String name, String password) {
        realm.executeTransactionAsync(r -> {
            // Check if User already exists
            RealmResults<User> existing = r.where(User.class)
                    .equalTo("name", name)
                    .findAll();

            if (existing.size() > 0) {
                throw new IllegalArgumentException("User already exists");
            }

            User newUser = r.createObject(User.class, java.util.UUID.randomUUID().toString());
            newUser.setName(name);
            newUser.setPassword(password);
            if (rawImageBytes != null) {
                newUser.setPhoto(rawImageBytes);
            }
        }, () -> {
            Toast.makeText(RegisterActivity.this, "Regular User registered successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }, error -> {
            if (error instanceof IllegalArgumentException || error.getCause() instanceof IllegalArgumentException) {
                Toast.makeText(RegisterActivity.this, "User already exists", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    private void registerRestaurant(String name, String password, String gmaps, String description) {
        realm.executeTransactionAsync(r -> {
            // Check if Restaurant already exists
            RealmResults<Restaurant> existing = r.where(Restaurant.class)
                    .equalTo("name", name)
                    .findAll();

            if (existing.size() > 0) {
                throw new IllegalArgumentException("Restaurant already exists");
            }

            Restaurant newRestaurant = r.createObject(Restaurant.class, java.util.UUID.randomUUID().toString());
            newRestaurant.setName(name);
            newRestaurant.setPassword(password);
            newRestaurant.setGmaps(gmaps);
            newRestaurant.setDescription(description);
            if (rawImageBytes != null) {
                newRestaurant.setPhoto(rawImageBytes);
            }
        }, () -> {
            Toast.makeText(RegisterActivity.this, "Restaurant registered successfully!", Toast.LENGTH_SHORT).show();
            finish();
        }, error -> {
            if (error instanceof IllegalArgumentException || error.getCause() instanceof IllegalArgumentException) {
                Toast.makeText(RegisterActivity.this, "Restaurant already exists", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(RegisterActivity.this, "Error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 100 && resultCode == ImageActivity.RESULT_CODE_IMAGE_TAKEN && data != null) {
            byte[] jpeg = data.getByteArrayExtra("rawJpeg");
            if (jpeg != null) {
                this.rawImageBytes = jpeg;

                // Display the photo preview
                Bitmap bitmap = BitmapFactory.decodeByteArray(jpeg, 0, jpeg.length);
                imgRegisterProfile.setImageBitmap(bitmap);
                Toast.makeText(this, "Profile picture attached!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (realm != null) {
            realm.close();
        }
    }
}
