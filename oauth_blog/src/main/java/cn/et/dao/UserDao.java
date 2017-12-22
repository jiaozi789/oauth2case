package cn.et.dao;

import java.util.List;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

import cn.et.entity.Blog;
import cn.et.entity.UserInfo;

public interface UserDao extends CrudRepository<UserInfo, Integer> {
	@Query(value="from UserInfo where userName =:userName and password = :password",nativeQuery=false)
	public List<UserInfo> queryByContent(@Param("userName") String userName,@Param("password") String password);
}
