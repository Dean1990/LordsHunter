package com.deanlib.lordshunter.data.entity;

import java.io.File;

public class OCR {

    private String name;
    private File file;
    private boolean isExist;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public File getFile() {
        return file;
    }

    public void setFile(File file) {
        this.file = file;
    }

    public boolean isExist() {
        return isExist;
    }

    public void setExist(boolean exist) {
        isExist = exist;
    }
}
