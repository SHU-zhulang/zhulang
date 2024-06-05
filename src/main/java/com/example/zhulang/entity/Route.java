package com.example.zhulang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("route")
@Data
public class Route {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private String name;
    private String leader;
    private String member;
    private Integer allocated;
}
