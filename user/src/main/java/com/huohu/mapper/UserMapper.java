package com.huohu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huohu.pojo.User;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface UserMapper extends BaseMapper<User> {
}
