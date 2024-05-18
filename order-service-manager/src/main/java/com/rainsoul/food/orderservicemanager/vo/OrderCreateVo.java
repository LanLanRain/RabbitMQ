package com.rainsoul.food.orderservicemanager.vo;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

/**
 * 订单创建视图对象
 * 用于封装订单创建时所需的基本信息
 */

@Getter
@Setter
@ToString
public class OrderCreateVo {
    private Integer accountId; // 账户ID
    private String address; // 地址信息
    private Integer productId; // 产品ID
}

