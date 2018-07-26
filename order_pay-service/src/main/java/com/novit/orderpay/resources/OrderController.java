package com.novit.orderpay.resources;

import com.alipay.api.AlipayApiException;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.demo.trade.config.Configs;
import com.google.common.collect.Maps;
import com.novit.orderpay.common.Const;
import com.novit.orderpay.common.ResponseCode;
import com.novit.orderpay.common.ServerResponse;
import com.novit.orderpay.domain.model.User;
import com.novit.orderpay.domain.service.IOrderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Iterator;
import java.util.Map;

@Controller
@RequestMapping("/order/")
public class OrderController {

    private static  final Logger logger = LoggerFactory.getLogger(OrderController.class);

    @Autowired
    private IOrderService iOrderService;

    //订单模块的接口
    //创建订单接口
    @RequestMapping("create.do")
    @ResponseBody
    public ServerResponse create(HttpSession session, Integer shippingId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.createOrder(user.getId(),shippingId);
    }

    //取消订单的接口，在未付款的情况下取消
    @RequestMapping("cancel.do")
    @ResponseBody
    public ServerResponse cancel(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.cancel(user.getId(),orderNo);
    }

    //获取购物车中商品信息的接口，预览的时候看到购物车里的明细（只查看已经选中的产品详情）
    @RequestMapping("get_order_cart_product.do")
    @ResponseBody
    public ServerResponse getOrderCartProduct(HttpSession session){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderCartProduct(user.getId());
    }

    //前台用户个人中心中订单详情接口
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse detail(HttpSession session,Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderDetail(user.getId(),orderNo);
    }

    //前台用户个人中心中订单列表接口
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse list(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        return iOrderService.getOrderList(user.getId(),pageNum,pageSize);
    }









    //以下三个是支付接口

    @RequestMapping("pay.do")
    @ResponseBody
    //支付的时候是支付什么，支付订单号Long orderNo，传过来订单号供支付。
    //用HttpServletRequest request获取servlet的上下文，拿到upload文件夹，然后把自动生成的二维码传到ftp服务器上，然后返回给前端二维码的图片地址，前端进行展示，然后扫码支付
    public ServerResponse pay(HttpSession session, Long orderNo, HttpServletRequest request){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        String path = request.getSession().getServletContext().getRealPath("upload");//获取path路径
        return iOrderService.pay(orderNo,user.getId(),path);
    }

    //支付宝支付回调处理接口
    @RequestMapping("alipay_callback.do")
    @ResponseBody
    //使用object返回是因为会根基alipay的要求的返回来进行返回。参数只有request，支付宝的回调会把所有的数据放到request里面
    public Object alipayCallback(HttpServletRequest request){
        Map<String,String> params = Maps.newHashMap();//用自己的map接收

        Map requestParams = request.getParameterMap();//获取参数的map，支付宝的回调不像平时调用的时候把参数放到后面去拼接，都是放到request里面，需要从request里面获取
        //遍历map，动态查询它的key和value，并且value放到数组里，（因为getParameterMap的格式是如此）要把数组里的value取出来，使用迭代器
        for(Iterator iter = requestParams.keySet().iterator(); iter.hasNext();){//遍历看迭代器是否有下一个
            String name = (String)iter.next();//取出名字
            String[] values = (String[]) requestParams.get(name);//取出value
            String valueStr = "";//先声明一个空字符串，然后遍历数组
            for(int i = 0 ; i <values.length;i++){
                //遍历的时候如果没有到头就用逗号分隔开来
                valueStr = (i == values.length -1)?valueStr + values[i]:valueStr + values[i]+",";
            }
            params.put(name,valueStr);//用自己的map接收，这个时候就从request里面拿到了这个参数
        }
        logger.info("支付宝回调,sign:{},trade_status:{},参数:{}",params.get("sign"),params.get("trade_status"),params.toString());

        //非常重要,验证回调的正确性,是不是支付宝发的.并且还要避免重复通知.

        params.remove("sign_type");//AlipaySignature中只除去了sign，文档中说要除去两个，手动去除
        try {
            boolean alipayRSACheckedV2 = AlipaySignature.rsaCheckV2(params, Configs.getAlipayPublicKey(),"utf-8",Configs.getSignType());

            if(!alipayRSACheckedV2){
                return ServerResponse.createByErrorMessage("非法请求,验证不通过,再恶意请求我就报警找网警了");
            }
        } catch (AlipayApiException e) {
            logger.error("支付宝验证回调异常",e);
        }

        //todo 验证各种数据

        //alipayRSACheckedV2为true时的正确业务逻辑
        ServerResponse serverResponse = iOrderService.aliCallback(params);
        if(serverResponse.isSuccess()){
            return Const.AlipayCallback.RESPONSE_SUCCESS;
        }
        return Const.AlipayCallback.RESPONSE_FAILED;
    }

    //前台轮询查询订单的支付状态接口
    //在二维码付款的页面，扫码付款成功之后，前台会调用这个接口，看是否付款成功，若成功会跳到订单
    @RequestMapping("query_order_pay_status.do")
    @ResponseBody
    public ServerResponse<Boolean> queryOrderPayStatus(HttpSession session, Long orderNo){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user ==null){//只有在登录的状态下才能操作
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),ResponseCode.NEED_LOGIN.getDesc());
        }
        //判断支付状态，如果是已支付返回true
        ServerResponse serverResponse = iOrderService.queryOrderPayStatus(user.getId(),orderNo);
        if(serverResponse.isSuccess()){
            return ServerResponse.createBySuccess(true);
        }
        return ServerResponse.createBySuccess(false);
    }
}
