package kateats.app;

import static java.lang.System.in;

import java.util.UUID;

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
    private String[] menu;
    private float rating;
    private String[] cuisine;

    public String[] getCuisine() {
        return cuisine;
    }

    public void setCuisine(String[] cuisine) {
        this.cuisine = cuisine;
    }

    public boolean isCuisine(String c){
        for (String cu : cuisine){
            if (cu.equals(c)){
                return true;
            }
        }
        return false;
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

    private boolean liked;

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

    public String[] getMenu() {
        return menu;
    }

    public void setMenu(String[] menu) {
        this.menu = menu;
    }
}
