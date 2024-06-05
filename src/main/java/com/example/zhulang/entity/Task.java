package com.example.zhulang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("task")
@Data
public class Task {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer routeId;
    private Integer memberId;
    private Integer masterId;
}
