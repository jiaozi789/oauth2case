package cn.et.controller;

import java.util.concurrent.TimeUnit;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.oltu.oauth2.as.issuer.MD5Generator;
import org.apache.oltu.oauth2.as.issuer.OAuthIssuerImpl;
import org.apache.oltu.oauth2.as.request.OAuthTokenRequest;
import org.apache.oltu.oauth2.as.response.OAuthASResponse;
import org.apache.oltu.oauth2.common.error.OAuthError;
import org.apache.oltu.oauth2.common.exception.OAuthSystemException;
import org.apache.oltu.oauth2.common.message.OAuthResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundValueOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import cn.et.dao.ClientDao;
/**
 * 通过授权码获取token的阶段
 * @author jiaozi
 *
 */
@Controller
public class AccessTokenController {
	@Autowired
	ClientDao ca;
	@Autowired
	StringRedisTemplate rt;
	/**
	 * 当用户点击页面授权后 自动跳转到这个控制层生成授权码 必须 重定向 用户指定的RedirectURI
	 * 客户端向认证服务器申请令牌的HTTP请求，包含以下参数：
		grant_type：表示使用的授权模式，必选项，此处的值固定为"authorization_code"。
		code：表示上一步获得的授权码，必选项。
		redirect_uri：表示重定向URI，必选项，且必须与A步骤中的该参数值保持一致。
		client_id：表示客户端ID，必选项。
	     认证服务器发送的HTTP回复，包含以下参数：
		access_token：表示访问令牌，必选项。
		token_type：表示令牌类型，该值大小写不敏感，必选项，可以是bearer类型或mac类型。
		expires_in：表示过期时间，单位为秒。如果省略该参数，必须其他方式设置过期时间。
		refresh_token：表示更新令牌，用来获取下一次的访问令牌，可选项。
		scope：表示权限范围，如果与客户端申请的范围一致，此项可省略。授权响应码的参数传给回调url 一般为：
	 *  code：表示授权码，必选项。该码的有效期应该很短，通常设为10分钟，客户端只能使用该码一次，否则会被授权服务器拒绝。该码与客户端ID和重定向URI，是一一对应关系。
        state：如果客户端的请求中包含这个参数，认证服务器的回应也必须一模一样包含这个参数。
	 *  下面是一个例子。

	     HTTP/1.1 200 OK
	     Content-Type: application/json;charset=UTF-8
	     Cache-Control: no-store
	     Pragma: no-cache
	
	     {
	       "access_token":"2YotnFZFEjr1zCsicMWpAA",
	       "token_type":"example",
	       "expires_in":3600,
	       "refresh_token":"tGzv3JOkF0XG5Qx2TlKWIA",
	       "example_parameter":"example_value"
	     }
	 * @param request
	 * @return
	 */
	@RequestMapping(value="access_token",method=RequestMethod.POST)
	public Object authorizeCode(HttpServletRequest request){
		try {
			//解析oauth客户端请求
			OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
			String clientId=oauthRequest.getClientId();
			String clientSecret = oauthRequest.getClientSecret();
			String grantType=oauthRequest.getGrantType();
			String code = oauthRequest.getCode();
			ResponseEntity<String> checkAuthorizeCodeRequest = checkAuthorizeCodeRequest(oauthRequest);
			//验证失败 返回失败的ResponseEntity
			if(checkAuthorizeCodeRequest!=null){
				return checkAuthorizeCodeRequest;
			}
			//授权码只能使用一次 用完就delete掉
			rt.delete("authcode_"+code);
			//生成token
			OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());  
	        String accessToken = oauthIssuerImpl.accessToken(); 
	        String refreshToken = oauthIssuerImpl.refreshToken();
	        OAuthResponse r = OAuthASResponse
	                .tokenResponse(HttpServletResponse.SC_OK)
	                .setAccessToken(accessToken)
	                .setExpiresIn("3600")
	                .setRefreshToken(refreshToken)
	                .buildJSONMessage();
	        BoundValueOperations<String, String> boundValueOps = rt.boundValueOps("TOKEN_"+accessToken);
	        boundValueOps.expire(3600, TimeUnit.SECONDS);
	        boundValueOps.set("1");
	        HttpHeaders headers=new HttpHeaders();
	        headers.add("Content-Type", "application/json;charset=UTF-8"); 
	        headers.add("Cache-Control", "no-store"); 
	        headers.add("Pragma", "no-cache"); 
	        //允许跨域ajax
	        headers.add("Access-Control-Allow-Origin","*");
	        ResponseEntity<String> responseEntity = new ResponseEntity<String>(r.getBody(),headers,HttpStatus.valueOf(r.getResponseStatus()));
	       
	        return responseEntity;
	        //将授权码存储在redis中 设置有效期是10分钟 
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("非法请求",HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * 检查获取授权码请求的验证
	 *  判断客户端id 秘钥 授权模式等
	 * @return
	 */
	public ResponseEntity<String> checkAuthorizeCodeRequest(OAuthTokenRequest oauthRequest){
		try {
			//解析oauth客户端请求
			String clientId=oauthRequest.getClientId();
			String clientSecret = oauthRequest.getClientSecret();
			String redirectURI = oauthRequest.getRedirectURI();
			String code = oauthRequest.getCode();
			//判断clientId和key是否在数据库中已经存在 否则抛出不合法的客户端异常
			if(ca.queryClientByClientId(clientId, clientSecret).size()==0){
				OAuthResponse response = OAuthASResponse  
			             .errorResponse(HttpServletResponse.SC_OK)  //设置状态码
			             .setError(OAuthError.TokenResponse.INVALID_CLIENT)  //设置状态码对应的错误文本
			             .setErrorDescription("不合法的客户端异常")              //设置显示的错误消息体
			             .buildJSONMessage();  
				 HttpHeaders headers=new HttpHeaders();
			        //允许跨域ajax
			    headers.add("Access-Control-Allow-Origin","*");
				return new ResponseEntity<String>(response.getBody(),headers,HttpStatus.valueOf(response.getResponseStatus()));
			}
			//判断授权码是否存在
			if(!rt.hasKey("authcode_"+code)){
				OAuthResponse response = OAuthASResponse  
			             .errorResponse(HttpServletResponse.SC_OK)  //设置状态码
			             .setError(OAuthError.TokenResponse.INVALID_CLIENT)  //设置状态码对应的错误文本
			             .setErrorDescription("不合法的客户端异常 授权码无效")              //设置显示的错误消息体
			             .buildJSONMessage();  
				 HttpHeaders headers=new HttpHeaders();
			        //允许跨域ajax
			    headers.add("Access-Control-Allow-Origin","*");
				return new ResponseEntity<String>(response.getBody(),headers,HttpStatus.valueOf(response.getResponseStatus()));

			}
		} catch (Exception e) {
			OAuthResponse response=null;
			try {
				response = OAuthASResponse  
				         .errorResponse(HttpServletResponse.SC_OK)  //设置状态码
				         .setError(OAuthError.TokenResponse.INVALID_CLIENT)  //设置状态码对应的错误文本
				         .setErrorDescription("未知异常")              //设置显示的错误消息体
				         .buildJSONMessage();
			} catch (OAuthSystemException e1) {
				e1.printStackTrace();
			}  
			HttpHeaders headers=new HttpHeaders();
	        //允许跨域ajax
	    headers.add("Access-Control-Allow-Origin","*");
			return new ResponseEntity<String>(response.getBody(),headers,HttpStatus.valueOf(response.getResponseStatus()));
		}
		return null;
	}
}
