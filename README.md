# Boite安全框架
前后端分离的轻量级安全框架

## 功能
认证 - 用户身份识别，常被称为用户“登录”，提供账号锁定功能；
授权 - 访问控制（权限控制）；
密码加密 - 保护或隐藏数据防止被偷窥；
会话管理 - 每用户相关的时间敏感的状态；
单设备登录

## 核心概念
##### SubjectHandle：
“操作用户的句柄”。通过SubjectHandleUtil.createHandle(String sessionId)创建，该方法返回一个SubjectHandle对象；另外，通过SubjectHandleUtil.getHandle(String sessionId)可以获取SubjectHandle对象，通过该方法获得句柄的前提是之前已经创建过handle。得到句柄handle后，可以通过该句柄操作用户，例如handle.login(String username, String clearPassword),handle.getPermissions()等。

##### Realm：
Realm充当了框架与应用安全数据间的“桥梁”或者“连接器”。也就是说，当切实与像用户帐户这类安全相关数据进行交互，执行认证(登录)和授权(访问控制)时，框架会从应用配置的Realm中查找很多内容。
从这个意义上讲，Realm实质上是一个安全相关的DAO：它封装了数据源的连接细节，并在需要时将相关数据提供给框架。
Realm中有两个抽象方法：
```
void authenticate(String username, String clearPassword) throws AuthenticationException; //身份认证
Set<String> authorize(String username); //授权
```
AuthenticationException异常有三个子类：
UnknownAccountException 用户名不存在
IncorrectCredentialsException 密码错误
LockedAccountException 账户锁定
重写authenticate方法时，只需抛出UnknownAccountException和IncorrectCredentialsException，LockedAccountException可以在调用handle.login方法时直接使用，框架内部已经做了处理。

重写authorize方法时，需要返回一个Set，就是用户对应的权限列表。用户的权限控制就是基于这个列表来控制的。

## 使用
##### 1.首先要使用boite框架，必须先引入以下jar包：
org.springframework.spring-core
org.springframework.spring-beans
org.springframework.spring-context
org.springframework.spring-aop
org.springframework.spring-webmvc
org.springframework.spring-web
org.aspectj.aspectjrt
org.aspectj.aspectjweaver
commons-io.commons-io
net.sf.ehcache.ehcache-core
ch.qos.logback.logback-core
ch.qos.logback.logback-classic
##### 2.引入boite的jar包，如果是maven项目，则将jar包存到本地仓库
##### 3.新建一个类，继承AbstractRealm，重写两个方法
##### 4.将前一步中新建的类加入IOC容器，bean的id为realm
##### 5.对于身份认证（登录），登陆或注册前需要建立连接，发送请求到"/static/connect"接口，带一个sessionId参数（在客户端生成，通过调用boite.generateOnlyString()生成），该接口返回一个公钥，输入密码后使用该公钥加密（框架使用的是RSA非对称加密，也可以自定义加密方式），以后每个请求都要带上这个sessionId。创建句柄后即可操作用户。
##### 6.对于授权，每个需要权限的方法都应该提供一个带注解——@RequestParam("sessionId") 的String型参数，然后在方法上加上注解：
```
@RequiresPermissions(values = {"visit","write"}, logical = Logical.AND)
```
logical默认为AND，可以改为OR
例如：
```
@RequiresPermissions(values = {"visit","write"}, logical = Logical.OR)
@RequestMapping(value = "static/hello")
@ResponseBody
public String hello(@RequestParam("name") String name, @RequestParam("sessionId") String sId, String tt) {
    return "{\"data\":\"hello\"}";
}
```
对于上面这个方法，如果一个用户没有visit和write权限，就会抛出异常。（具体权限是在重写的authorize方法中赋予的）

## 关于前端
框架中提供了两个方法:
```
boite.generateSessionId(); //返回一个字符串
boite.encrypt (str_publicKey, password); //返回加密后的密码
```

## API文档
SubjectHandle中的方法：
```
/**
 * 判断本次连接是否通过验证，一般在login方法之后调用
 * @return
 */
public boolean isAuthenticated();

/**
 * 打开单点登录功能
 */
public void openSingleDeviceOn();

/**
 * 设置登录尝试次数，超过这个值就是锁定账号
 * @param TRY_COUNT
 */
public void setTryCount(int TRY_COUNT);
/**
 * 设置锁定时间
 * @param LOCK_TIME 单位：分钟， 要求 0 < LOCK_TIME <= 30
 */
public void setLockTime(int LOCK_TIME);
/**
 * 设置记住密码
 * 记住密码与不记住密码的区别仅为session对象在缓存中存放的时间长短，前台主要将本次的sessionId缓存起来，下次直接发送该sessionId，而不是重新生成
 * @param keepPassword
 */
public void setKeepPassword(boolean keepPassword);


/**
 * 解密密码，采用RSA非对称加密
 * @param cryptoPassword 加密后的密码
 * @return
 * @throws ConnectTimeOutException 每次连接都有实效，如果过了实效，解密就会抛出异常
 */
public String decodePassword(String cryptoPassword) throws ConnectTimeOutException;

/**
 * 增加登录失败次数，在每次登录验证失败后需要调用该方法
 * @param username
 */
public void addFailCount(String username);

/**
 * 登录操作，在登录成功之后会维护一个Session，与本次连接绑定
 * @param username
 * @param clearPassword 明文
 * @return String token
 * @throws LockedAccountException 账号锁定
 * @throws UnknownAccountException 账号不存在
 * @throws IncorrectCredentialsException 密码错误
 */
public String login(String username, String clearPassword)
        throws LockedAccountException, UnknownAccountException, IncorrectCredentialsException;

/**
 * 判断是否登录
 * @param token
 * @return boolean
 */
public boolean isLogin(String token);

/**
 * 注销
 */
public void logout();

/**
 * 获取当前用户的权限
 * @return Set<String> 权限集合
 */
public Set<String> getPermissions();
```

###### 源代码中提供了一个简单的例子。




