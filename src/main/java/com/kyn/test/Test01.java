package com.kyn.test;

import com.kyn.dao.UserInfoDaoImpl;
import com.kyn.po.User;
import com.kyn.service.UserInfoServiceImpl;
import org.apache.commons.dbcp.BasicDataSource;
import org.junit.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Test01 {

    @Test
    public void springTest(){

        BasicDataSource dataSource=new BasicDataSource();
        dataSource.setDriverClassName("com.mysql.jdbc.Driver");
        dataSource.setUrl("jdbc:mysql://47.113.81.149:3306/kkb?characterEncoding=utf8");
        dataSource.setUsername("root");
        dataSource.setPassword("kkb0826");

        UserInfoDaoImpl userInfoDao=new UserInfoDaoImpl();
        userInfoDao.setDataSource(dataSource);

        UserInfoServiceImpl userInfoService=new UserInfoServiceImpl();
        userInfoService.setUserInfoDao(userInfoDao);

        Map<String,Object> map = new HashMap<>();
        map.put("username","千年老亚瑟");
        List<User> userInfoList=userInfoService.queryUserList(map);
        System.out.println(userInfoList);
    }

}
