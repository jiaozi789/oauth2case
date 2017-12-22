package cn.et.conf;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
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
/**
 * 自定义realm的实现
 * @author jiaozi
 *
 */
public class MyRealm extends AuthorizingRealm {
	//用于存放用户信息
	static Map<String,String> userList=null;
	//用于存放橘色信息
	static Map<String,String> roleList=null;
	//每个realm都有一个名字
	static String REALM_NAME="myrealm";
	static{
		//这里也可以从数据库读取
		//模拟用户
		userList=new HashMap();
		userList.put("zs", "123456,role2,role3");
		//模拟权限
		roleList=new HashMap();
		roleList.put("role2","user:query:*");
		roleList.put("role3", "user:*");
	}
	/**
	 * 支持哪种令牌 
	 */
	@Override
	public boolean supports(AuthenticationToken token) {
		// TODO Auto-generated method stub
		return token instanceof UsernamePasswordToken;
	}
	/**
	 * 获取权限过程
	 */
	@Override
	protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
		//获取用户名
		String userName=principals.getPrimaryPrincipal().toString();
		//构建权限的类
		SimpleAuthorizationInfo sai=new SimpleAuthorizationInfo();
		Set<String> proleList=new HashSet<String>();
		Set<String> stringPermissions=new HashSet<String>();
		if(userList.containsKey(userName)){
			String[] roles=userList.get(userName).toString().split(",");
			for(int i=1;i<roles.length;i++){
				proleList.add(roles[i]);
				String pp=roleList.get(roles[i]);
				String[] ppArry=pp.split(",");
				for(int j=0;j<ppArry.length;j++){
					stringPermissions.add(ppArry[j]);
				}
			}
		}
		sai.setRoles(proleList);
		sai.setStringPermissions(stringPermissions);
		return sai;
	}
	/**
	 * 认证过程
	 */
	@Override
	protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken token) throws AuthenticationException {
		UsernamePasswordToken upt=(UsernamePasswordToken)token;
		String userName=token.getPrincipal().toString();
		String password=String.valueOf(upt.getPassword());
		if(userList.containsKey(userName)){
			String realPwd=userList.get(userName).toString().split(",")[0];
			if(realPwd.equals(password)){
				SimpleAccount sa=new SimpleAccount(userName,password,"REALM_NAME");
				return sa;
			}
		}
		return null;
	}

}
