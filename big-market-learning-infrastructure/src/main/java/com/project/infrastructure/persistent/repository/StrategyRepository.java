package com.project.infrastructure.persistent.repository;

import com.project.domain.strategy.model.entity.StrategyAwardEntity;
import com.project.domain.strategy.model.entity.StrategyEntity;
import com.project.domain.strategy.model.entity.StrategyRuleEntity;
import com.project.domain.strategy.model.valobj.*;
import com.project.domain.strategy.repository.IStrategyRepository;
import com.project.infrastructure.persistent.dao.*;
import com.project.infrastructure.persistent.po.*;
import com.project.infrastructure.persistent.redis.IRedisService;
import com.project.types.common.Constants;
import com.project.types.enums.ResponseCode;
import com.project.types.exception.AppException;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBlockingQueue;
import org.redisson.api.RDelayedQueue;
import org.springframework.stereotype.Repository;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Slf4j
@Repository
public class StrategyRepository implements IStrategyRepository {

    @Resource
    private IRaffleActivityDao raffleActivityDao;

    @Resource
    private IStrategyAwardDao strategyAwardDao;

    @Resource
    private IRedisService redisService;

    @Resource
    private IStrategyDao strategyDao;

    @Resource
    private IStrategyRuleDao strategyRuleDao;

    @Resource
    private IRaffleActivityAccountDayDao activityAccountDayDao;

    @Resource
    private IRuleTreeDao ruleTreeDao;

    @Resource
    private IRuleTreeNodeDao ruleTreeNodeDao;

    @Resource
    private IRuleTreeNodeLineDao ruleTreeNodeLineDao;

    @Override
    public List<StrategyAwardEntity> queryStrategyAwardEntityList(Long strategyId) {
        //查到某个策略id下的所有奖品信息
        //先从缓存中获取， 缓存中没有 先保存到数据库 再插入到缓存
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_LIST_KEY + strategyId;
        List<StrategyAwardEntity> strategyAwardEntitieList = redisService.getValue(cacheKey);
        if(null != strategyAwardEntitieList && !strategyAwardEntitieList.isEmpty()) return strategyAwardEntitieList;
        //从数据库中获取
        List<StrategyAward> strategyAwardList = strategyAwardDao.queryStrategyAwardEntityListByStrategyId(strategyId);
        strategyAwardEntitieList = new ArrayList<>(strategyAwardList.size());
        for(StrategyAward strategyAward : strategyAwardList) {
            StrategyAwardEntity strategyAwardEntity = new StrategyAwardEntity();
            strategyAwardEntity.setStrategyId(strategyId);
            strategyAwardEntity.setAwardId(strategyAward.getAwardId());
            strategyAwardEntity.setAwardCount(strategyAward.getAwardCount());
            strategyAwardEntity.setAwardCountSurplus(strategyAward.getAwardCountSurplus());
            strategyAwardEntity.setAwardRate(strategyAward.getAwardRate());
            strategyAwardEntity.setSort(strategyAward.getSort());
            strategyAwardEntity.setAwardSubtitle(strategyAward.getAwardSubtitle());
            strategyAwardEntity.setAwardTitle(strategyAward.getAwardTitle());
            strategyAwardEntitieList.add(strategyAwardEntity);
        }
        redisService.setValue(cacheKey, strategyAwardEntitieList);
        return strategyAwardEntitieList;
    }

    @Override
    public StrategyAwardEntity queryStrategyAwardEntity(Long strategyId, Integer awardId) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_KEY + strategyId + "_" + awardId;
        StrategyAwardEntity strategyAwardEntity = redisService.getValue(cacheKey);
        if(strategyAwardEntity != null) return strategyAwardEntity;
        StrategyAward strategyAwardReq = new StrategyAward();
        strategyAwardReq.setStrategyId(strategyId);
        strategyAwardReq.setAwardId(awardId);
        StrategyAward strategyAwardRes = strategyAwardDao.queryStrategyAward(strategyAwardReq);
        strategyAwardEntity = StrategyAwardEntity.builder()
                .strategyId(strategyAwardRes.getStrategyId())
                .awardId(strategyAwardRes.getAwardId())
                .awardTitle(strategyAwardRes.getAwardTitle())
                .awardSubtitle(strategyAwardRes.getAwardSubtitle())
                .awardCount(strategyAwardRes.getAwardCount())
                .awardCountSurplus(strategyAwardRes.getAwardCountSurplus())
                .awardRate(strategyAwardRes.getAwardRate())
                .sort(strategyAwardRes.getSort())
                .build();

