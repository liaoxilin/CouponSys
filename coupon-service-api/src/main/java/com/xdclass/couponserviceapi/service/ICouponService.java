package com.xdclass.couponserviceapi.service;

import com.xdclass.couponserviceapi.dto.CouponDto;
import com.xdclass.couponserviceapi.dto.CouponNotifyDto;
import com.xdclass.couponserviceapi.dto.UserCouponDto;
import com.xdclass.couponserviceapi.dto.UserCouponInfoDto;

import java.util.List;

public interface ICouponService {

    /**
     * 用户领券功能
     */
    public String saveUserCoupon(UserCouponDto userCouponDto);

    public List<CouponDto> getCouponList();

    public List<UserCouponInfoDto> userCouponList(Integer userId);

    public List<CouponNotifyDto> queryCouponNotice();

}
