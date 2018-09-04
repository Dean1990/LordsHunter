package com.deanlib.lordshunter.entity;

import com.deanlib.ootb.entity.BaseEntity;

import org.xutils.db.annotation.Column;
import org.xutils.db.annotation.Table;

@Table(name = "image")
public class ImageInfo extends BaseEntity {

    @Column(name = "id",isId = true)
    int id;
    @Column(name = "preyName")
    String preyName;
    @Column(name = "preyLevel")
    int preyLevel;
    @Column(name = "dataTime")
    String dataTime;
    @Column(name = "uri")
    String uri;
    @Column(name = "md5")
    String md5;

    boolean isKill;

    public int getId() {
        return id;
    }

    public void setId(int id) {
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
