package com.example.zhulang.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.Data;

@TableName("user")
@Data
public class User {
    @TableId(type = IdType.AUTO)
    private Integer uid;
    private String phone;
    private String password;
    private String realName;
    private Integer role;
    public String nickName;
    private String gender;
    private String whatsup;
}
