package com.huohu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huohu.mapper.MarketMapper;
import com.huohu.pojo.Market;
import com.huohu.service.MarketService;
import org.springframework.stereotype.Service;

@Service
public class MarketServiceimpl extends ServiceImpl<MarketMapper, Market> implements MarketService {
}
