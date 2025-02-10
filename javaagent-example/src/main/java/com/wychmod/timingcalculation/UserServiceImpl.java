package com.wychmod.timingcalculation;

/**
 * @description: 被插桩类
 * @author: wychmod
 * @date: 2025-02-09
 */
public class UserServiceImpl {

    public void findUser(String name) {

        System.out.println("获取用户");
        if(name.equals("wychmod")){
            return;
        }
        try {
            Thread.sleep(1000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
