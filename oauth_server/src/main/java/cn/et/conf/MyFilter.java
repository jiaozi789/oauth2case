package cn.et.conf;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;

import org.apache.shiro.web.filter.authz.AuthorizationFilter;

public class MyFilter extends AuthorizationFilter {

	/*
	 * ����true��ʾ������� false��ʾ���������
	 	 */
	@Override
	protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue)
			throws Exception {
		return false;
	}

}
