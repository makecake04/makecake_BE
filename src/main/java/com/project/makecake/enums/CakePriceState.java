package com.project.makecake.enums;

import lombok.Getter;

@Getter
public enum CakePriceState {

    FIXED("FIXED"),
    UNFIXED("UNFIXED"),
    RANGE("RANGE");

    private String value;

    CakePriceState(String value){
        this.value = value;
    }

}