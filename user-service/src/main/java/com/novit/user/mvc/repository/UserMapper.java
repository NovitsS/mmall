package com.novit.user.mvc.repository;

import com.novit.user.mvc.model.User;
import org.apache.ibatis.annotations.Param;

public interface UserMapper {
    int deleteByPrimaryKey(Integer id);

    int insert(User record);//将该user插入到数据库中

    int insertSelective(User record);

    User selectByPrimaryKey(Integer id);

    int updateByPrimaryKeySelective(User record);

    int updateByPrimaryKey(User record);

    int checkUsername(String username);//检查用户名是否存在

    int checkEmail(String email);//检查email是否存在

    User selectLogin(@Param("username") String username, @Param("password")String password);//检查用户名和密码是否匹配（多参数就需要这样的mybatis注解）

    String selectQuestionByUsername(String username);//通过用户名查找密码提示问题

    int checkAnswer(@Param("username")String username,@Param("question")String question,@Param("answer")String answer);//校验问题答案是否正确

    int updatePasswordByUsername(@Param("username")String username,@Param("passwordNew")String passwordNew);//通过用户名重置密码

    int checkPassword(@Param(value="password")String password,@Param("userId")Integer userId);//校验密码
    //校验email的时候查库里有没有email，并且这个email所属的user不是当前登录的user，如果查到结果的话说明这个email是其他人已经占用的了，所以count>0的时候代表email已经存在，无法更新
    int checkEmailByUserId(@Param(value="email")String email,@Param(value="userId")Integer userId);
}
