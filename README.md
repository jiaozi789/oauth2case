OATH2授权模拟
=====================
## 1.开发技术 ##
 ### 1.1 框架 ###
 使用java开源框架实现
 * MAVEN构建
 * SPRINGBOOT快速SPRING开发
 * SHIRO 权限控制
 * OLTU 解析请求参数 生成OAUTH响应
 * SPRING-DATA-JPA写入资源和权限数据
 * SPRING-DATA-REDIS用于缓存授权码和TOKEN
### 1.2 数据存储 ###
 * redis存储授权码和TOKEN
 * mysql存储用户信息和博客信息
## 2.各模块介绍 ##
### 2.1 oauth_blog ###
  是oauth2中的资源服务器 提供模拟简单的登录 登录后查看博客文章  可以新增博客 使用shiro进行了authc的登录认证
#### 项目配置
   预先启动mysql数据库 修改jdbc连接四要素
  ```properties
  spring.datasource.url=jdbc:mysql://localhost/myblog
spring.datasource.username=root
spring.datasource.password=123456
spring.datasource.driver-class-name=com.mysql.jdbc.Driver
```



### 2.2 oauth_person ###
  是oauth2中的客户端也就是第三方客户端 比如用户进入某些网站 可以使用微信授权登录  这些网站就是客户端 执行Main启动后 访问 index.jsp就可以模拟
跳转到认证服务器认证 认证服务器跳转到资源服务器登录 用户点击授权后 跳转到认证服务器获取授权 通过授权码获取令牌 通过令牌去资源服务器获取对应的资源（博客）
### 2.3 oauth_server ###
  是oauth2的授权服务器 提供转向到登录页 提供获取授权码 提供获取token

  
  
