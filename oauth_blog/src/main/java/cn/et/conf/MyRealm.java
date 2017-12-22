package cn.et.conf;


import java.util.HashSet;
import java.util.Set;

import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.AuthenticationInfo;
import org.apache.shiro.authc.AuthenticationToken;
import org.apache.shiro.authc.SimpleAccount;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.authz.AuthorizationInfo;
import org.apache.shiro.authz.SimpleAuthorizationInfo;
import org.apache.shiro.realm.AuthorizingRealm;
import org.apache.shiro.subject.PrincipalCollection;
import org.springframework.beans.factory.annotation.Autowired;
/**
 * 读取数据库的realm
 * @author jiaozi
 *
 */
import org.springframework.stereotype.Component;

import cn.et.dao.UserDao;
@Component
public class MyRealm extends AuthorizingRealm {
	private static final String REALM_NAME = "my_ream";
	@Autowired
	UserDao userDao;
	/**
	 * 支持什么样的令牌
	 */
	@Override
	public boolean supports(AuthenticationToken token) {
		// TODO Auto-generated method stub
		return token instanceof UsernamePasswordToken;
	}
	/**
	 * 获取权限 这里暂时不涉及权限控制
	 */
	@Override 
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		String userName=principals.getPrimaryPrincipal().toString();
		SimpleAuthorizationInfo sai=new SimpleAuthorizationInfo();
		Set<String> proleList=new HashSet<String>();
		Set<String> stringPermissions=new HashSet<String>();
		sai.setRoles(proleList);
		sai.setStringPermissions(stringPermissions);
		return sai;
	}
	/**
	 * 认证
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upt=(UsernamePasswordToken)token;
		String userName=token.getPrincipal().toString();
		String password=String.valueOf(upt.getPassword());
		if(userDao.queryByContent(userName, password).size()>0){
			SimpleAccount sa=new SimpleAccount(userName,password,REALM_NAME);
			return sa;
		}
		return null;
	}

}
