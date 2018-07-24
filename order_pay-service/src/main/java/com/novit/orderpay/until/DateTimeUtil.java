package com.novit.orderpay.until;

import org.apache.commons.lang3.StringUtils;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.Date;

public class DateTimeUtil {
    //joda-time

    public static final String STANDARD_FORMAT = "yyyy-MM-dd HH:mm:ss";


    //str->Date字符串转化成Date类型
    public static Date strToDate(String dateTimeStr, String formatStr){//dateTimeStr是时间字符串，formatStr是转成Date类型字符串的格式
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(formatStr);//用DateTimeFormatter进行声明
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);//用DateTime进行接收
        return dateTime.toDate();
    }

    //Date->str Date转化成字符串类型
    public static String dateToStr(Date date,String formatStr){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(formatStr);
    }


    //方法重载，使用时间的标准格式，在上面定义了
    public static Date strToDate(String dateTimeStr){
        DateTimeFormatter dateTimeFormatter = DateTimeFormat.forPattern(STANDARD_FORMAT);
        DateTime dateTime = dateTimeFormatter.parseDateTime(dateTimeStr);
        return dateTime.toDate();
    }

    public static String dateToStr(Date date){
        if(date == null){
            return StringUtils.EMPTY;
        }
        DateTime dateTime = new DateTime(date);
        return dateTime.toString(STANDARD_FORMAT);
    }


    public static void main(String[] args) {
        System.out.println(DateTimeUtil.dateToStr(new Date(),"yyyy-MM-dd HH:mm:ss"));
        System.out.println(DateTimeUtil.strToDate("2010-01-01 11:11:11","yyyy-MM-dd HH:mm:ss"));

    }
}
