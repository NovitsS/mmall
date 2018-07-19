package com.novit.pro.resources;

import com.github.pagehelper.PageInfo;
import com.novit.pro.common.ServerResponse;
import com.novit.pro.domain.model.Product;
import com.novit.pro.domain.model.ProductDetailVo;
import com.novit.pro.domain.repository.ProductMapper;
import com.novit.pro.domain.service.IProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
@RequestMapping("/product/")
public class ProductController {

    @Autowired
    private IProductService iProductService;
    @Autowired
    private ProductMapper productMapper;

    //前台获取商品详情的接口
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse<ProductDetailVo> detail(Integer productId){
        return iProductService.getProductDetail(productId);
    }

    //前台产品列表接口（用户搜索时的请求，返回list）
    @RequestMapping("list.do")
    @ResponseBody
    public ServerResponse<PageInfo> list(@RequestParam(value = "keyword",required = false)String keyword,
                                         @RequestParam(value = "categoryId",required = false)Integer categoryId,
                                         @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,
                                         @RequestParam(value = "pageSize",defaultValue = "10") int pageSize,
                                         @RequestParam(value = "orderBy",defaultValue = "") String orderBy){
        return iProductService.getProductByKeywordCategory(keyword,categoryId,pageNum,pageSize,orderBy);
    }

    @RequestMapping("get")
    @ResponseBody
    public Product getPro(@RequestParam(value="id")int id){
        return productMapper.selectByPrimaryKey(id);
    }
}
