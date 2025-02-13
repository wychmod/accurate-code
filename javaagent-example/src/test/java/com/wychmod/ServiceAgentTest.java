package com.wychmod;

import com.wychmod.timingcalculation.UserServiceImpl;

/**
 * @description: 测试ServiceAgent
 * @author: wychmod
 * @date: 2025-02-11
 */
public class ServiceAgentTest {
    public static void main(String[] args) {
        UserServiceImpl userService = new UserServiceImpl();
        userService.findUser("111");
    }
}
