package com.project.makecake.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderFormRequestDto {
    private long storeId;
    private String form;
    private String instruction;
}