package com.project.infrastructure.persistent.po;

import lombok.Data;

import java.util.Date;

/**
 * '用户行为返利流水订单表 持久化对象
 */

@Data
public class UserBehaviorRebateOrder {
    private Long id;

    private String userId;

    private String orderId;
    /** 行为类型（sign 签到、openai_pay 支付） */
    private String behaviorType;

    private String rebateDesc;
    /** 返利类型（sku 活动库存充值商品、integral 用户活动积分） */
    private String rebateType;

    /** 返利配置【sku值，积分值】 */
    private String rebateConfig;
    /** 业务ID - 拼接的唯一值 */
    private String bizId;
    private Date createTime;
    private Date updateTime;

}
