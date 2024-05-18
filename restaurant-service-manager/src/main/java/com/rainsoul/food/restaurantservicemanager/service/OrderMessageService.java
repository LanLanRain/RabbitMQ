package com.rainsoul.food.restaurantservicemanager.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.*;
import com.rainsoul.food.restaurantservicemanager.dao.ProductDao;
import com.rainsoul.food.restaurantservicemanager.dao.RestaurantDao;
import com.rainsoul.food.restaurantservicemanager.dto.OrderMessageDTO;
import com.rainsoul.food.restaurantservicemanager.enummeration.ProductStatus;
import com.rainsoul.food.restaurantservicemanager.enummeration.RestaurantStatus;
import com.rainsoul.food.restaurantservicemanager.po.ProductPO;
import com.rainsoul.food.restaurantservicemanager.po.RestaurantPO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@Service
public class OrderMessageService {

    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    ProductDao productDao;

    @Autowired
    RestaurantDao restaurantDao;

    /**
     * 异步处理消息的方法。该方法主要用于监听来自餐厅订单的消息。
     * 无参数和返回值，但可能会抛出 IOException, TimeoutException, InterruptedException 异常。
     * 使用 RabbitMQ 的 Java 客户端来建立连接、声明交换器、队列以及绑定，并消费队列中的消息。
     */
    @Async
    public void handleMassage() throws IOException, TimeoutException, InterruptedException {
        log.info("start listening message"); // 开始监听消息的日志记录
        ConnectionFactory connectionFactory = new ConnectionFactory(); // 创建连接工厂
        connectionFactory.setHost("203.195.210.183"); // 设置RabbitMQ服务器的地址

        // 尝试创建连接和通道，并进行一系列的队列和交换器的声明与绑定
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {

            // 声明一个直接类型的交换器
            channel.exchangeDeclare(
                    "exchange.order.restaurant",
                    BuiltinExchangeType.DIRECT,
                    true,
                    false,
                    null);

            // 声明一个持久化的队列
            channel.queueDeclare(
                    "queue.restaurant",
                    true,
                    false,
                    false,
                    null);

            // 将队列绑定到交换器上，指定路由键
            channel.queueBind(
                    "queue.restaurant",
                    "exchange.order.restaurant",
                    "key.restaurant");

            // 开始消费队列中的消息
            channel.basicConsume("queue.restaurant", true, deliverCallback, consumerTag -> {
            });
        }

        // 保持程序运行，不断循环以持续监听消息
        while (true) {
            Thread.sleep(100000); // 每100秒检查一次
        }
    }

    /**
     * 定义一个回调函数，用于处理消息的交付。
     *
     * @param consumerTag 消费者的标签，用于标识消费者
     * @param message 被消费的消息对象，包含消息正文和其他属性
     * */
    DeliverCallback deliverCallback = (consumerTag, message) -> {
        // 将消息体从字节转换为字符串
        String messageBody = new String(message.getBody());
        log.info("deliveryCallback:messageBody:{}", messageBody);

        // 初始化连接工厂并设置RabbitMQ服务器的主机地址
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("203.195.210.183");

        try {
            // 将消息正文反序列化为订单消息对象
            OrderMessageDTO orderMessageDTO = objectMapper.readValue(messageBody, OrderMessageDTO.class);

            // 根据订单消息中的产品ID查询产品信息
            ProductPO productPO = productDao.selectProductById(orderMessageDTO.getProductId());
            log.info("onMessage:productPO:{}", productPO);

            // 根据产品的餐厅ID查询餐厅信息
            RestaurantPO restaurantPO = restaurantDao.selectRestaurantById(productPO.getRestaurantId());
            log.info("onMessage:restaurantPO:{}", restaurantPO);

            // 检查产品和餐厅的状态，确认订单是否有效
            if (ProductStatus.AVALIABIE == productPO.getStatus() && RestaurantStatus.OPEN == restaurantPO.getStatus()) {
                orderMessageDTO.setConfirmed(true);
                orderMessageDTO.setPrice(productPO.getPrice());
            } else {
                orderMessageDTO.setConfirmed(false);
            }
            log.info("sendMessage:restaurantOrderMessageDTO:{}", orderMessageDTO);

            // 使用RabbitMQ的连接工厂创建连接和通道，发送处理后的订单消息
            try (Connection connection = connectionFactory.newConnection();
                 Channel channel = connection.createChannel()) {
                // 将订单消息对象序列化为字符串消息体
                String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
                // 发布消息到指定的交换器和路由键
                channel.basicPublish("exchange.order.restaurant", "key.order", null, messageToSend.getBytes());
            }
        } catch (JsonProcessingException | TimeoutException e) {
            // 处理序列化或超时异常
            e.printStackTrace();
        }
    };

}
