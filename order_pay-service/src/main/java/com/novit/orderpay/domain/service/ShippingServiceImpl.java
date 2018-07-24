package com.novit.orderpay.domain.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Maps;
import com.novit.orderpay.common.ServerResponse;
import com.novit.orderpay.domain.model.Shipping;
import com.novit.orderpay.domain.repository.ShippingMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service("iShippingService")
public class ShippingServiceImpl implements IShippingService {

    @Autowired
    private ShippingMapper shippingMapper;

    public ServerResponse add(Integer userId, Shipping shipping){
        shipping.setUserId(userId);
        int rowCount = shippingMapper.insert(shipping);//调用insert方法，判断生效行数，为了使新增之后立刻拿到主键id，在insert中增加两个配置
        if(rowCount > 0){
            Map result = Maps.newHashMap();//insert之后返回shippingId，并且shippingId是key，不需要单独创建对象来承载数据，直接用map
            result.put("shippingId",shipping.getId());//前面是key，后面是value
            return ServerResponse.createBySuccess("新建地址成功",result);
        }
        return ServerResponse.createByErrorMessage("新建地址失败");
    }

    public ServerResponse<String> del(Integer userId,Integer shippingId){
        int resultCount = shippingMapper.deleteByShippingIdUserId(userId,shippingId);//如果使用deleteByPrimaryKey，就没有跟登录的用户关联，产生横向越权问题
        if(resultCount > 0){//删除的数量大于0
            return ServerResponse.createBySuccess("删除地址成功");
        }
        return ServerResponse.createByErrorMessage("删除地址失败");
    }

    public ServerResponse update(Integer userId, Shipping shipping){
        shipping.setUserId(userId);//shipping里的userId也是可以模拟的，如果不从登录用户取userId的话，直接用传过来的userId，会把别人的地址更新掉
        int rowCount = shippingMapper.updateByShipping(shipping);//更新也有越权问题，要指定判断userId再更新
        if(rowCount > 0){
            return ServerResponse.createBySuccess("更新地址成功");
        }
        return ServerResponse.createByErrorMessage("更新地址失败");
    }

    public ServerResponse<Shipping> select(Integer userId, Integer shippingId){
        Shipping shipping = shippingMapper.selectByShippingIdUserId(userId,shippingId);//同样存在横向越权问题，编写一个根据userId来查找的sql
        if(shipping == null){
            return ServerResponse.createByErrorMessage("无法查询到该地址");
        }
        return ServerResponse.createBySuccess("更新地址成功",shipping);
    }

    public ServerResponse<PageInfo> list(Integer userId, int pageNum, int pageSize){
        PageHelper.startPage(pageNum,pageSize);
        List<Shipping> shippingList = shippingMapper.selectByUserId(userId);//查询该用户下所有的地址
        PageInfo pageInfo = new PageInfo(shippingList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
