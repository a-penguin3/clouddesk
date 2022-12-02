package com.zybio.clouddesk.enums;

public enum OptGroupEnum {
    ADD("addGroup"),
    MOVE("removeGroup");

    OptGroupEnum(String value){
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
