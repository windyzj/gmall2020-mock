package com.atguigu.gmall2020.mock.db.service.impl;

import com.atguigu.gmall2020.mock.db.bean.CartInfo;
import com.atguigu.gmall2020.mock.db.bean.OrderInfo;
import com.atguigu.gmall2020.mock.db.bean.SkuInfo;
import com.atguigu.gmall2020.mock.db.bean.UserInfo;
import com.atguigu.gmall2020.mock.db.constant.GmallConstant;
import com.atguigu.gmall2020.mock.db.mapper.CartInfoMapper;
import com.atguigu.gmall2020.mock.db.mapper.SkuInfoMapper;
import com.atguigu.gmall2020.mock.db.mapper.UserInfoMapper;
import com.atguigu.gmall2020.mock.db.service.CartInfoService;
import com.atguigu.gmall2020.mock.db.service.SkuInfoService;
import com.atguigu.gmall2020.mock.db.service.UserInfoService;
import com.atguigu.gmall2020.mock.db.util.ParamUtil;
import com.atguigu.gmall2020.mock.db.util.RandomNum;
import com.atguigu.gmall2020.mock.db.util.RandomNumString;
import com.atguigu.gmall2020.mock.db.util.RandomOptionGroup;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * <p>
 * 购物车表 用户登录系统时更新冗余 服务实现类
 * </p>
 *
 * @author zc
 * @since 2020-02-24
 */
@Service
@Slf4j
public class CartInfoServiceImpl extends ServiceImpl<CartInfoMapper, CartInfo> implements CartInfoService {
    @Autowired
    SkuInfoService skuInfoService;

    @Autowired
    UserInfoService userInfoService;


    @Value("${mock.cart.user-rate:50}")
    String cartUserRate;

    @Value("${mock.cart.max-sku-count:8}")
    String  maxSkuCount;

    @Value("${mock.cart.max-sku-num:3}")
    String  maxSkuNum;

    @Value("${mock.date}")
    String mockDate;

    @Value("${mock.cart.source-type-rate}")
    String sourceTypeRate;

    public void  genCartInfo( boolean ifClear){

        Date date = ParamUtil.checkDate(mockDate);
        RandomOptionGroup cartUserRateDice = new RandomOptionGroup(this.cartUserRate);

        if(ifClear){
            remove(new QueryWrapper<>());
        }
        List<SkuInfo> skuInfoList  = skuInfoService.list(new QueryWrapper<SkuInfo>());

        List<UserInfo> userInfoList = userInfoService.list(new QueryWrapper<>());
        List<CartInfo> cartInfoExistsList = this.list(new QueryWrapper<>());

        List<CartInfo> cartInfoList= new ArrayList<>();
        HashSet<String> userIdAndSkuIdSet = new HashSet<>();
        for (UserInfo userInfo : userInfoList) {
              if(cartUserRateDice.getRandBoolValue()){
                  int skuCount = RandomNum.getRandInt(1, ParamUtil.checkCount(maxSkuCount));
                  Set skuSet=new HashSet();
                  for (int i = 1; i <=skuCount ; i++) {
                      int skuIndex = RandomNum.getRandInt(0, skuInfoList.size() - 1);
                      boolean noExists = skuSet.add(skuIndex);
                      if(noExists){
                          SkuInfo skuInfo = skuInfoList.get(skuIndex);
                          CartInfo cartInfo = initCartInfo(skuInfo, userInfo.getId(), date);
                          cartInfo  = checkAndMergeCart(cartInfo, cartInfoExistsList);
                          cartInfoList.add(cartInfo);
                      }

                  }

              }
        }



        log.warn("共生成购物车"+cartInfoList.size()+"条");
        saveOrUpdateBatch(cartInfoList,100);
    }

    private  CartInfo checkAndMergeCart(CartInfo cartInfo,List<CartInfo> cartInfoExistsList){
        for (CartInfo cartInfoExists : cartInfoExistsList) {
            if(cartInfo.getUserId().equals(cartInfoExists.getUserId())&&cartInfo.getSkuId().equals(cartInfoExists.getSkuId())){
                cartInfoExists.setSkuNum(cartInfo.getSkuNum()+cartInfoExists.getSkuNum());
                return  cartInfoExists;
            }
        }
        return cartInfo;
    }

    public  CartInfo initCartInfo( SkuInfo skuInfo,Long userId,Date date){
        Integer skuCount = ParamUtil.checkCount(maxSkuNum);

        Integer[] sourceTypeRateArray = ParamUtil.checkRate(this.sourceTypeRate,4);
        RandomOptionGroup sourceTypeGroup = RandomOptionGroup.builder().add(GmallConstant.SOURCE_TYPE_QUREY, sourceTypeRateArray[0])
                .add(GmallConstant.SOURCE_TYPE_PROMOTION, sourceTypeRateArray[1])
                .add(GmallConstant.SOURCE_TYPE_AUTO_RECOMMEND, sourceTypeRateArray[2])
                .add(GmallConstant.SOURCE_TYPE_ACTIVITY, sourceTypeRateArray[3]).build();
        String sourceType = sourceTypeGroup.getRandStringValue();


        CartInfo cartInfo = new CartInfo();
        cartInfo.setCartPrice(skuInfo.getPrice());
        cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
        cartInfo.setSkuId(skuInfo.getId());
        cartInfo.setSkuName(skuInfo.getSkuName());
        cartInfo.setUserId(userId);
        cartInfo.setIsOrdered(0);
        cartInfo.setSkuNum(RandomNum.getRandInt(1,skuCount)+0L);
        cartInfo.setCreateTime(date);
        cartInfo.setSourceType(sourceType);
        if(sourceType.equals(GmallConstant.SOURCE_TYPE_PROMOTION)){
            cartInfo.setSourceId(RandomNum.getRandInt(10,100)+0L);
        }else if (sourceType.equals(GmallConstant.SOURCE_TYPE_ACTIVITY)){
            cartInfo.setSourceId(RandomNum.getRandInt(1,2)+0L);
        }

        return cartInfo;
    }


}
