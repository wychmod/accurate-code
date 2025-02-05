package com.wychmod;

import javassist.*;

import java.io.IOException;
import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.instrument.Instrumentation;
import java.lang.instrument.UnmodifiableClassException;
import java.security.ProtectionDomain;

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
        HelloWorld helloWorld = new HelloWorld();
        inst.addTransformer(new ClassFileTransformer() {

            /**
             * 变换指定类的字节码
             *
             * @param loader 加载该类的类加载器，可能为null
             * @param className 要转换的类的名称，以替换'/'为路径分隔符
             * @param classBeingRedefined 正在被重新定义的类，如果类正在被重新定义，否则为null
             * @param protectionDomain 保护域，可能为null
             * @param classfileBuffer 类文件的字节码
             * @return 转换后的类文件字节码，如果不需要转换则返回null
             * @throws IllegalClassFormatException 如果类文件的格式不正确
             */
            @Override
            public byte[] transform(ClassLoader loader,
                                    String className, Class<?> classBeingRedefined,
                                    ProtectionDomain protectionDomain,
                                    byte[] classfileBuffer) throws IllegalClassFormatException {
                // 仅处理名为"com/wychmod/HelloWorld"的类
                if (!"com/wychmod/HelloWorld".equals(className)) {
                    return null;
                }
                try {
                    // 创建一个新的类池来管理类
                    ClassPool pool = new ClassPool();
                    // 将要转换的类的字节码添加到类池中
                    pool.appendClassPath(new ByteArrayClassPath("com.wychmod.HelloWorld", classfileBuffer));
                    // 将系统路径添加到类池中，以确保可以访问系统类
                    pool.appendSystemPath();
                    // 获取要转换的类
                    CtClass ctClass = pool.get("com.wychmod.HelloWorld");
                    // 获取类中的"hello"方法
                    CtMethod ctMethod = ctClass.getDeclaredMethod("hello");
                    // 在"hello"方法的开始处插入新的代码
                    ctMethod.insertBefore("System.out.println(\"插入前置逻辑\");");
                    // 返回转换后的类的字节码
                    return ctClass.toBytecode();
                } catch (NotFoundException | CannotCompileException | IOException e) {
                    // 如果发生异常，打印堆栈跟踪信息
                    e.printStackTrace();
                }
                return null;
            }


        }, true);

        // 重新走过滤器
        try {
            inst.retransformClasses(HelloWorld.class);
        } catch (UnmodifiableClassException e) {
            e.printStackTrace();
        }
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
