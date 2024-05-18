package com.rainsoul.food.deliverymanservicemanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.rainsoul.food.deliverymanservicemanager.dao.DeliverymanDao;
import com.rainsoul.food.deliverymanservicemanager.dto.OrderMessageDTO;
import com.rainsoul.food.deliverymanservicemanager.enummeration.DeliverymanStatus;
import com.rainsoul.food.deliverymanservicemanager.po.DeliverymanPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderMessageService {

    @Autowired
    DeliverymanDao deliverymanDao;

    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 定义一个回调函数，用于处理消息的传递。
     *
     * @param consumerTag 消费者的标签，用于标识消费者
     * @param message 接收到的消息内容
     *
     * 此回调函数主要完成以下功能：
     * 1. 将消息体转换为字符串。
     * 2. 日志记录接收到的消息。
     * 3. 根据消息内容，设置配送员ID。
     * 4. 将更新后的消息发送到另一个队列。
     */
    DeliverCallback deliverCallback = (consumerTag, message) -> {
        // 将消息体从字节转换为字符串，并记录日志
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:messageBody:{}", messageBody);

        // 创建连接工厂并设置主机地址
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("203.195.210.183");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("rain");
        connectionFactory.setPassword("123456");

        try {
            // 将消息内容解析为订单消息对象
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody,
                    OrderMessageDTO.class);

            // 查询可用的配送员
            List<DeliverymanPO> deliverymanPOS = deliverymanDao.selectAvailableDeliveryman(DeliverymanStatus.AVALIABIE);

            // 设置订单消息的配送员ID
            orderMessageDTO.setDeliverymanId(deliverymanPOS.get(0).getId());
            log.info("onMessage:restaurantOrderMessageDTO:{}", orderMessageDTO);

            // 创建连接和通道，将更新后的订单消息发送到指定的交换器
            try (Connection connection = connectionFactory.newConnection();
                 Channel channel = connection.createChannel()) {
                String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                channel.basicPublish("exchange.order.restaurant", "key.order", null, messageToSend.getBytes());
            }
        } catch (JsonProcessingException | TimeoutException e) {
            // 处理解析异常和超时异常
            e.printStackTrace();
        }
    };



    /**
     * 异步处理消息的函数。
     * 此函数创建一个与消息队列服务器的连接，声明交换器和队列，将队列绑定到交换器上，
     * 并开始消费队列中的消息。
     * 注意：此实现为长轮询消费模式，通过不断休眠来等待消息。
     *
     * @throws Exception 如果连接、通道创建或消息处理过程中发生错误，则抛出异常。
     */
    @Async
    public void handleMessage() throws Exception {
        log.info("start listening message");
        // 创建连接工厂并设置服务器地址
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("203.195.210.183");
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            // 声明订单配送员交换器
            channel.exchangeDeclare(
                    "exchange.order.deliveryman",
                    BuiltinExchangeType.DIRECT,
                    true,
                    false,
                    null
            );

            // 声明配送员队列
            channel.queueDeclare(
                    "queue.deliveryman",
                    true,
                    false,
                    false,
                    null
            );

            // 将配送员队列绑定到订单配送员交换器上
            channel.queueBind(
                    "queue.deliveryman",
                    "exchange.order.deliveryman",
                    "key.deliveryman"
            );

            // 开始消费队列中的消息
            channel.basicConsume("queue.deliveryman", true, deliverCallback, consumerTag -> {
            });
            // 长轮询等待消息，不断休眠
            while (true) {
                Thread.sleep(100000);
            }
        }
    }
}
