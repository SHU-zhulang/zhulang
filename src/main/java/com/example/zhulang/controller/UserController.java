package com.example.zhulang.controller;

import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.example.zhulang.entity.User;
import com.example.zhulang.mapper.UserMapper;
import com.example.zhulang.utils.Result;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/user")
public class UserController {
    @Resource
    private UserMapper userMapper;

    /***
     * 注册
     * @param user
     * @return
     */
    @PostMapping("/signup")
    public Result<?> signup(@RequestBody User user) {
        if(userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, user.getPhone())) != null) {
            return Result.error("-1", "该手机号已被注册");
        }
        if(user.getWhatsup().isEmpty()){
            user.setWhatsup("上大逐浪，不浪会死！");
        }
        user.setRole(3);
        userMapper.insert(user);
        return Result.success(user);
    }

    /***
     * 登录
     * @param user
     * @return
     */
    @PostMapping("/login")
    public Result<?> login(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, user.getPhone()).eq(User::getPassword, user.getPassword()));
        if(res == null){
            return Result.error("-1","用户名或密码错误");
        }
        return Result.success(res);
    }

    /***
     * 找回密码
     * @param user
     * @return
     */
    @PostMapping("/findPwd")
    public Result<?> findPwd(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, user.getPhone()));
        if(res == null){
            return Result.error("-1", "该用户未注册");
        }
        res.setPassword(user.getPassword());
        userMapper.updateById(res);
        return Result.success(res);
    }

    /***
     * 修改个人信息
     * @param user
     * @return
     */
    @PostMapping("/update")
    public Result<?> update(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, user.getPhone()));
        res.setRealName(user.getRealName());
        res.setNickName(user.getNickName());
        res.setGender(user.getGender());
        res.setWhatsup(user.getWhatsup());
        userMapper.updateById(res);
        return Result.success(res);
    }

    /***
     * 寻找正在请求升级的账号
     * @return
     */
    @GetMapping("/searchLevelup")
    public Result<?> searchLevelup(){
        return Result.success(userMapper.searchLevelup());
    }

    /***
     * 申请账号升级
     * @param user
     * @return
     */
    @PostMapping("/applyForLevelup")
    public Result<?> applyForLevelup(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, user.getPhone()));
        res.setRole(4);
        userMapper.updateById(res);
        return Result.success(res);
    }

    /***
     * 账号升级
     * @param user
     * @return
     */
    @PostMapping("/levelup")
    public Result<?> levelup(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, user.getPhone()));
        res.setRole(2);
        userMapper.updateById(res);
        return Result.success();
    }

    /***
     * 拒绝账号升级
     * @param user
     * @return
     */
    @PostMapping("/refuseToLevelup")
    public Result<?> refuseToLevelup(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, user.getPhone()));
        res.setRole(3);
        userMapper.updateById(res);
        return Result.success();
    }

    /***
     * 模糊查询user
     * @param phone
     * @param realName
     * @param nickName
     * @param gender
     * @return
     */
    @GetMapping("/getUser")
    public Result<?> getUser(@RequestParam(defaultValue = "") String phone,
                             @RequestParam(defaultValue = "") String realName,
                             @RequestParam(defaultValue = "") String nickName,
                             @RequestParam(defaultValue = "") String gender){
        LambdaQueryWrapper<User> wrapper = Wrappers.<User>lambdaQuery();
        if(StrUtil.isNotBlank(phone)){
            wrapper.like(User::getPhone, phone);
        }
        if(StrUtil.isNotBlank(realName)){
            wrapper.like(User::getRealName, realName);
        }
        if(StrUtil.isNotBlank(nickName)){
            wrapper.like(User::getNickName, nickName);
        }
        if(!gender.equals("全部")){
            wrapper.eq(User::getGender, gender);
        }
        wrapper.last("ORDER BY CASE WHEN role IN (1, 2) THEN 0 ELSE 1 END, uid ASC");
        return Result.success(userMapper.selectList(wrapper));
    }

    /***
     * 根据字符串找出队员
     * @param team
     * @return
     */
    @GetMapping("/getByString")
    public Result<?> getByString(@RequestParam(defaultValue = "") String team){
        List<String> member = StrUtil.split(team, ',');
        List<User> res = new ArrayList<>();
        for (String m : member) {
            res.add(userMapper.selectById(Integer.parseInt(m)));
        }
        return Result.success(res);
    }

    /***
     * 密码查询
     * @param user
     * @return
     */
    @PostMapping("searchPwd")
    public Result<?> searchPwd(@RequestBody User user){
        User res = userMapper.selectOne(Wrappers.<User>lambdaQuery().eq(User::getPhone, user.getPhone()));
        if(res == null){
            return Result.error("-1","用户名错误或用户不存在");
        }
        return Result.success(res);
    }
}
