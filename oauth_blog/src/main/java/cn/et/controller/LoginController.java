package cn.et.controller;


import javax.servlet.http.HttpServletRequest;

import org.apache.shiro.SecurityUtils;
import org.apache.shiro.authc.AuthenticationException;
import org.apache.shiro.authc.UsernamePasswordToken;
import org.apache.shiro.subject.Subject;
import org.apache.shiro.web.util.SavedRequest;
import org.apache.shiro.web.util.WebUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.et.dao.UserDao;
@Controller
public class LoginController {
	@Autowired
	UserDao userDao;
	
	/**
	 * 登录的控制层
	 * @param userName
	 * @param password
	 * @return
	 */
	@RequestMapping("loginBlog")
	public String login(String userName,String password,HttpServletRequest request){
		Subject subject = SecurityUtils.getSubject();
		UsernamePasswordToken upt=new UsernamePasswordToken(userName,password);
		try {
			subject.login(upt);
			subject.getSession().setAttribute("userInfo", userDao.queryByContent(userName, password).get(0));
			SavedRequest savedRequest = WebUtils.getSavedRequest(request);
			if(savedRequest!=null)
				return "redirect:"+savedRequest.getRequestURI()+"?"+savedRequest.getQueryString();
			return "/queryBlog";
		} catch (AuthenticationException e) {
			return "redirect:/login.html";
		}
		
	}
	@RequestMapping("loginout")
	public String loginOut(){
		Subject subject = SecurityUtils.getSubject();
		subject.logout();
		return "redirect:/login.html";
	}
	
}
