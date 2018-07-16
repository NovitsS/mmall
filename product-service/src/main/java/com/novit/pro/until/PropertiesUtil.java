package com.novit.pro.until;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public class PropertiesUtil {

    private static Logger logger = LoggerFactory.getLogger(PropertiesUtil.class);

    private static Properties props;

    //使用静态块，使tomcat启动的时候就读取到里面的配置
    static {
        String fileName = "mmall.properties";
        props = new Properties();
        try {
            props.load(new InputStreamReader(PropertiesUtil.class.getClassLoader().getResourceAsStream(fileName),"UTF-8"));
        } catch (IOException e) {
            logger.error("配置文件读取异常",e);
        }
    }

    //通过mmall.properties里面的key来获取它的value
    public static String getProperty(String key){
        String value = props.getProperty(key.trim());//传的时候避免两端空格，trim一下
        if(StringUtils.isBlank(value)){
            return null;
        }
        return value.trim();
    }

    //方法重载，如果value为空就传默认的defaultValue
    public static String getProperty(String key,String defaultValue){
        String value = props.getProperty(key.trim());
        if(StringUtils.isBlank(value)){
            value = defaultValue;
        }
        return value.trim();
    }

}
