package com.project.domain.activity.model.entity;

import lombok.Data;

@Data
public class SkuRechargeOrderEntity {

    private String userId;

    private Long sku;


    //唯一索引 防止下单重复
    private String outBusinessNo;
}
