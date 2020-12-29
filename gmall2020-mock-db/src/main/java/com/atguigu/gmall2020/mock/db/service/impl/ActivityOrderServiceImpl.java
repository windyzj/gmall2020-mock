package com.atguigu.gmall2020.mock.db.service.impl;

import com.atguigu.gmall2020.mock.db.bean.*;
import com.atguigu.gmall2020.mock.db.constant.GmallConstant;
import com.atguigu.gmall2020.mock.db.mapper.ActivityOrderMapper;
import com.atguigu.gmall2020.mock.db.service.ActivityOrderService;
import com.atguigu.gmall2020.mock.db.service.ActivityRuleService;
import com.atguigu.gmall2020.mock.db.service.ActivitySkuService;
import com.atguigu.gmall2020.mock.db.util.ParamUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * <p>
 * 活动与订单关联表 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2020-02-25
 */
@Service
@Slf4j
public class ActivityOrderServiceImpl extends ServiceImpl<ActivityOrderMapper, OrderDetailActivity> implements ActivityOrderService {

    @Autowired
    ActivitySkuService activitySkuService;

    @Autowired
    ActivityRuleService activityRuleService;


    @Value("${mock.date}")
    String mockDate;


    public List<OrderDetailActivity>   genActivityOrder(List<OrderInfo> orderInfoList , Boolean ifClear){
        Date date = ParamUtil.checkDate(mockDate);

        if(ifClear){
            remove(new QueryWrapper<OrderDetailActivity>());
        }


        List<ActivitySku> activitySkuList = activitySkuService.list(new QueryWrapper<>());
        List<ActivityRule> activityRuleList  = activityRuleService.list(new QueryWrapper<>());

        List<OrderDetailActivity> allOrderactivityList =new ArrayList<>();


        int orderCount=0;
        for (OrderInfo orderInfo : orderInfoList) {
            // 检查每个订单里是否有对应的活动商品 如果有随机进行优惠
            List<OrderDetailActivity> curOrderactivityList =new ArrayList<>();

            List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
             for (OrderDetail orderDetail : orderDetailList) {
                for (ActivitySku activitySku : activitySkuList) {
                   if( orderDetail.getSkuId().equals(activitySku.getSkuId())) {
                       curOrderactivityList.add ( OrderDetailActivity.builder().skuId(orderDetail.getSkuId()).orderDetail(orderDetail).orderInfo(orderInfo).createTime(date).activityId(activitySku.getActivityId()).build() ) ;
                   }
                }
            }
             if(curOrderactivityList.size()>0){
                 List<OrderDetailActivity> matchedOrderDetailActivitieList = matchRule(  orderInfo, curOrderactivityList, activityRuleList);
                 if(matchedOrderDetailActivitieList.size()>0){
                     orderCount++;
                     allOrderactivityList.addAll(matchedOrderDetailActivitieList);
                     orderInfo.sumTotalAmount();
                 }

             }

        }



        log.warn("共有"+orderCount +"订单参与活动条");
        return allOrderactivityList;


    }


    private  List<OrderDetailActivity>   matchRule(OrderInfo orderInfo,List<OrderDetailActivity> activityOrderList,List<ActivityRule>  activityRuleList){
        List<OrderDetailActivity> matchedActivityOrderList = new ArrayList<>();
        Map<Long ,OrderActivitySum> orderActivitySumMap=genOrderActivitySumList(activityOrderList);
        for (OrderActivitySum orderActivitySum : orderActivitySumMap.values()) {
            ActivityRule matchedRule=null;
            for (ActivityRule activityRule : activityRuleList) {
                if(orderActivitySum.getActivityId().equals(activityRule.getActivityId()) ){
                    if(matchedRule!=null && activityRule.getBenefitLevel()<matchedRule.getBenefitLevel()){
                        continue;
                    }
                    if(activityRule.getActivityType().equals(GmallConstant.ACTIVITY_RULE_TYPE_MJ )&&orderActivitySum.getOrderDetailAmountSum().compareTo(activityRule.getConditionAmount())>=0){
                        matchedRule=activityRule;
                    }else if(activityRule.getActivityType().equals(GmallConstant.ACTIVITY_RULE_TYPE_ML )&&orderActivitySum.getSkuNumSum().compareTo(activityRule.getConditionNum())>=0){
                        matchedRule=activityRule;
                    }

                }
            }
            if(matchedRule!=null){
                List<OrderDetailActivity> orderDetailActivityList = orderActivitySum.getOrderDetailActivityList();
                for (OrderDetailActivity orderDetailActivity : orderDetailActivityList) {
                    orderDetailActivity.setActivityRule(matchedRule);
                    orderDetailActivity.setActivityRuleId(matchedRule.getId());
                    matchedActivityOrderList.add(orderDetailActivity);
                }
                calOrderActivityReduceAmount(orderInfo,orderActivitySum,matchedRule);
            }
        }

        return matchedActivityOrderList;


    }



    @Data
    @AllArgsConstructor
    class OrderActivitySum  {
        Long activityId=0L;
        BigDecimal  orderDetailAmountSum;
        Long  skuNumSum=0L;
        List<OrderDetailActivity> orderDetailActivityList=new ArrayList<>();

    }

