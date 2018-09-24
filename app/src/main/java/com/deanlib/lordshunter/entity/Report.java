package com.deanlib.lordshunter.entity;


import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Report extends RealmObject {

    @Ignore
    public static final int STATUS_NEW = 0;//新增
    @Ignore
    public static final int STATUS_EXIST = 1;//存在
    @Ignore
    public static final int STATUS_REPET = 2;//图片重复

    @PrimaryKey
    String id;
    String group;//组
    String date;//日期
    String time;//时间
    String name;//用户名称
    ImageInfo image;
    @Ignore
    int status;//添加时记录状态，用于返回信息

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
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

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }
}
