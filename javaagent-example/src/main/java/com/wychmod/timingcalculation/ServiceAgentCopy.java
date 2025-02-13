package com.wychmod.timingcalculation;

import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtMethod;
import javassist.CtNewMethod;

import java.lang.instrument.ClassFileTransformer;
import java.lang.instrument.IllegalClassFormatException;
import java.security.ProtectionDomain;

/**
 * @description: Copy插桩-耗时时间
 * @author: wychmod
 * @date: 2025-02-11
 */
public class ServiceAgentCopy implements ClassFileTransformer {
    @Override
    public byte[] transform(ClassLoader loader, String className, Class<?> classBeingRedefined, ProtectionDomain protectionDomain, byte[] classfileBuffer) throws IllegalClassFormatException {
        if (!"com/wychmod/timingcalculation/UserServiceImpl".equals(className)) {
            return null;
        }

        try {
            ClassPool pool=new ClassPool();
            pool.appendSystemPath();
            CtClass ctClass = pool.get("com.wychmod.timingcalculation.UserServiceImpl");
            CtMethod ctMethod = ctClass.getDeclaredMethods("findUser")[0];
            // 1.拷贝新方法
            CtMethod NewMethod = CtNewMethod.copy(ctMethod, ctClass, null);
            // 2.重命名新方法
            NewMethod.setName(NewMethod.getName()+"$agent");
            // 3. 添加新方法到以前的类
            ctClass.addMethod(NewMethod);
            // 4. 旧类调用新方法
            // $$ 是 Javassist 框架中的特殊符号，表示将原始方法的所有参数传递给新方法。
            ctMethod.setBody("{    long begin = System.currentTimeMillis();\n" +
                    "            try {\n" +
                    "                findUser$agent($$);\n" +
                    "            } finally {\n" +
                    "                long end = System.currentTimeMillis();\n" +
                    "                System.out.println(end - begin);\n" +
                    "            } }");

            return ctClass.toBytecode();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
    }
}
