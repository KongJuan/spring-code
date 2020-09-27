package com.kyn.service;

import com.kyn.dao.UserInfoDao;
import com.kyn.po.User;

import java.util.List;
import java.util.Map;

public class UserInfoServiceImpl implements UserInfoService {

    //依赖注入UserInfoDao
    private UserInfoDao userInfoDao;

    //通过set方式注入
    public void setUserInfoDao(UserInfoDao userInfoDao){
        this.userInfoDao=userInfoDao;
    }

    @Override
    public List<User> queryUserList(Map<String, Object> map) {
        return userInfoDao.queryUserList(map);
    }
}
