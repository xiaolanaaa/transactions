package com.huohu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;

@Data
public class Position implements Serializable {
    @TableId(type = IdType.AUTO)
    //唯一标识
    private  Integer id;
    //股票名称
    private String tranname;
    //持仓数量
    private Integer number;
    //时间
    private Date time;
    //买入价格
    private Integer salary;
    //成本价
    private Integer costprice;
    //买入or卖出
    private String put;
    //持仓用户名
    private String username;
    //持仓用户id
    private Integer userid;
}
