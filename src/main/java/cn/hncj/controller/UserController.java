package cn.hncj.controller;

import cn.hncj.pojo.Users;
import cn.hncj.pojo.vo.UsersVo;
import cn.hncj.service.UserService;
import cn.hncj.utils.IMoocJSONResult;
import cn.hncj.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @Author FanJian
 * @Date 2022-12-20 15:38
 */
@RestController
@RequestMapping("/u")
public class UserController {
    @Autowired
    private UserService userService;
    @PostMapping("/registerOrLogin")
    public IMoocJSONResult registerOrLogin(@RequestBody Users user) throws Exception {
        if (StringUtils.isBlank(user.getUsername())
                || StringUtils.isBlank(user.getPassword())) {
            return IMoocJSONResult.errorMsg("用户名或密码不能为空");
        }

        boolean isExist = userService.queryUsernameIsExist(user.getUsername());
        Users userResult = null;
        if (isExist) {
            userResult = userService.queryUserForLogin(user.getUsername(), MD5Utils.getMD5Str(user.getPassword()));
            if (userResult == null) {
                return IMoocJSONResult.errorMsg("用户不存在");
            }
        } else {
            // 注册
            user.setUsername(user.getUsername());
            user.setFaceImage("");
            user.setFaceImageBig("");
            user.setPassword(MD5Utils.getMD5Str(user.getPassword()));
            user.setNickname(user.getUsername());
            userResult = userService.saveUser(user);
        }
        UsersVo usersVo = new UsersVo();
        BeanUtils.copyProperties(userResult,usersVo);
        return IMoocJSONResult.ok(usersVo);
    }
}
