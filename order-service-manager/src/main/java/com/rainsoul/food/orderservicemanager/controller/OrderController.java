package com.rainsoul.food.orderservicemanager.controller;

import com.rainsoul.food.orderservicemanager.service.OrderService;
import com.rainsoul.food.orderservicemanager.vo.OrderCreateVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Slf4j
public class OrderController {

    @Autowired
    private OrderService orderService;

    @PostMapping("/orders")
    public void createOrder(@RequestBody OrderCreateVo orderCreateVo) throws Exception {
        log.info("createOrder:orderCreateVO:{}", orderCreateVo);
        orderService.createOrder(orderCreateVo);
    }


}
