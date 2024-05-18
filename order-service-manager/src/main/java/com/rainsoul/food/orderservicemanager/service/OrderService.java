package com.rainsoul.food.orderservicemanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
import com.rainsoul.food.orderservicemanager.dao.OrderDetailDao;
import com.rainsoul.food.orderservicemanager.dto.OrderMessageDTO;
import com.rainsoul.food.orderservicemanager.enummeration.OrderStatus;
import com.rainsoul.food.orderservicemanager.po.OrderDetailPO;
import com.rainsoul.food.orderservicemanager.vo.OrderCreateVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.concurrent.TimeoutException;

@Service
public class OrderService {

    @Autowired
    private OrderDetailDao orderDetailDao;

    ObjectMapper objectMapper = new ObjectMapper();

    /**
     * 创建订单并发送订单消息。
     *
     * @param orderCreateVo 订单创建视图对象，包含订单创建所需的所有信息。
     *                       其中应包括地址    、账户ID和产品ID等。
     */
    public void createOrder(OrderCreateVo orderCreateVo) throws IOException, TimeoutException {
        // 将订单信息转换为持久化对象，用于数据库存储
        OrderDetailPO orderDetailPO = new OrderDetailPO();
        orderDetailPO.setAddress(orderCreateVo.getAddress());
        orderDetailPO.setAccountId(orderCreateVo.getAccountId());
        orderDetailPO.setDate(new Date());
        orderDetailPO.setProductId(orderCreateVo.getProductId());
        orderDetailPO.setStatus(OrderStatus.ORDER_CREATING);
        // 在数据库中插入订单详情
        orderDetailDao.insert(orderDetailPO);

        // 准备订单消息，用于发送到消息队列
        OrderMessageDTO orderMessageDTO = new OrderMessageDTO();
        orderMessageDTO.setOrderId(orderDetailPO.getId());
        orderMessageDTO.setAccountId(orderCreateVo.getAccountId());
        orderMessageDTO.setProductId(orderCreateVo.getProductId());

        // 设置消息队列的连接工厂，配置消息队列服务器地址
        ConnectionFactory connectionFactory = new ConnectionFactory();
        connectionFactory.setHost("203.195.210.183");

        // 建立消息队列连接，并创建通道，发送订单消息
        try (Connection connection = connectionFactory.newConnection();
             Channel channel = connection.createChannel()) {
            // 将订单消息转换为JSON字符串，准备发送
            String messageToSend = objectMapper.writeValueAsString(orderMessageDTO);
            // 在指定的交换器上发布消息，指定路由键和消息内容
         /* - `channel`: 是一个`Channel`对象，它是RabbitMQ客户端与服务器之间的通信信道，用于执行各种AMQP操作，如声明交换器、队列和绑定，以及发布和接收消息。

            - `"exchange.order.restaurant"`: 这是交换器（Exchange）的名字，交换器负责根据预定义的规则（路由键）将消息路由到相应的队列。在这里，它的名字表明可能是处理餐厅订单的交换器。

            - `"key.restaurant"`: 这是路由键（Routing Key），它是一个字符串，用于匹配交换器的绑定规则，决定消息将被送到哪个队列。在这里，"key.restaurant"可能表示消息与餐厅相关。

            - `null`: 这通常代表消息的属性（BasicProperties），在这里设置为`null`，意味着使用默认属性。

            - `messageToSend.getBytes(StandardCharsets.UTF_8)`: `messageToSend`是要发送的实际消息内容，这里将其转换为UTF-8编码的字节数组，以便于RabbitMQ可以处理。这样做的目的是确保消息内容在网络传输过程中保持正确的字符编码。
*/
            channel.basicPublish(
                    "exchange.order.restaurant",
                    "key.restaurant",
                    null,
                    messageToSend.getBytes(StandardCharsets.UTF_8)
            );
        }
    }
}
