package com.atguigu.gmall2020.mock.db.service.impl;

import com.atguigu.gmall2020.mock.db.bean.OrderInfo;
import com.atguigu.gmall2020.mock.db.bean.PaymentInfo;
import com.atguigu.gmall2020.mock.db.constant.GmallConstant;
import com.atguigu.gmall2020.mock.db.mapper.PaymentInfoMapper;
import com.atguigu.gmall2020.mock.db.service.CouponUseService;
import com.atguigu.gmall2020.mock.db.service.OrderInfoService;
import com.atguigu.gmall2020.mock.db.service.OrderStatusLogService;
import com.atguigu.gmall2020.mock.db.service.PaymentInfoService;
import com.atguigu.gmall2020.mock.db.util.ParamUtil;
import com.atguigu.gmall2020.mock.db.util.RanOpt;
import com.atguigu.gmall2020.mock.db.util.RandomNumString;
import com.atguigu.gmall2020.mock.db.util.RandomOptionGroup;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.atguigu.gmall2020.mock.db.constant.GmallConstant.*;

/**
 * <p>
 * 支付流水表 服务实现类
 * </p>
 *
 * @author zc
 * @since 2020-02-24
 */
@Service
@Slf4j
public class PaymentInfoServiceImpl extends ServiceImpl<PaymentInfoMapper, PaymentInfo> implements PaymentInfoService {



    @Autowired
    OrderInfoService orderInfoService;

    @Autowired
    OrderStatusLogService orderStatusLogService;

    @Autowired
    CouponUseService couponUseService;


    @Value("${mock.date}")
    String mockDate;

    @Value("${mock.payment.rate:70}")
    String ifPaymentRate;

    @Value("${mock.payment.payment-type:30:60:10}")
    String paymentTypeRate;



    public void  genPayments(Boolean ifClear){
        Date date = ParamUtil.checkDate(mockDate);
        Integer ifPaymentWeight = ParamUtil.checkRatioNum(this.ifPaymentRate);
        Integer[] paymentTypeRateWeight = ParamUtil.checkRate(this.paymentTypeRate,3);

        RandomOptionGroup<Boolean> ifPayment=new RandomOptionGroup(new RanOpt(true,ifPaymentWeight),new RanOpt(false,100-ifPaymentWeight));
        RandomOptionGroup<String>  paymentOptionGroup=new RandomOptionGroup(new RanOpt(PAYMENT_TYPE_ALIPAY,paymentTypeRateWeight[0]),new RanOpt(PAYMENT_TYPE_WECHAT,paymentTypeRateWeight[1]),new RanOpt(PAYMENT_TYPE_UNION,paymentTypeRateWeight[2]));


        if(ifClear){
            remove(new QueryWrapper<PaymentInfo>());
        }


        QueryWrapper<OrderInfo> orderInfoQueryWrapper = new QueryWrapper<>();
        orderInfoQueryWrapper.eq("order_status",GmallConstant.ORDER_STATUS_UNPAID);
        orderInfoQueryWrapper.orderByAsc("id");
        List<OrderInfo> orderInfoList = orderInfoService.listWithDetail(orderInfoQueryWrapper);
        List<PaymentInfo> paymentList=new ArrayList();

        if(orderInfoList.size()==0){
            System.out.println("没有需要支付的订单！！ ");
            return;
        }
        for (OrderInfo orderInfo : orderInfoList) {
           if( ifPayment.getRandBoolValue() ){
               PaymentInfo paymentInfo = new PaymentInfo();
               paymentInfo.setOrderId(orderInfo.getId());
               paymentInfo.setTotalAmount(orderInfo.getTotalAmount());
               paymentInfo.setUserId(orderInfo.getUserId());
               paymentInfo.setOutTradeNo(orderInfo.getOutTradeNo());
               paymentInfo.setTradeNo(RandomNumString.getRandNumString(1,9,34,""));
               paymentInfo.setPaymentType(paymentOptionGroup.getRandStringValue());
               paymentInfo.setSubject(orderInfo.getTradeBody());
               paymentInfo.setCreateTime(date);
               paymentInfo.setCallbackTime(DateUtils.addSeconds(date,20) );
               paymentList.add(paymentInfo);

               orderInfo.setOrderStatus(GmallConstant.ORDER_STATUS_PAID);
           }
        }

         couponUseService.usedCoupon(orderInfoList);

         orderInfoService.updateOrderStatus(orderInfoList);
        log.warn("共有"+paymentList.size()+"订单完成支付");
         saveBatch(paymentList,100);



    }
}
