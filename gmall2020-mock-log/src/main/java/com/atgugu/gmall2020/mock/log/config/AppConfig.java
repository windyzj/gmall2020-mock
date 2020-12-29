package com.atgugu.gmall2020.mock.log.config;


import com.atguigu.gmall2020.mock.db.util.ParamUtil;
import lombok.Data;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Component;

import java.util.Date;

@Configuration
public class AppConfig {




  //public static Date date=new Date();

   public static  String mock_date="";

   public static Integer mock_count=1000;

   public static String mock_type="log";

   public static String mock_url="http://localhost:80";

   public static Integer max_mid=500;

   public static Integer max_uid=500;

   public static Integer max_coupon_id=3;

   public static  Integer max_sku_id=10 ;


   public static  Integer page_during_max_ms=20000 ;


    public static  Integer error_rate=3;


    public static  Integer log_sleep=100;

    public static  Integer if_favor_rate =30;

    public static  Integer if_favor_cancel_rate =10;

    public static Integer if_cart_rate =10;

    public static  Integer if_cart_add_num_rate =10;

    public static  Integer if_cart_minus_num_rate =10;

    public static  Integer if_cart_rm_rate =10;

    public static  Integer if_add_address =15;

    public static  Integer if_get_coupon =25;

    public static Integer  max_display_count=10;

    public static Integer  min_display_count=4;

    public static Integer  max_activity_count=2;
    public static Integer  max_pos_id=5;


    public static Integer[]  sourceTypeRate;


    public static String[]  searchKeywords;

    public static String kafka_server;
    public static String kafka_topic;





    @Value("${mock.type}")
    public   void setMock_type(String mock_type) {
        AppConfig.mock_type = mock_type;
    }

    @Value("${mock.url}")
    public   void setMock_url(String mock_url) {
        AppConfig.mock_url = mock_url;
    }

    @Value("${mock.kafka-server}")
    public   void setKafka_server(String kafka_server) {
        AppConfig.kafka_server = kafka_server;
    }
    @Value("${mock.kafka-topic}")
    public   void setKafka_topic(String kafka_topic) {
        AppConfig.kafka_topic = kafka_topic;
    }


    @Value("${mock.startup.count}")
    public   void setMock_count(String mock_count) {
        AppConfig.mock_count =  ParamUtil.checkCount(mock_count)  ;
    }
    @Value("${mock.max.mid}")
    public   void setMax_mid(String  max_mid) {
        AppConfig.max_mid = ParamUtil.checkCount(max_mid);
    }
    @Value("${mock.max.uid}")
    public   void setMax_uid(String max_uid) {
        AppConfig.max_uid = ParamUtil.checkCount(max_uid);
    }
    @Value("${mock.max.sku-id}")
    public   void setMax_sku_id(String max_sku_id) {
        AppConfig.max_sku_id = ParamUtil.checkCount(max_sku_id);
    }
    @Value("${mock.page.during-time-ms}")
    public   void setPage_during_max_ms(String page_during_max_ms) {
        AppConfig.page_during_max_ms = ParamUtil.checkCount(page_during_max_ms);
    }
     @Value("${mock.error.rate}")
    public   void setError_rate(String error_rate) {
        AppConfig.error_rate = ParamUtil.checkRatioNum( error_rate);
    }
    @Value("${mock.log.sleep}")
    public   void setLog_sleep(String log_sleep) {
        AppConfig.log_sleep = ParamUtil.checkCount(log_sleep);
    }

    public static void setIf_favor_rate(Integer if_favor_rate) {
        AppConfig.if_favor_rate = if_favor_rate;
    }

    public static void setIf_favor_cancel_rate(Integer if_favor_cancel_rate) {
        AppConfig.if_favor_cancel_rate = if_favor_cancel_rate;
    }

    public static void setIf_cart_rate(Integer if_cart_rate) {
        AppConfig.if_cart_rate = if_cart_rate;
    }

    public static void setIf_cart_add_num_rate(Integer if_cart_add_num_rate) {
        AppConfig.if_cart_add_num_rate = if_cart_add_num_rate;
    }

    public static void setIf_cart_minus_num_rate(Integer if_cart_minus_num_rate) {
        AppConfig.if_cart_minus_num_rate = if_cart_minus_num_rate;
    }

    public static void setIf_cart_rm_rate(Integer if_cart_rm_rate) {
        AppConfig.if_cart_rm_rate = if_cart_rm_rate;
    }

    public static void setIf_add_address(Integer if_add_address) {
        AppConfig.if_add_address = if_add_address;
    }

    public static void setMax_display_count(Integer max_display_count) {
        AppConfig.max_display_count = max_display_count;
    }

    public static void setMin_display_count(Integer min_display_count) {
        AppConfig.min_display_count = min_display_count;
    }

    public static void setMax_activity_count(Integer max_activity_count) {
        AppConfig.max_activity_count = max_activity_count;
    }


//    public     void setMockDate(String  mockDate) {
//        AppConfig.date = ParamUtil.checkDate(mockDate);
//
//    }

    @Value("${mock.date}")
    public     void setMockDate(String  mockDate) {
        AppConfig.mock_date =  mockDate ;

    }

    @Value("${mock.detail.source-type-rate}")
    public     void setSourceType(String  sourceTypeRate) {
        Integer[] sourceTypeRateArray = ParamUtil.checkRate( sourceTypeRate,4);
        AppConfig.sourceTypeRate = sourceTypeRateArray;

    }

    @Value("${mock.search.keyword}")
    public void setSearchKeywords(String keywords){
        AppConfig.searchKeywords= ParamUtil.checkArray(keywords);
    }


    @Value("${mock.if_get_coupon_rate}")
    public void setIf_get_coupon(String if_get_coupon_ratio){
        AppConfig.if_get_coupon= ParamUtil.checkRatioNum( if_get_coupon_ratio);
    }

    @Value("${mock.max.coupon-id}")
    public   void setMaxCouponId(String couponId) {
        AppConfig.max_coupon_id = ParamUtil.checkCount(couponId);
    }





}
