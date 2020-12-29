package com.atguigu.gmall2020.mock.db.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import org.springframework.format.annotation.DateTimeFormat;

import java.io.Serializable;

/**
 * <p>
 * 活动与订单关联表
 * </p>
 *
 * @author zhangchen
 * @since 2020-02-25
 */
@Data
@AllArgsConstructor
@Builder
public class OrderDetailActivity implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;


    private Long orderDetailId ;

    private Long activityRuleId;

    private Long skuId;

    /**
     * 活动id 
     */
    private Long activityId;

    /**
     * 订单编号
     */
    private Long orderId;



    @TableField(exist = false)
    private OrderDetail orderDetail;
    @TableField(exist = false)
    private OrderInfo orderInfo;
    @TableField(exist = false)
    private ActivityRule activityRule;

    /**
     * 发生日期
     */
    private Date createTime;




}
