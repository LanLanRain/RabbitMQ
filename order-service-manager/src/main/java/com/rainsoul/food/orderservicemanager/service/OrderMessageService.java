package com.rainsoul.food.orderservicemanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.rainsoul.food.orderservicemanager.dao.OrderDetailDao;
import com.rainsoul.food.orderservicemanager.dto.OrderMessageDTO;
import com.rainsoul.food.orderservicemanager.enummeration.OrderStatus;
import com.rainsoul.food.orderservicemanager.po.OrderDetailPO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

/**
 * 消息处理相关业务逻辑
 */
@Service
public class OrderMessageService {
    private static final Logger log = LoggerFactory.getLogger(OrderMessageService.class);

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    private OrderDetailDao orderDetailDao;

    /**
     * 处理消息的函数。
     * 此函数创建到RabbitMQ服务器的连接，并在该连接上声明交换器、队列以及将它们绑定起来。
     * 通过这种方式，可以实现消息的路由和传递。
     *
     * @throws IOException      如果在与RabbitMQ服务器的通信中发生IO错误。
     * @throws TimeoutException 如果在建立连接或执行其他操作时超时。
     */
    @Async
    public void handleMessage() throws Exception {
        // 创建连接工厂，用于生产连接到RabbitMQ服务器的连接
        ConnectionFactory connectionFactory = new ConnectionFactory();
        // 设置连接工厂要连接的主机地址
        connectionFactory.setHost("203.195.210.183");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("rain");
        connectionFactory.setPassword("123456");

        // 使用try-with-resources语句确保连接和通道在使用后被正确关闭
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            // 声明一个用于订单的交换机，使用直接交换类型
            channel.exchangeDeclare(
                    "exchange.order.restaurant",
                    BuiltinExchangeType.DIRECT,
                    true,
                    false,
                    null
            );

            // 声明一个用于存储订单的队列
            channel.queueDeclare(
                    "queue.order",
                    true,
                    false,
                    false,
                    null
            );

            // 将订单队列与订单交换机绑定，使用"key.order"作为路由键
            channel.queueBind(
                    "queue.order",
                    "exchange.order.restaurant",
                    "key.order"
            );

            // 声明一个用于配送员的交换机，同样使用直接交换类型
            channel.exchangeDeclare(
                    "exchange.order.deliveryman",
                    BuiltinExchangeType.DIRECT,
                    true,
                    false,
                    null
            );

            // 将订单队列与配送员交换机也进行绑定，这确保了订单消息可以被正确路由到配送员
            channel.queueBind(
                    "queue.order",
                    "exchange.order.deliveryman",
                    "key.order"
            );

            channel.basicConsume("queue.order", true, deliverCallback, consumerTag -> {
            });
            while (true) {
                Thread.sleep(1000000);
            }
        }
    }

    /**
     * 创建一个回调函数，用于处理消息的交付。
     * 当消息被消费者接收到时，该回调函数会被执行，对消息进行处理。
     *
     * @param consumerTag 消费者的标签，用于标识消费者。
     * @param message 接收到的消息对象。
     */
    DeliverCallback deliverCallback = ((consumerTag, message) -> {
        // 将消息体从字节转换为字符串
        String messageBody = new String(message.getBody());
        log.info("deliverCallback:massageBody:{}", messageBody);

        // 初始化连接工厂并设置主机地址
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("203.195.210.183");
        connectionFactory.setPort(5672);
        connectionFactory.setUsername("rain");
        connectionFactory.setPassword("123456");

        try {
            // 将消息内容解析为订单消息DTO
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody,
                    OrderMessageDTO.class);
            // 根据订单ID查询订单详情
            OrderDetailPO orderDetailPO = orderDetailDao.selectOrderById(orderMessageDTO.getOrderId());

            // 根据订单的不同状态进行处理
            switch (orderDetailPO.getStatus()) {

                case ORDER_CREATING:
                    // 餐厅确认订单逻辑处理
                    if (orderMessageDTO.getConfirmed() && null != orderMessageDTO.getPrice()) {
                        orderDetailPO.setStatus(OrderStatus.RESTAURANT_CONFIRMED);
                        orderDetailPO.setPrice(orderMessageDTO.getPrice());
                        orderDetailDao.update(orderDetailPO);
                        // 向deliveryman交换器发送消息
                        try (Connection connection = connectionFactory.newConnection();
                             Channel channel = connection.createChannel()) {
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish("exchange.order.deliveryman", "key.deliveryman", null,
                                    messageToSend.getBytes());
                        }
                    } else {
                        // 订单失败处理
                        orderDetailPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderDetailPO);
                    }
                    break;
                case RESTAURANT_CONFIRMED:
                    // 配送员确认订单逻辑处理
                    if (null != orderMessageDTO.getDeliverymanId()) {
                        orderDetailPO.setStatus(OrderStatus.DELIVERYMAN_CONFIRMED);
                        orderDetailPO.setDeliverymanId(orderMessageDTO.getDeliverymanId());
                        orderDetailDao.update(orderDetailPO);
                        // 向settlement交换器发送消息
                        try (Connection connection = connectionFactory.newConnection();
                             Channel channel = connection.createChannel()) {
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish("exchange.order.settlement", "key.settlement", null,
                                    messageToSend.getBytes());
                        }
                    } else {
                        orderDetailPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderDetailPO);
                    }
                    break;
                case DELIVERYMAN_CONFIRMED:
                    // 结算确认订单逻辑处理
                    if (null != orderMessageDTO.getSettlementId()) {
                        orderDetailPO.setStatus(OrderStatus.SETTLEMENT_CONFIRMED);
                        orderDetailPO.setSettlementId(orderMessageDTO.getSettlementId());
                        orderDetailDao.update(orderDetailPO);
                        // 向reward交换器发送消息
                        try (Connection connection = connectionFactory.newConnection();
                             Channel channel = connection.createChannel()) {
                            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                            channel.basicPublish("exchange.order.reward", "key.reward", null, messageToSend.getBytes());
                        }
                    } else {
                        orderDetailPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderDetailPO);
                    }
                    break;
                case SETTLEMENT_CONFIRMED:
                    // 订单创建完成逻辑处理
                    if (null != orderMessageDTO.getRewardId()) {
                        orderDetailPO.setStatus(OrderStatus.ORDER_CREATED);
                        orderDetailPO.setRewardId(orderMessageDTO.getRewardId());
                        orderDetailDao.update(orderDetailPO);
                    } else {
                        orderDetailPO.setStatus(OrderStatus.FAILED);
                        orderDetailDao.update(orderDetailPO);
                    }
                    break;
            }

        } catch (JsonProcessingException | TimeoutException e) {
            // 处理解析异常或超时异常
            e.printStackTrace();
        }
    });

}
