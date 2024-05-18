package com.rainsoul.food.orderservicemanager.config;

import com.rainsoul.food.orderservicemanager.service.OrderMessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
/**
 * RabbitMQ配置类，用于配置和初始化RabbitMQ消息监听。
 */
@Configuration
public class RabbitMQConfig {

    // 注入OrderMessageService，用于处理订单消息
    @Autowired
    OrderMessageService orderMessageService;

    /**
     * 启动消息监听。
     * 无参数。
     * 无返回值。
     * @throws Exception 如果启动过程中遇到任何错误，则抛出异常。
     */
    public void startListenMessage() throws Exception {
        orderMessageService.handleMessage(); // 处理订单消息
    }
}

