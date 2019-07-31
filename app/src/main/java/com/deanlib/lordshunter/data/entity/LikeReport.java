package com.deanlib.lordshunter.data.entity;


import android.os.Parcel;
import android.os.Parcelable;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.PrimaryKey;

public class LikeReport implements Parcelable,Cloneable {

    String id;
    String group;//组
    String date;//日期
    String time;//时间
    String name;//用户名称
    LikeImageInfo image;
    long timestamp;

    int status;//添加时记录状态，用于返回信息

    public LikeReport(Report report){
        if (report!=null){
            id = report.id;
            group = report.group;
            date = report.date;
            time = report.time;
            name = report.name;
            image = new LikeImageInfo(report.image);
            timestamp = report.timestamp;
            status = report.status;
        }
    }

    public Report toReport(){
        Report report = new Report();
        report.setId(id);
        report.setGroup(group);
        report.setDate(date);
        report.setTime(time);
        report.setName(name);
        report.setImage(image.toImageInfo());
        report.setTimestamp(timestamp);
        report.setStatus(status);
        return report;
    }

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

    public LikeImageInfo getImage() {
        return image;
    }

    public void setImage(LikeImageInfo image) {
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

    public LikeReport() {
    }

    protected LikeReport(Parcel in) {
        this.id = in.readString();
        this.group = in.readString();
        this.date = in.readString();
        this.time = in.readString();
        this.name = in.readString();
        this.image = in.readParcelable(LikeImageInfo.class.getClassLoader());
        this.timestamp = in.readLong();
        this.status = in.readInt();
    }

    public static final Creator<LikeReport> CREATOR = new Creator<LikeReport>() {
        @Override
        public LikeReport createFromParcel(Parcel source) {
            return new LikeReport(source);
        }

        @Override
        public LikeReport[] newArray(int size) {
            return new LikeReport[size];
        }
    };

    @Override
    public String toString() {
        return "LikeReport{" +
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

    @Override
    public Object clone() throws CloneNotSupportedException {
        //深拷贝
        Object obj = super.clone();
        LikeImageInfo i = ((LikeReport)obj).getImage();
        ((LikeReport)obj).setImage((LikeImageInfo) i.clone());
        return obj;
    }

}
