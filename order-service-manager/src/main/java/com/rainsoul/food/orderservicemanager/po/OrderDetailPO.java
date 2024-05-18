package com.rainsoul.food.orderservicemanager.po;

import com.rainsoul.food.orderservicemanager.enummeration.OrderStatus;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.math.BigDecimal;
import java.util.Date;

/**
 * 订单详情持久化对象类
 * 用于存储订单的详细信息
 */
@Getter
@Setter
@ToString
public class OrderDetailPO {
    private Integer id; // 订单详情的唯一标识符
    private OrderStatus status; // 订单的状态
    private String address; // 订单的配送地址
    private Integer accountId; // 下单用户的账户ID
    private Integer productId; // 订单包含的产品ID
    private Integer deliverymanId; // 配送员的ID
    private Integer settlementId; // 结算信息的ID
    private Integer rewardId; // 奖励信息的ID
    private BigDecimal price; // 订单的总价格
    private Date date; // 订单的日期信息
}
