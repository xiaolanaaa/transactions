package com.huohu.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.huohu.mapper.ReportMapper;
import com.huohu.pojo.Report;
import com.huohu.service.ReportService;
import org.springframework.stereotype.Service;

@Service
public class ReportServiceimpl extends ServiceImpl<ReportMapper, Report> implements ReportService {
}
