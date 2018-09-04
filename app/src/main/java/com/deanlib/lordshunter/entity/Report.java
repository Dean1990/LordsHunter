package com.deanlib.lordshunter.entity;

import com.deanlib.ootb.entity.BaseEntity;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "report")
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
    @Column(name = "imgMd5")
    String imgMd5;

    //以下为图片识别
    @Column(name = "imgPreyName")
    String imgPreyName;
    @Column(name = "imgPreyLevel")
    int imgPreyLevel;
    @Column(name = "imgDataTime")
    String imgDataTime;

    boolean isKill;


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

    public String getImgPreyName() {
        return imgPreyName;
    }

    public void setImgPreyName(String imgPreyName) {
        this.imgPreyName = imgPreyName;
    }

    public int getImgPreyLevel() {
        return imgPreyLevel;
    }

    public void setImgPreyLevel(int imgPreyLevel) {
        this.imgPreyLevel = imgPreyLevel;
    }

    public String getImgDataTime() {
        return imgDataTime;
    }

    public void setImgDataTime(String imgDataTime) {
        this.imgDataTime = imgDataTime;
    }

    public String getImgMd5() {
        return imgMd5;
    }

    public void setImgMd5(String imgMd5) {
        this.imgMd5 = imgMd5;
    }

    public boolean isKill() {
        return isKill;
    }

    public void setKill(boolean kill) {
        isKill = kill;
    }
}
