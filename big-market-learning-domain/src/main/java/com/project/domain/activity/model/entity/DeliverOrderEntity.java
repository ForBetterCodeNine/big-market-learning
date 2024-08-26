package com.project.domain.activity.model.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 出货单实体对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class DeliverOrderEntity {
    private String userId;

    private String outBusinessNo;
}
