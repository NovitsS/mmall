package com.novit.user.mvc.service;

import com.novit.user.common.ServerResponse;
import com.novit.user.mvc.model.User;


public interface IUserService {
    ServerResponse<User> login(String username, String password);//通过泛型实现通用数据响应对象

    ServerResponse<String> register(User user);

    ServerResponse<String> checkValid(String str,String type);

    ServerResponse selectQuestion(String username);

    ServerResponse<String> checkAnswer(String username,String question,String answer);

    ServerResponse<String> forgetResetPassword(String username,String passwordNew,String forgetToken);

    ServerResponse<String> resetPassword(String passwordOld,String passwordNew,User user);

    ServerResponse<User> updateInformation(User user);

    ServerResponse<User> getInformation(Integer userId);

    ServerResponse checkAdminRole(User user);
}

