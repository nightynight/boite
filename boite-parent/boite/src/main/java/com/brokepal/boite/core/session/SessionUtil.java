package com.brokepal.boite.core.session;


import com.brokepal.boite.cache.SessionCache;

/**
 * Created by Administrator on 2017/5/23.
 */
public class SessionUtil {

    public static Session getSession(String sessionId){
        Session session = SessionCache.get(sessionId);
        return session;
    }
}
