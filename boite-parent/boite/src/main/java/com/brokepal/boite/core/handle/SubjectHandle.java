package com.brokepal.boite.core.handle;

import com.brokepal.boite.cache.SessionCache;
import com.brokepal.boite.cache.ShakeHandCache;
import com.brokepal.boite.exception.*;
import com.brokepal.boite.core.realm.AbstractRealm;
import com.brokepal.boite.core.session.Session;
import com.brokepal.boite.web.bo.LoginFailInfo;
import com.brokepal.boite.web.constant.BoiteConst;
import com.brokepal.boite.web.util.SecurityUtil;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import static com.brokepal.boite.cache.CacheContainer.*;

/**
 * Created by Administrator on 2017/5/22.
 */
public class SubjectHandle implements Serializable {
    private String sessionId;
    private AbstractRealm realm;
    private boolean isAuthenticated = false;
    private int TRY_COUNT = BoiteConst.TRY_COUNT;
    private int LOCK_TIME = BoiteConst.LOCK_TIME;
    private boolean isSingleDeviceOn = false;
    private boolean keepPassword = false;

    private SubjectHandle(){}

    SubjectHandle(AbstractRealm realm, String sessionId){
        this.realm = realm;
        this.sessionId = sessionId;
    }

    /**
     * 判断本次连接是否通过验证，一般在login方法之后调用
     * @return
     */
    public boolean isAuthenticated(){
        return isAuthenticated;
    }

    /**
     * 打开单点登录功能
     */
    public void openSingleDeviceOn(){
        this.isSingleDeviceOn = true;
    }

    /**
     * 设置登录尝试次数，超过这个值就是锁定账号
     * @param TRY_COUNT
     */
    public void setTryCount(int TRY_COUNT) {
        this.TRY_COUNT = TRY_COUNT;
    }

    /**
     * 设置锁定时间
     * @param LOCK_TIME 单位：分钟， 要求 0 < LOCK_TIME <= 30
     */
    public void setLockTime(int LOCK_TIME) {
        if (LOCK_TIME > 30 || LOCK_TIME <= 0)
            throw new IllegalArgumentException();
        this.LOCK_TIME = LOCK_TIME;
    }

    /**
     * 设置记住密码
     * 记住密码与不记住密码的区别仅为session对象在缓存中存放的时间长短，前台主要将本次的sessionId缓存起来，下次直接发送该sessionId，而不是重新生成
     * @param keepPassword
     */
    public void setKeepPassword(boolean keepPassword) {
        this.keepPassword = keepPassword;
    }


    /**
     * 解密密码，采用RSA非对称加密
     * @param cryptoPassword 加密后的密码
     * @return
     * @throws ConnectTimeOutException 每次连接都有实效，如果过了实效，解密就会抛出异常
     */
    public String decodePassword(String cryptoPassword) throws ConnectTimeOutException {
        String privateKey = ShakeHandCache.get(sessionId);
        String clearPassword = SecurityUtil.RSADecode(privateKey,cryptoPassword);
        return clearPassword;
    }

    /**
     * 判断该账号是否锁定
     * @param username
     * @return
     */
    private boolean isLocked(String username){
        LoginFailInfo info = LockAccountCache.get(username);
        if (info == null){
            info = new LoginFailInfo();
            LockAccountCache.put(username, info);
        }
        if (info.getFailCount() < TRY_COUNT)
            return false;

        if (new Date().getTime() - info.getLastLockTime().getTime() > LOCK_TIME * 60 * 1000){ //已经过了锁定时间
            info.setFailCount(0);
            return false;
        }
        return true;
    }

    /**
     * 增加登录失败次数，在每次登录验证失败后需要调用该方法
     * @param username
     */
    public void addFailCount(String username){
        LoginFailInfo info = LockAccountCache.get(username);
        if (info == null){
            info = new LoginFailInfo();
            LockAccountCache.put(username, info);
        }
        else {
            LockAccountCache.resetExpire(username);
        }
        int failCount = info.getFailCount() + 1;
        info.setFailCount(failCount);
        if (failCount == TRY_COUNT){
            info.setLastLockTime(new Date());
        }
    }

    private void clearFailInfo(String username){
        LoginFailInfo info = LockAccountCache.get(username);
        info.setFailCount(0);
        info.setLastLockTime(null);
        LockAccountCache.remove(username);
    }

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
            throws LockedAccountException, UnknownAccountException, IncorrectCredentialsException {
        SubjectHandle handle;
        try {
            handle = SubjectHandleUtil.getSubjectHandle(sessionId);
            if (handle.isLocked(username))
                throw new LockedAccountException();
            realm.authenticate(username,clearPassword);//如果认证不通过，则抛出异常
            //登录成功
            isAuthenticated = true;
            //清理工作
            this.clearFailInfo(username);
            ShakeHandCache.remove(sessionId);
        } catch (NoSubjectHandleException e) {
            e.printStackTrace();
        } catch (AuthenticationException e) {
            if (e instanceof UnknownAccountException)
                throw new UnknownAccountException();
            if (e instanceof IncorrectCredentialsException)
                throw new IncorrectCredentialsException();
            if (e instanceof LockedAccountException){
                SessionCache.remove(sessionId); //当用户登录成功后，没有没有页面跳转，继续登录此账号，如果此时，连续几次输错密码，账号锁定后，需要删除session
                throw new LockedAccountException();
            }
        }
        Set<String> permissions = realm.authorize(username); //授权
        if (permissions == null)
            permissions = new HashSet<String>();

        if (isSingleDeviceOn)
            LastSessionIdCache.put(username,sessionId); //用来维护单点登录
        Session session = new Session();
        session.put(BoiteConst.KEY_PERMISSION, permissions);
        SessionCache.put(sessionId, session, this.keepPassword); //登录之后才开始维护session

        return SecurityUtil.generateToken(username, clearPassword);
    }

    /**
     * 判断是否登录
     * @param token
     * @return boolean
     */
    public boolean isLogin(String token){
        if (isSingleDeviceOn){
            String username = SecurityUtil.getUsernameFromToken(token);
            String lastSessionId = LastSessionIdCache.get(username);
            if (sessionId.equals(lastSessionId)){
                if (SessionCache.has(sessionId))
                    return true;
                else
                    return false;
            }
            else {
                if (!SessionCache.has(lastSessionId) && SessionCache.has(sessionId))
                    return true;
                else
                    return false;
            }
        }
        else {
            if (SessionCache.has(sessionId))
                return true;
            else
                return false;
        }
    }

    public void logout(){
        SessionCache.remove(sessionId);
    }

    /**
     * 获取当前用户的权限
     * @return Set<String> 权限集合
     */
    public Set<String> getPermissions(){
        Set<String> permissions = new HashSet<String>();
        Session session = SessionCache.get(sessionId);
        if (session != null)
            permissions = session.get(BoiteConst.KEY_PERMISSION);
        return permissions;
    }
}
