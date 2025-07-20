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
import java.util.List;
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
        int maxTry = 1000;
        int tryCount = 0;
        boolean success = false;
        while(!success && tryCount < maxTry){
            tryCount++;
            List<String> members = new ArrayList<>(Arrays.asList(routeMapper.getMemberById(route.getId()).split(",")));
            List<Integer> boys = new ArrayList<>();
            List<Integer> girls = new ArrayList<>();
            for(String member : members){
                Integer m = Integer.valueOf(member);
                if(userMapper.isBoy(m) == 1){
                    boys.add(m);
                } else {
                    girls.add(m);
                }
            }
            // 以人数多的为more，少的为less
            List<Integer> more = (boys.size() >= girls.size() ? boys : girls);
            List<Integer> less = (boys.size() < girls.size() ? boys : girls);

            // less组（如女生）的小天使一定是more组（如男生），且不能抽到自己，且不能重复
            List<Integer> morePool = new ArrayList<>(more);
            java.util.Collections.shuffle(morePool);
            boolean hasSelf = false;
            boolean[] used = new boolean[morePool.size()];
            int[] lessToMore = new int[less.size()];
            Arrays.fill(lessToMore, -1);
            for(int i = 0; i < less.size(); i++) {
                boolean found = false;
                for(int j = 0; j < morePool.size(); j++) {
                    if(!used[j] && !less.get(i).equals(morePool.get(j))) {
                        lessToMore[i] = morePool.get(j);
                        used[j] = true;
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    hasSelf = true;
                    break;
                }
            }
            if(hasSelf) continue;

            // more组的小天使可以是所有人（more+less），不能抽到自己，不能重复
            List<Integer> allPool = new ArrayList<>(more);
            allPool.addAll(less);
            java.util.Collections.shuffle(allPool);
            used = new boolean[allPool.size()];
            int[] moreToAll = new int[more.size()];
            Arrays.fill(moreToAll, -1);
            for(int i = 0; i < more.size(); i++) {
                boolean found = false;
                for(int j = 0; j < allPool.size(); j++) {
                    if(!used[j] && !more.get(i).equals(allPool.get(j))) {
                        // 还要保证这个人没有被less组抽到过
                        boolean alreadyAssigned = false;
                        for(int k = 0; k < lessToMore.length; k++) {
                            if(lessToMore[k] == allPool.get(j)) {
                                alreadyAssigned = true;
                                break;
                            }
                        }
                        if(alreadyAssigned) continue;
                        moreToAll[i] = allPool.get(j);
                        used[j] = true;
                        found = true;
                        break;
                    }
                }
                if(!found) {
                    hasSelf = true;
                    break;
                }
            }
            if(hasSelf) continue;

            // 分配成功，插入数据库
            for(int i = 0; i < less.size(); i++) {
                Task task = new Task();
                task.setRouteId(route.getId());
                task.setMemberId(less.get(i));
                task.setMasterId(lessToMore[i]);
                taskMapper.insert(task);
            }
            for(int i = 0; i < more.size(); i++) {
                Task task = new Task();
                task.setRouteId(route.getId());
                task.setMemberId(more.get(i));
                task.setMasterId(moreToAll[i]);
                taskMapper.insert(task);
            }
            route.setAllocated(1);
            routeMapper.updateAllocatedById(route.getId(), 1);
            success = true;
        }
        if(!success){
            return Result.error("-1", "分配失败，请重试");
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
