package com.deanlib.lordshunter.entity;

/**
 * 猎物
 */
public class Prey{

    int level;//等级
    String nameLocal;//本地语言
    String nameEng;//英文 英文暂时不做
    String nameChiSim;//简体
    String nameChiTra;//繁体

    public Prey(String nameLocal,String nameChiSim, String nameChiTra) {
        this.nameLocal = nameLocal;
        this.nameChiSim = nameChiSim;
        this.nameChiTra = nameChiTra;
    }

    @Override
    public boolean equals(Object obj) {
        if (nameChiSim.equals(obj)){
            return true;
        }else if (nameChiTra.equals(obj)){
            return true;
        }

        return false;
    }

    public String getNameLocal() {
        return nameLocal;
    }

    public void setNameLocal(String nameLocal) {
        this.nameLocal = nameLocal;
    }

    public int getLevel() {
        return level;
    }

    public void setLevel(int level) {
        this.level = level;
    }

    public String getNameEng() {
        return nameEng;
    }

    public void setNameEng(String nameEng) {
        this.nameEng = nameEng;
    }

    public String getNameChiSim() {
        return nameChiSim;
    }

    public void setNameChiSim(String nameChiSim) {
        this.nameChiSim = nameChiSim;
    }

    public String getNameChiTra() {
        return nameChiTra;
    }

    public void setNameChiTra(String nameChiTra) {
        this.nameChiTra = nameChiTra;
    }
}
