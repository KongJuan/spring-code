package com.kyn.test;

import com.kyn.factory.support.DefaultListableBeanFactory;
import com.kyn.po.User;
import com.kyn.reader.XmlBeanDefinitionReader;
import com.kyn.resource.ClasspathResource;
import com.kyn.resource.Resource;
import com.kyn.service.UserInfoService;
import org.junit.Before;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test03 {

    private DefaultListableBeanFactory beanFactory;
    @Before
    public void before(){
        /**
         * 注册BeanDefinition
         */
        //实例化beanFactory
        beanFactory=new DefaultListableBeanFactory();
        //解析配置文件中的资源信息
        Resource resource=new ClasspathResource("beans.xml");
        XmlBeanDefinitionReader beanDefinitionReader=new XmlBeanDefinitionReader(beanFactory);
        beanDefinitionReader.loadBeanDefinitions(resource);
    }
    @Test
    public void springTest(){
        //getBean
        UserInfoService userInfoService=(UserInfoService)beanFactory.getBean("userInfoService");
        Map<String,Object> map = new HashMap<>();
        map.put("username","千年老亚瑟");
        List<User> userInfoList=userInfoService.queryUserList(map);
        System.out.println(userInfoList);
    }
}
