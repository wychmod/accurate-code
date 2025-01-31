package com.wychmod;

import java.lang.instrument.Instrumentation;

/**
 * @description: agent类
 * @author: wychmod
 * @date: 2025/2/1
 */
public class MyAgent {

    /**
     * Java 虚拟机启动时调用的预主方法 [加载时启动]
     * 用于在虚拟机初始化阶段添加 instrumentation
     *
     * @param agentArgs 代理参数字符串，可以传递额外的配置信息
     * @param inst      Instrumentation 实例，用于获取已加载类的信息和重定义类
     */
    public static void premain(String agentArgs, Instrumentation inst) {
        System.out.println("premain");
    }

    /**
     * Java 虚拟机运行时调用的代理主方法 [运行时启动]
     * 用于在虚拟机运行时添加 instrumentation
     *
     * @param agentArgs 代理参数字符串，可以传递额外的配置信息
     * @param inst      Instrumentation 实例，用于获取已加载类的信息和重定义类
     */
    public static void agentmain(String agentArgs, Instrumentation inst) {
        System.out.println("agentmain");
    }
}
