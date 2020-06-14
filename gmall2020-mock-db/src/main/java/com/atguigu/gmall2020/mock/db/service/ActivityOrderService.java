package com.atguigu.gmall2020.mock.db.service;

import com.atguigu.gmall2020.mock.db.bean.ActivityOrder;
import com.atguigu.gmall2020.mock.db.bean.OrderInfo;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.Date;
import java.util.List;

/**
 * <p>
 * 活动与订单关联表 服务类
 * </p>
 *
 * @author zhangchen
 * @since 2020-02-25
 */
public interface ActivityOrderService extends IService<ActivityOrder> {



    public List<ActivityOrder>  genActivityOrder(List<OrderInfo> orderInfoList, Boolean ifClear);

    public  void  saveActivityOrderList( List<ActivityOrder> activityOrderList);

}
