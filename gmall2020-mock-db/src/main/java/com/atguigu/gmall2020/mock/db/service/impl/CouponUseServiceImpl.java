package com.atguigu.gmall2020.mock.db.service.impl;

import com.atguigu.gmall2020.mock.db.bean.*;
import com.atguigu.gmall2020.mock.db.constant.GmallConstant;
import com.atguigu.gmall2020.mock.db.mapper.CouponUseMapper;
import com.atguigu.gmall2020.mock.db.service.CouponInfoService;
import com.atguigu.gmall2020.mock.db.service.CouponUseService;
import com.atguigu.gmall2020.mock.db.service.SkuInfoService;
import com.atguigu.gmall2020.mock.db.service.UserInfoService;
import com.atguigu.gmall2020.mock.db.util.ParamUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * <p>
 * 优惠券领用表 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2020-02-26
 */
@Service
@Slf4j
public class CouponUseServiceImpl extends ServiceImpl<CouponUseMapper, CouponUse> implements CouponUseService {

    @Autowired
    CouponInfoService couponInfoService;

    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    UserInfoService userInfoService;



    @Autowired
    CouponUseMapper couponUseMapper;

    @Value("${mock.date}")
    String mockDate;

    @Value("${mock.coupon.user-count:1000}")
    String userCount;


    public void genCoupon(  Boolean ifClear) {
        Date date = ParamUtil.checkDate(mockDate);
        Integer userCount = ParamUtil.checkCount(this.userCount);
        if (ifClear) {
            remove(new QueryWrapper<>());

        }
        QueryWrapper<UserInfo> userInfoQueryWrapper = new QueryWrapper<>();
        userInfoQueryWrapper.last("limit " + userCount);
        Integer userTotal = userInfoService.count(userInfoQueryWrapper);


        List<CouponInfo> couponInfoList = couponInfoService.list(new QueryWrapper<>());
          userCount = Math.min(userTotal, userCount);
        List<CouponUse> couponUseList = new ArrayList<>();
        for (CouponInfo couponInfo : couponInfoList) {
            for (int userId = 1; userId <= userCount; userId++) {
                CouponUse couponUse = new CouponUse();
                couponUse.setCouponStatus(GmallConstant.COUPON_STATUS_UNUSED);
                couponUse.setGetTime(date);
                couponUse.setExpireTime(couponInfo.getExpireTime());
                couponUse.setUserId(userId+0L);
                couponUse.setCouponId(couponInfo.getId());
                couponUseList.add(couponUse);
            }
        }
         log.warn("共优惠券"+couponUseList.size()+"张");
        saveBatch(couponUseList);


    }

    public Pair<List<CouponUse>,List<OrderDetailCoupon>>  usingCoupon(List<OrderInfo> orderInfoList) {
        List<CouponUse> couponUseListForUpdate = new ArrayList<>();


        Date date = ParamUtil.checkDate(mockDate);

        List<CouponUse> unUsedCouponList = couponUseMapper.selectUnusedCouponUseListWithInfo();

        List<SkuInfo> skuInfoList = skuInfoService.list(new QueryWrapper<>());

        List<OrderDetailCoupon> allOrderDetailCouponList = new ArrayList<>();


        int orderCount = 0;
        for (OrderInfo orderInfo : orderInfoList) {
            // 检查每个订单里是否有对应的活动商品 如果有随机进行优惠
            List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
            List<OrderDetailCoupon> curOrderDetailCouponList = new ArrayList<>();

            for (OrderDetail orderDetail : orderDetailList) {
                for (CouponUse unUsedCouponUse : unUsedCouponList) {
                    if (!orderInfo.getUserId().equals(unUsedCouponUse.getUserId())) {
                        continue;
                    }
                    orderDetail.setSkuInfo(skuInfoService.getSkuInfoById(skuInfoList,orderDetail.getSkuId()));
                    boolean isMatched = matchCouponByRange(orderDetail, unUsedCouponUse);
                    if (isMatched) {
                        OrderDetailCoupon orderDetailCoupon = OrderDetailCoupon.builder().skuId(orderDetail.getSkuId()).createTime(date).orderInfo(orderInfo)
                                .orderDetail(orderDetail)
                                .couponId(unUsedCouponUse.getCouponInfo().getId())
                                .couponInfo(unUsedCouponUse.getCouponInfo())
                                .couponUse(unUsedCouponUse)
                                .build();
                        curOrderDetailCouponList.add(orderDetailCoupon);
                    }

                }
            }

            if (curOrderDetailCouponList.size() > 0) {
                List<OrderDetailCoupon> matchedOrderDetailCouponList = matchRule(orderInfo, curOrderDetailCouponList);
                if(matchedOrderDetailCouponList!=null&&matchedOrderDetailCouponList.size()>0){
                    for (OrderDetailCoupon orderDetailCoupon : matchedOrderDetailCouponList) {
                        CouponUse couponUse = orderDetailCoupon.getCouponUse();
                        couponUse.setOrderInfo(orderInfo);
                        couponUse.setCouponStatus(GmallConstant.COUPON_STATUS_USING);
                        couponUse.setUsingTime(date);
                        couponUseListForUpdate.add(couponUse);
                    }
                    orderCount++;
                    allOrderDetailCouponList.addAll(matchedOrderDetailCouponList);
                    orderInfo.sumTotalAmount();
                }

            }

        }

        log.warn("共有" + orderCount + "订单参与活动条");

        return  new ImmutablePair<>(couponUseListForUpdate,allOrderDetailCouponList)   ;
    }

