package com.rainsoul.food.restaurantservicemanager.config;

import com.rainsoul.food.restaurantservicemanager.service.OrderMessageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Configuration
public class RabbitConfig {

    @Autowired
    OrderMessageService orderMessageService;

    public void startListenMessage() throws IOException, TimeoutException, InterruptedException {
        orderMessageService.handleMassage();
    }
}
