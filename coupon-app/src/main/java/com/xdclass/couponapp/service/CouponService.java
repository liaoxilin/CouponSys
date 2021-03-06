package com.xdclass.couponapp.service;
import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.google.common.collect.Lists;
import com.xdclass.couponapp.constant.Constant;
import com.xdclass.couponapp.domain.TCoupon;
import com.xdclass.couponapp.domain.TCouponExample;
import com.xdclass.couponapp.domain.TUserCoupon;
import com.xdclass.couponapp.domain.TUserCouponExample;
import com.xdclass.couponapp.mapper.TCouponMapper;
import com.xdclass.couponapp.mapper.TUserCouponMapper;
import com.xdclass.couponapp.util.SnowflakeIdWorker;
import com.xdclass.couponserviceapi.dto.CouponDto;
import com.xdclass.couponserviceapi.dto.CouponNotifyDto;
import com.xdclass.couponserviceapi.dto.UserCouponDto;
import com.xdclass.couponserviceapi.dto.UserCouponInfoDto;
import com.xdclass.couponserviceapi.service.ICouponService;
import com.xdclass.userapi.service.IUserService;
import org.apache.commons.lang3.StringUtils;
import org.apache.dubbo.config.annotation.Reference;
import org.apache.dubbo.config.annotation.Service;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeanUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.CollectionUtils;


