package com.xdclass.couponapp.service.schedule;



import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

@Service
public class HelloSchedule {



    @Scheduled(cron = "0/10 * * * * ?")
    public void hello(){
        System.out.println("enter hello job");
    }
}
