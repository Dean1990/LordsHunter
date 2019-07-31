package com.deanlib.lordshunter.data.entity;


import android.os.Parcel;
import android.os.Parcelable;

import java.util.List;

import io.realm.RealmObject;
import io.realm.annotations.Ignore;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class ImageInfo extends RealmObject implements Parcelable,Cloneable {


    @PrimaryKey
    String id;
    String preyName;//猎物名称
    int preyLevel;//猎物等级
    String dataTime;//猎杀时间
    String uri;//截图地址
    @Index
    String md5;
    boolean isKill;

    @Ignore
    List<Report> attachReports;//一张图片中可能 有多条信息

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPreyName() {
        return preyName;
    }

    public void setPreyName(String preyName) {
        this.preyName = preyName;
    }

    public int getPreyLevel() {
        return preyLevel;
    }

    public void setPreyLevel(int preyLevel) {
        this.preyLevel = preyLevel;
    }

    public String getDataTime() {
        return dataTime;
    }

    public void setDataTime(String dataTime) {
        this.dataTime = dataTime;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public String getMd5() {
        return md5;
    }

    public void setMd5(String md5) {
        this.md5 = md5;
    }

    public boolean isKill() {
        return isKill;
    }

    public void setKill(boolean kill) {
        isKill = kill;
    }

    public List<Report> getAttachReports() {
        return attachReports;
    }

    public void setAttachReports(List<Report> attachReports) {
        this.attachReports = attachReports;
    }


    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.id);
        dest.writeString(this.preyName);
        dest.writeInt(this.preyLevel);
        dest.writeString(this.dataTime);
        dest.writeString(this.uri);
        dest.writeString(this.md5);
        dest.writeByte(this.isKill ? (byte) 1 : (byte) 0);
        dest.writeTypedList(this.attachReports);
    }

    public ImageInfo() {
    }

    protected ImageInfo(Parcel in) {
        this.id = in.readString();
        this.preyName = in.readString();
        this.preyLevel = in.readInt();
        this.dataTime = in.readString();
        this.uri = in.readString();
        this.md5 = in.readString();
        this.isKill = in.readByte() != 0;
        this.attachReports = in.createTypedArrayList(Report.CREATOR);
    }

    public static final Creator<ImageInfo> CREATOR = new Creator<ImageInfo>() {
        @Override
        public ImageInfo createFromParcel(Parcel source) {
            return new ImageInfo(source);
        }

        @Override
        public ImageInfo[] newArray(int size) {
            return new ImageInfo[size];
        }
    };

    @Override
    public String toString() {
        return "ImageInfo{" +
                "id='" + id + '\'' +
                ", preyName='" + preyName + '\'' +
                ", preyLevel=" + preyLevel +
                ", dataTime='" + dataTime + '\'' +
                ", uri='" + uri + '\'' +
                ", md5='" + md5 + '\'' +
                ", isKill=" + isKill +
                ", attachReports=" + attachReports +
                '}';
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }
}