        redisService.setValue(cacheKey, strategyAwardEntity);
        return strategyAwardEntity;
    }

    @Override
    public void storeStrategyAwardEntityRateTables(String key, Integer rateRange, Map<Integer, Integer> map) {
        //把某个策略下 对应的奖品转换后的下标信息 以及随机数的范围 存放到redis中
        redisService.setValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key, rateRange);
        Map<Integer, Integer> searchTable = redisService.getMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key);
        searchTable.putAll(map);
    }

    @Override
    public Integer getRateRangeByStrategyId(Long strategyId) {
        return getRateRangeByStrategyId(String.valueOf(strategyId));
    }

    @Override
    public Integer getRateRangeByStrategyId(String key) {
        //如果在抽奖之前 没有装配 则抽奖失败
        String cacheKey = Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key;
        if (!redisService.isExists(cacheKey)) {
            throw new AppException(ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY.getCode(), cacheKey + Constants.COLON + ResponseCode.UN_ASSEMBLED_STRATEGY_ARMORY.getInfo());
        }
        return redisService.getValue(Constants.RedisKey.STRATEGY_RATE_RANGE_KEY + key);
    }


    @Override
    public Integer getStrategyRandom(String key, Integer rateKey) {
        return redisService.getFromMap(Constants.RedisKey.STRATEGY_RATE_TABLE_KEY + key, rateKey);
    }

    @Override
    public StrategyEntity queryStrategyByStrategyId(Long strategyId) {
        String cacheKey = Constants.RedisKey.STRATEGY_KEY + strategyId;
        StrategyEntity strategyEntity = redisService.getValue(cacheKey);
        if(strategyEntity != null) return strategyEntity;
        strategyEntity = new StrategyEntity();
        Strategy strategy = strategyDao.queryStrategyByStrategyId(strategyId);
        strategyEntity.setStrategyId(strategy.getStrategyId());
        strategyEntity.setRuleModels(strategy.getRuleModels());
        strategyEntity.setStrategyDesc(strategy.getStrategyDesc());
        redisService.setValue(cacheKey, strategyEntity);
        return strategyEntity;
    }

    @Override
    public StrategyRuleEntity queryStrategyRuleByStrategyIdAndRuleWeight(Long strategyId, String ruleModel) {
        String cacheKey = Constants.RedisKey.STRATEGY_RULE_KEY + strategyId;
        StrategyRuleEntity strategyRuleEntity = redisService.getValue(cacheKey);
        if(strategyRuleEntity != null) return strategyRuleEntity;
        StrategyRule strategyRule = strategyRuleDao.queryStrategyRuleByStrategyIdAndRuleWeight(strategyId, ruleModel);
        strategyRuleEntity = new StrategyRuleEntity();
        strategyRuleEntity.setStrategyId(strategyRule.getStrategyId());
        strategyRuleEntity.setRuleValue(strategyRule.getRuleValue());
        strategyRuleEntity.setRuleType(strategyRule.getRuleType());
        strategyRuleEntity.setAwardId(strategyRule.getAwardId());
        strategyRuleEntity.setRuleDesc(strategyRule.getRuleDesc());
        strategyRuleEntity.setRuleModel(strategyRule.getRuleModel());
        redisService.setValue(cacheKey, strategyRuleEntity);
        return strategyRuleEntity;
    }

    @Override
    public String queryStrategyRuleValue(Long strategyId, Integer awardId, String ruleModel) {
        StrategyRule rule = new StrategyRule();
        rule.setStrategyId(strategyId);
        rule.setAwardId(awardId);
        rule.setRuleModel(ruleModel);
        return strategyRuleDao.queryStrategyRuleValue(rule);
    }

    @Override
    public StrategyAwardRuleModelVO queryStrategyRuleModelsByStrategyIdAndAwardId(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setAwardId(awardId);
        strategyAward.setStrategyId(strategyId);
        String ruleModels = strategyAwardDao.queryStrategyAwardByStrategyIdAndAwardId(strategyAward);
        StrategyAwardRuleModelVO vo = new StrategyAwardRuleModelVO();
        vo.setRuleModels(ruleModels);
        return vo;
    }


    /**
     * 根据规则树id 查询整个树结构
     * @param treeId
     * @return
     */
    @Override
    public RuleTreeVO queryRuleTreeVOByTreeId(String treeId) {
        String cacheKey = Constants.RedisKey.RULE_TREE_VO_KEY + treeId;
        RuleTreeVO ruleTreeVO = redisService.getValue(cacheKey);
        if(ruleTreeVO != null) return ruleTreeVO;

        //先获取RuleTree
        RuleTree ruleTree = ruleTreeDao.queryRuleTreeByTreeId(treeId);
        List<RuleTreeNode> ruleTreeNodeList = ruleTreeNodeDao.queryRuleTreeNodeListByTreeId(treeId);
        List<RuleTreeNodeLine> ruleTreeNodeLineList = ruleTreeNodeLineDao.queryRuleTreeNodeLineListByTreeId(treeId);
        //tree node line转为map
        Map<String, List<RuleTreeNodeLineVO>> ruleTreeNodeLineMap = new HashMap<>();
        for(RuleTreeNodeLine nodeLine : ruleTreeNodeLineList) {
            RuleTreeNodeLineVO lineVO = new RuleTreeNodeLineVO();
            lineVO.setTreeId(treeId);
            lineVO.setRuleLimitType(RuleLimitTypeVO.valueOf(nodeLine.getRuleLimitType()));
            lineVO.setRuleNodeFrom(nodeLine.getRuleNodeFrom());
            lineVO.setRuleNodeTo(nodeLine.getRuleNodeTo());
            lineVO.setRuleLimitValue(RuleLogicCheckTypeVO.valueOf(nodeLine.getRuleLimitValue()));

            List<RuleTreeNodeLineVO> ruleTreeNodeLineVOS = ruleTreeNodeLineMap.computeIfAbsent(nodeLine.getRuleNodeFrom(), k-> new ArrayList<>());
            ruleTreeNodeLineVOS.add(lineVO);
        }

        //将tree node 转为map
        Map<String, RuleTreeNodeVO> ruleTreeNodeVOMap = new HashMap<>();
        for(RuleTreeNode treeNode : ruleTreeNodeList) {
            RuleTreeNodeVO treeNodeVO = new RuleTreeNodeVO();
            treeNodeVO.setTreeId(treeId);
            treeNodeVO.setRuleDesc(treeNode.getRuleDesc());
            treeNodeVO.setRuleKey(treeNode.getRuleKey());
            treeNodeVO.setTreeNodeLineVOList(ruleTreeNodeLineMap.get(treeNode.getRuleKey()));
            treeNodeVO.setRuleValue(treeNode.getRuleValue());

            ruleTreeNodeVOMap.put(treeNode.getRuleKey(), treeNodeVO);
        }


        RuleTreeVO voDb = new RuleTreeVO();
        voDb.setTreeId(treeId);
        voDb.setTreeName(ruleTree.getTreeName());
        voDb.setTreeNodeMap(ruleTreeNodeVOMap);
        voDb.setTreeDesc(ruleTree.getTreeDesc());
        voDb.setTreeRootRuleNode(ruleTree.getTreeRootRuleKey());

        redisService.setValue(cacheKey, voDb);
        return voDb;
    }

    @Override
    public void cacheAwardCount(String cacheKey, Integer awardCount) {
        if(redisService.isExists(cacheKey)) return;
        redisService.setAtomicLong(cacheKey, awardCount);
    }

    @Override
    public boolean subtractionAwardStock(String cacheKey) {
        long surPlus = redisService.decr(cacheKey);
        if(surPlus < 0) {
            redisService.setValue(cacheKey, 0);
            return false;
        }
        // 1. 按照cacheKey decr 后的值，如 99、98、97 和 key 组成为库存锁的key进行使用。
        // 2. 加锁为了兜底，如果后续有恢复库存，手动处理等，也不会超卖。因为所有的可用库存key，都被加锁了。
        String lockKey = cacheKey + "_" + surPlus;
        Boolean lock = redisService.setNx(lockKey);
        if(!lock) {
            log.info("策略奖品库存加锁失败 {}", lockKey);
        }
        return lock;
    }

    @Override
    public void awardStockConsumeSendQueue(StrategyAwardStockKeyVO strategyAwardStockKeyVO) {
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        RDelayedQueue<StrategyAwardStockKeyVO> delayedQueue = redisService.getDelayedQueue(blockingQueue);
        delayedQueue.offer(strategyAwardStockKeyVO, 3, TimeUnit.SECONDS);
    }

    @Override
    public StrategyAwardStockKeyVO takeQueue() throws Exception{
        String cacheKey = Constants.RedisKey.STRATEGY_AWARD_COUNT_QUERY_KEY;
        RBlockingQueue<StrategyAwardStockKeyVO> blockingQueue = redisService.getBlockingQueue(cacheKey);
        return blockingQueue.poll();
    }

    @Override
    public void updateStrategyAwardStock(Long strategyId, Integer awardId) {
        StrategyAward strategyAward = new StrategyAward();
        strategyAward.setAwardId(awardId);
        strategyAward.setStrategyId(strategyId);
        strategyAwardDao.updateStrategyAwardStock(strategyAward);
    }

    @Override
    public Long queryStrategyByActivityId(Long activityId) {
        return raffleActivityDao.queryStrategyByActivityId(activityId);
    }

    @Override
    public Integer queryTodayUserRaffleCount(String userId, Long strategyId) {
        Long activityId = raffleActivityDao.queryActivityIdByStrategyId(strategyId);
        RaffleActivityAccountDay raffleActivityAccountDayReq = new RaffleActivityAccountDay();
        raffleActivityAccountDayReq.setUserId(userId);
        raffleActivityAccountDayReq.setActivityId(activityId);
        raffleActivityAccountDayReq.setDay(raffleActivityAccountDayReq.currentDay());
        RaffleActivityAccountDay res = activityAccountDayDao.queryActivityAccountDay(raffleActivityAccountDayReq);
        if(null == res) return 0;
        // 总次数 - 剩余的，等于今日参与的
        return res.getDayCount() - res.getDayCountSurplus();
    }
}
