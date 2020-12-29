package com.atguigu.gmall2020.mock.db.service.impl;

import com.atguigu.gmall2020.mock.db.bean.OrderDetail;
import com.atguigu.gmall2020.mock.db.bean.RefundPayment;
import com.atguigu.gmall2020.mock.db.mapper.OrderDetailMapper;
import com.atguigu.gmall2020.mock.db.mapper.RefundPaymentMapper;
import com.atguigu.gmall2020.mock.db.service.OrderDetailService;
import com.atguigu.gmall2020.mock.db.service.RefundPaymentService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 订单明细表 服务实现类
 * </p>
 *
 * @author zc
 * @since 2020-02-23
 */
@Service
public class RefundPaymentImpl extends ServiceImpl<RefundPaymentMapper, RefundPayment> implements RefundPaymentService {

}
