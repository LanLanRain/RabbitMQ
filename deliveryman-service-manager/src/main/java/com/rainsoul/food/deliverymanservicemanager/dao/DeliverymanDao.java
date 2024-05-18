package com.rainsoul.food.deliverymanservicemanager.dao;

import com.rainsoul.food.deliverymanservicemanager.enummeration.DeliverymanStatus;
import com.rainsoul.food.deliverymanservicemanager.po.DeliverymanPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

import java.util.List;

@Mapper
@Repository
public interface DeliverymanDao {

    @Select("SELECT id,name,status,date FROM deliveryman WHERE id = #{id}")
    DeliverymanPO selectDeliverymanById(Integer id);

    @Select("SELECT id,name,status,date FROM deliveryman WHERE status = #{status}")
    List<DeliverymanPO> selectAvailableDeliveryman(DeliverymanStatus status);
}
