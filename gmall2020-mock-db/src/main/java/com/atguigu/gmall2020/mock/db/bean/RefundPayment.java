package com.atguigu.gmall2020.mock.db.bean;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefundPayment {

    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    Long id;

    String outTradeNo;
    Long orderId;
    Long skuId;
    String paymentType;
    BigDecimal totalAmount;
    String subject;
    String refundStatus;
    Date createTime;
    Date callbackTime;
    String callbackContent;
}
