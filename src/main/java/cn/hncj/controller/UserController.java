package cn.hncj.controller;

import cn.hncj.enums.OperatorFriendRequestTypeEnum;
import cn.hncj.enums.SearchFriendsStatusEnum;
import cn.hncj.pojo.ChatMsg;
import cn.hncj.pojo.Users;
import cn.hncj.pojo.bo.UsersBo;
import cn.hncj.pojo.vo.MyFriendsVO;
import cn.hncj.pojo.vo.UsersVo;
import cn.hncj.service.UserService;
import cn.hncj.utils.FastDFSClient;
import cn.hncj.utils.FileUtils;
import cn.hncj.utils.IMoocJSONResult;
import cn.hncj.utils.MD5Utils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

/**
 * @Author FanJian
 * @Date 2022-12-20 15:38
 */
@RestController
@RequestMapping("/u")
public class UserController {
    @Autowired
    private UserService userService;
    @Autowired
    FastDFSClient fastDFSClient;
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


    @PostMapping("/uploadFaceBase64")
    public IMoocJSONResult uploadFaceBase64(@RequestBody UsersBo userBo) throws Exception {
        String faceData = userBo.getFaceData();
        String userFacePath = "C:\\" + userBo.getUserId() + "userface64.png";
        FileUtils.base64ToFile(userFacePath,faceData);
        MultipartFile faceFile = FileUtils.fileToMultipart(userFacePath);
        String url = fastDFSClient.uploadBase64(faceFile);
        System.out.println(url);
        // 获取缩略图的url
        String thump = "_80x80.";
        String[] split = url.split("\\.");
        String thumpUrl = split[0] + thump + split[1];
        Users user = new Users();
        user.setId(userBo.getUserId());
        user.setFaceImage(thumpUrl);
        user.setFaceImageBig(url);
        user = userService.updateUserInfo(user);
        return IMoocJSONResult.ok(user);

    }

    /**
     * @Description: 设置用户昵称
     */
    @PostMapping("/setNickname")
    public IMoocJSONResult setNickname(@RequestBody UsersBo userBO) throws Exception {

        Users user = new Users();
        user.setId(userBO.getUserId());
        user.setNickname(userBO.getNickname());

        Users result = userService.updateUserInfo(user);

        return IMoocJSONResult.ok(result);
    }


    /**
     * @Description: 搜索好友接口, 根据账号做匹配查询而不是模糊查询
     */
    @PostMapping("/search")
    public IMoocJSONResult searchUser(String myUserId, String friendUsername)
            throws Exception {

        // 0. 判断 myUserId friendUsername 不能为空
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUsername)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status.equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            Users user = userService.queryUserInfoByUsername(friendUsername);
            UsersVo userVO = new UsersVo();
            BeanUtils.copyProperties(user, userVO);
            return IMoocJSONResult.ok(userVO);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }
    }
    @PostMapping("/addFriendRequest")
    public IMoocJSONResult addFriendRequest(String myUserId, String friendUsername) {
        // 0. 判断 myUserId friendUsername 不能为空
        if (StringUtils.isBlank(myUserId)
                || StringUtils.isBlank(friendUsername)) {
            return IMoocJSONResult.errorMsg("");
        }
        // 前置条件 - 1. 搜索的用户如果不存在，返回[无此用户]
        // 前置条件 - 2. 搜索账号是你自己，返回[不能添加自己]
        // 前置条件 - 3. 搜索的朋友已经是你的好友，返回[该用户已经是你的好友]
        Integer status = userService.preconditionSearchFriends(myUserId, friendUsername);
        if (status.equals(SearchFriendsStatusEnum.SUCCESS.status)) {
            userService.sendFriendRequest(myUserId,friendUsername);
        } else {
            String errorMsg = SearchFriendsStatusEnum.getMsgByKey(status);
            return IMoocJSONResult.errorMsg(errorMsg);
        }
        return IMoocJSONResult.ok();
    }

    /**
     * @Description: 发送添加好友的请求
     */
    @PostMapping("/queryFriendRequests")
    public IMoocJSONResult queryFriendRequests(String userId) {

        // 0. 判断不能为空
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 1. 查询用户接受到的朋友申请
        return IMoocJSONResult.ok(userService.queryFriendRequestList(userId));
    }


    /**
     * @Description: 接受方 通过或者忽略朋友请求
     */
    @PostMapping("/operFriendRequest")
    public IMoocJSONResult operFriendRequest(String acceptUserId, String sendUserId,
                                             Integer operType) {

        // 0. acceptUserId sendUserId operType 判断不能为空
        if (StringUtils.isBlank(acceptUserId)
                || StringUtils.isBlank(sendUserId)
                || operType == null) {
            return IMoocJSONResult.errorMsg("");
        }

        // 1. 如果operType 没有对应的枚举值，则直接抛出空错误信息
        if (StringUtils.isBlank(OperatorFriendRequestTypeEnum.getMsgByType(operType))) {
            return IMoocJSONResult.errorMsg("");
        }

        if (operType.equals(OperatorFriendRequestTypeEnum.IGNORE.type)) {
            // 2. 判断如果忽略好友请求，则直接删除好友请求的数据库表记录
            userService.deleteFriendRequest(sendUserId, acceptUserId);
        } else if (operType.equals(OperatorFriendRequestTypeEnum.PASS.type)) {
            // 3. 判断如果是通过好友请求，则互相增加好友记录到数据库对应的表
            //	   然后删除好友请求的数据库表记录
            userService.passFriendRequest(sendUserId, acceptUserId);
        }

        // 4. 数据库查询好友列表
        List<MyFriendsVO> myFirends = userService.queryMyFriends(acceptUserId);

        return IMoocJSONResult.ok(myFirends);
    }


    /**
     * @Description: 查询我的好友列表
     */
    @PostMapping("/myFriends")
    public IMoocJSONResult myFriends(String userId) {
        // 0. userId 判断不能为空
        if (StringUtils.isBlank(userId)) {
            return IMoocJSONResult.errorMsg("");
        }

        // 1. 数据库查询好友列表
        List<MyFriendsVO> myFirends = userService.queryMyFriends(userId);

        return IMoocJSONResult.ok(myFirends);
    }
    /**
     * @Description: 查询我的好友列表
     */
    @PostMapping("/getUnReadMsgList")
    public IMoocJSONResult getUnReadMsgList(String acceptUserId) {
        // 0. userId 判断不能为空
        if (StringUtils.isBlank(acceptUserId)) {
            return IMoocJSONResult.errorMsg("");
        }
        // 1. 数据库查询好友列表
        List<ChatMsg> unReadMsgList = userService.getUnReadMsgList(acceptUserId);
        return IMoocJSONResult.ok(unReadMsgList);
    }


}
