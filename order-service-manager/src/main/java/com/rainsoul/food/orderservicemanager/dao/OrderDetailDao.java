package com.rainsoul.food.orderservicemanager.dao;

import com.rainsoul.food.orderservicemanager.po.OrderDetailPO;
import org.apache.ibatis.annotations.*;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface OrderDetailDao {
    @Insert("Insert into order_detail (status, address, account_id, product_id, deliveryman_id, settlement_id, reward_id, price, date) VALUES " +
            "(#{status}, #{address},#{accountId},#{productId},#{deliverymanId},#{settlementId},#{rewardId},#{price},#{date})")
    @Options(useGeneratedKeys = true, keyProperty = "id")
    void insert(OrderDetailPO orderDetailPO);

    @Update("update order_detail set status = #{status}, address = #{address}, account_id = #{accountId}, product_id = #{productId}" +
            ", deliveryman_id = #{deliverymanId}, settlement_id = #{settlementId}, reward_id = #{rewardId}, product_id = #{productId}, date = #{date}" +
            " where id = #{id}")
    void update(OrderDetailPO orderDetailPO);

    @Select("select id, status, address, account_id accountId, product_id productId, deliveryman_id deliverymanId, " +
            "settlement_id settlementId, reward_id rewardId, price, date  from order_detail where id = #{id}")
    OrderDetailPO selectOrderById(Integer id);
}
