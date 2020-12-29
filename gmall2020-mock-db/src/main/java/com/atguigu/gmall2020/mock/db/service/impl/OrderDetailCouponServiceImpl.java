package com.atguigu.gmall2020.mock.db.service.impl;

import com.atguigu.gmall2020.mock.db.bean.CouponInfo;
import com.atguigu.gmall2020.mock.db.bean.OrderDetailCoupon;
import com.atguigu.gmall2020.mock.db.mapper.CouponInfoMapper;
import com.atguigu.gmall2020.mock.db.mapper.OrderDetailCouponMapper;
import com.atguigu.gmall2020.mock.db.service.CouponInfoService;
import com.atguigu.gmall2020.mock.db.service.OrderDetailCouponService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 优惠券表 服务实现类
 * </p>
 *
 * @author zhangchen
 * @since 2020-02-26
 */
@Service
public class OrderDetailCouponServiceImpl extends ServiceImpl<OrderDetailCouponMapper, OrderDetailCoupon> implements OrderDetailCouponService {

}
