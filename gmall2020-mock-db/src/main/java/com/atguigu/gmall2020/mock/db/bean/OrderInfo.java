package com.atguigu.gmall2020.mock.db.bean;

import java.math.BigDecimal;
import com.baomidou.mybatisplus.annotation.IdType;
import java.util.Date;

import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.sun.org.apache.bcel.internal.generic.IFNULL;
import lombok.Data;

import java.io.Serializable;
import java.util.List;

/**
 * <p>
 * 订单表 订单表
 * </p>
 *
 * @author zc
 * @since 2020-02-23
 */
@Data
public class OrderInfo implements Serializable {

    private static final long serialVersionUID = 1L;


    /**
     * 编号
     */
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    /**
     * 收货人
     */
    private String consignee;

    /**
     * 收件人电话
     */
    private String consigneeTel;

    /**
     * 总金额
     */
    private BigDecimal totalAmount;

    /**
     * 订单状态
     */
    private String orderStatus;

    /**
     * 用户id
     */
    private Long userId;


    /**
     * 送货地址
     */
    private String deliveryAddress;

    /**
     * 订单备注
     */
    private String orderComment;

    /**
     * 订单交易编号（第三方支付用)
     */
    private String outTradeNo;

    /**
     * 订单描述(第三方支付用)
     */
    private String tradeBody;

    /**
     * 创建时间
     */
    private Date createTime;

    /**
     * 操作时间
     */
    private Date operateTime;

    /**
     * 失效时间
     */
    private Date expireTime;

    /**
     * 物流单编号
     */
    private String trackingNo;

    /**
     * 父订单编号
     */
    private Long parentOrderId;

    /**
     * 图片路径
     */
    private String imgUrl;

    /**
     * 地区
     */
    private Integer provinceId;


    private BigDecimal originalTotalAmount;

    private BigDecimal feightFee;

    private BigDecimal activityReduceAmount;

    private BigDecimal couponReduceAmount;




    @TableField(exist = false)
    private List<OrderDetail> orderDetailList;





    public void sumTotalAmount(){

        this.activityReduceAmount = this.activityReduceAmount ==null?BigDecimal.ZERO:this.activityReduceAmount;
        this.couponReduceAmount = this.couponReduceAmount ==null?BigDecimal.ZERO:this.couponReduceAmount;
        this.feightFee= this.feightFee==null?BigDecimal.ZERO:this.feightFee;
        this.originalTotalAmount= this.originalTotalAmount==null?BigDecimal.ZERO:this.originalTotalAmount;
        this.totalAmount = this.totalAmount ==null?BigDecimal.ZERO:this.totalAmount;



        BigDecimal totalAmount= BigDecimal.ZERO;
        for (OrderDetail orderDetail : orderDetailList) {
            BigDecimal splitActivityAmount = orderDetail.getSplitActivityAmount() ==null?BigDecimal.ZERO:orderDetail.getSplitActivityAmount();
            BigDecimal couponReduceAmount = orderDetail.getSplitCouponAmount() ==null?BigDecimal.ZERO:orderDetail.getSplitCouponAmount();

            BigDecimal splitTotalamount = orderDetail.getOrderPrice().multiply(new BigDecimal(orderDetail.getSkuNum()));
            splitTotalamount=splitTotalamount.subtract(  splitActivityAmount).subtract(couponReduceAmount);
            orderDetail.setSplitTotalAmount(splitTotalamount);
            totalAmount= totalAmount.add(splitTotalamount);
        }
        this.originalTotalAmount=  totalAmount;

        this.totalAmount =originalTotalAmount.subtract(activityReduceAmount).subtract(couponReduceAmount).add(feightFee);

    }

    //生成摘要
    public String getOrderSubject(){
        String body="";
        if(orderDetailList!=null&&orderDetailList.size()>0){
            body=  orderDetailList.get(0).getSkuName();
        }
        body+="等"+getTotalSkuNum()+"件商品";
        return body;

    }

    public Long getTotalSkuNum(){
        Long totalNum=0L;
        for (OrderDetail orderDetail : orderDetailList) {
            totalNum+=  orderDetail.getSkuNum();
        }
        return totalNum;
    }
}
