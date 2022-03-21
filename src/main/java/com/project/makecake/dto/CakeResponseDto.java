package com.project.makecake.dto;

import com.project.makecake.model.Cake;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CakeResponseDto {

    private Long cakeId;
    private String img;
    private Long storeId;
    private String storeName;
    private int likeCnt;
    private boolean myLike;

    // 생성자
    public CakeResponseDto(Cake cake,boolean myLike) {
        this.cakeId = cake.getCakeId();
        this.img = cake.getUrl();
        this.storeId = cake.getStore().getStoreId();
        this.storeName = cake.getStore().getName();
        this.likeCnt = cake.getLikeCnt();
        this.myLike = myLike;
    }
}