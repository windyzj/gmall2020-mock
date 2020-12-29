package com.atgugu.gmall2020.mock.log.bean;

import com.atgugu.gmall2020.mock.log.config.AppConfig;
import com.atgugu.gmall2020.mock.log.enums.ItemType;
import com.atgugu.gmall2020.mock.log.enums.PageId;
import com.atgugu.gmall2020.mock.log.enums.ActionId;
import com.atguigu.gmall2020.mock.db.util.RandomNum;
import com.atguigu.gmall2020.mock.db.util.RandomOptionGroup;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
public class AppAction {

     public AppAction( ActionId action_id,ItemType item_type,String item ){
          this.action_id=action_id;
          this.item_type=item_type;
          this.item=item;

     }


     ActionId action_id;

     ItemType item_type;

     String item ;

     String extend1;

     String extend2;

     Long ts;


     public  static  List<AppAction> buildList(AppPage appPage,Long startTs,Integer duringTime){


          List<AppAction> actionList=new ArrayList();
          Boolean ifFavor=  RandomOptionGroup.builder().add(true ,AppConfig.if_favor_rate).add(false,100-AppConfig.if_favor_rate).build().getRandBoolValue();
          Boolean ifCart=  RandomOptionGroup.builder().add(true ,AppConfig.if_cart_rate).add(false,100-AppConfig.if_cart_rate).build().getRandBoolValue();
          Boolean ifCartAddNum=RandomOptionGroup.builder().add(true ,AppConfig.if_cart_add_num_rate).add(false,100-AppConfig.if_cart_add_num_rate).build().getRandBoolValue();
          Boolean ifCartMinusNum=RandomOptionGroup.builder().add(true ,AppConfig.if_cart_minus_num_rate).add(false,100-AppConfig.if_cart_minus_num_rate).build().getRandBoolValue();
          Boolean ifCartRm=RandomOptionGroup.builder().add(true ,AppConfig.if_cart_rm_rate).add(false,100-AppConfig.if_cart_rm_rate).build().getRandBoolValue();
          Boolean ifGetCouponRm=RandomOptionGroup.builder().add(true ,AppConfig.if_get_coupon).add(false,100-AppConfig.if_get_coupon).build().getRandBoolValue();
          if(appPage.page_id== PageId.good_detail){

               if(ifFavor){
                    AppAction favorAction = new AppAction(ActionId.favor_add, appPage.item_type, appPage.item);
                    actionList.add(favorAction);
               }
               if(ifCart){
                    AppAction cartAction = new AppAction(ActionId.cart_add, appPage.item_type, appPage.item);
                    actionList.add(cartAction);

               }
               if(ifGetCouponRm){
                    int couponId = RandomNum.getRandInt(1, AppConfig.max_coupon_id);
                    AppAction couponAction = new AppAction(ActionId.get_coupon, ItemType.coupon_id, String.valueOf(couponId));
                    actionList.add(couponAction);
               }

          }
          else if(appPage.page_id==PageId.cart){

               if(ifCartAddNum){
                    int skuId = RandomNum.getRandInt(1, AppConfig.max_sku_id);
                    AppAction favorAction = new AppAction(ActionId.cart_add_num, ItemType.sku_id, skuId+"");
                    actionList.add(favorAction);
               }
               if(ifCartMinusNum){
                    int skuId = RandomNum.getRandInt(1, AppConfig.max_sku_id);
                    AppAction favorAction = new AppAction(ActionId.cart_minus_num, ItemType.sku_id, skuId+"");
                    actionList.add(favorAction);
               }
               if(ifCartRm){
                    int skuId = RandomNum.getRandInt(1, AppConfig.max_sku_id);
                    AppAction favorAction = new AppAction(ActionId.cart_remove, ItemType.sku_id, skuId+"");
                    actionList.add(favorAction);
               }

          }
          else if(appPage.page_id==PageId.trade){
               Boolean ifAddAddress=RandomOptionGroup.builder().add(true ,AppConfig.if_add_address).add(false,100-AppConfig.if_add_address).build().getRandBoolValue();
               if(ifAddAddress){
                    AppAction appAction = new AppAction(ActionId.trade_add_address, null, null);
                    actionList.add(appAction);
               }

          }
          else if(appPage.page_id==PageId.favor){
               Boolean ifFavorCancel=RandomOptionGroup.builder().add(true ,AppConfig.if_favor_cancel_rate).add(false,100-AppConfig.if_favor_cancel_rate).build().getRandBoolValue();
               int skuId = RandomNum.getRandInt(1, AppConfig.max_sku_id);
               for (int i = 0; i < 3; i++) {
                    if(ifFavorCancel){
                         AppAction appAction = new AppAction(ActionId.favor_canel, ItemType.sku_id, skuId+i+"");
                         actionList.add(appAction);
                    }
               }

          }


          int size = actionList.size();
          long avgActionTime = duringTime / (size+1);
          for (int i = 1; i <= actionList.size(); i++) {
               AppAction appAction = actionList.get(i-1);
               appAction.setTs(startTs+i*avgActionTime);
          }
          return actionList;


     }





}
