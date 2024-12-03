package com.example.zhulang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhulang.entity.People;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface PeopleMapper extends BaseMapper<People> {
    // 检查当前队员是否提交过本条线路的人物贴
    @Select("select * from people where route_id = #{routeId} and member_id = #{memberId}")
    List<People> isSaved(Integer routeId, Integer memberId);

    // 计算当前路线有几个人已经交人物贴了
    @Select("select * from people where route_id = #{routeId}")
    List<People> memberHasSubmitted(Integer routeId);
}
