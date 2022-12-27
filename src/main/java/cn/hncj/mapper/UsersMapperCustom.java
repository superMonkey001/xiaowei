package cn.hncj.mapper;

import cn.hncj.pojo.Users;
import cn.hncj.pojo.vo.FriendRequestVO;
import cn.hncj.pojo.vo.MyFriendsVO;
import cn.hncj.utils.MyMapper;

import java.util.List;

public interface UsersMapperCustom extends MyMapper<Users> {
	
	public List<FriendRequestVO> queryFriendRequestList(String acceptUserId);
	
	public List<MyFriendsVO> queryMyFriends(String userId);
	
	public void batchUpdateMsgSigned(List<String> msgIdList);
	
}