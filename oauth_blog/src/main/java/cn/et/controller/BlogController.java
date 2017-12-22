package cn.et.controller;


import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.apache.oltu.oauth2.common.exception.OAuthProblemException;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.types.ParameterStyle;
import org.apache.oltu.oauth2.rs.request.OAuthAccessResourceRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import cn.et.dao.BlogDao;
import cn.et.entity.Blog;
@Controller
public class BlogController {
	@Autowired
	BlogDao blogDao;
	@Autowired
	StringRedisTemplate rt;
	/**
	 * 登录的控制层
	 * @param userName
	 * @param password
	 * @return
	 */
	@RequestMapping("/queryBlog")
	public String queryBlog(String name,Model model){
		if(name==null) name="";
		List<Blog> blogList = blogDao.queryByContent(name);
		model.addAttribute("blogList", blogList);
		return "/query.jsp";
	}
	
	/**
	 * 用户获取到token数据后就可以到这里请求资源了
	 * @param userName
	 * @param password
	 * @return
	 */
	@RequestMapping("/queryBlogByToken")
	public Object queryBlog(HttpServletRequest request){
		Message message=new Message();
		HttpHeaders headers=new HttpHeaders();
        //允许跨域ajax
        headers.add("Access-Control-Allow-Origin","*");
		OAuthAccessResourceRequest oauthRequest;
		ObjectMapper mapper = new ObjectMapper();  
		try {
			oauthRequest = new
			        OAuthAccessResourceRequest(request, ParameterStyle.QUERY);
			String accessToken = oauthRequest.getAccessToken();
			if(rt.hasKey("TOKEN_"+accessToken)){
				List<Blog> blogList = blogDao.queryByContent("");
				message.setData(blogList);
				return new ResponseEntity<String>(mapper.writeValueAsString(message),headers,HttpStatus.OK);
			}
		} catch (OAuthSystemException e) {
			e.printStackTrace();
		} catch (OAuthProblemException e) {
			e.printStackTrace();
		}catch (Exception e) {
			e.printStackTrace();
		}
		message.setState(0);
		message.setMessage("无法获取数据 请检查您的权限");
		try {
			return new ResponseEntity<String>(mapper.writeValueAsString(message),headers,HttpStatus.OK);
		} catch (JsonProcessingException e) {
			e.printStackTrace();
		}
		return null;
	}
	/**
	 * 登录的控制层
	 * @param userName
	 * @param password
	 * @return
	 */
	@RequestMapping(value="/blog",method=RequestMethod.POST)
	public String saveBlog(Blog blog,Model model){
		blogDao.save(blog);
		return queryBlog(null,model);
	}
	
	static class Message{
		private List data;
		private int state;
		private String message;
		public List getData() {
			return data;
		}
		public void setData(List data) {
			this.data = data;
		}
		public String getMessage() {
			return message;
		}
		public void setMessage(String message) {
			this.message = message;
		}
		public int getState() {
			return state;
		}
		public void setState(int state) {
			this.state = state;
		}
		
		
	}
}
