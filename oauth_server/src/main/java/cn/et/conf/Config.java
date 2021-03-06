﻿package cn.et.conf;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import org.apache.shiro.realm.Realm;
import org.apache.shiro.spring.LifecycleBeanPostProcessor;
import org.apache.shiro.spring.web.ShiroFilterFactoryBean;
import org.apache.shiro.web.mgt.DefaultWebSecurityManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.filter.DelegatingFilterProxy;

@Configuration
public class Config {
	/**
	 * 等價于 web.xml配置
	 * 自動 將 /*的請求 委託給spring容器中 bean名字和filter-name一致的bean處理
	 * 
	   <filter>
	    <filter-name>shiroFilter</filter-name>
	    <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
	    <init-param>
	        <param-name>targetFilterLifecycle</param-name>
	        <param-value>true</param-value>
	    </init-param>
		</filter>
		<filter-mapping>
		    <filter-name>shiroFilter</filter-name>
		    <url-pattern>/*</url-pattern>
		</filter-mapping>
	 * @return
	 */
	@Bean
	public FilterRegistrationBean webShiroFilter(){
		FilterRegistrationBean frb=new FilterRegistrationBean();
		DelegatingFilterProxy dfp=new DelegatingFilterProxy();
		frb.setFilter(dfp);
		frb.setName("shiroFilter");
		LinkedHashSet<String> linkedHashSet = new LinkedHashSet<String>();
		linkedHashSet.add("/*");
		frb.setUrlPatterns(linkedHashSet);
		
		
		Map<String, String> initParameters=new HashMap<String, String>();
		initParameters.put("targetFilterLifecycle", "true");
		frb.setInitParameters(initParameters);
		return frb;
	}
	/**
	 * 配置我的realm
	 * @return
	 */
	@Bean
	public Realm myRealm(){
		return new MyRealm();
	}
	/**
	 * 定義默認的securityManager
	 * @return
	 */
	@Bean
	public DefaultWebSecurityManager securityManager(@Autowired Realm myRealm){
		DefaultWebSecurityManager dwm=new DefaultWebSecurityManager();
		dwm.setRealm(myRealm);
		return dwm;
	}
	
	/**
	 * 定義和過濾器一致名字的ShiroFilterFactoryBean
	 */
	@Bean
	public ShiroFilterFactoryBean shiroFilter(@Autowired org.apache.shiro.mgt.SecurityManager securityManager){
		ShiroFilterFactoryBean sffb=new ShiroFilterFactoryBean();
		sffb.setSecurityManager(securityManager);
		sffb.setLoginUrl("/login.html");
		sffb.setUnauthorizedUrl("/un.jsp");
		Map<String, String> urls=new HashMap<String, String>();
		/*
		 * 
			/login.html = anon
			/loginServlet = anon
			/query.jsp = authc
			/add.jsp = roles[role2]
		 * */
		
		urls.put("/login.html", "anon");
		urls.put("/loginServlet", "anon");
		urls.put("/query.jsp", "authc");
		urls.put("/add.jsp", "roles[role1]");
		sffb.setFilterChainDefinitionMap(urls);
		return sffb;
	}
	/**
	 * 定義後置處理器
	 * @return
	 */
	@Bean
	public LifecycleBeanPostProcessor lifecycleBeanPostProcessor(){
		return new LifecycleBeanPostProcessor();
	}
}
