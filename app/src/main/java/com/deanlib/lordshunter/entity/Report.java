package com.deanlib.lordshunter.entity;


import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

public class Report extends RealmObject {


    @PrimaryKey
    int id;
    String group;
    String data;
    String time;
    String name;
    ImageInfo image;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public ImageInfo getImage() {
        return image;
    }

    public void setImage(ImageInfo image) {
        this.image = image;
    }

}
