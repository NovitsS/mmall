package com.novit.user.common;

import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.map.annotate.JsonSerialize;

import java.io.Serializable;

@JsonSerialize(include = JsonSerialize.Inclusion.NON_NULL)
//保证序列化json的时候,如果是null的对象,key也会消失;在调用没有data的方法/构造器的时候（类似于第一个），那么在json序列化之后，msg和data就都没有了
public class ServerResponse<T> implements Serializable {
    private int status;
    private String msg;
    private T data;

    private ServerResponse(int status){
        this.status = status;
    }

    private ServerResponse(int status,T data){
        this.status = status;
        this.data = data;
    }

    private ServerResponse(int status,String msg,T data){
        this.status = status;
        this.msg = msg;
        this.data = data;
    }

    private ServerResponse(int status,String msg){
        this.status = status;
        this.msg = msg;
    }

    @JsonIgnore
    //使之不在json序列化结果当中
    public boolean isSuccess(){
        return this.status == ResponseCode.SUCCESS.getCode();//如果status等于0就返回true，不是就返回false
    }

    public int getStatus(){
        return status;
    }
    public T getData(){
        return data;
    }
    public String getMsg(){
        return msg;
    }

    public static <T> ServerResponse<T> createBySuccess(){//创建这个对象，通过一个成功的，也就是说code是0，代表这个响应ok
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode());//返回一个status
    }

    public static <T> ServerResponse<T> createBySuccessMessage(String msg){//也是成功，但与上一个不同在于要返回一个文本
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg);
    }

    public static <T> ServerResponse<T> createBySuccess(T data){//单纯返回一个正确的数据
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),data);//这个响应是说，要成功创建一个服务器响应，然后把data填充进去
    }

    public static <T> ServerResponse<T> createBySuccess(String msg,T data){//消息和数据一起传过去
        return new ServerResponse<T>(ResponseCode.SUCCESS.getCode(),msg,data);
    }


    public static <T> ServerResponse<T> createByError(){
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),ResponseCode.ERROR.getDesc());
    }


    public static <T> ServerResponse<T> createByErrorMessage(String errorMessage){//返回错误的提示
        return new ServerResponse<T>(ResponseCode.ERROR.getCode(),errorMessage);
    }

    public static <T> ServerResponse<T> createByErrorCodeMessage(int errorCode,String errorMessage){//把code作为变量
        return new ServerResponse<T>(errorCode,errorMessage);
    }
}
