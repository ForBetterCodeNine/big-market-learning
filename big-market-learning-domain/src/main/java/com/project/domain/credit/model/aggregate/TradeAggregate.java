package com.project.domain.credit.model.aggregate;

import com.project.domain.credit.model.entity.CreditAccountEntity;
import com.project.domain.credit.model.entity.CreditOrderEntity;
import com.project.domain.credit.model.valobj.TradeNameVO;
import com.project.domain.credit.model.valobj.TradeTypeVO;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang.RandomStringUtils;

import java.math.BigDecimal;

/**
 * 交易聚合对象
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TradeAggregate {
    private String userId;

    private CreditAccountEntity creditAccountEntity;

    private CreditOrderEntity creditOrderEntity;

    public static CreditAccountEntity createCreditAccountEntity(String userId, BigDecimal adjustAmount) {
        return CreditAccountEntity.builder().userId(userId).adjustAmount(adjustAmount).build();
    }

    public static CreditOrderEntity createCreditOrderEntity(String userId, TradeNameVO tradeNameVO, TradeTypeVO tradeTypeVO, BigDecimal bigDecimal,
                                                            String outBusinessNo) {
        return CreditOrderEntity.builder()
                .userId(userId)
                .tradeName(tradeNameVO)
                .tradeType(tradeTypeVO)
                .tradeAmount(bigDecimal)
                .orderId(RandomStringUtils.randomNumeric(12))
                .outBusinessNo(outBusinessNo)
                .build();

    }
}
