package com.atguigu.gmall2020.mock.db.bean;

import lombok.Data;

import java.math.BigDecimal;
import java.io.Serializable;

/**
 * <p>
 * 优惠规则
 * </p>
 *
 * @author zhangchen
 * @since 2020-02-25
 */
@Data
public class ActivityRule implements Serializable {




    private static final long serialVersionUID = 1L;

    /**
     * 编号
     */
    private Long id;

    /**
     * 类型
     */
    private Long activityId;

    /**
     * 满减金额
     */
    private BigDecimal conditionAmount;

    /**
     * 满减件数
     */
    private Long conditionNum;

    /**
     * 优惠金额
     */
    private BigDecimal benefitAmount;

    /**
     * 优惠折扣
     */
    private BigDecimal benefitDiscount;

    /**
     * 优惠级别
     */
    private Long benefitLevel;

    private String activityType;

}
