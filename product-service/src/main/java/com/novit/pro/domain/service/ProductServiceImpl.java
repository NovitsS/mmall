package com.novit.pro.domain.service;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.google.common.collect.Lists;
import com.novit.pro.common.Const;
import com.novit.pro.common.ResponseCode;
import com.novit.pro.common.ServerResponse;
import com.novit.pro.domain.model.Category;
import com.novit.pro.domain.model.Product;
import com.novit.pro.domain.model.ProductDetailVo;
import com.novit.pro.domain.model.ProductListVo;
import com.novit.pro.domain.repository.CategoryMapper;
import com.novit.pro.domain.repository.ProductMapper;
import com.novit.pro.until.DateTimeUtil;
import com.novit.pro.until.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service("iProductService")
public class ProductServiceImpl implements IProductService {

    @Autowired
    private ProductMapper productMapper;

    @Autowired
    private CategoryMapper categoryMapper;

    @Autowired
    private ICategoryService iCategoryService;

    //保存或更新产品，对于前端来说保存和更新是两个操作，但对于后台来说可以用一个接口搞定。只不过要判断对于这个产品是新增还是保存还是更新，insert和update的判断
    public ServerResponse saveOrUpdateProduct(Product product){
        if(product != null) {
            if(StringUtils.isNotBlank(product.getSubImages())){//判断子图是否为空
                String[] subImageArray = product.getSubImages().split(",");//将其用逗号分隔
                if(subImageArray.length > 0){
                    product.setMainImage(subImageArray[0]);//取子图的第一个赋值给主图
                }
            }

            //此处判断对该产品是新增还是更新，若productId为空则新增，不为空则是更新
            if(product.getId() != null){//如果要更新，productId肯定不为空
                int rowCount = productMapper.updateByPrimaryKey(product);//rowCount代表更新成功的数量
                if(rowCount > 0){
                    return ServerResponse.createBySuccess("更新产品成功");
                }
                return ServerResponse.createBySuccess("更新产品失败");
            }else{//如果为空的话就insert该产品
                int rowCount = productMapper.insert(product);
                if(rowCount > 0){
                    return ServerResponse.createBySuccess("新增产品成功");
                }
                return ServerResponse.createBySuccess("新增产品失败");
            }
        }
        return ServerResponse.createByErrorMessage("新增或更新产品参数不正确");//产品为空
    }

    public ServerResponse<String> setSaleStatus(Integer productId,Integer status){
        if(productId == null || status == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = new Product();//创建对象
        product.setId(productId);
        product.setStatus(status);//将id和status传过来
        int rowCount = productMapper.updateByPrimaryKeySelective(product);
        if(rowCount > 0){
            return ServerResponse.createBySuccess("修改产品销售状态成功");
        }
        return ServerResponse.createByErrorMessage("修改产品销售状态失败");
    }

    //VO对象——value object，承载对象各个值的对象，里面承载了各种数据要求的值
    public ServerResponse<ProductDetailVo> manageProductDetail(Integer productId){
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);//声明一个private的assemble方法，通过product把ProductDetailVo组装上
        return ServerResponse.createBySuccess(productDetailVo);
    }

    private ProductDetailVo assembleProductDetailVo(Product product){
        ProductDetailVo productDetailVo = new ProductDetailVo();
        productDetailVo.setId(product.getId());
        productDetailVo.setSubtitle(product.getSubtitle());
        productDetailVo.setPrice(product.getPrice());
        productDetailVo.setMainImage(product.getMainImage());
        productDetailVo.setSubImages(product.getSubImages());
        productDetailVo.setCategoryId(product.getCategoryId());
        productDetailVo.setDetail(product.getDetail());
        productDetailVo.setName(product.getName());
        productDetailVo.setStatus(product.getStatus());
        productDetailVo.setStock(product.getStock());//库存
        //TODO:property
        //imageHost要从配置文件中获取，配置和代码分离，不需要把URL硬编码到项目当中，如果图片服务器修改的话只需要改properties配置
        //productDetailVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));//获取的时候会把http://img.happymmall.com/填充到imageHost里

        //parentCategoryId
        Category category = categoryMapper.selectByPrimaryKey(product.getCategoryId());
        if(category == null){
            productDetailVo.setParentCategoryId(0);//默认根节点
        }else{
            productDetailVo.setParentCategoryId(category.getParentId());
        }

        //createTime
        productDetailVo.setCreateTime(DateTimeUtil.dateToStr(product.getCreateTime()));
        //updateTime
        productDetailVo.setUpdateTime(DateTimeUtil.dateToStr(product.getUpdateTime()));
        return productDetailVo;
    }


    public ServerResponse<PageInfo> getProductList(int pageNum, int pageSize){
        //pageHelper使用方法第一步startPage--start，记录开始
        //第二步填充自己的sql查询逻辑
        //第三步pageHelper-收尾
        PageHelper.startPage(pageNum,pageSize);//首先进行第一步调用startPage
        List<Product> productList = productMapper.selectList();//第二步执行sql逻辑

        List<ProductListVo> productListVoList = Lists.newArrayList();//list不需要product的所有详情，毕竟只是list不是详情，所以创建一个ProductListVo来存放需要的属性
        for(Product productItem : productList){//for each循环这个集合
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);//第三步进行收尾，分页结果直接通过构造器，里面就传sql返回的集合，它就会根据这个集合进行自动的分页处理
        pageResult.setList(productListVoList);//前端展示不是把整个product给它，但是还要运用productList进行分页，这时候把list重置就可以了。把vo放到list里，把那个list放到这里，这样分页的结果里面的集合就是想要的productListVo了
        return ServerResponse.createBySuccess(pageResult);
    }