import javax.annotation.Resource;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class CouponService implements ICouponService{

    @Resource
    private TCouponMapper tCouponMapper;
    @Resource
    private TUserCouponMapper tUserCouponMapper;

    @Resource
    private RedisTemplate redisTemplate;


    @Reference
    private IUserService iUserService;

    private static final Logger logger = LoggerFactory.getLogger(CouponService.class);

    private static final String COUPON="couponSet";

    private static final int COUPON_NUM=10;


    /**
     * Caffine??????
     */
    com.github.benmanes.caffeine.cache.LoadingCache<Integer,List<TCoupon>>couponCaffeine  = Caffeine.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).refreshAfterWrite(5,TimeUnit.MINUTES)
            .build(new com.github.benmanes.caffeine.cache.CacheLoader<Integer,List<TCoupon>>(){
                @Override
                public List<TCoupon> load(Integer o) throws Exception {
                    return loadCoupon(o);
                }
            });


    /**
     * ??????GuavaCache??????
     */
    LoadingCache<Integer,List<TCoupon>> couponCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).refreshAfterWrite(5,TimeUnit.MINUTES)
            .build(new CacheLoader<Integer, List<TCoupon>>() {
                @Override
                public List<TCoupon> load(Integer o) throws Exception {
                    return loadCoupon(o);
                }
            });




    LoadingCache<Integer,TCoupon> couponIdsCache = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES).refreshAfterWrite(5,TimeUnit.MINUTES)
            .build(new CacheLoader<Integer, TCoupon>() {
                @Override
                public TCoupon load(Integer o) throws Exception {
                    return loadIdCoupon(o);
                }
            });
    private TCoupon loadIdCoupon(Integer id) {
        return tCouponMapper.selectByPrimaryKey(id);
    }



    private Map couponMap = new ConcurrentHashMap();


    /**
     * ??????springboot???????????????????????????????????????????????????????????????
     */
    public void updateCouponMap(){

        Map couponMap1 = new ConcurrentHashMap();
        List<TCoupon> tCoupons =Lists.newArrayList();
        try{
            tCoupons = this.loadCoupon(1);
            logger.info("update coupon list:{},coupon list size:{}", JSON.toJSONString(tCoupons),tCoupons.size());
            couponMap1.put(1,tCoupons);
            couponMap=couponMap1;
        }catch (Exception e){
            logger.error("update coupon list:{},coupon list size:{}", JSON.toJSONString(tCoupons),tCoupons.size(),e);
        }


    }



    /**
     * ????????????????????????????????????????????????????????????
     * @return
     */
    public List<TCoupon> getCouponList4Map(){
        List<TCoupon> tCoupons = (List<TCoupon>) couponMap.get(1);
        return tCoupons;
    }

    /**
     * ????????????id????????????????????????key???????????????????????????????????????????????????????????????????????????????????????
     */
    public List<TCoupon> getCouponListByIds(String ids){
        String[] idStr=ids.split(",");
        List<Integer> loadFromDB = Lists.newArrayList();
        List<TCoupon> tCoupons = Lists.newArrayList();
        List<String> idList = Lists.newArrayList(idStr);
        for(String id:idList){
            TCoupon tCoupon= couponIdsCache.getIfPresent(id);
            if(tCoupon==null){
                loadFromDB.add(Integer.parseInt(id));
            }else{
                tCoupons.add(tCoupon);
            }
        }
        List<TCoupon> tCoupons1 =couponByIds(loadFromDB);
        Map<Integer,TCoupon> tCouponMap = tCoupons1.stream().collect(Collectors.toMap(TCoupon::getId,TCoupon->TCoupon));
        tCoupons.addAll(couponByIds(loadFromDB));
        //???????????????????????????????????????
        couponIdsCache.putAll(tCouponMap);
        return tCoupons;
    }


    private List<TCoupon> couponByIds(List<Integer> ids) {
        TCouponExample example=new TCouponExample();
        example.createCriteria().andIdIn(ids);
        return tCouponMapper.selectByExample(example);
    }

    /**
     * ???????????????????????????????????????
     * @param o
     * @return
     */
    public List<TCoupon> loadCoupon(Integer o) {
        TCouponExample example=new TCouponExample();
        example.createCriteria().andStatusEqualTo(Constant.USEFUL)
                .andStartTimeLessThan(new Date()).andEndTimeGreaterThan(new Date());
        return tCouponMapper.selectByExample(example);
    }


    /**
     * ???Caffeine??????????????????????????????
     * ????????????????????????????????????????????????????????????
     * @return
     */
    public List<TCoupon> getCouponListCaffeine(){
        List<TCoupon> tCoupons= Lists.newArrayList();
        try {
            tCoupons=couponCaffeine.get(1);
        }catch (Exception e){
            e.printStackTrace();
        }

        return tCoupons;
    }

    /**
     * ????????????????????????????????????????????????????????????????????????????????????
     * @return
     */
    @Override
    public List<CouponDto> getCouponList(){
        List<TCoupon> tCoupons= Lists.newArrayList();
        List<CouponDto> dtos= Lists.newArrayList();
        try {
            tCoupons =  couponCache.get(1);
        } catch (ExecutionException e) {
            e.printStackTrace();
        }
        tCoupons.forEach(tCoupon -> {
            CouponDto dto=new CouponDto();
            BeanUtils.copyProperties(tCoupon,dto);
            dtos.add(dto);
        });
        return dtos;
    }

    /**
     * ???????????????????????????
     * @param userId
     * @return
     */
    @Override
    public List<UserCouponInfoDto> userCouponList(Integer userId) {
        List<UserCouponInfoDto> dtos=Lists.newArrayList();
        if(userId==null){
            return Lists.newArrayList();
        }
        List<TUserCoupon> userCoupons=getUserCoupon(userId);
        if(CollectionUtils.isEmpty(userCoupons)){
            return dtos;
        }
        Map<Integer,TCoupon> idCouponMap = getCouponMap(userCoupons);
        //??????coupon
        return wrapCoupon(userCoupons,idCouponMap);
    }

    private  Map<Integer,TCoupon> getCouponMap(List<TUserCoupon> userCoupons){
        Set<Integer> couponIds=getCouponIds(userCoupons);
        List<TCoupon> coupons=getCouponListByIds(StringUtils.join(couponIds,","));
        Map<Integer,TCoupon> idCouponMap =couponList2Map(coupons);
        return idCouponMap;
    }

    /**
     *???????????????????????????
     */
    private List<TUserCoupon> getUserCoupon(Integer userId){
        //???????????????????????????
        TUserCouponExample example =new TUserCouponExample();
        example.createCriteria().andUserIdEqualTo(userId).andStatusEqualTo(0);
        List<TUserCoupon> userCoupons =tUserCouponMapper.selectByExample(example);
        return userCoupons;
    }

    /**
     * ??????couponIds
     */
    private Set<Integer> getCouponIds(List<TUserCoupon> userCoupons){
        Set<Integer> couponIds=userCoupons.stream().map(userCoupon-> userCoupon.getCouponId()).collect(Collectors.toSet());
        return couponIds;
    }

    /**
     * ???list??????map
     */
    private Map<Integer,TCoupon> couponList2Map(List<TCoupon> coupons){
       return coupons.stream().collect(Collectors.toMap(o->o.getId(),o->o));
    }

    /**
     * ??????coupon
     */
    private List<UserCouponInfoDto> wrapCoupon(List<TUserCoupon> userCoupons,Map<Integer,TCoupon> idCouponMap){
        UserCouponInfoDto dto =new UserCouponInfoDto();
        List<UserCouponInfoDto> dtos = userCoupons.stream().map(userCoupon ->{
            int couponId=userCoupon.getCouponId();
            TCoupon coupon=idCouponMap.get(couponId);
            dto.setAchieveAmount(coupon.getAchieveAmount());
            dto.setReduceAmount(coupon.getReduceAmount());
            BeanUtils.copyProperties(userCoupon,dto);
            return dto;
        }).collect(Collectors.toList());
        logger.info("invoke get user coupon list");
        return dtos;
    }


    public void print(){
        System.err.println("enter coupon service");
    }


    public String query(){
        TCouponExample example = new TCouponExample();
        example.createCriteria().andCodeEqualTo("0057da3c-f2ad-42bd-b6d2-8bb58b6dbc90");
        List<TCoupon> tCoupon =  tCouponMapper.selectByExample(example);
        return tCoupon.get(0).toString();
    }