    public  void  saveCouponUseList( List<CouponUse> couponUseList){
        saveBatch(couponUseList,100);
    }




    @Data
    @AllArgsConstructor
    class OrderCouponSum  {
        private Long couponId=0L;

        private BigDecimal  orderDetailAmountSum=BigDecimal.ZERO;
        private  Long  skuNumSum=0L;
        private List<OrderDetailCoupon> orderDetailCouponList=new ArrayList<>();
        private CouponInfo couponInfo;
        private BigDecimal  reduceAmount=BigDecimal.ZERO;

    }

    /**
     *  目的是获得以适用的优惠券为单位的聚类，以便批量参加某个活动的商品是否达到要求
     * @param orderDetailCouponList
     * @return
     */
    private  Map<Long, OrderCouponSum> genOrderCouponSumMap(List<OrderDetailCoupon>  orderDetailCouponList){
        Map<Long ,  OrderCouponSum> orderCouponSumMap=new HashMap<>();
        for (OrderDetailCoupon orderDetailCoupon : orderDetailCouponList) {
            OrderCouponSum orderCouponSum = orderCouponSumMap.get(orderDetailCoupon.getCouponId());
            if(orderCouponSum!=null){
                OrderDetail orderDetail = orderDetailCoupon.getOrderDetail();
                BigDecimal orderDetailAmount = orderDetail.getOrderPrice().multiply(BigDecimal.valueOf(orderDetail.getSkuNum()));
                orderCouponSum.setOrderDetailAmountSum(orderCouponSum.getOrderDetailAmountSum().add(orderDetailAmount));
                orderCouponSum.setSkuNumSum(orderCouponSum.getSkuNumSum()+orderDetail.getSkuNum() );
                orderCouponSum.getOrderDetailCouponList().add(orderDetailCoupon);
            }else{
                OrderDetail orderDetail = orderDetailCoupon.getOrderDetail();
                BigDecimal orderDetailAmount = orderDetail.getOrderPrice().multiply(BigDecimal.valueOf(orderDetail.getSkuNum()));
                OrderCouponSum orderCouponSumNew = new OrderCouponSum(orderDetailCoupon.getCouponId(), orderDetailAmount,orderDetail.getSkuNum(), new ArrayList<>(Arrays.asList(orderDetailCoupon)),orderDetailCoupon.getCouponInfo(),BigDecimal.ZERO);
                orderCouponSumMap.put(orderDetailCoupon.getCouponId(),orderCouponSumNew);
            }
        }
        return orderCouponSumMap;
    }