    private ProductListVo assembleProductListVo(Product product){//private的assemble方法，通过product把ProductListVo组装上
        ProductListVo productListVo = new ProductListVo();
        productListVo.setId(product.getId());
        productListVo.setName(product.getName());
        productListVo.setCategoryId(product.getCategoryId());
        //TODO:property
       // productListVo.setImageHost(PropertiesUtil.getProperty("ftp.server.http.prefix","http://img.happymmall.com/"));
        productListVo.setMainImage(product.getMainImage());
        productListVo.setPrice(product.getPrice());
        productListVo.setSubtitle(product.getSubtitle());
        productListVo.setStatus(product.getStatus());
        return productListVo;
    }

    //因为也需要分页，所以在泛型里写上PageInfo对象
    public ServerResponse<PageInfo> searchProduct(String productName,Integer productId,int pageNum,int pageSize){
        PageHelper.startPage(pageNum,pageSize);//pageHelper第一步
        if(StringUtils.isNotBlank(productName)){//对productName进行判断，如果不为空，就用sql中的某个查询构建productName
            productName = new StringBuilder().append("%").append(productName).append("%").toString();
        }
        //进行查询，根据productName和productId，进行空判断，如果空的话就不把条件放到sql查询当中
        List<Product> productList = productMapper.selectByNameAndProductId(productName,productId);//pageHelper第二步
        List<ProductListVo> productListVoList = Lists.newArrayList();//把product转成productListVo，同上面方法中的转化方式
        for(Product productItem : productList){
            ProductListVo productListVo = assembleProductListVo(productItem);
            productListVoList.add(productListVo);
        }
        PageInfo pageResult = new PageInfo(productList);//pageHelper第三步
        pageResult.setList(productListVoList);
        return ServerResponse.createBySuccess(pageResult);
    }

    //前台的获取商品详情
    //与后台获取商品详情的不同之处在于，在前台查看商品详情的时候，要判断产品是否处于在线状态，不在线就直接不返回了或者返回错误
    public ServerResponse<ProductDetailVo> getProductDetail(Integer productId){
        if(productId == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        Product product = productMapper.selectByPrimaryKey(productId);
        if(product == null){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }//上面的与后台获取商品详情相同
        if(product.getStatus() != Const.ProductStatusEnum.ON_SALE.getCode()){
            return ServerResponse.createByErrorMessage("产品已下架或者删除");
        }
        ProductDetailVo productDetailVo = assembleProductDetailVo(product);
        return ServerResponse.createBySuccess(productDetailVo);
    }

    public ServerResponse<PageInfo> getProductByKeywordCategory(String keyword,Integer categoryId,int pageNum,int pageSize,String orderBy){
        if(StringUtils.isBlank(keyword) && categoryId == null){//参数校验
            return ServerResponse.createByErrorCodeMessage(ResponseCode.ILLEGAL_ARGUMENT.getCode(),ResponseCode.ILLEGAL_ARGUMENT.getDesc());
        }
        List<Integer> categoryIdList = new ArrayList<Integer>();
        //该集合的用处是，当传分类的时候时，假设传一个高级的分类，例如传电子产品，电子产品下有手机，手机下还有智能机非智能机等
        //当传一个大的父类的时候，将调用之前的递归算法，把所有属于这个分类的子分类便利出来，并且加上它本身。把这些categoryId放到categoryIdList里
        //查询sql的时候直接命中产品所在集合的条件


        if(categoryId != null){//单独判断categoryId
            Category category = categoryMapper.selectByPrimaryKey(categoryId);
            if(category == null && StringUtils.isBlank(keyword)){
                //没有该分类,并且还没有关键字,这个时候返回一个空的结果集,不报错
                PageHelper.startPage(pageNum,pageSize);//PageHelper第一步
                List<ProductListVo> productListVoList = Lists.newArrayList();
                PageInfo pageInfo = new PageInfo(productListVoList);
                return ServerResponse.createBySuccess(pageInfo);
            }
            categoryIdList = iCategoryService.selectCategoryAndChildrenById(category.getId()).getData();//调用递归算法
        }
        if(StringUtils.isNotBlank(keyword)){//判断关键字
            keyword = new StringBuilder().append("%").append(keyword).append("%").toString();//进行拼接
        }

        PageHelper.startPage(pageNum,pageSize);
        //排序处理
        if(StringUtils.isNotBlank(orderBy)){
            if(Const.ProductListOrderBy.PRICE_ASC_DESC.contains(orderBy)){
                String[] orderByArray = orderBy.split("_");//用下划线进行分割
                PageHelper.orderBy(orderByArray[0]+" "+orderByArray[1]);
            }
        }
        List<Product> productList = productMapper.selectByNameAndCategoryIds(StringUtils.isBlank(keyword)?null:keyword,categoryIdList.size()==0?null:categoryIdList);

        List<ProductListVo> productListVoList = Lists.newArrayList();//把product转成productListVo，同getProductList方法中的转化方式
        for(Product product : productList){
            ProductListVo productListVo = assembleProductListVo(product);
            productListVoList.add(productListVo);
        }

        PageInfo pageInfo = new PageInfo(productList);
        pageInfo.setList(productListVoList);
        return ServerResponse.createBySuccess(pageInfo);
    }
}
