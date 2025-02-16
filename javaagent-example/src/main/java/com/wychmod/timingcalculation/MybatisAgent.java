package com.wychmod.timingcalculation;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.LoaderClassPath;
import org.apache.ibatis.mapping.BoundSql;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;
import java.util.Date;

/**
 * @description: mybatis代理打印sql，在springboot命令行执行会有问题，缺陷版，无法找到BoundSql包
 * @author: wychmod
 * @date: 2025-02-16
 */
public class MybatisAgent implements ClassFileTransformer {

    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!"org/apache/ibatis/executor/statement/BaseStatementHandler".equals(className)) {
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
            ctMethod.insertBefore("info=com.wychmod.timingcalculation.MybatisAgent.begin($args);");
            ctMethod.insertAfter("com.wychmod.timingcalculation.MybatisAgent.end(info);");
            System.out.println("插桩成功："+ctClass.getName());
            return ctClass.toBytecode();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
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
