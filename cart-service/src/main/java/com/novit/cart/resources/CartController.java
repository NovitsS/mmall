package com.novit.cart.resources;


import com.novit.cart.common.Const;
import com.novit.cart.common.ServerResponse;
import com.novit.cart.domain.model.CartVo;
import com.novit.cart.domain.model.User;
import com.novit.cart.domain.service.ICartService;
import com.novit.cart.common.ResponseCode;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/cart/")
public class CartController {

    @Autowired
    private ICartService iCartService;

    //添加商品至购物车的接口
    @RequestMapping("add.do")
    @ResponseBody
    public ServerResponse<CartVo> add(HttpSession session, Integer count, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.add(user.getId(),productId,count);
    }

    //更新购物车接口，改变购物车中产品的数量，例如用加减号更新购物车数量
    @RequestMapping("update.do")
    @ResponseBody
    public ServerResponse<CartVo> update(HttpSession session, Integer count, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.update(user.getId(),productId,count);
    }

    //在购物车中删除产品的接口
    @RequestMapping("delete_product.do")
    @ResponseBody
    //不需要数量这个参数了，并且删除的时候可能一次删除多个，例如左边勾选两个产品一起删除
    public ServerResponse<CartVo> deleteProduct(HttpSession session,String productIds){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.deleteProduct(user.getId(),productIds);
    }

    //购物车list列表接口（前面是增删改，这里是查）
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<CartVo> list(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.list(user.getId());
    }

    //以下四个接口分别为全选，全反选，单独选，单独反选。把它们封装成一个公用的方法，在controller里面传不同的参数来实现
    @RequestMapping("select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> selectAll(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select_all.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelectAll(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),null,Const.Cart.UN_CHECKED);
    }

    @RequestMapping("select.do")
    @ResponseBody
    public ServerResponse<CartVo> select(HttpSession session,Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.CHECKED);
    }

    @RequestMapping("un_select.do")
    @ResponseBody
    public ServerResponse<CartVo> unSelect(HttpSession session,Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iCartService.selectOrUnSelect(user.getId(),productId,Const.Cart.UN_CHECKED);
    }

    //查询当前用户的购物车里面的产品数量的接口，如果一个产品有10个，那么数量就是10（显示在右上角）
    @RequestMapping("get_cart_product_count.do")
    @ResponseBody
    //泛型设置为integer，只会返回一个数量
    public ServerResponse<Integer> getCartProductCount(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//如果没有登录的话也不能说错误，显示0就可以了
            return ServerResponse.createBySuccess(0);
        }
        return iCartService.getCartProductCount(user.getId());
    }

    @RequestMapping("test")
    @ResponseBody
    public String getSesionId(HttpServletRequest request){
        Cookie[] cookies=request.getCookies();
        String s = "hello";
        for(int i=0;i<cookies.length;i++){
            s=s+cookies[i].getValue();
        }
        return s;
    }

}
