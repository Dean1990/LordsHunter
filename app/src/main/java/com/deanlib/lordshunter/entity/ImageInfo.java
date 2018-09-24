package com.deanlib.lordshunter.entity;


import io.realm.RealmObject;
import io.realm.annotations.Index;
import io.realm.annotations.PrimaryKey;

public class ImageInfo extends RealmObject {


    @PrimaryKey
    String id;
    String preyName;//猎物名称
    int preyLevel;//猎物等级
    String dataTime;//猎杀时间
    String uri;//截图地址
    @Index
    String md5;
    boolean isKill;

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
}
