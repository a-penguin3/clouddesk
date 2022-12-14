package com.zybio.clouddesk.enums;

public enum RolesEnum {

    ADMIN("admin"),
    USER("user");

    RolesEnum(String value){
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
