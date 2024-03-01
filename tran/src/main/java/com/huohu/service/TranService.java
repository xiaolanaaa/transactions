package com.huohu.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.huohu.pojo.Tran;

import java.util.List;

public interface TranService extends IService<Tran> {


    List<Tran> findbuyList();

    List<Tran> findsellList();
}
