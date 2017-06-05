package com.brokepal.boite.cache;

/**
 * Created by Administrator on 2017/5/24.
 */
public class CacheContainer {
    public static Cache SubjectHandleCache = new Cache("boiteSubjectHandleCache"); //key为sessionId，value为SubjectHandle对象
    public static Cache LockAccountCache = new Cache("boiteLockAccountCache"); //key为username，value为LoginFailInfo对象
    public static Cache LastSessionIdCache = new Cache("boiteLastSessionIdCache"); //key为username，value为sessionId
}
