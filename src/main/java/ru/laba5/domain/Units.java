package ru.laba5.domain;

public enum Units {
    MOL_L,
    MMOL_L,
    MOL_ML,
    MMOL_ML,
    UNITLESS,
    SIEMENS;
    public static Units fromString(String s){
        try{
            return valueOf(s.toUpperCase());
        } catch (IllegalArgumentException e){
            return null;
        }
    }
}
