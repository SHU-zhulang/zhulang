package com.example.zhulang.controller;

import com.example.zhulang.entity.Route;
import com.example.zhulang.mapper.RouteMapper;
import com.example.zhulang.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;

@RestController
@RequestMapping("/route")
public class RouteController {
    @Resource
    private RouteMapper routeMapper;

    /***
     * 创建一条线路
     * @param route
     * @return
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Route route) {
        routeMapper.insert(route);
        return Result.success();
    }

    /***
     * 删除一条线路
     * @param route
     * @return
     */
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestBody Route route) {
        routeMapper.deleteById(route);
        return Result.success();
    }

    /***
     * 更新线路情况，可用于添加领队，添加队员操作
     * @param route
     * @return
     */
    @PostMapping("/update")
    public Result<?> update(@RequestBody Route route) {
        routeMapper.updateById(route);
        return Result.success();
    }

    /***
     * 获取当前用户的路线
     * @param uid
     * @return
     */
    @GetMapping("/myRoute")
    public Result<?> myRoute(@RequestParam Integer uid) {
        return Result.success(routeMapper.myRoute(uid));
    }

    /***
     * 获取当前领队带领的路线
     * @param uid
     * @return
     */
    @GetMapping("/myLeadRoute")
    public Result<?> myLeadRoute(@RequestParam Integer uid) {
        return Result.success(routeMapper.myLeadRoute(uid));
    }
}
