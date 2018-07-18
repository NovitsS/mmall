package com.novit.cart.until;

import java.math.BigDecimal;

public class BigDecimalUtil {

    //使这个构造器不能在外部进行实例化
    private BigDecimalUtil(){

    }

    public static BigDecimal add(double v1, double v2){//加法
        BigDecimal b1 = new BigDecimal(Double.toString(v1));//这个方法就把double的v1转化成了string，然后就会调用BigDecimal的string构造器
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.add(b2);
    }

    public static BigDecimal sub(double v1,double v2){//减法
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.subtract(b2);
    }

    public static BigDecimal mul(double v1,double v2){//乘法
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.multiply(b2);
    }

    public static BigDecimal div(double v1,double v2){//除法
        BigDecimal b1 = new BigDecimal(Double.toString(v1));
        BigDecimal b2 = new BigDecimal(Double.toString(v2));
        return b1.divide(b2,2,BigDecimal.ROUND_HALF_UP);//四舍五入,保留2位小数，第二个是保留小数的位数，最后一个是保留方法，此处四舍五入

        //除不尽的情况
    }
}
