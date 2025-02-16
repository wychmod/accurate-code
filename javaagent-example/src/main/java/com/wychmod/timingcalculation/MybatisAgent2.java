package com.wychmod.timingcalculation;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import org.apache.ibatis.mapping.BoundSql;

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
 * @description: mybatis代理打印sql，tomcat 中可行，在Spring boot中不可行
 * @author: wychmod
 * @date: 2025-02-16
 */
public class MybatisAgent2 implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!"org/apache/ibatis/executor/statement/BaseStatementHandler".equals(className)) {
            return null;
        }

        try {
            // tomcat 中可行，在Spring boot中不可行,下面为tomcat类加载器结构
            // tomcat打破了双亲委派，可以先找自己的类，找不到再去父类加载器中找
            // 这个时候mybatis和agent都加载在WebappClassLoader，便能互相调用正确
            // BootstrapClassLoader
            // ExtClassLoader
            // AppClassLoader
            // UrlClassLoader tomcat公用包
            // WebappClassLoader
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
            ctMethod.insertBefore("info=com.wychmod.timingcalculation.MybatisAgent2.begin($args);");
            ctMethod.insertAfter("com.wychmod.timingcalculation.MybatisAgent2.end(info);");
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
        String path = Objects.requireNonNull(MybatisAgent2.class.getResource("")).getPath();
        path = path.substring(0, path.indexOf("!/"));

        // 使用反射调用addURL方法，将路径添加到ClassLoader
        addURL.invoke(urlClassLoader, new URL(path));
    }


    public static SqlInfo begin(Object[] args) {
        SqlInfo sqlInfo = new SqlInfo();
        sqlInfo.beginTime=System.currentTimeMillis();
        BoundSql arg = (BoundSql) args[5];
        sqlInfo.sql=arg.getSql();
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
}
