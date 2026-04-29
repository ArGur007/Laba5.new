package ru.laba5.domain;

public enum MeasurementParam {
    PH,
    CONDUCTIVITY,
    NITRATE;

    public static MeasurementParam fromString(String s){
        try{
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e){
            return null;
        }
    }
}
