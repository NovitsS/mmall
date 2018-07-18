package com.novit.cart.domain.repository;

import com.novit.cart.domain.model.Cart;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface CartMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(Cart record);

    int insertSelective(Cart record);

    Cart selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(Cart record);

    int updateByPrimaryKey(Cart record);

    Cart selectCartByUserIdProductId(@Param("userId") Integer userId, @Param("productId") Integer productId);

    List<Cart> selectCartByUserId(Integer userId);

    int selectCartProductCheckedStatusByUserId(Integer userId);//查询是否有未勾选的，如果未勾选的数量为0，则代表是全选

    int deleteByUserIdProductIds(@Param("userId") Integer userId, @Param("productIdList") List<String> productIdList);//返回值就是生效行数，foreach遍历

    int checkedOrUncheckedProduct(@Param("userId") Integer userId, @Param("productId") Integer productId, @Param("checked") Integer checked);

    int selectCartProductCount(@Param("userId") Integer userId);//查询购物车中产品总数量。sum(quantity)查询这个数量的总和。
    // 有一个隐患，如果空的话无法返回给int这样的基本类型，加一个IFNULL内置函数，括号里面如果前面是null就给它后面这个默认值。
    List<Cart> selectCheckedCartByUserId(Integer userId);//从购物车中获取已经被勾选的产品
}