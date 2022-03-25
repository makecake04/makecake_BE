package com.project.makecake.dto;

import com.project.makecake.model.OrderForm;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class OrderFormDetailResponseDto {
    private long orderFormId;
    private String name;
    private List<String> formList;
    private List<String> instructionList;
    private StoreMoreDetailsDto moreDetails;

    @Builder
    OrderFormDetailResponseDto (OrderForm orderForm, List<String> formList, List<String> instructionList, StoreMoreDetailsDto moreDetails){
        this.orderFormId = orderForm.getOrderFormId();
        this.name = orderForm.getName();
        this.formList = formList;
        this.instructionList = instructionList;
        this.moreDetails = moreDetails;
    }

}