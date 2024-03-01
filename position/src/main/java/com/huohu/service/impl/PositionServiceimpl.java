package com.huohu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huohu.mapper.PositionMapper;
import com.huohu.pojo.Position;
import com.huohu.service.PositionService;
import org.springframework.stereotype.Service;

@Service
public class PositionServiceimpl extends ServiceImpl<PositionMapper, Position> implements PositionService {
}
