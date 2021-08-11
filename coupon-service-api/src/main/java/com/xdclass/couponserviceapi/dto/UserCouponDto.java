package com.xdclass.couponserviceapi.dto;

import org.omg.CORBA.PRIVATE_MEMBER;

import java.io.Serializable;
import java.util.Date;

public class UserCouponDto implements Serializable {


    private Integer couponId;

    private Integer userId;

    private Integer orderId;

    private String userCouponCode;

    public String getUserCouponCode() {
        return userCouponCode;
    }

    public void setUserCouponCode(String userCouponCode) {
        this.userCouponCode = userCouponCode;
    }

    public Integer getCouponId() {
        return couponId;
    }

    public void setCouponId(Integer couponId) {
        this.couponId = couponId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getOrderId() {
        return orderId;
    }

    public void setOrderId(Integer orderId) {
        this.orderId = orderId;
    }
}
