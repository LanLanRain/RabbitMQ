package com.rainsoul.food.restaurantservicemanager.dao;

import com.rainsoul.food.restaurantservicemanager.po.RestaurantPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface RestaurantDao {

    @Select("SELECT id,name,address,status,settlement_id settlementId,date FROM restaurant WHERE id = #{id}")
    RestaurantPO selectRestaurantById(Integer id);
}