//
    public String getUserById(int id){
        return iUserService.getUserById(id).toString();
    }

    /**
     * ??????????????????
     */
    @Override
    public String saveUserCoupon(UserCouponDto userCouponDto){
       String result=check(userCouponDto);
       if(result!=null){
           return result;
       }
       TCoupon tCoupon =  tCouponMapper.selectByPrimaryKey(userCouponDto.getCouponId());
        if(tCoupon==null){
            return "coupon ??????";
        }
       return save2DB(userCouponDto,tCoupon);

    }
    /**
     * ??????
     */
    private String check(UserCouponDto userCouponDto){
        Integer couponId=userCouponDto.getCouponId();
        Integer userId=userCouponDto.getUserId();
        if(couponId==null||userId==null){
            return "couponId??????UserId??????";
        }
        return null;
    }

    /**
     * coupon????????????
     * @return
     */
    private String save2DB(UserCouponDto userCouponDto,TCoupon tCoupon){
        TUserCoupon userCoupon=new TUserCoupon();
        BeanUtils.copyProperties(userCouponDto,userCoupon);
        userCoupon.setPicUrl(tCoupon.getPicUrl());
        userCoupon.setCreateTime(new Date());
        SnowflakeIdWorker worker=new SnowflakeIdWorker(0,0);
        userCoupon.setUserCouponCode(worker.nextId()+"");
        tUserCouponMapper.insertSelective(userCoupon);
        logger.info("save coupon success:{}",JSON.toJSONString(userCouponDto));
        return "????????????";
    }


    /**
     * ??????coupon????????????10?????????
     */
    @Override
    public List<CouponNotifyDto> queryCouponNotice(){
        //??????set?????????10?????????
        Set<String> couponSet =redisTemplate.opsForZSet().reverseRange(COUPON,0,-1);
        List<String> userCouponStrs =  couponSet.stream().limit(COUPON_NUM).collect(Collectors.toList());
        Map<String,String> couponUserMap=userCouponStrs.stream().collect(Collectors.toMap(o->o.split("_")[1],o->o.split("_")[0]));
        List<String> couponIds=userCouponStrs.stream().map(s->s.split("_")[1]).collect(Collectors.toList());
        //[1,2]=>1,2
        String couponIdStrs=StringUtils.join(couponIds,",");
        //??????couponIdStrs????????????coupon????????????
        List<TCoupon> tCoupons =getCouponListByIds(couponIdStrs);
        List<CouponNotifyDto> dtos=tCoupons.stream().map(tCoupon -> {
            CouponNotifyDto dto=new CouponNotifyDto();
            BeanUtils.copyProperties(tCoupon,dto);
            dto.setUserId(Integer.parseInt(couponUserMap.get(dto.getId()+"")));
            return dto;
        }).collect(Collectors.toList());
        return dtos;
    }

    /**
     * ?????????coupon???????????????mq??????????????????,????????????????????????N?????????
     */
    public void updateCoupon(String userCouponStr){
        redisTemplate.opsForZSet().add(COUPON,userCouponStr,System.currentTimeMillis());
        Set<String> couponSet = redisTemplate.opsForZSet().range(COUPON,0,-1);
        if(couponSet.size()>COUPON_NUM){
            String remUserCouponStr = couponSet.stream().findFirst().get();
            redisTemplate.opsForZSet().remove(COUPON,remUserCouponStr);
        }
    }


    /**
     * ????????????????????????coupon??????
     */
    private void updateCouponStatus(int orderId,String couponCode){
        TUserCouponExample example =new TUserCouponExample();
        example.createCriteria().andUserCouponCodeEqualTo(couponCode).andOrderIdEqualTo(orderId);
        List<TUserCoupon> tUserCoupons=tUserCouponMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(tUserCoupons)){
            logger.warn("can't no find couponCode:{}",couponCode);
            return;
        }
        TUserCoupon userCoupon=tUserCoupons.get(0);
        userCoupon.setStatus(1);
        tUserCouponMapper.updateByPrimaryKeySelective(userCoupon);
    }


    /**
     * ??????????????????coupon???order???????????????
     */
    public void saveOrder(int orderId,String couponCode,int userId){
        TUserCouponExample example =new TUserCouponExample();
        example.createCriteria().andUserCouponCodeEqualTo(couponCode).andOrderIdEqualTo(orderId);
        List<TUserCoupon> tUserCoupons=tUserCouponMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(tUserCoupons)){
            logger.warn("can't no find couponCode:{}",couponCode);
            return;
        }
        TUserCoupon userCoupon=tUserCoupons.get(0);
        userCoupon.setOrderId(orderId);
        userCoupon.setUserId(userId);
        //???????????????
        userCoupon.setStatus(0);
        tUserCouponMapper.updateByPrimaryKeySelective(userCoupon);
    }


    /**
     * ??????????????????coupon???????????????????????????
     */
    public void payResult(int orderId,int userId){
        TUserCouponExample example =new TUserCouponExample();
        example.createCriteria().andUserIdEqualTo(userId).andOrderIdEqualTo(orderId);
        List<TUserCoupon> tUserCoupons=tUserCouponMapper.selectByExample(example);
        if (CollectionUtils.isEmpty(tUserCoupons)){
            logger.warn("can't no find userId:{},orderId:{}",userId,orderId);
            return;
        }
        TUserCoupon userCoupon=tUserCoupons.get(0);
        userCoupon.setStatus(1);
        tUserCouponMapper.updateByPrimaryKeySelective(userCoupon);
    }
}
