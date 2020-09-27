package com.kyn.dao;

import com.kyn.po.User;

import java.util.List;
import java.util.Map;

public interface UserInfoDao {
    List<User> queryUserList(Map<String,Object> map);
}
