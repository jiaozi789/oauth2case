package cn.et.conf;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.authz.AuthorizationFilter;

public class MyFilter extends AuthorizationFilter {

	/*
	 * 返回true表示允许访问 false表示不允许访问
	 	 */
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		return false;
	}

}
