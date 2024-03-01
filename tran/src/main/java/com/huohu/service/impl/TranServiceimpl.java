package com.huohu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huohu.mapper.TranMapper;
import com.huohu.pojo.Tran;
import com.huohu.service.TranService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TranServiceimpl extends ServiceImpl<TranMapper, Tran> implements TranService {


    @Override
    public List<Tran> findbuyList() {
        return this.baseMapper.findbuyList();
    }

    @Override
    public List<Tran> findsellList() {
        return this.baseMapper.findsellList();
    }
}
