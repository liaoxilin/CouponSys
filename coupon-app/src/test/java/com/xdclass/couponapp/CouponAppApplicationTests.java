package com.xdclass.couponapp;


import com.alibaba.fastjson.JSON;
import com.xdclass.couponapp.domain.TCoupon;
import com.xdclass.couponapp.domain.TCouponExample;
import com.xdclass.couponapp.mapper.TCouponMapper;
import com.xdclass.couponapp.service.CouponService;
import com.xdclass.couponserviceapi.dto.CouponDto;
import com.xdclass.couponserviceapi.dto.UserCouponDto;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.test.context.junit4.SpringRunner;

import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.UUID;

@RunWith(SpringRunner.class)
@SpringBootTest(classes = CouponAppApplication.class)
public class CouponAppApplicationTests {


    @Resource
    private CouponService couponService;

    @Resource
    private TCouponMapper tCouponMapper;

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    public void contextLoads() {
        couponService.updateCoupon("1_1012");
        couponService.updateCoupon("1_1003");
        couponService.updateCoupon("1_1004");
        couponService.updateCoupon("1_1005");
        couponService.updateCoupon("1_1006");
        couponService.updateCoupon("1_1007");
        couponService.updateCoupon("1_1008");
        couponService.updateCoupon("1_1009");
        couponService.updateCoupon("1_1010");
        couponService.updateCoupon("1_1011");
        System.err.println("hello world");
    }
    @Test
    public void queryCouponNotice(){
        String ret=JSON.toJSONString(couponService.queryCouponNotice());
        System.err.println(ret);
    }

    @Test
    public void insert(){
        for(int i=0;i<100000;i++){
            TCoupon tCoupon = new TCoupon();
            tCoupon.setAchieveAmount(500);
            tCoupon.setReduceAmount(20);
            tCoupon.setCreateTime(new Date());
            tCoupon.setCode(UUID.randomUUID().toString());
            tCoupon.setPicUrl("1.jpg");
            tCoupon.setStatus(0);
            tCoupon.setStock(10);
            tCoupon.setTitle("测试coupon");
            tCouponMapper.insert(tCoupon);
        }

    }

    @Test
    public void delete(){
        tCouponMapper.deleteByPrimaryKey(7);
    }

    @Test
    public void update(){
        TCoupon tCoupon = new TCoupon();
        tCoupon.setId(8);
        tCoupon.setCode("9527");
        tCouponMapper.updateByPrimaryKeySelective(tCoupon);
        tCouponMapper.updateByPrimaryKey(tCoupon);
    }


    @Test
    public void select(){
        // select * from t_coupon where code = "00415d96-49bd-4cce-83e3-08302b9aa084" and status=0 and achieve_amount between (100,1000) and title not like '%111%';
        TCouponExample example = new TCouponExample();
        example.createCriteria().andCodeEqualTo("00415d96-49bd-4cce-83e3-08302b9aa084").andStatusEqualTo(0)
                .andAchieveAmountBetween(100,1000).andTitleNotLike("111");
        List<TCoupon> tCoupon =  tCouponMapper.selectByExample(example);
        System.err.println(tCoupon);
    }

    @Test
    public void testquery(){
        List<CouponDto> tCoupon = couponService.getCouponList();
        System.out.println(tCoupon);
    }

    @Test
    public void testSaveUserCoupon(){
        UserCouponDto userCouponDto=new UserCouponDto();
        userCouponDto.setUserId(1234);
        userCouponDto.setCouponId(1002);
        userCouponDto.setOrderId(10086);
        System.err.println(couponService.saveUserCoupon(userCouponDto));
    }

    @Test
    public void testUserCouponList(){
        System.err.println(JSON.toJSONString(couponService.userCouponList(1234)));
    }

    @Test
    public void testRedis(){
        //redisTemplate.opsForValue().set("name","liaoliao");
        redisTemplate.opsForZSet().add("mySet","one",1);
        redisTemplate.opsForZSet().add("mySet","two",2);
        redisTemplate.opsForZSet().add("mySet","three",3);
        redisTemplate.opsForZSet().add("mySet","four",4);


        System.out.println(JSON.toJSONString(redisTemplate.opsForZSet().range("mySet",0,-1)));
    }
}
