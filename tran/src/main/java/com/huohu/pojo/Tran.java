package com.huohu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;
import java.util.Date;


@Data
public class Tran implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;
    //股票名称
    private String name;
    //用户id
    private Integer userid;
    //用户名称
    private String username;
    //单价
    private Integer salary;
    //数量
    private Integer num;
    //订单状态
    private String status;
    //买入卖出
    private String put;
    //成交手数
    private Integer dealnum;
    //未成交手数
    private Integer uncompleted;
    //生成时间
    private Date time;
    //是否完成
    private boolean completed;


    public boolean isCompleted() {
        return completed;
    }

    public void setCompleted(boolean completed) {
        this.completed = completed;
    }
}