    // 取金额优惠最大的且符合要求的优惠券涉及的订单
    private  List<OrderDetailCoupon>   matchRule(OrderInfo orderInfo,List<OrderDetailCoupon> orderDetailCouponList ) {
        Map<Long, OrderCouponSum> orderCouponSumMap = genOrderCouponSumMap(orderDetailCouponList);
        OrderCouponSum maxAmountCouponSum = null;
        for (OrderCouponSum orderCouponSum : orderCouponSumMap.values()) {
            if (orderCouponSum.getCouponInfo().getCouponType().equals(GmallConstant.COUPON_TYPE_MJ) && orderCouponSum.orderDetailAmountSum.compareTo(orderCouponSum.getCouponInfo().getConditionAmount()) >= 0) {
                orderCouponSum.setReduceAmount(orderCouponSum.getCouponInfo().getBenefitAmount());
                if (maxAmountCouponSum == null || orderCouponSum.getReduceAmount().compareTo(maxAmountCouponSum.getReduceAmount()) > 0) {
                    maxAmountCouponSum = orderCouponSum;
                }
            } else if (orderCouponSum.getCouponInfo().getCouponType().equals(GmallConstant.COUPON_TYPE_ML) && orderCouponSum.getSkuNumSum().compareTo(orderCouponSum.getCouponInfo().getConditionNum()) >= 0) {
                orderCouponSum.setReduceAmount(orderCouponSum.getOrderDetailAmountSum().multiply(orderCouponSum.getCouponInfo().getBenefitDiscount()));
                if (maxAmountCouponSum == null || orderCouponSum.getReduceAmount().compareTo(maxAmountCouponSum.getReduceAmount()) > 0) {
                    maxAmountCouponSum = orderCouponSum;
                }
            }
        }
        if (maxAmountCouponSum != null) {
            List<OrderDetailCoupon> curOrderDetailCouponList = maxAmountCouponSum.getOrderDetailCouponList();
            if (maxAmountCouponSum.getCouponInfo().getCouponType().equals(GmallConstant.COUPON_TYPE_MJ)) {
                orderInfo.setCouponReduceAmount(maxAmountCouponSum.getReduceAmount());
                BigDecimal splitCouponAmountSum = BigDecimal.ZERO;
                for (int i = 0; i < curOrderDetailCouponList.size(); i++) {
                    OrderDetailCoupon orderDetailCoupon = curOrderDetailCouponList.get(i);
                    if (i < curOrderDetailCouponList.size() - 1) {
                        BigDecimal orderPrice = orderDetailCoupon.getOrderDetail().getOrderPrice();
                        BigDecimal skuNum = BigDecimal.valueOf(orderDetailCoupon.getOrderDetail().getSkuNum());
                        BigDecimal splitDetailAmount = orderPrice.multiply(skuNum);
                        //     分摊活动金额/活动总金额 =   订单明细金额/活动涉及订单金额
                        //移项 分摊活动金额= 活动总金额*订单明细金额 /活动涉及订单金额
                        BigDecimal couponReduceAmount = orderInfo.getCouponReduceAmount();
                        BigDecimal splitCouponAmount = couponReduceAmount.multiply(splitDetailAmount).divide(maxAmountCouponSum.getOrderDetailAmountSum(),2,RoundingMode.HALF_UP);
                        orderDetailCoupon.getOrderDetail().setSplitCouponAmount(splitCouponAmount);
                        splitCouponAmountSum=splitCouponAmountSum.add(splitCouponAmount);
                    } else {
                        BigDecimal splitCouponAmount = orderInfo.getCouponReduceAmount().subtract(splitCouponAmountSum);
                        orderDetailCoupon.getOrderDetail().setSplitCouponAmount(splitCouponAmount);
                    }
                }


            } else if (maxAmountCouponSum.getCouponInfo().getCouponType().equals(GmallConstant.COUPON_TYPE_ML)) {
                BigDecimal reduceAmount = BigDecimal.ZERO;
                ;
                for (OrderDetailCoupon orderDetailCoupon : curOrderDetailCouponList) {
                    BigDecimal orderPrice = orderDetailCoupon.getOrderDetail().getOrderPrice();
                    BigDecimal skuNum = BigDecimal.valueOf(orderDetailCoupon.getOrderDetail().getSkuNum());
                    BigDecimal splitDetailAmount = orderPrice.multiply(skuNum);
                    BigDecimal splitCouponAmount = splitDetailAmount.multiply(maxAmountCouponSum.couponInfo.getBenefitDiscount(), new MathContext(2, RoundingMode.HALF_UP));
                    orderDetailCoupon.getOrderDetail().setSplitCouponAmount(splitCouponAmount);
                    reduceAmount.add(splitCouponAmount);
                }

                orderInfo.setCouponReduceAmount(reduceAmount);
            }
            return maxAmountCouponSum.getOrderDetailCouponList();
        }

        return  null;

    }



    public   void  usedCoupon(List<OrderInfo> orderInfoList){
        Date date = ParamUtil.checkDate(mockDate);
        List<Long>  orderIdList=new ArrayList<>();
        for (OrderInfo orderInfo : orderInfoList) {
            orderIdList.add(orderInfo.getId());
        }
        CouponUse couponUse = new CouponUse();
        couponUse.setUsedTime(date);
        couponUse.setCouponStatus(GmallConstant.COUPON_STATUS_USED);
        update(couponUse,new QueryWrapper<CouponUse>().in("order_id",orderIdList));


    }

    public boolean  matchCouponByRange(OrderDetail orderDetail,CouponUse couponUse){
        List<CouponRange> couponRangeList = couponUse.getCouponRangeList();
        for (CouponRange couponRange : couponRangeList) {
            if(couponRange.getRangeType().equals(GmallConstant.COUPON_RANGE_TYPE_CATEGORY3)&&couponRange.getRangeId().equals(orderDetail.getSkuInfo().getCategory3Id())){
                return  true;
            }else if(couponRange.getRangeType().equals(GmallConstant.COUPON_RANGE_TYPE_TRADEMARK)&&couponRange.getRangeId().equals(orderDetail.getSkuInfo().getTmId())){
                return  true;
            }else if(couponRange.getRangeType().equals(GmallConstant.COUPON_RANGE_TYPE_SPU)&&couponRange.getRangeId().equals(orderDetail.getSkuInfo().getSpuId())) {
                return true;
            }
        }
        return  false;

    }





}
