package com.huohu.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.huohu.pojo.Tran;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;


@Mapper
public interface TranMapper extends BaseMapper<Tran> {

    List<Tran> findbuyList();


    List<Tran> findsellList();
}
