package com.madou.gebase.model.enums;

public enum TeamStatusEnums {

    PUBLIC(0,"公开"),
    PRIVATE(1,"私有"),
    SECRET(2,"加密");


    private int value;
    private String text;

    public static TeamStatusEnums getEnumByValues(Integer value){
        if(value == null){
            return null;
        }
        TeamStatusEnums[] values = TeamStatusEnums.values();
        for (TeamStatusEnums teamStatusEnum:values
             ) {
            if(teamStatusEnum.getValue() == value){
                return teamStatusEnum;
            }
        }
        return null;
    }

    TeamStatusEnums(int value, String text) {
        this.value = value;
        this.text = text;
    }

    public int getValue() {
        return value;
    }

    public void setValue(int value) {
        this.value = value;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }
}
