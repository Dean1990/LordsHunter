package com.deanlib.lordshunter.data.entity;


import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class Report extends RealmObject implements Parcelable {

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
    long timestamp;
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

    public long getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(long timestamp) {
        this.timestamp = timestamp;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.group);
        dest.writeString(this.date);
        dest.writeString(this.time);
        dest.writeString(this.name);
        dest.writeParcelable(this.image, flags);
        dest.writeLong(this.timestamp);
        dest.writeInt(this.status);
    }

    public Report() {
    }

    protected Report(Parcel in) {
        this.id = in.readString();
        this.group = in.readString();
        this.date = in.readString();
        this.time = in.readString();
        this.name = in.readString();
        this.image = in.readParcelable(ImageInfo.class.getClassLoader());
        this.timestamp = in.readLong();
        this.status = in.readInt();
    }

    public static final Creator<Report> CREATOR = new Creator<Report>() {
        @Override
        public Report createFromParcel(Parcel source) {
            return new Report(source);
        }

        @Override
        public Report[] newArray(int size) {
            return new Report[size];
        }
    };

    @Override
    public String toString() {
        return "Report{" +
                "id='" + id + '\'' +
                ", group='" + group + '\'' +
                ", date='" + date + '\'' +
                ", time='" + time + '\'' +
                ", name='" + name + '\'' +
                ", image=" + image +
                ", timestamp=" + timestamp +
                ", status=" + status +
                '}';
    }
}
