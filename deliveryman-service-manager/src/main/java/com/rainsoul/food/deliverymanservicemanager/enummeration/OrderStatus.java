package com.rainsoul.food.deliverymanservicemanager.enummeration;

/**
 * 订单状态枚举类，用于表示订单的各个状态。
 */
public enum OrderStatus {
    // 订单创建中
    ORDER_CREATING,
    // 餐厅已确认
    RESTAURANT_CONFIRMED,
    // 配送员已确认
    DELIVERYMAN_CONFIRMED,
    // 结算已确认
    SETTLEMENT_CONFIRMED,
    // 订单已创建
    ORDER_CREATED,
    // 订单创建失败
    FAILED;
}
