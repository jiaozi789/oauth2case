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
 * ͨ����Ȩ���ȡtoken�Ľ׶�
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
	 * ���û����ҳ����Ȩ�� �Զ���ת��������Ʋ�������Ȩ�� ���� �ض��� �û�ָ����RedirectURI
	 * �ͻ�������֤�������������Ƶ�HTTP���󣬰������²�����
		grant_type����ʾʹ�õ���Ȩģʽ����ѡ��˴���ֵ�̶�Ϊ"authorization_code"��
		code����ʾ��һ����õ���Ȩ�룬��ѡ�
		redirect_uri����ʾ�ض���URI����ѡ��ұ�����A�����еĸò���ֵ����һ�¡�
		client_id����ʾ�ͻ���ID����ѡ�
	     ��֤���������͵�HTTP�ظ����������²�����
		access_token����ʾ�������ƣ���ѡ�
		token_type����ʾ�������ͣ���ֵ��Сд�����У���ѡ�������bearer���ͻ�mac���͡�
		expires_in����ʾ����ʱ�䣬��λΪ�롣���ʡ�Ըò���������������ʽ���ù���ʱ�䡣
		refresh_token����ʾ�������ƣ�������ȡ��һ�εķ������ƣ���ѡ�
		scope����ʾȨ�޷�Χ�������ͻ�������ķ�Χһ�£������ʡ�ԡ���Ȩ��Ӧ��Ĳ��������ص�url һ��Ϊ��
	 *  code����ʾ��Ȩ�룬��ѡ��������Ч��Ӧ�ụ́ܶ�ͨ����Ϊ10���ӣ��ͻ���ֻ��ʹ�ø���һ�Σ�����ᱻ��Ȩ�������ܾ���������ͻ���ID���ض���URI����һһ��Ӧ��ϵ��
        state������ͻ��˵������а��������������֤�������Ļ�ӦҲ����һģһ���������������
	 *  ������һ�����ӡ�

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
			//����oauth�ͻ�������
			OAuthTokenRequest oauthRequest = new OAuthTokenRequest(request);
			String clientId=oauthRequest.getClientId();
			String clientSecret = oauthRequest.getClientSecret();
			String grantType=oauthRequest.getGrantType();
			String code = oauthRequest.getCode();
			ResponseEntity<String> checkAuthorizeCodeRequest = checkAuthorizeCodeRequest(oauthRequest);
			//��֤ʧ�� ����ʧ�ܵ�ResponseEntity
			if(checkAuthorizeCodeRequest!=null){
				return checkAuthorizeCodeRequest;
			}
			//��Ȩ��ֻ��ʹ��һ�� �����delete��
			rt.delete("authcode_"+code);
			//����token
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
	        //�������ajax
	        headers.add("Access-Control-Allow-Origin","*");
	        ResponseEntity<String> responseEntity = new ResponseEntity<String>(r.getBody(),headers,HttpStatus.valueOf(r.getResponseStatus()));
	       
	        return responseEntity;
	        //����Ȩ��洢��redis�� ������Ч����10���� 
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("�Ƿ�����",HttpStatus.BAD_REQUEST);
		}
	}
	
	/**
	 * ����ȡ��Ȩ���������֤
	 *  �жϿͻ���id ��Կ ��Ȩģʽ��
	 * @return
	 */
	public ResponseEntity<String> checkAuthorizeCodeRequest(OAuthTokenRequest oauthRequest){
		try {
			//����oauth�ͻ�������
			String clientId=oauthRequest.getClientId();
			String clientSecret = oauthRequest.getClientSecret();
			String redirectURI = oauthRequest.getRedirectURI();
			String code = oauthRequest.getCode();
			//�ж�clientId��key�Ƿ������ݿ����Ѿ����� �����׳����Ϸ��Ŀͻ����쳣
			if(ca.queryClientByClientId(clientId, clientSecret).size()==0){
				OAuthResponse response = OAuthASResponse  
			             .errorResponse(HttpServletResponse.SC_OK)  //����״̬��
			             .setError(OAuthError.TokenResponse.INVALID_CLIENT)  //����״̬���Ӧ�Ĵ����ı�
			             .setErrorDescription("���Ϸ��Ŀͻ����쳣")              //������ʾ�Ĵ�����Ϣ��
			             .buildJSONMessage();  
				 HttpHeaders headers=new HttpHeaders();
			        //�������ajax
			    headers.add("Access-Control-Allow-Origin","*");
				return new ResponseEntity<String>(response.getBody(),headers,HttpStatus.valueOf(response.getResponseStatus()));
			}
			//�ж���Ȩ���Ƿ����
			if(!rt.hasKey("authcode_"+code)){
				OAuthResponse response = OAuthASResponse  
			             .errorResponse(HttpServletResponse.SC_OK)  //����״̬��
			             .setError(OAuthError.TokenResponse.INVALID_CLIENT)  //����״̬���Ӧ�Ĵ����ı�
			             .setErrorDescription("���Ϸ��Ŀͻ����쳣 ��Ȩ����Ч")              //������ʾ�Ĵ�����Ϣ��
			             .buildJSONMessage();  
				 HttpHeaders headers=new HttpHeaders();
			        //�������ajax
			    headers.add("Access-Control-Allow-Origin","*");
				return new ResponseEntity<String>(response.getBody(),headers,HttpStatus.valueOf(response.getResponseStatus()));

			}
		} catch (Exception e) {
			OAuthResponse response=null;
			try {
				response = OAuthASResponse  
				         .errorResponse(HttpServletResponse.SC_OK)  //����״̬��
				         .setError(OAuthError.TokenResponse.INVALID_CLIENT)  //����״̬���Ӧ�Ĵ����ı�
				         .setErrorDescription("δ֪�쳣")              //������ʾ�Ĵ�����Ϣ��
				         .buildJSONMessage();
			} catch (OAuthSystemException e1) {
				e1.printStackTrace();
			}  
			HttpHeaders headers=new HttpHeaders();
	        //�������ajax
	    headers.add("Access-Control-Allow-Origin","*");
			return new ResponseEntity<String>(response.getBody(),headers,HttpStatus.valueOf(response.getResponseStatus()));
		}
		return null;
	}
}
