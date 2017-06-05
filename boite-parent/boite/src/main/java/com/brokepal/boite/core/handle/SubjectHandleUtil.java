package com.brokepal.boite.core.handle;

import com.brokepal.boite.SpringBeanContainer;
import com.brokepal.boite.exception.NoSubjectHandleException;
import com.brokepal.boite.core.realm.AbstractRealm;

import static com.brokepal.boite.cache.CacheContainer.SubjectHandleCache;

/**
 * Created by Administrator on 2017/5/24.
 */
public class SubjectHandleUtil {
    private static AbstractRealm realm = SpringBeanContainer.getBean("realm");
    /**
     * 需要在登录之前调用该方法，创建一个句柄，与sessionId绑定，之后只需要通过getSubjectHandle(String sessionId)就可以拿到句柄
     * @param sessionId
     * @return
     */
    public static SubjectHandle createHandle(String sessionId){
        SubjectHandle handle = new SubjectHandle(realm, sessionId);
        SubjectHandleCache.put(sessionId, handle);
        return handle;
    }

    /**
     * 获取句柄，在这之前必须先创建句柄（调用createHandle()方法），否则会抛出异常
     * @param sessionId
     * @return
     * @throws NoSubjectHandleException
     */
    public static SubjectHandle getSubjectHandle(String sessionId) throws NoSubjectHandleException {
        SubjectHandle handle = SubjectHandleCache.get(sessionId);
        if (handle == null)
            throw new NoSubjectHandleException();
        return handle;
    }
}
