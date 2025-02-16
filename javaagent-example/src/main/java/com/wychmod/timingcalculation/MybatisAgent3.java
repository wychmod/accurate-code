package com.wychmod.timingcalculation;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.security.ProtectionDomain;
import java.util.Date;
import java.util.Objects;

/**
 * @description: mybatis代理打印sql完整版
 *
 * 1. 每个类加载器有独立的命名空间，第三方包的类若不在当前类加载器的搜索路径中，直接引用会触发ClassNotFoundException
 * 2. 反射操作（如Class.forName()）默认使用调用者的类加载器，若调用者类加载器无法加载目标类，同样会失败
 * 类中不再有直接import的mybatis的路径
 * @author: wychmod
 * @date: 2025-02-16
 */
public class MybatisAgent3 implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!"org/apache/ibatis/executor/statement/BaseStatementHandler".equals(className)) {
            return null;
        }

        try {
            // tomcat 中可行，在Spring boot中不可行,下面为tomcat类加载器结构
            appendToLoader(loader);
        } catch (Exception e) {
            System.err.println("jar 注入失败");
            e.printStackTrace();
            return null;
        }


        try {
            ClassPool pool = new ClassPool();
            pool.appendSystemPath();
            pool.appendClassPath(new LoaderClassPath(loader));
            CtClass ctClass = pool.get("org.apache.ibatis.executor.BaseExecutor");
            // 查询
            CtMethod ctMethod = ctClass.getDeclaredMethods("query")[1];
            ctMethod.addLocalVariable("info", pool.get(SqlInfo.class.getName()));
            ctMethod.insertBefore("info=com.wychmod.timingcalculation.MybatisAgent3.begin($args);");
            ctMethod.insertAfter("com.wychmod.timingcalculation.MybatisAgent3.end(info);");
            System.out.println("插桩成功："+ctClass.getName());
            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 将指定的路径添加到ClassLoader的搜索路径中
     *
     * @param loader 要添加路径的ClassLoader
     */
    private void appendToLoader(ClassLoader loader) throws NoSuchMethodException, MalformedURLException, InvocationTargetException, IllegalAccessException {
        // 将传入的ClassLoader转换为URLClassLoader
        URLClassLoader urlClassLoader = (URLClassLoader) loader;

        // 获取URLClassLoader类中声明的addURL方法，并设置其可访问
        Method addURL = URLClassLoader.class.getDeclaredMethod("addURL", URL.class);
        addURL.setAccessible(true);

        // 获取MybatisAgent2类资源的路径，并处理路径，以适应ClassLoader的要求
        String path = Objects.requireNonNull(MybatisAgent3.class.getResource("")).getPath();
        path = path.substring(0, path.indexOf("!/"));

        // 使用反射调用addURL方法，将路径添加到ClassLoader
        addURL.invoke(urlClassLoader, new URL(path));
    }


    public static SqlInfo begin(Object[] args) {
        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.beginTime=System.currentTimeMillis();
        BoundSqlAdapter adapter = new BoundSqlAdapter(args[5]);
        sqlInfo.sql=adapter.getSql();
        return sqlInfo;
    }

    public static void end(SqlInfo info) {
        info.useTime=System.currentTimeMillis()-info.beginTime;
        System.out.println(info);
    }
    public static class SqlInfo {
        public long beginTime;
        public long useTime;
        public String sql;

        @Override
        public String toString() {
            return "SqlInfo{" +
                    "beginTime=" + new Date(beginTime) +
                    ", useTime=" + useTime +
                    ", sql='" + sql + '\'' +
                    '}';
        }
    }

    /**
     * BoundSqlAdapter类适配了一个包含SQL的类，以便可以从该类中提取SQL字符串
     * 这个类主要通过反射机制来调用目标类中的getSql方法，从而获取SQL内容
     */
    public static class BoundSqlAdapter {
        // 目标对象，即包含SQL的类的实例
        Object target;
        // getSql方法的反射对象，用于调用目标类中的getSql方法
        private static Method getSql;
        // 目标类的Class对象，用于反射机制初始化
        private static Class aClass;

        /**
         * 初始化反射机制，为给定的类准备getSql方法的调用
         * 此方法确保只被同一个类初始化一次，以提高性能
         *
         * @param cls 目标类的Class对象，用于反射机制初始化
         */
        private synchronized static void init(Class cls) {
            try {
                // 设置目标类和getSql方法，并使getSql方法可访问，即使它是私有的
                aClass = cls;
                getSql = cls.getDeclaredMethod("getSql");
                getSql.setAccessible(true);
            } catch (NoSuchMethodException e) {
                // 如果目标类中没有getSql方法，则抛出运行时异常
                throw new RuntimeException(e);
            }
        }

        /**
         * 构造函数，创建一个BoundSqlAdapter实例
         * 如果尚未初始化目标类，则调用init方法进行初始化
         *
         * @param target 目标对象，即包含SQL的类的实例
         */
        public BoundSqlAdapter(Object target) {
            this.target = target;
            // 如果尚未初始化目标类，则进行初始化
            if (aClass == null) {
                init(target.getClass());
            }
            // 冗余的赋值操作，已经在构造函数参数中完成
            this.target = target;
        }

        /**
         * 通过反射机制调用目标对象的getSql方法，获取SQL字符串
         *
         * @return 目标对象的getSql方法返回的SQL字符串
         */
        public String getSql() {
            try {
                // 调用目标对象的getSql方法，并返回结果
                return (String) getSql.invoke(target);
            } catch (Exception e) {
                // 如果调用过程中发生任何异常，则抛出运行时异常
                throw new RuntimeException(e);
            }
        }
    }

}
