package com.huohu.pojo;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import lombok.Data;

import java.io.Serializable;

@Data
public class Market implements Serializable {
    @TableId(type = IdType.AUTO)
    private Integer id;

}
