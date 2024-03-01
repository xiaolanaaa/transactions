package com.huohu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huohu.mapper.UserMapper;
import com.huohu.pojo.User;
import com.huohu.service.UserService;
import org.springframework.stereotype.Service;

@Service
public class UserServiceimpl extends ServiceImpl<UserMapper, User> implements UserService {
}
