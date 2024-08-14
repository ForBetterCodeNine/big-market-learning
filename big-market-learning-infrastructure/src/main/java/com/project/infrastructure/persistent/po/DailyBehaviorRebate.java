package com.project.infrastructure.persistent.po;


import lombok.Data;

import java.util.Date;

/**
 * 用户日常反利活动配置
 */
@Data
public class DailyBehaviorRebate {
    private Long id;

    /**
     * 行为类型 签到 支付等
     */
    private String behaviorType;

    private String rebateDesc;

    /** 返利类型（sku 活动库存充值商品、integral 用户活动积分） */
    private String rebateType;
    /** 返利配置 */
    private String rebateConfig;
    /** 状态（open 开启、close 关闭） */
    private String state;
    private Date createTime;
    private String updateTime;
}
