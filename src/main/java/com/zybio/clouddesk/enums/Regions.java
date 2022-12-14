package com.zybio.clouddesk.enums;

public enum Regions {

    REGION1("公共"),
    REGION2("质保来料文件");

    Regions(String value){
        this.value = value;
    }

    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
