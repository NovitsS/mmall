package com.novit.pro.resources;

import com.google.common.collect.Maps;
import com.novit.pro.common.Const;
import com.novit.pro.common.ResponseCode;
import com.novit.pro.common.ServerResponse;
import com.novit.pro.domain.model.Product;
import com.novit.pro.domain.model.User;
import com.novit.pro.domain.service.IFileService;
import com.novit.pro.domain.service.IProductService;
import com.novit.pro.until.PropertiesUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.Map;

@Controller
@RequestMapping("/manage/product")
public class ProductManageController {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private IProductService iProductService;

    @Autowired
    private IFileService iFileService;

    //新增或更新产品的接口
    @RequestMapping("save.do")
    @ResponseBody
    public ServerResponse productSave(HttpSession session, Product product){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//判断登录
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }
        String str=restTemplate.getForObject("http://user-service/user/check_admin?admin_name="+user.getRole(),String.class);
        if(str.equals("1")){
            //填充我们增加产品的业务逻辑
            return iProductService.saveOrUpdateProduct(product);
//            return str;
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
//            return str;
        }
    }

    //产品上下架的接口，也就是更新产品销售的状态
    @RequestMapping("set_sale_status.do")
    @ResponseBody
    public ServerResponse setSaleStatus(HttpSession session, Integer productId,Integer status){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//判断登录
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }
        String str=restTemplate.getForObject("http://user-service/user/check_admin?admin_name="+user.getRole(),String.class);
        if(str.equals("1")){
            return iProductService.setSaleStatus(productId,status);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
//
    //获取产品详情的接口
    @RequestMapping("detail.do")
    @ResponseBody
    public ServerResponse getDetail(HttpSession session, Integer productId){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//判断登录
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }
        String str=restTemplate.getForObject("http://user-service/user/check_admin?admin_name="+user.getRole(),String.class);
        if(str.equals("1")){
            //填充业务
            return iProductService.manageProductDetail(productId);

        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
//
    //产品接口list（后台商品列表动态分页）
    @RequestMapping("list.do")
    @ResponseBody
    //参数中pageNum是第几页，默认为第一页，pageSize页面容量，默认一页容量为10条
    public ServerResponse getList(HttpSession session, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum, @RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//判断登录
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }
        String str=restTemplate.getForObject("http://user-service/user/check_admin?admin_name="+user.getRole(),String.class);
        if(str.equals("1")){
            //填充动态分页
            return iProductService.getProductList(pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
//
    //后台产品搜索接口
    @RequestMapping("search.do")
    @ResponseBody
    public ServerResponse productSearch(HttpSession session,String productName,Integer productId, @RequestParam(value = "pageNum",defaultValue = "1") int pageNum,@RequestParam(value = "pageSize",defaultValue = "10") int pageSize){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//判断登录
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }
        String str=restTemplate.getForObject("http://user-service/user/check_admin?admin_name="+user.getRole(),String.class);
        if(str.equals("1")){
            //填充业务
            return iProductService.searchProduct(productName,productId,pageNum,pageSize);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }
//
    //文件上传（图片上传）接口，在编辑产品的时候，把产品的图片上传到服务器上
    @RequestMapping("upload.do")
    @ResponseBody
    //参数中MultipartFile是SpringMVC的文件上传，HttpServletRequest是根据Servlet的上下文动态的创建一个相对路径出来
    public ServerResponse upload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//判断登录
        if(user == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"用户未登录,请登录管理员");
        }
        String str=restTemplate.getForObject("http://user-service/user/check_admin?admin_name="+user.getRole(),String.class);
        if(str.equals("1")){
            String path = request.getSession().getServletContext().getRealPath("upload");
            //首先拿到path，从request的getSession的getServletContext中拿到servlet的上下文，然后通过getRealPath方法，
            //将要上传的文件夹名称叫upload，上传后会存到webapp与WEB-INF和index.jsp同级

            String targetFileName = iFileService.upload(file,path);
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;//根据与前端的约定要把前缀拿过来

            Map fileMap = Maps.newHashMap();
            fileMap.put("uri",targetFileName);
            fileMap.put("url",url);
            return ServerResponse.createBySuccess(fileMap);
        }else{
            return ServerResponse.createByErrorMessage("无权限操作");
        }
    }

//    //富文本上传接口
    @RequestMapping("richtext_img_upload.do")
    @ResponseBody
    //参数大体与上面一个一样，有一个区别是，在用富文本上传的时候需要修改response的header
    public Map richtextImgUpload(HttpSession session, @RequestParam(value = "upload_file",required = false) MultipartFile file, HttpServletRequest request, HttpServletResponse response){
        Map resultMap = Maps.newHashMap();
        User user = (User)session.getAttribute(Const.CURRENT_USER);//判断登录
        if(user == null){
            resultMap.put("success",false);//因为没有权限所以success字段是false
            resultMap.put("msg","请登录管理员");
            return resultMap;
        }
        //富文本中对于返回值有自己的要求,我们使用是simditor所以按照simditor的要求进行返回
        //JSON response after uploading complete:上传完成之后，JSON返回相应格式，所以返回格式应该如下
//        {
//            "success": true/false,
//            "msg": "error message", # optional
//            "file_path": "[real file path]"
//        }
        String str=restTemplate.getForObject("http://user-service/user/check_admin?admin_name="+user.getRole(),String.class);
        if(str.equals("1")){
            String path = request.getSession().getServletContext().getRealPath("upload");
            String targetFileName = iFileService.upload(file,path);//与上面方法中一样
            if(StringUtils.isBlank(targetFileName)){
                resultMap.put("success",false);
                resultMap.put("msg","上传失败");
                return resultMap;
            }
            String url = PropertiesUtil.getProperty("ftp.server.http.prefix")+targetFileName;
            resultMap.put("success",true);
            resultMap.put("msg","上传成功");
            resultMap.put("file_path",url);
            response.addHeader("Access-Control-Allow-Headers","X-File-Name");
            return resultMap;
        }else{
            resultMap.put("success",false);
            resultMap.put("msg","无权限操作");
            return resultMap;
        }
    }
}
