package com.wychmod.timingcalculation;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @description: 插桩耗时时间
 * @author: wychmod
 * @date: 2025-02-11
 */
public class ServiceAgent implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        System.out.println("className:"+className);
        if (!"com/wychmod/timingcalculation/UserServiceImpl".equals(className)) {
            return null;
        }

        try {
            ClassPool pool=new ClassPool();
            pool.appendSystemPath();
            CtClass ctClass = pool.get("com.wychmod.timingcalculation.UserServiceImpl");
            CtMethod ctMethod = ctClass.getDeclaredMethods("findUser")[0];
            ctMethod.addLocalVariable("startTime",CtClass.longType);
            ctMethod.insertBefore("startTime = System.currentTimeMillis();");
            ctMethod.insertAfter("System.out.println(\"耗时：\"+(System.currentTimeMillis()-startTime));");
            return ctClass.toBytecode();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
