package com.project.domain.activity.model.entity;

import com.project.domain.activity.model.valobj.OrderTradeTypeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SkuRechargeOrderEntity {

    private String userId;

    private Long sku;


    //唯一索引 防止下单重复
    private String outBusinessNo;

    private OrderTradeTypeVO orderTradeTypeVO = OrderTradeTypeVO.rebate_no_pay_trade;
}
