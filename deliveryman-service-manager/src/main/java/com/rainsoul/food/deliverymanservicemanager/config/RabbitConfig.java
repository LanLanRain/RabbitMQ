package com.rainsoul.food.deliverymanservicemanager.config;

import com.rainsoul.food.deliverymanservicemanager.service.OrderMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    OrderMessageService orderMessageService;

    public void startListenMessage() throws Exception {
        orderMessageService.handleMessage();
    }
}

