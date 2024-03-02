package com.huohu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Report implements Serializable {
    //唯一标识
    @TableId(type = IdType.AUTO)
    private Integer id;
    //卖方
    private String sell;
    //买方
    private String buy;
    //成交数量
    private Integer dealnum;
    //成交时间
    private Date time;
    //成交均价
    private Integer salary;
    //股票名称
    private String tranname;
}
