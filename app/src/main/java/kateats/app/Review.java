package kateats.app;

import java.util.UUID;

import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Review extends RealmObject {

    @PrimaryKey
    private String uuid = UUID.randomUUID().toString();
    private Restaurant restaurant;
    // private User user;
    private float overall;
    private float ambiance;
    private float food;
    private float value;
    private float service;
    private String comment;

    public Review(String uuid, Restaurant restaurant, float overall, float ambiance, float food, float value, float service, String comment) {
        this.uuid = uuid;
        this.restaurant = restaurant;
        this.overall = overall;
        this.ambiance = ambiance;
        this.food = food;
        this.value = value;
        this.service = service;
        this.comment = comment;
    }

    public Review(){}

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public Restaurant getRestaurant() {
        return restaurant;
    }

    public void setRestaurant(Restaurant restaurant) {
        this.restaurant = restaurant;
    }

    public float getOverall() {
        return overall;
    }

    public void setOverall(float overall) {
        this.overall = overall;
    }

    public float getAmbiance() {
        return ambiance;
    }

    public void setAmbiance(float ambiance) {
        this.ambiance = ambiance;
    }

    public float getFood() {
        return food;
    }

    public void setFood(float food) {
        this.food = food;
    }

    public float getValue() {
        return value;
    }

    public void setValue(float value) {
        this.value = value;
    }

    public float getService() {
        return service;
    }

    public void setService(float service) {
        this.service = service;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
