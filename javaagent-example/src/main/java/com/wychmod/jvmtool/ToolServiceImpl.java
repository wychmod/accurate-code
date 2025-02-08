package com.wychmod.jvmtool;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.util.Arrays;
import java.util.stream.Collectors;

/**
 * @description: 实现了ToolService接口的工具服务类
 * @author: wychmod
 * @date: 2025-02-08
 */
public class ToolServiceImpl extends UnicastRemoteObject implements ToolService {

    /**
     * 构造函数
     *
     * 初始化ToolServiceImpl实例，由于继承了UnicastRemoteObject，构造过程中可能抛出RemoteException
     *
     * @throws RemoteException 如果远程方法调用失败
     */
    public ToolServiceImpl() throws RemoteException {
    }

    /**
     * 根据类名查找已加载的类
     *
     * 该方法通过流式处理过滤出类名中包含指定关键词的类，限制最大结果数量为20条，并将其名称拼接成字符串返回
     * 主要解决了如何从大量已加载类中快速查找特定类的问题
     *
     * @param name 要查找的类名关键词
     * @return 匹配的类名字符串，每行一个类名
     * @throws RemoteException 如果远程方法调用失败
     */
    @Override
    public String findClassName(String name) throws RemoteException {
        return Arrays.stream(Agent.instrumentation.getAllLoadedClasses())
                .filter(s -> s.getName().toUpperCase().contains(name.toUpperCase()))
                .limit(20)
                .map(Class::getName)
                .collect(Collectors.joining("\r\n"));
    }

    /**
     * 使用Jad工具反编译指定类
     *
     * 该方法调用了Jad的decompiler方法来反编译指定的类名，如果反编译过程中出现异常，则返回异常信息
     * 主要解决了如何将已加载类的字节码转换为可读源代码的问题
     *
     * @param className 需要反编译的类名
     * @return 反编译后的源代码字符串，如果反编译失败则返回错误信息
     * @throws RemoteException 如果远程方法调用失败
     */
    @Override
    public String jadClass(String className) throws RemoteException {
        try {
            return Jad.decompiler(className);
        } catch (Exception e) {
            e.printStackTrace();
            return e.getMessage();
        }
    }
}
