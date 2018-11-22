package com.deanlib.lordshunter.data.entity;

import android.support.annotation.NonNull;

public class Member implements Comparable<Member> {

    //0 隐藏；1 显示
    public static final int STATE_HIDE = 0;
    public static final int STATE_SHOW = 1;

    public Member(){

    }

    public Member(String name,String group, long count) {
        this.name = name;
        this.group = group;
        this.count = count;
    }

    String name;
    long count;
    String group;
    boolean isHide;
    boolean isChecked;

    public String getGroup() {
        return group;
    }

    public void setGroup(String group) {
        this.group = group;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getCount() {
        return count;
    }

    public void setCount(long count) {
        this.count = count;
    }

    public boolean isHide() {
        return isHide;
    }

    public void setHide(boolean hide) {
        isHide = hide;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public int compareTo(@NonNull Member o) {
        return this.count>o.getCount()?-1:1;
    }
}
