package cn.hncj.service.impl;

import cn.hncj.mapper.UsersMapper;
import cn.hncj.pojo.Users;
import cn.hncj.pojo.vo.FriendRequestVO;
import cn.hncj.pojo.vo.MyFriendsVO;
import cn.hncj.service.UserService;
import org.n3r.idworker.Sid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import tk.mybatis.mapper.entity.Example;

import java.util.List;

/**
 * @Author FanJian
 * @Date 2022-12-21 15:51
 */
@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UsersMapper userMapper;

    @Autowired
    private Sid id;

    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public boolean queryUsernameIsExist(String username) {
        Users user = new Users();
        user.setUsername(username);
        Users res = userMapper.selectOne(user);
        if (res == null) {
            return false;
        } else {
            return true;
        }
    }

    /**
     * TODO
     */
    @Transactional(propagation = Propagation.SUPPORTS)
    @Override
    public Users queryUserForLogin(String username, String pwd) {
        Example userExample = new Example(Users.class);
        Example.Criteria criteria = userExample.createCriteria();
        criteria.andEqualTo("username",username);
        criteria.andEqualTo("password",pwd);
        Users user = userMapper.selectOneByExample(userExample);
        return user;
    }

    @Override
    public Users saveUser(Users user) {
        String userId = id.nextShort();
        // 二维码
        user.setQrcode("");
        user.setId(userId);
        userMapper.insert(user);
        return user;
    }

    @Override
    public Users updateUserInfo(Users user) {
        return null;
    }

    @Override
    public Integer preconditionSearchFriends(String myUserId, String friendUsername) {
        return null;
    }

    @Override
    public Users queryUserInfoByUsername(String username) {
        return null;
    }

    @Override
    public void sendFriendRequest(String myUserId, String friendUsername) {

    }

    @Override
    public List<FriendRequestVO> queryFriendRequestList(String acceptUserId) {
        return null;
    }

    @Override
    public void deleteFriendRequest(String sendUserId, String acceptUserId) {

    }

    @Override
    public void passFriendRequest(String sendUserId, String acceptUserId) {

    }

    @Override
    public List<MyFriendsVO> queryMyFriends(String userId) {
        return null;
    }

    @Override
    public void updateMsgSigned(List<String> msgIdList) {

    }
}
