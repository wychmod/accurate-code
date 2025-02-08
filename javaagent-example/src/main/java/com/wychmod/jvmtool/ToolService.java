package com.wychmod.jvmtool;

import java.rmi.Remote;
import java.rmi.RemoteException;

/**
 * @description: 远程工具服务接口，用于提供远程类信息查询和反编译服务
 * @author: wychmod
 * @date: 2025-02-08
 */
public interface ToolService extends Remote {

    /**
     * 根据类名查找对应的类信息
     *
     * @param name 类名，用于标识需要查找的类
     * @return 返回找到的类信息字符串表示
     * @throws RemoteException 当远程调用失败时抛出此异常
     */
    String findClassName(String name) throws RemoteException;

    /**
     * 根据类名反编译类代码
     *
     * @param className 类名，用于标识需要反编译的类
     * @return 返回反编译后的类代码字符串表示
     * @throws RemoteException 当远程调用失败时抛出此异常
     */
    String jadClass(String className) throws RemoteException;
}
