package com.novit.user.domain.service;

import com.novit.user.common.Const;
import com.novit.user.common.ServerResponse;
import com.novit.user.common.TokenCache;
import com.novit.user.domain.model.User;
import com.novit.user.domain.repository.UserMapper;
import com.novit.user.until.MD5Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("iUserService")
//向上注入的时候就注入IUserService这个接口，属性名字就是iUserService，就可以注入上controller了
public class UserServiceImpl implements IUserService {

    @Autowired
    private UserMapper userMapper;

    @Override
    public ServerResponse<User> login(String username, String password) {
        int resultCount = userMapper.checkUsername(username);//检查登录的用户名存不存在
        if (resultCount == 0) {
            return ServerResponse.createByErrorMessage("用户名不存在");
        }

        //密码登录MD5
        //String md5Password = MD5Util.MD5EncodeUtf8(password);

        //检查用户名和密码是否正确
        User user = userMapper.selectLogin(username, password);//在插入数据库的时候密码已经被MD5加密过，此处sql要比较的就是加密后的password
        if (user == null) {
            return ServerResponse.createByErrorMessage("密码错误");//如果用户名不存在的话，上面就已经返回了，如果到了这，就是username和password没有匹配上
        }

        //如果逻辑到这里都没有return就需要处理返回值的密码了
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);//密码置空
        return ServerResponse.createBySuccess("登录成功", user);

    }

    public ServerResponse<String> register(User user){
//        int resultCount = userMapper.checkUsername(user.getUsername());//校验注册的时候用户名是否存在
//        if(resultCount > 0 ){
//            return ServerResponse.createByErrorMessage("用户名已存在");
//        }
        ServerResponse validResponse = this.checkValid(user.getUsername(),Const.USERNAME);//复用下面写的的校验功能，作用其实跟之前写的一样
        if(!validResponse.isSuccess()){//如果成功表示校验通过
            return validResponse;
        }

//        resultCount = userMapper.checkEmail(user.getEmail());
//        if(resultCount > 0 ){
//            return ServerResponse.createByErrorMessage("Email已存在");
//        }
        validResponse = this.checkValid(user.getEmail(),Const.EMAIL);//同理校验email，作用其实跟之前写的一样
        if(!validResponse.isSuccess()){
            return validResponse;
        }

        user.setRole(Const.Role.ROLE_CUSTOMER);
        //MD5加密
        //user.setPassword(MD5Util.MD5EncodeUtf8(user.getPassword()));
        int resultCount = userMapper.insert(user);//将该user插入到数据库中
        if(resultCount == 0){//可能是db或其他出现了问题
            return ServerResponse.createByErrorMessage("注册失败");
        }
        return ServerResponse.createBySuccessMessage("注册成功");
    }

    public ServerResponse<String> checkValid(String str,String type){//str是它的value值，type根据传的是email还是username来判断str调用哪个sql校验
        if(org.apache.commons.lang3.StringUtils.isNotBlank(type)){//如果type不是空的才开始校验
            //开始校验
            if(Const.USERNAME.equals(type)){//校验用户名（直接在上面拿代码 一样的）
                int resultCount = userMapper.checkUsername(str);
                if(resultCount > 0 ){
                    return ServerResponse.createByErrorMessage("用户名已存在");
                }
            }
            if(Const.EMAIL.equals(type)){//校验email（直接在上面拿代码 一样的）
                int resultCount = userMapper.checkEmail(str);
                if(resultCount > 0 ){
                    return ServerResponse.createByErrorMessage("email已存在");
                }
            }
        }else{
            return ServerResponse.createByErrorMessage("参数错误");
        }
        return ServerResponse.createBySuccessMessage("校验成功");
    }

    public ServerResponse selectQuestion(String username){
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);//校验用户名是否存在
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");//跟上面不同在于，一个取非的问题
        }
        String question = userMapper.selectQuestionByUsername(username);//查找question
        if(org.apache.commons.lang3.StringUtils.isNotBlank(question)){
            return ServerResponse.createBySuccess(question);//成功
        }
        return ServerResponse.createByErrorMessage("找回密码的问题是空的");//错误
    }

    public ServerResponse<String> checkAnswer(String username,String question,String answer){
        int resultCount = userMapper.checkAnswer(username,question,answer);//查找的是数量，并非具体用户。如果查找到的数量大于0，表示答案是对的
        if(resultCount>0){//说明问题及问题答案是这个用户的,并且是正确的
            String forgetToken = UUID.randomUUID().toString();//声明一个token，生成UUID
            TokenCache.setKey(TokenCache.TOKEN_PREFIX+username,forgetToken);//把forgetToken放到本地cache中，然后设置它的有效期。key加上了一个前缀，为了区分，也可抽象理解为一个namespace
            return ServerResponse.createBySuccess(forgetToken);
        }
        return ServerResponse.createByErrorMessage("问题的答案错误");
    }

    public ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken){
        if(org.apache.commons.lang3.StringUtils.isBlank(forgetToken)){//校验参数
            return ServerResponse.createByErrorMessage("参数错误,token需要传递");
        }
        //校验username，在拼接token的时候用的是token_username，若username为blank，那么这个key是有危险的，因为它变成了一个没有变量控制的命名
        ServerResponse validResponse = this.checkValid(username,Const.USERNAME);
        if(validResponse.isSuccess()){
            //用户不存在
            return ServerResponse.createByErrorMessage("用户不存在");
        }
        String token = TokenCache.getKey(TokenCache.TOKEN_PREFIX+username);//从cache中获取token
        if(org.apache.commons.lang3.StringUtils.isBlank(token)){//对cache里的token进行校验
            return ServerResponse.createByErrorMessage("token无效或者过期");
        }
        //用StringUtils中的equals就不用考虑空指针问题，即第一个参数（equals前面的对象）可以为null
        if(org.apache.commons.lang3.StringUtils.equals(forgetToken,token)){
            //String md5Password  = MD5Util.MD5EncodeUtf8(passwordNew);
            int rowCount = userMapper.updatePasswordByUsername(username,passwordNew);//生效行数

            if(rowCount > 0){
                return ServerResponse.createBySuccessMessage("修改密码成功");
            }
        }else{
            return ServerResponse.createByErrorMessage("token错误,请重新获取重置密码的token");
        }
        return ServerResponse.createByErrorMessage("修改密码失败");
    }

    public ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user){
        //防止横向越权,要校验一下这个用户的旧密码,一定要指定是这个用户.因为我们会查询一个count(1),如果不指定id,那么结果就是true啦count>0;
        //整个用户表里有很多密码，查询count出来，很大概率是大于0的，可以拿个字典不断的试这个接口，就能试出来。所以查询一定要加上userID
        int resultCount = userMapper.checkPassword(MD5Util.MD5EncodeUtf8(passwordOld),user.getId());
        if(resultCount == 0){
            return ServerResponse.createByErrorMessage("旧密码错误");
        }

        user.setPassword(MD5Util.MD5EncodeUtf8(passwordNew));
        int updateCount = userMapper.updateByPrimaryKeySelective(user);//根据主键有选择性的更新，方便扩展，此处是哪个不为空更新哪个，另一个是全部更新
        if(updateCount > 0){
            return ServerResponse.createBySuccessMessage("密码更新成功");
        }
        return ServerResponse.createByErrorMessage("密码更新失败");
    }

    public ServerResponse<User> updateInformation(User user){
        //username是不能被更新的
        //email也要进行一个校验,校验新的email是不是已经存在,并且存在的email如果相同的话,不能是我们当前的这个用户的.
        int resultCount = userMapper.checkEmailByUserId(user.getEmail(),user.getId());
        if(resultCount > 0){
            return ServerResponse.createByErrorMessage("email已存在,请更换email再尝试更新");
        }
        User updateUser = new User();//此处的对象只用于更新
        updateUser.setId(user.getId());
        updateUser.setEmail(user.getEmail());
        updateUser.setPhone(user.getPhone());
        updateUser.setQuestion(user.getQuestion());
        updateUser.setAnswer(user.getAnswer());

        int updateCount = userMapper.updateByPrimaryKeySelective(updateUser);//只有不等于空的时候才去更新
        if(updateCount > 0){
            return ServerResponse.createBySuccess("更新个人信息成功",updateUser);
        }
        return ServerResponse.createByErrorMessage("更新个人信息失败");
    }

    public ServerResponse<User> getInformation(Integer userId){
        User user = userMapper.selectByPrimaryKey(userId);//从db中获取这个userID
        if(user == null){//空判断
            return ServerResponse.createByErrorMessage("找不到当前用户");
        }
        user.setPassword(org.apache.commons.lang3.StringUtils.EMPTY);//正确的话就将密码设置为空
        return ServerResponse.createBySuccess(user);
    }

    //backend

    /**
     * 校验是否是管理员
     * @param user
     * @return
     */
    public ServerResponse checkAdminRole(User user){
        if(user != null && user.getRole().intValue() == Const.Role.ROLE_ADMIN){
            return ServerResponse.createBySuccess();
        }
        return ServerResponse.createByError();
    }

}
