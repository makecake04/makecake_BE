package com.project.makecake.dto;

import com.project.makecake.model.StoreOption;
import lombok.Builder;
import lombok.Getter;

@Getter
public class StoreMoreCakeOptionDto {
    private String name;
    private String price;
    private String moreInfo;

    @Builder
    public StoreMoreCakeOptionDto(StoreOption storeOption){
        this.name = storeOption.getMainCat();
        this.price = storeOption.getAddedPrice();
        this.moreInfo = storeOption.getMoreInfo();
    }
}
