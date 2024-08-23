package com.project.domain.award.service.distribute.impl;

import com.project.domain.award.model.aggregate.GiveoutPrizesAggregate;
import com.project.domain.award.model.entity.DistributeAwardEntity;
import com.project.domain.award.model.entity.UserAwardRecordEntity;
import com.project.domain.award.model.entity.UserCreditAwardEntity;
import com.project.domain.award.model.valobj.AwardStateVO;
import com.project.domain.award.repository.IAwardRepository;
import com.project.domain.award.service.distribute.IDistributeAward;
import com.project.types.common.Constants;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.math.BigDecimal;
import java.math.MathContext;

/**
 * 用户积分奖品，支持 award_config 透传，满足黑名单积分奖励。
 */

@Component("user_credit_random")
public class UserCreditRandomAward implements IDistributeAward {

    @Resource
    private IAwardRepository repository;


    @Override
    public void giveOutPrizes(DistributeAwardEntity distributeAwardEntity) {
        Integer awardId = distributeAwardEntity.getAwardId();
        String awardConfig = distributeAwardEntity.getAwardConfig();
        if(StringUtils.isBlank(awardConfig)) {
            awardConfig = repository.queryAwardConfig(awardId);
        }

        String[] creditRange = awardConfig.split(Constants.SPLIT);
        if(creditRange.length != 2) {
            throw new RuntimeException("award_config 「" + awardConfig + "」配置不是一个范围值，如 1,100");
        }

        //生成随机积分值
        BigDecimal creditAmount = generateRandom(new BigDecimal(creditRange[0]), new BigDecimal(creditRange[1]));


        //构建聚合对象
        UserAwardRecordEntity userAwardRecordEntity = GiveoutPrizesAggregate.buildDistributeUserAwardRecordEntity(
                distributeAwardEntity.getUserId(),
                distributeAwardEntity.getOrderId(),
                distributeAwardEntity.getAwardId(),
                AwardStateVO.complete
        );

        UserCreditAwardEntity userCreditAwardEntity = GiveoutPrizesAggregate.buildUserCreditAwardEntity(
                distributeAwardEntity.getUserId(),
                creditAmount
        );

        GiveoutPrizesAggregate giveoutPrizesAggregate = new GiveoutPrizesAggregate();
        giveoutPrizesAggregate.setUserId(distributeAwardEntity.getUserId());
        giveoutPrizesAggregate.setUserAwardRecordEntity(userAwardRecordEntity);
        giveoutPrizesAggregate.setUserCreditAwardEntity(userCreditAwardEntity);

        repository.saveGiveoutPrizesAggregate(giveoutPrizesAggregate);
    }

    private BigDecimal generateRandom(BigDecimal small, BigDecimal large) {
        if(small.equals(large)) return small;
        BigDecimal randomBigDecimal = small.add(BigDecimal.valueOf(Math.random()).multiply(large.subtract(small)));
        return randomBigDecimal.round(new MathContext(3));
    }
}
