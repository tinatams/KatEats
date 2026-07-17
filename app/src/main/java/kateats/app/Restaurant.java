package kateats.app;

import static java.lang.System.in;

import java.util.UUID;

import io.realm.RealmList;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Restaurant extends RealmObject {
    @PrimaryKey
    private String uuid = UUID.randomUUID().toString();
    private String name;
    private String description;
    private String password;
    private byte[] photo;
    private String gmaps;
    private RealmList<String> menu = new RealmList<>();
    private float rating;
    public static String[] CUISINE_OPTIONS = {"All", "Italian", "Japanese", "Filipino", "Thai"};
    private RealmList<String> cuisine = new RealmList<>();    private boolean liked;

    // Updated to return and accept RealmList<String>
    public RealmList<String> getCuisine() {
        return cuisine;
    }

    public void setCuisine(RealmList<String> cuisine) {
        this.cuisine = cuisine;
    }

    // Modernized using Realm's built-in .contains() method
    public boolean isCuisine(String c) {
        if (cuisine == null) return false;
        return cuisine.contains(c);
    }

    public float getRating() {
        return rating;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRating(float rating) {
        this.rating = rating;
    }

    public Restaurant(){}

    public boolean isLiked() {
        return liked;
    }

    public void setLiked(boolean liked) {
        this.liked = liked;
    }

    public String getUuid() {
        return uuid;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public byte[] getPhoto() {
        return photo;
    }

    public void setPhoto(byte[] photo) {
        this.photo = photo;
    }

    public String getGmaps() {
        return gmaps;
    }

    public void setGmaps(String gmaps) {
        this.gmaps = gmaps;
    }

    // Updated to return and accept RealmList<String>
    public RealmList<String> getMenu() {
        return menu;
    }

    public void setMenu(RealmList<String> menu) {
        this.menu = menu;
    }
}