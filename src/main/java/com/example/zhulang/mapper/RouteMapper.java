package com.example.zhulang.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.zhulang.entity.Route;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import java.util.List;

@Mapper
public interface RouteMapper extends BaseMapper<Route> {
    @Select("select member from route where id = #{id}")
    String getMemberById(Integer id);

    @Update("update route set allocated = #{allocated} where id = #{id}")
    void updateAllocatedById(Integer id, Integer allocated);

    @Select("select * from route where member like concat('%,', #{uid}, ',%') or member like concat(#{uid}, ',%') or member like concat('%,', #{uid}) or member like #{uid} order by id desc")
    List<Route> myRoute(Integer uid);

    @Select("select * from route where leader like concat('%,', #{uid}, ',%') or leader like concat(#{uid}, ',%') or leader like concat('%,', #{uid}) or leader like #{uid} order by id desc ")
    List<Route> myLeadRoute(Integer uid);
}
