package com.zybio.clouddesk.enums;

public enum FileOpsType {

    ENCODE_FILE("ENCODE_FILE"),
    DECODE_FILE("DECODE_FILE");

    FileOpsType(String value) {
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
