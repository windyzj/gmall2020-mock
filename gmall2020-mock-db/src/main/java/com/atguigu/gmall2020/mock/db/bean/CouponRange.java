package com.atguigu.gmall2020.mock.db.bean;

import com.baomidou.mybatisplus.annotation.TableField;
import lombok.Data;

import java.util.List;

@Data
public class CouponRange {

    Long id;

    Long couponId;

    String rangeType;

    Long rangeId;



}
