package kateats.app;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;
import java.util.UUID;

public class User extends RealmObject {
    @PrimaryKey
    private String uuid = UUID.randomUUID().toString();
    private String name;
    private String password;

    // this stores the profile image as raw bytes to match your teammates' style
    private byte[] photo;

    // Realm representation of Restaurant[]
    private RealmList<Restaurant> disliked;

    public User() {
        // this is a required empty constructor for Realm
    }

    public String getUuid() { return uuid; }
    public void setUuid(String uuid) { this.uuid = uuid; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public byte[] getPhoto() { return photo; }
    public void setPhoto(byte[] photo) { this.photo = photo; }

    public RealmList<Restaurant> getDisliked() { return disliked; }
    public void setDisliked(RealmList<Restaurant> disliked) { this.disliked = disliked; }
}