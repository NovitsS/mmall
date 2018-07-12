package com.novit.user.common;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

public class TokenCache {
    private static Logger logger = LoggerFactory.getLogger(TokenCache.class);//声明日志

    public static final String TOKEN_PREFIX = "token_";//key的前缀，为了区分，也可抽象理解为一个namespace

    // 声明一个静态内存块,LoadingCache是guava里的本地缓存
    // CacheBuilder后开始构建本地cache。缓存的初始化容量1000，缓存的最大容量10000，当超过这个容量的时候，guava的cache会使用LRU算法（最少使用算法）来移除缓存项
    // expireAfterAccess设置有效期，设置为12小时
    // CacheLoader是一个抽象类，要在里面写匿名实现
    private  static LoadingCache<String,String> localCache = CacheBuilder.newBuilder().initialCapacity(1000).maximumSize(10000).expireAfterAccess(12, TimeUnit.HOURS)
            .build(new CacheLoader<String, String>() {
                //默认的数据加载实现,当调用get取值的时候,如果key没有对应的值,就调用这个方法进行加载.
                @Override
                public String load(String s) throws Exception {
                    return "null";
                }
            });

    public static void setKey(String key,String value){
        localCache.put(key,value);
    }

    public static String getKey(String key){
        String value = null;//初始化为空
        try {
            value = localCache.get(key);
            if("null".equals(value)){//空判断
                return null;
            }
            return value;
        }catch (Exception e){
            logger.error("localCache get error",e);//打印异常堆栈
        }
        return null;
    }
}
