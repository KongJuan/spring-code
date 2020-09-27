package com.kyn.service;

import com.kyn.po.User;

import java.util.List;
import java.util.Map;

public interface UserInfoService {
    List<User> queryUserList(Map<String,Object> map);
}
