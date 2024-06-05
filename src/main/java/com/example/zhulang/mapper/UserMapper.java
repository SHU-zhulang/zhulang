package com.example.zhulang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhulang.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface UserMapper extends BaseMapper<User> {
    @Select("select count(*) from user where uid = #{uid} and gender = '男'")
    Integer isBoy(Integer uid);

    @Select("select * from user where role = 4")
    List<User> searchLevelup();
}