    /**
     *  目的是获得以参加的活动为单位的聚类，以便批量参加某个活动的商品是否达到要求
     * @param activityOrderList
     * @return
     */
    private  Map<Long,OrderActivitySum> genOrderActivitySumList(List<OrderDetailActivity> activityOrderList){
        Map<Long ,OrderActivitySum> orderActivitySumMap=new HashMap<>();
        for (OrderDetailActivity orderDetailActivity : activityOrderList) {
            OrderActivitySum orderActivitySum = orderActivitySumMap.get(orderDetailActivity.getActivityId());
            if(orderActivitySum!=null){
                OrderDetail orderDetail = orderDetailActivity.getOrderDetail();
                BigDecimal orderDetailAmount = orderDetail.getOrderPrice().multiply(BigDecimal.valueOf(orderDetail.getSkuNum()));
                orderActivitySum.setOrderDetailAmountSum(orderActivitySum.getOrderDetailAmountSum().add(orderDetailAmount));
                orderActivitySum.setSkuNumSum(orderActivitySum.getSkuNumSum()+orderDetail.getSkuNum() );
                orderActivitySum.getOrderDetailActivityList().add(orderDetailActivity);
            }else{
                OrderDetail orderDetail = orderDetailActivity.getOrderDetail();
                BigDecimal orderDetailAmount = orderDetail.getOrderPrice().multiply(BigDecimal.valueOf(orderDetail.getSkuNum()));
                OrderActivitySum orderActivitySumNew = new OrderActivitySum(orderDetailActivity.getActivityId(), orderDetailAmount,orderDetail.getSkuNum(), new ArrayList<>(Arrays.asList(orderDetailActivity)));
                orderActivitySumMap.put(orderDetailActivity.getActivityId(),orderActivitySumNew);
            }
        }
        return orderActivitySumMap;
    }

    private  void calOrderActivityReduceAmount(OrderInfo orderInfo,OrderActivitySum orderActivitySum,ActivityRule matchedRule){
        if(matchedRule.getActivityType().equals(GmallConstant.ACTIVITY_RULE_TYPE_MJ)){
            orderInfo.setActivityReduceAmount(orderInfo.getActivityReduceAmount().add(matchedRule.getBenefitAmount()));

            List<OrderDetailActivity> orderDetailActivityList = orderActivitySum.getOrderDetailActivityList();
            BigDecimal splitActivityAmountSum=BigDecimal.ZERO;
            for (int i = 0; i < orderDetailActivityList.size(); i++) {
                OrderDetailActivity orderDetailActivity = orderDetailActivityList.get(i);

                if(i<orderDetailActivityList.size()-1){
                    BigDecimal orderPrice = orderDetailActivity.getOrderDetail().getOrderPrice();
                    BigDecimal skuNum = BigDecimal.valueOf( orderDetailActivity.getOrderDetail().getSkuNum());
                    BigDecimal splitDetailAmount = orderPrice.multiply(skuNum);
                    //     分摊活动金额/活动总金额 =   订单明细金额/活动涉及订单金额
                    //移项 分摊活动金额= 活动总金额*订单明细金额 /活动涉及订单金额
                    BigDecimal activityReduceAmount = orderInfo.getActivityReduceAmount();
                    BigDecimal splitActivityAmount = activityReduceAmount.multiply(splitDetailAmount).divide(orderActivitySum.getOrderDetailAmountSum(),2,RoundingMode.HALF_UP );
                    orderDetailActivity.getOrderDetail().setSplitActivityAmount(splitActivityAmount);
                    splitActivityAmountSum=splitActivityAmountSum.add(splitActivityAmount);
                }else{
                    BigDecimal splitActivityAmount=orderInfo.getActivityReduceAmount().subtract(splitActivityAmountSum);
                    orderDetailActivity.getOrderDetail().setSplitActivityAmount(splitActivityAmount );
                }
            }


        }else if (matchedRule.getActivityType().equals(GmallConstant.ACTIVITY_RULE_TYPE_ML)){
            BigDecimal reduceAmount = BigDecimal.ZERO;;
            List<OrderDetailActivity> orderDetailActivityList = orderActivitySum.getOrderDetailActivityList();

            for (OrderDetailActivity orderDetailActivity : orderDetailActivityList) {
                BigDecimal orderPrice = orderDetailActivity.getOrderDetail().getOrderPrice();
                BigDecimal skuNum = BigDecimal.valueOf( orderDetailActivity.getOrderDetail().getSkuNum());
                BigDecimal splitDetailAmount = orderPrice.multiply(skuNum);
                BigDecimal splitActivityAmount = splitDetailAmount.multiply(matchedRule.getBenefitDiscount(), new MathContext(2, RoundingMode.HALF_UP));
                orderDetailActivity.getOrderDetail().setSplitActivityAmount(splitActivityAmount);
                reduceAmount.add(splitActivityAmount);
            }

            orderInfo.setActivityReduceAmount(reduceAmount);
        }

    }


    public  void  saveActivityOrderList( List<OrderDetailActivity> activityOrderList){
        saveBatch(activityOrderList,100);
    }

}
