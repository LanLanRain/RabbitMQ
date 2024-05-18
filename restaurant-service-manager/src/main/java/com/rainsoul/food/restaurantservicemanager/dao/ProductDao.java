package com.rainsoul.food.restaurantservicemanager.dao;

import com.rainsoul.food.restaurantservicemanager.po.ProductPO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.springframework.stereotype.Repository;

@Mapper
@Repository
public interface ProductDao {

    @Select("SELECT id,name,price,restaurant_id restaurantId,status,date FROM product WHERE id = #{id}")
    ProductPO selectProductById(Integer id);
}
