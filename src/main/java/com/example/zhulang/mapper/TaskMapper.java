package com.example.zhulang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhulang.entity.Task;
import com.example.zhulang.entity.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface TaskMapper extends BaseMapper<Task> {
    @Select("select * from user where uid = (select master_id from task where route_id = #{routeId} and member_id = #{uid})")
    User myMaster(Integer uid, Integer routeId);

    @Select("select count(*) from task where route_id = #{routeId} and member_id = master_id")
    Integer isRepeat(Integer routeId);
}
