package kateats.app;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.karumi.dexter.Dexter;
import com.karumi.dexter.MultiplePermissionsReport;
import com.karumi.dexter.listener.multi.BaseMultiplePermissionsListener;

import io.realm.Realm;
import io.realm.RealmResults;

public class LoginActivity extends AppCompatActivity {

    private EditText etUsername, etPassword;
    private RadioGroup rgLoginType;
    private RadioButton rbLoginUser, rbLoginRestaurant;
    private Button btnLogin, btnGoToRegister, btnAdminPanel;

    private Realm realm;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_login);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        checkPermissions();
    }

    private void init() {
        etUsername = findViewById(R.id.etLoginUsername);
        etPassword = findViewById(R.id.etLoginPassword);
        rgLoginType = findViewById(R.id.rgLoginType);
        rbLoginUser = findViewById(R.id.rbLoginUser);
        rbLoginRestaurant = findViewById(R.id.rbLoginRestaurant);
        btnLogin = findViewById(R.id.btnLogin);
        btnGoToRegister = findViewById(R.id.btnGoToRegister);
        btnAdminPanel = findViewById(R.id.btnAdminPanel);

        realm = Realm.getDefaultInstance();
        sharedPreferences = getSharedPreferences("KatEatsPrefs", Context.MODE_PRIVATE);

        btnGoToRegister.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });

        btnAdminPanel.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, AdminActivity.class);
            startActivity(intent);
        });

        btnLogin.setOnClickListener(v -> {
            String usernameInput = etUsername.getText().toString().trim();
            String passwordInput = etPassword.getText().toString().trim();
            boolean isRestaurant = rbLoginRestaurant.isChecked();

            if (usernameInput.isEmpty() || passwordInput.isEmpty()) {
                Toast.makeText(LoginActivity.this, "Please fill in all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            if (isRestaurant) {
                verifyRestaurantLogin(usernameInput, passwordInput);
            } else {
                verifyUserLogin(usernameInput, passwordInput);
            }
        });
    }

    private void verifyUserLogin(String username, String password) {
        RealmResults<User> results = realm.where(User.class)
                .equalTo("name", username)
                .findAll();

        if (results.isEmpty()) {
            Toast.makeText(this, "No user account found with that name", Toast.LENGTH_SHORT).show();
        } else {
            User user = results.first();
            if (user != null && user.getPassword().equals(password)) {
                // Save current user session ID
                sharedPreferences.edit()
                        .putString("logged_user_uuid", user.getUuid())
                        .putString("logged_user_type", "customer")
                        .apply();

                Toast.makeText(this, "Welcome back, " + user.getName() + "!", Toast.LENGTH_SHORT).show();

                // Redirected to RestaurantList
                Intent intent = new Intent(LoginActivity.this, RestaurantList.class);
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void verifyRestaurantLogin(String username, String password) {
        RealmResults<Restaurant> results = realm.where(Restaurant.class)
                .equalTo("name", username)
                .findAll();

        if (results.isEmpty()) {
            Toast.makeText(this, "No restaurant account found with that name", Toast.LENGTH_SHORT).show();
        } else {
            Restaurant restaurant = results.first();
            if (restaurant != null && restaurant.getPassword().equals(password)) {
                // Save current restaurant session ID
                sharedPreferences.edit()
                        .putString("logged_user_uuid", restaurant.getUuid())
                        .putString("logged_user_type", "restaurant")
                        .apply();

                Toast.makeText(this, "Logged in as " + restaurant.getName() + "!", Toast.LENGTH_SHORT).show();

                // Redirects to RestaurantList
                Intent intent = new Intent(LoginActivity.this, RestaurantList.class);
                intent.putExtra("RESTO_UUID", restaurant.getUuid());
                startActivity(intent);
                finish();
            } else {
                Toast.makeText(this, "Incorrect password", Toast.LENGTH_SHORT).show();
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

    public void checkPermissions()
    {
        Dexter.withContext(this)
                .withPermissions(
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.WRITE_EXTERNAL_STORAGE,
                        Manifest.permission.CAMERA

                )

                .withListener(new BaseMultiplePermissionsListener()
                {
                    public void onPermissionsChecked(MultiplePermissionsReport report)
                    {
                        if (report.areAllPermissionsGranted())
                        {
                            // all permissions accepted proceed
                            init();
                        }
                        else
                        {
                            // notify about permissions
                            toastRequirePermissions();
                        }
                    }
                })
                .check();
    }

    public void toastRequirePermissions() {
        Toast.makeText(this, "You must provide permissions for app to run", Toast.LENGTH_LONG).show();
        finish();
    }
}
