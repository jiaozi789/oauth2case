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
 * �����û���Ȩ
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
	 * �������ͻ��� ������Ȩ����� ���ø÷��� ��֤����ͨ���Զ��ض��� �û�������ҳ��  
	 * �ο�����΢�� ��ȡ��Ȩ�� http://open.weibo.com/wiki/Oauth2/authorize
	 * �ͻ�����Ȩģʽ������ 1 �ͻ��˵���Ȩģʽ �����ο�http://www.ruanyifeng.com/blog/2014/05/oauth_2_0.html
	 * �ͻ��������лᴫ��һ�²�����ֻ֧�ֿͻ��˵���Ȩģʽ  code��ʾ����ģʽ��
	 * response_type����ʾ��Ȩ���ͣ���ѡ��˴���ֵ�̶�Ϊ"code"
		client_id����ʾ�ͻ��˵�ID����ѡ��  �����Ҫ��ȡ���� ����Ҫ��������Ȩ���� ����ͻ���id����Կ
		redirect_uri����ʾ�ض���URI����ѡ��
		scope����ʾ�����Ȩ�޷�Χ����ѡ��
		state����ʾ�ͻ��˵ĵ�ǰ״̬������ָ������ֵ����֤��������ԭ�ⲻ���ط������ֵ��
		client_secret���ͻ�����Կ
	 * 
	 * 
	 * @param userName
	 * @param password
	 * @return
	 */
	
	@RequestMapping("authorize")
	public Object login(HttpServletRequest request){
		
		try {
			//����oauth�ͻ�������
			OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);
			String clientId=oauthRequest.getClientId();
			String clientSecret = oauthRequest.getClientSecret();
			String responseType=oauthRequest.getResponseType();
			String redirectURI = oauthRequest.getRedirectURI();
			ResponseEntity<String> checkAuthorizeCodeRequest = checkAuthorizeCodeRequest(oauthRequest);
			//��֤ʧ�� ����ʧ�ܵ�ResponseEntity
			if(checkAuthorizeCodeRequest!=null){
				return checkAuthorizeCodeRequest;
			}
			return "redirect:"+AUTHCONFIRMPAGE+"?client_id="+clientId+"&redirect_uri="+redirectURI+"&response_type=code&client_secret="+clientSecret;
		} catch (Exception e) {
			e.printStackTrace();
			return new ResponseEntity<String>("���Ϸ�����",HttpStatus.BAD_REQUEST);
		}
	}
	/**
	 * ���û����ҳ����Ȩ�� �Զ���ת��������Ʋ�������Ȩ�� ���� �ض��� �û�ָ����RedirectURI
	 *  ��Ȩ��Ӧ��Ĳ��������ص�url һ��Ϊ��
	 *  code����ʾ��Ȩ�룬��ѡ��������Ч��Ӧ�ụ́ܶ�ͨ����Ϊ10���ӣ��ͻ���ֻ��ʹ�ø���һ�Σ�����ᱻ��Ȩ�������ܾ���������ͻ���ID���ض���URI����һһ��Ӧ��ϵ��
        state������ͻ��˵������а��������������֤�������Ļ�ӦҲ����һģһ���������������
	 *  
	 * @param request
	 * @return
	 */
	@RequestMapping("authorizeCode")
	public Object authorizeCode(HttpServletRequest request){
		try {
			//����oauth�ͻ�������
			OAuthAuthzRequest oauthRequest = new OAuthAuthzRequest(request);
			String clientId=oauthRequest.getClientId();
			String clientSecret = oauthRequest.getClientSecret();
			String responseType=oauthRequest.getResponseType();
			String redirectURI = oauthRequest.getRedirectURI();
			String userId=oauthRequest.getParam("user_id");
			ResponseEntity<String> checkAuthorizeCodeRequest = checkAuthorizeCodeRequest(oauthRequest);
			//��֤ʧ�� ����ʧ�ܵ�ResponseEntity
			if(checkAuthorizeCodeRequest!=null){
				return checkAuthorizeCodeRequest;
			}
			//�û����ͬ�� �����û�id����
			if(userId==null){
				OAuthResponse response = OAuthASResponse  
			             .errorResponse(HttpServletResponse.SC_BAD_REQUEST)  //����״̬��
			             .setError(OAuthError.TokenResponse.INVALID_CLIENT)  //����״̬���Ӧ�Ĵ����ı�
			             .setErrorDescription("���Ϸ��Ŀͻ������� �û�id ������ ���ȵ�¼����ϵͳ")              //������ʾ�Ĵ�����Ϣ��
			             .buildJSONMessage();  
				return new ResponseEntity<String>(response.getBody(),HttpStatus.valueOf(response.getResponseStatus()));
			}
			//������Ȩ��
			OAuthIssuerImpl oauthIssuerImpl = new OAuthIssuerImpl(new MD5Generator());  
	        String authorizationCode = oauthIssuerImpl.authorizationCode();  
			 OAuthResponse resp = OAuthASResponse
		             .authorizationResponse(request,HttpServletResponse.SC_FOUND)//�Զ�������state������������
		             .setCode(authorizationCode) //������Ȩ��
		             .location(redirectURI) //�ͻ��˻ص�url
		             .buildQueryMessage();
			 String key="authcode_"+authorizationCode;
			 //����Ȩ��洢��redis�� ������Ч����10����  ֵ�Ƕ�Ӧ���û�id
			 BoundValueOperations<String, String> boundValueOps = rt.boundValueOps(key);
			 boundValueOps.expire(10, TimeUnit.MINUTES);
			 boundValueOps.set(userId);
			 
			return "redirect:"+resp.getLocationUri();
		} catch (Exception e) {
			return new ResponseEntity<String>("δ֪�쳣 "+e.getMessage(),HttpStatus.valueOf(500));
		}
	}
	static final String AUTHCONFIRMPAGE="http://localhost:8888/auth.jsp";
	/**
	 * ����ȡ��Ȩ���������֤
	 *  �жϿͻ���id ��Կ ��Ȩģʽ��
	 * @return
	 */
	public ResponseEntity<String> checkAuthorizeCodeRequest(OAuthAuthzRequest oauthRequest){
		try {
			//����oauth�ͻ�������
			String clientId=oauthRequest.getClientId();
			String clientSecret = oauthRequest.getClientSecret();
			String responseType=oauthRequest.getResponseType();
			String redirectURI = oauthRequest.getRedirectURI();
			String userId=oauthRequest.getParam("user_id");
			//�ж�clientId��key�Ƿ������ݿ����Ѿ����� �����׳����Ϸ��Ŀͻ����쳣
			if(ca.queryClientByClientId(clientId, clientSecret).size()==0){
				OAuthResponse response = OAuthASResponse  
			             .errorResponse(HttpServletResponse.SC_BAD_REQUEST)  //����״̬��
			             .setError(OAuthError.TokenResponse.INVALID_CLIENT)  //����״̬���Ӧ�Ĵ����ı�
			             .setErrorDescription("���Ϸ��Ŀͻ����쳣")              //������ʾ�Ĵ�����Ϣ��
			             .buildJSONMessage();  
				return new ResponseEntity<String>(response.getBody(),HttpStatus.valueOf(response.getResponseStatus()));
			}
			
			//������Ȩģʽֻ֧�ֿͻ��˵���Ȩģʽ 
			if(!"code".equals(responseType)){
				OAuthResponse response = OAuthASResponse  
			             .errorResponse(HttpServletResponse.SC_BAD_REQUEST)  //����״̬��
			             .setError(OAuthError.TokenResponse.INVALID_REQUEST)  //����״̬���Ӧ�Ĵ����ı�
			             .setErrorDescription("��֧�ֵ�֧�ֿͻ��˵���Ȩģʽ ")              //������ʾ�Ĵ�����Ϣ��
			             .buildJSONMessage();  
			}
		} catch (Exception e) {
			return new ResponseEntity<String>("δ֪�쳣",HttpStatus.valueOf(500));
		}
			return null;
	}
}
