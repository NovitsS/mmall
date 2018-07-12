package com.novit.user.resources;

import com.novit.user.common.Const;
import com.novit.user.common.ResponseCode;
import com.novit.user.common.ServerResponse;
import com.novit.user.domain.model.User;
import com.novit.user.domain.service.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpSession;

@Controller
@RequestMapping("/user/")   //把请求地址全部打到/user/这个命名空间下
public class UserController {

    @Autowired
    private IUserService iUserService;//注入IUserService

    //登录接口
    @RequestMapping(value = "login.do",method = RequestMethod.POST)
    @ResponseBody   //在返回的时候，自动通过springMVC的jackson插件将返回值序列化成json
    public ServerResponse<User> login(String username, String password, HttpSession session){//要判断登录状态就需要HttpSession
        //service-->mybatis-->dao
        ServerResponse<User> response = iUserService.login(username,password);//将username和password传过去
        if(response.isSuccess()){
            session.setAttribute(Const.CURRENT_USER,response.getData());//如果成功就往session里面把用户放进来
        }
        return response;
    }

    //登出接口
    @RequestMapping(value = "logout.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> logout(HttpSession session){
        session.removeAttribute(Const.CURRENT_USER);//将添加的currentuser删除即可
        return ServerResponse.createBySuccess();
    }

    //注册接口
    @RequestMapping(value = "register.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> register(User user){
        return iUserService.register(user);
    }

    //校验用户名和email是否存在，防止恶意用户通过接口调用我们的注册接口，当我们输入完用户名点击下一个input框的时候实时调用一个校验接口，在前台实时反馈
    @RequestMapping(value = "check_valid.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> checkValid(String str,String type){
        return iUserService.checkValid(str,type);
    }

    //获取用户登录信息接口
    @RequestMapping(value = "get_user_info.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> getUserInfo(HttpSession session){
        User user = (User) session.getAttribute(Const.CURRENT_USER);//从session中获取user
        if(user != null){
            return ServerResponse.createBySuccess(user);
        }
        return ServerResponse.createByErrorMessage("用户未登录,无法获取当前用户的信息");
    }

    //忘记密码接口
    @RequestMapping(value = "forget_get_question.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetGetQuestion(String username){
        return iUserService.selectQuestion(username);//其实是密码提示问题的获取
    }

    //校验问题答案是否正确
    @RequestMapping(value = "forget_check_answer.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetCheckAnswer(String username,String question,String answer){//在写service逻辑的时候会用到guava，利用本地的guava缓存来做token，利用其有效期搞定token
        return iUserService.checkAnswer(username,question,answer);
    }

    //忘记密码中的重置密码接口
    //需要上面的token，和缓存里的做对比
    @RequestMapping(value = "forget_reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> forgetRestPassword(String username,String passwordNew,String forgetToken){
        return iUserService.forgetResetPassword(username,passwordNew,forgetToken);
    }

    //登录状态下重置密码接口
    @RequestMapping(value = "reset_password.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<String> resetPassword(HttpSession session,String passwordOld,String passwordNew){
        User user = (User)session.getAttribute(Const.CURRENT_USER);//从session中获取用户
        if(user == null){
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        return iUserService.resetPassword(passwordOld,passwordNew,user);
    }

    //更新用户个人信息的接口
    @RequestMapping(value = "update_information.do",method = RequestMethod.POST)
    @ResponseBody
    //返回值是user，因为在更新完用户个人信息之后，要把新的用户信息放到session里，同时要把新的用户信息返回给前端，前端可以直接更新到页面上
    public ServerResponse<User> update_information(HttpSession session,User user){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);//跟上一个一样，获取用户判断登录状态
        if(currentUser == null){//只有在登录的状态下才能更新用户的信息
            return ServerResponse.createByErrorMessage("用户未登录");
        }
        user.setId(currentUser.getId());//user传过来的参数没有id，此处把userID赋成登录的id，防止id被变化
        user.setUsername(currentUser.getUsername());//id和username都是从登录用户中获取的，在更新个人信息的时候不能改变id和username
        ServerResponse<User> response = iUserService.updateInformation(user);//并且接到的返回值里的泛型user要放到session中，所以一定要有username（再次解释上面一句），否则session中存的对象就没有username了
        if(response.isSuccess()){
            response.getData().setUsername(currentUser.getUsername());
            session.setAttribute(Const.CURRENT_USER,response.getData());//更新session
        }
        return response;
    }

    //获取用户的详细信息接口，例如在个人中心修改用户个人信息的时候，首先要get_information，然后才调用上面的update_information
    //在get_information的时候就要判断使之强制登录，所以在update的时候只要判断是否登录就可以了
    @RequestMapping(value = "get_information.do",method = RequestMethod.POST)
    @ResponseBody
    public ServerResponse<User> get_information(HttpSession session){
        User currentUser = (User)session.getAttribute(Const.CURRENT_USER);//如果调用这个接口没有登录，要进行强制登陆
        if(currentUser == null){
            return ServerResponse.createByErrorCodeMessage(ResponseCode.NEED_LOGIN.getCode(),"未登录,需要强制登录status=10");//status=10就是NEED_LOGIN的状态码10，一旦传10就要强制登录
        }
        return iUserService.getInformation(currentUser.getId());
    }

}

