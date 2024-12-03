package com.example.zhulang.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.zhulang.entity.Route;
import com.example.zhulang.entity.Task;
import com.example.zhulang.mapper.RouteMapper;
import com.example.zhulang.mapper.TaskMapper;
import com.example.zhulang.mapper.UserMapper;
import com.example.zhulang.utils.Result;
import org.springframework.web.bind.annotation.*;
import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Random;

@RestController
@RequestMapping("/task")
public class TaskController {
    @Resource
    TaskMapper taskMapper;

    @Resource
    RouteMapper routeMapper;

    @Resource
    UserMapper userMapper;

    /***
     * 创建一次天使与主人，不区分性别
     * @param route
     * @return
     */
    @PostMapping("/create")
    public Result<?> create(@RequestBody Route route) {
        if(routeMapper.selectOne(Wrappers.<Route>lambdaQuery().eq(Route::getId, route.getId())).getAllocated() == 1){
            return Result.error("-1", "该线路已分配天使与主人");
        }
        while(route.getAllocated() == 0){
            String[] members = routeMapper.getMemberById(route.getId()).split(",");
            String[] masters = shuffle(members.clone());
            for(int i = 0; i < members.length; i++) {
                Task task = new Task();
                task.setRouteId(route.getId());
                task.setMemberId(Integer.parseInt(members[i]));
                task.setMasterId(Integer.parseInt(masters[i]));
                taskMapper.insert(task);
            }
            if(taskMapper.isRepeat(route.getId()) != 0){
                delete(route);
            }
            else{
                route.setAllocated(1);
                routeMapper.updateAllocatedById(route.getId(), 1);
            }
        }
        return Result.success();
    }

    /***
     * 创建一次天使与主人，区分性别
     * @param route
     * @return
     */
    @PostMapping("/createByGender")
    public Result<?> createByGender(@RequestBody Route route) {
        if(routeMapper.selectOne(Wrappers.<Route>lambdaQuery().eq(Route::getId, route.getId())).getAllocated() == 1){
            return Result.error("-1", "该线路已分配天使与主人");
        }
        while(route.getAllocated() == 0){
            ArrayList<String> members = new ArrayList<>(Arrays.asList(routeMapper.getMemberById(route.getId()).split(",")));
            ArrayList<Integer> boys = new ArrayList<Integer>();
            ArrayList<Integer> girls = new ArrayList<Integer>();
            for(String member : members){
                Integer m = Integer.parseInt(member);
                if(userMapper.isBoy(m) == 1){
                    boys.add(m);
                }
                else{
                    girls.add(m);
                }
            }
            Integer[] more = (boys.size() >= girls.size() ? boys : girls).toArray(new Integer[0]);
            Integer[] less = (boys.size() < girls.size() ? boys : girls).toArray(new Integer[0]);
            shuffle(more);
            Integer[] masters = Arrays.copyOfRange(more, 0, less.length);
            Integer[] temp = Arrays.copyOf(less, more.length);
            System.arraycopy(more, less.length, temp, less.length, more.length - less.length);
            shuffle(temp);
            for(int i = 0; i < less.length; i++) {
                Task task = new Task();
                task.setRouteId(route.getId());
                task.setMemberId(less[i]);
                task.setMasterId(masters[i]);
                taskMapper.insert(task);
            }
            for(int i = 0; i < more.length; i++) {
                Task task = new Task();
                task.setRouteId(route.getId());
                task.setMemberId(more[i]);
                task.setMasterId(temp[i]);
                taskMapper.insert(task);
            }
            if(taskMapper.isRepeat(route.getId()) != 0){
//                System.out.println("发生了重复");
                delete(route);
            }
            else{
//                System.out.println("没有发生重复");
                route.setAllocated(1);
                routeMapper.updateAllocatedById(route.getId(), 1);
            }
        }
        return Result.success();
    }

    /***
     * Fisher Yates洗牌算法
     * @param array
     */
    public static <T> T[] shuffle(T[] array) {
        Random random = new Random();
        for(int i = array.length - 1; i > 0; i--){
            int index = random.nextInt(i);
//            int index = random.nextInt(i + 1); // 这种方式更符合实际，存在闭环，且有可能发生重复
            swap(array, index, i); // 这种方式不会发生闭环，且目前没测试出重复的情况
        }
        return array;
    }

    /***
     * 交换，辅助洗牌
     * @param array
     * @param i
     * @param j
     * @param <T>
     */
    public static <T> void swap(T[] array, int i, int j){
        T t = array[i];
        array[i] = array[j];
        array[j] = t;
    }

    /***
     * 删除本次线路的天使主人分配
     * @param route
     * @return
     */
    @DeleteMapping("/delete")
    public Result<?> delete(@RequestBody Route route) {
        LambdaQueryWrapper<Task> wrapper = Wrappers.<Task>lambdaQuery();
        wrapper.eq(Task::getRouteId, route.getId());
        taskMapper.delete(wrapper);
        routeMapper.updateAllocatedById(route.getId(), 0);
        return Result.success();
    }

    /***
     * 找到我的主人
     * @param uid
     * @param routeId
     * @return
     */
    @GetMapping("/myMaster")
    public Result<?> myMaster(@RequestParam Integer uid,
                              @RequestParam Integer routeId) {
        return Result.success(taskMapper.myMaster(uid, routeId));
    }
}
