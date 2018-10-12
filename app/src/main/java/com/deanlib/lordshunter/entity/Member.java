package com.deanlib.lordshunter.entity;

import android.support.annotation.NonNull;

public class Member implements Comparable<Member> {

    public Member(String name,String group, long count) {
        this.name = name;
        this.group = group;
        this.count = count;
    }

    String name;
    long count;
    String group;

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

    @Override
    public int compareTo(@NonNull Member o) {
        return this.count>0?1:-1;
    }
}
