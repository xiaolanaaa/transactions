package com.huohu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huohu.mapper.WarehouseMapper;
import com.huohu.pojo.Warehouse;
import com.huohu.service.WarehouseService;
import org.springframework.stereotype.Service;

@Service
public class WarehouseServiceimpl extends ServiceImpl<WarehouseMapper, Warehouse> implements WarehouseService {
}
