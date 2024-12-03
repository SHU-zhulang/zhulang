package com.example.zhulang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("people")
@Data
public class People {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer routeId;
    private Integer memberId;
    public String nickName;
    public String photo;
    public String self;
    public String content;
}
