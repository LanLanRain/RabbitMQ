package com.rainsoul.food.orderservicemanager.dto;

import com.rainsoul.food.orderservicemanager.enummeration.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;

/**
 * 订单消息数据传输对象（DTO）
 * 用于在系统中传递订单相关的消息数据
 */
@Getter
@Setter
@ToString
public class OrderMessageDTO {
    private String orderId; // 订单编号
    private String productId; // 产品编号
    private OrderStatus orderStatus; // 订单状态
    private BigDecimal price; // 订单价格
    private Integer deliverymanId; // 配送员编号
    private Integer accountId; // 账户编号
    private String address; // 收货地址
    private Integer settlementId; // 结算编号
    private Integer rewardId; // 积分编号
    private Integer rewardAmount; // 积分数量
    private Boolean confirmed; // 是否已确认
}
