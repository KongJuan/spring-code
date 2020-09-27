package com.kyn.test;

import com.kyn.dao.UserInfoDaoImpl;
import com.kyn.ioc.BeanDefinition;
import com.kyn.ioc.PropertyValue;
import com.kyn.ioc.RuntimeBeanReference;
import com.kyn.ioc.TypedStringValue;
import com.kyn.po.User;
import com.kyn.service.UserInfoService;
import com.kyn.service.UserInfoServiceImpl;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Before;
import org.junit.Test;

import java.lang.reflect.Constructor;
import java.util.*;

public class Test02 {
    //存储单例Bean的集合（多例或者叫原型Bean不存储到集合）
    private Map<String,Object> beanMap=new HashMap<String, Object>();
    //存储配置文件中的bean的定义信息
    private Map<String,Object> beanDefinitions=new HashMap<>();

    @Before
    public void before(){

    }

    @Test
    public void springTest(){
        //版本一：
        //UserInfoService userInfoService=getUserService();
        //版本二：
        UserInfoService userInfoService=(UserInfoService)getObject("userService");
        //版本三：

        Map<String,Object> map = new HashMap<>();
        map.put("username","千年老亚瑟");
        List<User> userInfoList=userInfoService.queryUserList(map);
        System.out.println(userInfoList);
    }
    //版本三：将类与类之间的依赖关系在XML中进行配置，然后通过解析配置文件的方式获取配置文件中的内容
    public Object getBean(String beanName){
        //1.从缓存中获取Bean实例
        Object bean=beanMap.get(beanName);
        //2.判断缓存中是否有对应的bean对象，，
        if(bean!=null){
            //2.1如果存在直接返回
            return bean;
        }
        //2.2如果不存在需要xml解析出来的对应的信息（map结构中的BeanDefinition）--beanname为key
        BeanDefinition beanDefinition=(BeanDefinition)beanDefinitions.get(beanName);
        if(beanDefinition==null){
            return null;
        }
        //3.判断要创建的bean实例是单例的还是多例的,如果为单例，需将创建的对象放入缓存中
        if(beanDefinition.isSingleton()){
            //4.根据xml解析出来的信息创建Bean实例，并放入缓存中
            bean=doCreateBean(beanDefinition);
            beanMap.put(beanName,bean);
        }else if(beanDefinition.isPrototype()){
            bean=doCreateBean(beanDefinition);
        }
        return bean;
    }

    private Object doCreateBean(BeanDefinition beanDefinition) {
        //第一步：对象实例化
        Object bean=createInstanceBean(beanDefinition);
        //第二步：依赖注入
        populateBean(bean,beanDefinition);
        //第三步：对象初始化（调用初始化方法）
        initializeBean(bean,beanDefinition);
        return null;
    }

    private Object createInstanceBean(BeanDefinition beanDefinition) {
        try{
            Class<?> clazz=beanDefinition.getClazzType();
            Constructor<?> constructor=clazz.getDeclaredConstructor();
            return constructor.newInstance();
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    private void populateBean(Object bean, BeanDefinition beanDefinition) {
        List<PropertyValue> propertyValues=beanDefinition.getPropertyValues();
        for(PropertyValue pv:propertyValues){
            String name=pv.getName();
            Object value=pv.getValue();// 此时value是TypeStringValue或者RuntimeBeanReference

            // 获取可以完成依赖注入的值
            Object valueToUse = resoleValue(value);

            // 完成属性注入
            setProperty(bean, name, valueToUse);
        }
    }


    private Object resoleValue(Object value) {
        if(value instanceof TypedStringValue){
            TypedStringValue typedStringValue=(TypedStringValue)value;
            String stringValue=typedStringValue.getValue();
            Class<?> targetValueType=typedStringValue.getTargetType();
            if(targetValueType!=null){
                //根据类型做类型处理
                if(targetValueType==Integer.class){
                    return Integer.parseInt(stringValue);
                }else if(targetValueType==String.class){
                    return stringValue;
                }
            }
        }
        else if(value instanceof RuntimeException){
            RuntimeBeanReference beanReference = (RuntimeBeanReference) value;
            String ref = beanReference.getRef();
            // 此处会发生循环依赖问题（后面会去讲）
            return getBean(ref);
        }
        return null;
    }
    private void setProperty(Object bean, String name, Object valueToUse) {

    }


    private void initializeBean(Object bean, BeanDefinition beanDefinition) {
    }

    //版本二：代码违背了开闭原则
    //版本一只是针对UserInfoService类有效，版本二升级为通用版本
    public Object getObject(String beanName){
        if("userService".equals(beanName)){
            BasicDataSource dataSource=new BasicDataSource();
            dataSource.setDriverClassName("com.mysql.jdbc.Driver");
            dataSource.setUrl("jdbc:mysql://47.113.81.149:3306/kkb?characterEncoding=utf8");
            dataSource.setUsername("root");
            dataSource.setPassword("kkb0826");

            UserInfoDaoImpl userInfoDao=new UserInfoDaoImpl();
            userInfoDao.setDataSource(dataSource);

            UserInfoServiceImpl userInfoService=new UserInfoServiceImpl();
            userInfoService.setUserInfoDao(userInfoDao);
            return userInfoService;
        }else if("".equals(beanName)){
            //...
        }
        return null;
    }


    //版本一：
    public UserInfoService getUserService(){
        BasicDataSource dataSource=new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://47.113.81.149:3306/kkb?characterEncoding=utf8");
        dataSource.setUsername("root");
        dataSource.setPassword("kkb0826");

        UserInfoDaoImpl userInfoDao=new UserInfoDaoImpl();
        userInfoDao.setDataSource(dataSource);

        UserInfoServiceImpl userInfoService=new UserInfoServiceImpl();
        userInfoService.setUserInfoDao(userInfoDao);
        return userInfoService;
    }
}
