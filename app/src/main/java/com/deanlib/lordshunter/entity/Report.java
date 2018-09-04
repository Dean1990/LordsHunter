package com.deanlib.lordshunter.entity;

import com.deanlib.ootb.entity.BaseEntity;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "Report")
public class Report extends BaseEntity {

    @Column(name = "id",isId = true)
    int id;
    @Column(name = "group")
    String group;
    @Column(name = "data")
    String data;
    @Column(name = "time")
    String time;
    @Column(name = "name")
    String name;
    @Column(name = "image")
    String image;

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

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }
}
