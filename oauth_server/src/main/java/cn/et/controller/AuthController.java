package cn.et.controller;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthAuthzRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

import cn.et.dao.ClientDao;
/**
 * 申请用户授权
 * @author jiaozi
 *
 */
@Controller
public class AuthController {
	@Autowired
	ClientDao ca;
	@Autowired
	StringRedisTemplate rt;
	/**
	 * 第三方客户端 调用授权的入口 调用该方法 验证参数通过自动重定向到 用户点击许可页面  
	 * 参考新浪微博 获取授权码 http://open.weibo.com/wiki/Oauth2/authorize
	 * 客户端授权模式有四种 1 客户端的授权模式 其他参考http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html
	 * 客户端请求中会传递一下参数（只支持客户端的授权模式  code表示这种模式）
	 * response_type：表示授权类型，必选项，此处的值固定为"code"
		client_id：表示客户端的ID，必选项  如果需要获取数据 必须要到博客授权中心 申请客户端id和秘钥
		redirect_uri：表示重定向URI，可选项
		scope：表示申请的权限范围，可选项
		state：表示客户端的当前状态，可以指定任意值，认证服务器会原封不动地返回这个值。
		client_secret：客户端秘钥
	 * 
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	
	@RequestMapping("authorize")
	public Object login(HttpServletRequest request){
		
		try {
			//解析oauth客户端请求
			OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);
			String clientId=oauthRequest.getClientId();
			String clientSecret = oauthRequest.getClientSecret();
			String responseType=oauthRequest.getResponseType();
			String redirectURI = oauthRequest.getRedirectURI();
			ResponseEntity<String> checkAuthorizeCodeRequest = checkAuthorizeCodeRequest(oauthRequest);
			//验证失败 返回失败的ResponseEntity
			if(checkAuthorizeCodeRequest!=null){
				return checkAuthorizeCodeRequest;
			}
			return "redirect:"+AUTHCONFIRMPAGE+"?client_id="+clientId+"&redirect_uri="+redirectURI+"&response_type=code&client_secret="+clientSecret;
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("不合法请求",HttpStatus.BAD_REQUEST);
		}
	}
	/**
	 * 当用户点击页面授权后 自动跳转到这个控制层生成授权码 必须 重定向 用户指定的RedirectURI
	 *  授权响应码的参数传给回调url 一般为：
	 *  code：表示授权码，必选项。该码的有效期应该很短，通常设为10分钟，客户端只能使用该码一次，否则会被授权服务器拒绝。该码与客户端ID和重定向URI，是一一对应关系。
        state：如果客户端的请求中包含这个参数，认证服务器的回应也必须一模一样包含这个参数。
	 *  
	 * @param request
	 * @return
	 */
	@RequestMapping("authorizeCode")
	public Object authorizeCode(HttpServletRequest request){
		try {
			//解析oauth客户端请求
			OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);
			String clientId=oauthRequest.getClientId();
			String clientSecret = oauthRequest.getClientSecret();
			String responseType=oauthRequest.getResponseType();
			String redirectURI = oauthRequest.getRedirectURI();
			String userId=oauthRequest.getParam("user_id");
			ResponseEntity<String> checkAuthorizeCodeRequest = checkAuthorizeCodeRequest(oauthRequest);
			//验证失败 返回失败的ResponseEntity
			if(checkAuthorizeCodeRequest!=null){
				return checkAuthorizeCodeRequest;
			}
			//用户点击同意 传入用户id过来
			if(userId==null){
				OAuthResponse response = OAuthASResponse  
			             .errorResponse(HttpServletResponse.SC_BAD_REQUEST)  //设置状态码
			             .setError(OAuthError.TokenResponse.INVALID_CLIENT)  //设置状态码对应的错误文本
			             .setErrorDescription("不合法的客户端请求 用户id 不存在 请先登录博客系统")              //设置显示的错误消息体
			             .buildJSONMessage();  
				return new ResponseEntity<String>(response.getBody(),HttpStatus.valueOf(response.getResponseStatus()));
			}
			//生成授权码
			OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());  
	        String authorizationCode = oauthIssuerImpl.authorizationCode();  
			 OAuthResponse resp = OAuthASResponse
		             .authorizationResponse(request,HttpServletResponse.SC_FOUND)//自动将请求state构建到参数中
		             .setCode(authorizationCode) //设置授权码
		             .location(redirectURI) //客户端回调url
		             .buildQueryMessage();
			 String key="authcode_"+authorizationCode;
			 //将授权码存储在redis中 设置有效期是10分钟  值是对应的用户id
			 BoundValueOperations<String, String> boundValueOps = rt.boundValueOps(key);
			 boundValueOps.expire(10, TimeUnit.MINUTES);
			 boundValueOps.set(userId);
			 
			return "redirect:"+resp.getLocationUri();
		} catch (Exception e) {
			return new ResponseEntity<String>("未知异常 "+e.getMessage(),HttpStatus.valueOf(500));
		}
	}
	static final String AUTHCONFIRMPAGE="http://localhost:8888/auth.jsp";
	/**
	 * 检查获取授权码请求的验证
	 *  判断客户端id 秘钥 授权模式等
	 * @return
	 */
	public ResponseEntity<String> checkAuthorizeCodeRequest(OAuthAuthzRequest oauthRequest){
		try {
			//解析oauth客户端请求
			String clientId=oauthRequest.getClientId();
			String clientSecret = oauthRequest.getClientSecret();
			String responseType=oauthRequest.getResponseType();
			String redirectURI = oauthRequest.getRedirectURI();
			String userId=oauthRequest.getParam("user_id");
			//判断clientId和key是否在数据库中已经存在 否则抛出不合法的客户端异常
			if(ca.queryClientByClientId(clientId, clientSecret).size()==0){
				OAuthResponse response = OAuthASResponse  
			             .errorResponse(HttpServletResponse.SC_BAD_REQUEST)  //设置状态码
			             .setError(OAuthError.TokenResponse.INVALID_CLIENT)  //设置状态码对应的错误文本
			             .setErrorDescription("不合法的客户端异常")              //设置显示的错误消息体
			             .buildJSONMessage();  
				return new ResponseEntity<String>(response.getBody(),HttpStatus.valueOf(response.getResponseStatus()));
			}
			
			//四种授权模式只支持客户端的授权模式 
			if(!"code".equals(responseType)){
				OAuthResponse response = OAuthASResponse  
			             .errorResponse(HttpServletResponse.SC_BAD_REQUEST)  //设置状态码
			             .setError(OAuthError.TokenResponse.INVALID_REQUEST)  //设置状态码对应的错误文本
			             .setErrorDescription("不支持的支持客户端的授权模式 ")              //设置显示的错误消息体
			             .buildJSONMessage();  
			}
		} catch (Exception e) {
			return new ResponseEntity<String>("未知异常",HttpStatus.valueOf(500));
		}
			return null;
	}
}
