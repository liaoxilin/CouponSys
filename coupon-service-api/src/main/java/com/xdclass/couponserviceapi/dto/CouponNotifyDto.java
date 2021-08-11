package com.xdclass.couponserviceapi.dto;

import java.io.Serializable;
import java.util.Date;

public class CouponNotifyDto implements Serializable {
    private Integer id;

    private String code;

    private String picUrl;

    /**
     * 达到金额，如满500减50
     */
    private Integer achieveAmount;

    /**
     * 优惠金额
     */
    private Integer reduceAmount;

    private Integer stock;

    private String title;

    private Integer status;

    private Integer userId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getPicUrl() {
        return picUrl;
    }

    public void setPicUrl(String picUrl) {
        this.picUrl = picUrl;
    }

    public Integer getAchieveAmount() {
        return achieveAmount;
    }

    public void setAchieveAmount(Integer achieveAmount) {
        this.achieveAmount = achieveAmount;
    }

    public Integer getReduceAmount() {
        return reduceAmount;
    }

    public void setReduceAmount(Integer reduceAmount) {
        this.reduceAmount = reduceAmount;
    }

    public Integer getStock() {
        return stock;
    }

    public void setStock(Integer stock) {
        this.stock = stock;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }


    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append(getClass().getSimpleName());
        sb.append(" [");
        sb.append("Hash = ").append(hashCode());
        sb.append(", id=").append(id);
        sb.append(", code=").append(code);
        sb.append(", picUrl=").append(picUrl);
        sb.append(", achieveAmount=").append(achieveAmount);
        sb.append(", reduceAmount=").append(reduceAmount);
        sb.append(", stock=").append(stock);
        sb.append(", title=").append(title);
        sb.append(", status=").append(status);
        sb.append("]");
        return sb.toString();
    }


}
