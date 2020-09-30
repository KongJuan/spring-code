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
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.junit.Before;
import org.junit.Test;

import java.io.InputStream;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

public class Test02 {
    //存储单例Bean的集合（多例或者叫原型Bean不存储到集合）
    private Map<String,Object> beanMap=new HashMap<String, Object>();
    //存储配置文件中的bean的定义信息
    private Map<String,Object> beanDefinitions=new HashMap<>();

    @Before
    public void before(){
        // 配置文件的解析，把配置文件中的内容，封装到一个Java对象中保存（BeanDefinition）
        String location="beans.xml";
        // 得到对应文件的流对象
        InputStream inputStream = getResourceAsStream(location);
        // 得到对象XML文件的Document对象
        Document document = getDocument(inputStream);
        // 按照spring语义解析文档
        loadBeanDefinitions(document.getRootElement());
    }

    private InputStream getResourceAsStream(String location) {
        return this.getClass().getClassLoader().getResourceAsStream(location);
    }

    private Document getDocument(InputStream inputStream) {
        try {
            SAXReader reader = new SAXReader();
            return reader.read(inputStream);
        }catch (Exception e){
            e.printStackTrace();
        }
        return  null;
    }

    private void loadBeanDefinitions(Element rootElement) {
        List<Element> elements = rootElement.elements();
        for (Element element : elements) {
            if (element.getName().equals("bean")){
                parseDefaultElement(element);
            }else {
                parseCustom(element);
            }
        }
    }

    private void parseDefaultElement(Element beanElement) {
        try {
            if (beanElement == null) {
                return;
            }
            // 获取id属性
            String id = beanElement.attributeValue("id");

            // 获取name属性
            String name = beanElement.attributeValue("name");

            // 获取class属性
            String clazzName = beanElement.attributeValue("class");
            if (clazzName == null || "".equals(clazzName)) {
                return;
            }

            // 获取init-method属性
            String initMethod = beanElement.attributeValue("init-method");
            // 获取scope属性
            String scope = beanElement.attributeValue("scope");
            scope = scope != null && !scope.equals("") ? scope : "singleton";

            // 获取beanName
            String beanName = id == null ? name : id;
            Class<?> clazzType = Class.forName(clazzName);
            beanName = beanName == null ? clazzType.getSimpleName() : beanName;
            // 创建BeanDefinition对象
            // 此次可以使用构建者模式进行优化
            BeanDefinition beanDefinition = new BeanDefinition(clazzName, beanName);
            beanDefinition.setInitMethod(initMethod);
            beanDefinition.setScope(scope);
            // 获取property子标签集合
            List<Element> propertyElements = beanElement.elements();
            for (Element propertyElement : propertyElements) {
                parsePropertyElement(beanDefinition, propertyElement);
            }

            // 注册BeanDefinition信息
            this.beanDefinitions.put(beanName, beanDefinition);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }
    private void parsePropertyElement(BeanDefinition beanDefination, Element propertyElement) {
        if (propertyElement == null)
            return;

        // 获取name属性
        String name = propertyElement.attributeValue("name");
        // 获取value属性
        String value = propertyElement.attributeValue("value");
        // 获取ref属性
        String ref = propertyElement.attributeValue("ref");

        // 如果value和ref都有值，则返回
        if (value != null && !value.equals("") && ref != null && !ref.equals("")) {
            return;
        }

        /**
         * PropertyValue就封装着一个property标签的信息
         */
        PropertyValue pv = null;

        if (value != null && !value.equals("")) {
            // 因为spring配置文件中的value是String类型，而对象中的属性值是各种各样的，所以需要存储类型
            TypedStringValue typeStringValue = new TypedStringValue(value);

            Class<?> targetType = getTypeByFieldName(beanDefination.getClazzName(), name);
            typeStringValue.setTargetType(targetType);

            pv = new PropertyValue(name, typeStringValue);
            beanDefination.addPropertyValue(pv);
        } else if (ref != null && !ref.equals("")) {

            RuntimeBeanReference reference = new RuntimeBeanReference(ref);
            pv = new PropertyValue(name, reference);
            beanDefination.addPropertyValue(pv);
        } else {
            return;
        }
    }
    private Class<?> getTypeByFieldName(String beanClassName, String name) {
        try {
            Class<?> clazz = Class.forName(beanClassName);
            Field field = clazz.getDeclaredField(name);
            return field.getType();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }
    private Class resoleType(String className) {
        try {
            return Class.forName(className);
        }catch (Exception e){
            e.printStackTrace();
        }

        return null;
    }

    private void parseCustom(Element element) {

    }




    @Test
    public void springTest(){
        //版本一：
        //UserInfoService userInfoService=getUserService();
        //版本二：
        //UserInfoService userInfoService=(UserInfoService)getObject("userService");
        //版本三：
        UserInfoService userInfoService=(UserInfoService)getBean("userInfoService");
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
        return bean;
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
        else if(value instanceof RuntimeBeanReference){
            RuntimeBeanReference beanReference = (RuntimeBeanReference) value;
            String ref = beanReference.getRef();
            // 此处会发生循环依赖问题（后面会去讲）
            return getBean(ref);
        }
        return null;
    }
    private void setProperty(Object bean, String name, Object valueToUse) {
        try{
            Class<?> clazz=bean.getClass();
            Field field=clazz.getDeclaredField(name);
            field.setAccessible(true);
            field.set(bean,valueToUse);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private void initializeBean(Object bean, BeanDefinition beanDefinition) {
        String initMethod=beanDefinition.getInitMethod();
        if(initMethod==null || "" .equals(initMethod)){
            return;
        }
        try{
            Class<?> clazzType=beanDefinition.getClazzType();
            Method method=clazzType.getMethod(initMethod);
            method.invoke(bean);
        }catch (Exception e){
            e.printStackTrace();
        }
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
