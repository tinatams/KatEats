package kateats.app;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

import io.realm.Realm;

public class UserEdit extends AppCompatActivity {

    private EditText etRegUsername, etRegPassword, etRegConfirmPassword;
    private String uuid;
    User user;
    private Button btnSave, btnCancel;
    private ImageView imgRegisterProfile;
    private Realm realm;
    File getImageDir;
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_user_edit);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.cancelTinder), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });


        uuid = getIntent().getStringExtra("userUuid");
        realm = Realm.getDefaultInstance();

        user = realm.where(User.class).equalTo("uuid", uuid).findFirst();

        // Bind layouts
        etRegUsername = findViewById(R.id.editEtRegisterUsername);
        etRegPassword = findViewById(R.id.editEtRegisterPassword);
        etRegConfirmPassword = findViewById(R.id.editEtRegisterConfirmPassword);
        btnSave = findViewById(R.id.editBtnRegisterSave);
        btnCancel = findViewById(R.id.editBtnRegisterCancel);
        imgRegisterProfile = findViewById(R.id.editImgRegisterProfile);
        imgRegisterProfile.setImageResource(R.mipmap.ic_launcher);

        etRegUsername.setText(user.getName());
        etRegPassword.setText(user.getPassword());
        etRegConfirmPassword.setText(user.getPassword());

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

            if (usernameInput.isEmpty()) {
                Toast.makeText(UserEdit.this, "Name must not be blank", Toast.LENGTH_SHORT).show();
                return;
            }
            if (passwordInput.isEmpty() || confirmPasswordInput.isEmpty()) {
                Toast.makeText(UserEdit.this, "Password fields cannot be empty", Toast.LENGTH_SHORT).show();
                return;
            }
            if (!passwordInput.equals(confirmPasswordInput)) {
                Toast.makeText(UserEdit.this, "Confirm password does not match", Toast.LENGTH_SHORT).show();
                return;
            }

            registerRegularUser(usernameInput, passwordInput);
        });
    }

    private void registerRegularUser(String name, String password) {
        User existing = realm.where(User.class).equalTo("name", name).notEqualTo("uuid", uuid).findFirst();

        if (existing == null) {
            realm.beginTransaction();
            user.setName(name);
            user.setPassword(password);
            realm.copyToRealmOrUpdate(user);
            realm.commitTransaction();
            Toast.makeText(UserEdit.this, "Regular User registered successfully!", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(UserEdit.this, "User already exists", Toast.LENGTH_SHORT).show();
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
