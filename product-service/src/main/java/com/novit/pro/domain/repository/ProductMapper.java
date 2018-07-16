package com.novit.pro.domain.repository;

import com.novit.pro.domain.model.Product;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface ProductMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Product record);

    int insertSelective(Product record);

    Product selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Product record);

    int updateByPrimaryKey(Product record);

    List<Product> selectList();//返回的是对象就要用resultMap

    List<Product> selectByNameAndProductId(@Param("productName") String productName, @Param("productId") Integer productId);//多参数就要parameterType="map"

    List<Product> selectByNameAndCategoryIds(@Param("productName") String productName, @Param("categoryIdList") List<Integer> categoryIdList);
}