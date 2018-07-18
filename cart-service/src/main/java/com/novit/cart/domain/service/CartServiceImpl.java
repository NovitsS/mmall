package com.novit.cart.domain.service;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.novit.cart.domain.model.Cart;
import com.novit.cart.domain.model.Product;
import com.novit.cart.until.BigDecimalUtil;
import com.novit.cart.until.PropertiesUtil;
import com.novit.cart.common.Const;
import com.novit.cart.common.ResponseCode;
import com.novit.cart.common.ServerResponse;
import com.novit.cart.domain.model.CartProductVo;
import com.novit.cart.domain.model.CartVo;
import com.novit.cart.domain.repository.CartMapper;
import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;

@Service("iCartService")
public class CartServiceImpl implements ICartService {

    @Autowired
    private CartMapper cartMapper;


    public ServerResponse<CartVo> add(Integer userId, Integer productId, Integer count){
        if(productId == null || count == null){//校验参数
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(), ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }

        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);//通过用户ID和产品ID查到该购物车
        if(cart == null){//如果该购物车为空的话，表示这个产品不在这个购物车里,需要新增一个这个产品的记录
            Cart cartItem = new Cart();
            cartItem.setQuantity(count);//设置数量
            cartItem.setChecked(Const.Cart.CHECKED);//自动设置为选中状态
            cartItem.setProductId(productId);
            cartItem.setUserId(userId);
            cartMapper.insert(cartItem);//将信息插入至表中
        }else{//else代表这个产品已经在购物车里了，如果产品已存在,数量相加
            count = cart.getQuantity() + count;
            cart.setQuantity(count);
            cartMapper.updateByPrimaryKeySelective(cart);//将购物车中的数量重新设置之后进行表更新
        }
        //CartVo cartVo = this.getCartVoLimit(userId);//重新在db中获取最新的
        //return ServerResponse.createBySuccess(cartVo);
        return this.list(userId);//这里跟写上面两行是完全一样的，详见list里面的代码，只是将它封装成了一个微服务而已
    }


    public ServerResponse<CartVo> update(Integer userId,Integer productId,Integer count){
        if(productId == null || count == null){//校验参数
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Cart cart = cartMapper.selectCartByUserIdProductId(userId,productId);//将对象从db中找出来进行判断是否为空
        if(cart != null){
            cart.setQuantity(count);//更新产品的数量
        }
        cartMapper.updateByPrimaryKey(cart);//将购物车中的数量重新设置之后进行表更新
        //CartVo cartVo = this.getCartVoLimit(userId);
        //return ServerResponse.createBySuccess(cartVo);
        return this.list(userId);
    }


    public ServerResponse<CartVo> deleteProduct(Integer userId,String productIds){
        //guava的splitter方法，否则要把String productIds转成数组，然后再遍历数组添加到集合当中，用这个splitter方法就可以直接搞定，
        //Splitter.on后面就是用什么分割，在此用逗号，splitToList是把它转成集合。这样它会用逗号分隔这个字符串，然后自动添加到productList这个集合当中
        List<String> productList = Splitter.on(",").splitToList(productIds);
        if(CollectionUtils.isEmpty(productList)){//对分好的集合进行判断
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        cartMapper.deleteByUserIdProductIds(userId,productList);//若不为空则删除购物车中产品的那一条数量
        //CartVo cartVo = this.getCartVoLimit(userId);
        //return ServerResponse.createBySuccess(cartVo);
        return this.list(userId);
    }


    public ServerResponse<CartVo> list (Integer userId){
        CartVo cartVo = this.getCartVoLimit(userId);//因为在getCartVoLimit这个方法里面就是在CartVo里面组装cartProductVo，所以下面是有一个集合的
        return ServerResponse.createBySuccess(cartVo);//直接返回就可以了，因为它本身也是list
    }


    public ServerResponse<CartVo> selectOrUnSelect (Integer userId,Integer productId,Integer checked){
        cartMapper.checkedOrUncheckedProduct(userId,productId,checked);
        return this.list(userId);
    }

    public ServerResponse<Integer> getCartProductCount(Integer userId){
        if(userId == null){//如果没有登录的话也不能说错误，显示0就可以了
            return ServerResponse.createBySuccess(0);
        }
        return ServerResponse.createBySuccess(cartMapper.selectCartProductCount(userId));//查询购物车中产品总数量（用到mysql的内置函数）
    }







    //封装一个private方法，限制并且返回
    //之后购物车的计算都会用这个方法，包括全选，反选等
    private CartVo getCartVoLimit(Integer userId){
        CartVo cartVo = new CartVo();//先new一个返回值后面去拼装它
        List<Cart> cartList = cartMapper.selectCartByUserId(userId);//根据userId查购物车的集合
        List<CartProductVo> cartProductVoList = Lists.newArrayList();//把CartProductVo放到CartVo中，先创建list

        BigDecimal cartTotalPrice = new BigDecimal("0");//初始化购物车总价

        if(CollectionUtils.isNotEmpty(cartList)){//对cartList进行空判断
            for(Cart cartItem : cartList){//若不为空则遍历cartList
                CartProductVo cartProductVo = new CartProductVo();//创建cartProductVo
                cartProductVo.setId(cartItem.getId());//set需要的信息
                cartProductVo.setUserId(userId);
                cartProductVo.setProductId(cartItem.getProductId());
                //todo: 设计productAPI给查询产品提供服务
                //查询购物车里的产品
                //Product product = productMapper.selectByPrimaryKey(cartItem.getProductId());
                Product product=null;
                if(product != null){//若product不为空的话就继续组装cartProductVo
                    cartProductVo.setProductMainImage(product.getMainImage());
                    cartProductVo.setProductName(product.getName());
                    cartProductVo.setProductSubtitle(product.getSubtitle());
                    cartProductVo.setProductStatus(product.getStatus());
                    cartProductVo.setProductPrice(product.getPrice());
                    cartProductVo.setProductStock(product.getStock());//库存
                    //判断库存
                    int buyLimitCount = 0;//初始化可以购买的限制的数量为0
                    if(product.getStock() >= cartItem.getQuantity()){//当产品大于购物车里的数量时，即库存充足的时候
                        buyLimitCount = cartItem.getQuantity();//可以购买的限制数量等于购物车里的数量
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_SUCCESS);//此时这个limit就等于成功
                    }else{
                        buyLimitCount = product.getStock();//否则的话，购物车里的数量超出了库存，就要将有效库存放进去，因为最多也只能买库存剩余的数量
                        cartProductVo.setLimitQuantity(Const.Cart.LIMIT_NUM_FAIL);//此时限制失败
                        //购物车中更新有效库存
                        Cart cartForQuantity = new Cart();//为了更新数量创建对象
                        cartForQuantity.setId(cartItem.getId());
                        cartForQuantity.setQuantity(buyLimitCount);
                        cartMapper.updateByPrimaryKeySelective(cartForQuantity);
                    }
                    cartProductVo.setQuantity(buyLimitCount);//设置购物车里的数量
                    //计算总价
                    cartProductVo.setProductTotalPrice(BigDecimalUtil.mul(product.getPrice().doubleValue(),cartProductVo.getQuantity()));//这里的总价只是某一个商品的单价乘以数量，不是将各种不同商品相加
                    cartProductVo.setProductChecked(cartItem.getChecked());//对产品进行勾选
                }

                if(cartItem.getChecked() == Const.Cart.CHECKED){
                    //如果已经勾选,增加到整个的购物车总价中
                    cartTotalPrice = BigDecimalUtil.add(cartTotalPrice.doubleValue(),cartProductVo.getProductTotalPrice().doubleValue());//这里才是将不同商品价格相加
                }
                cartProductVoList.add(cartProductVo);//把cartProductVo添加到list中
            }
        }
        //组建cartVo并返回
        cartVo.setCartTotalPrice(cartTotalPrice);
        cartVo.setCartProductVoList(cartProductVoList);
        cartVo.setAllChecked(this.getAllCheckedStatus(userId));
        cartVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix"));

        return cartVo;
    }

    //封装一个private方法，判断购物车是否全选了
    private boolean getAllCheckedStatus(Integer userId){
        if(userId == null){
            return false;
        }
        return cartMapper.selectCartProductCheckedStatusByUserId(userId) == 0;//如果为0表示未勾选数量为0，就直接返回true，表示是全选了

    }

}
