package com.brokepal.boite.cache;

import com.brokepal.boite.exception.SessionNotExistException;
import com.brokepal.boite.core.session.Session;
import net.sf.ehcache.Element;

/**
 * Created by Administrator on 2017/5/27.
 */
public class SessionCache {
    public static Cache ShortSessionCache = new Cache("boiteSessionCache"); //key为sessionId，value为Session对象
    public static Cache LongSessionCache = new Cache("boiteLongSessionCache"); //key为sessionId，value为Session对象

    private SessionCache() {}

    public static void put(String sessionId, Session session, boolean keepPassword){
        Element element = new Element(sessionId, session);
        if (keepPassword){
            LongSessionCache.put(sessionId,session);
        }
        else {
            ShortSessionCache.put(sessionId,session);
        }
    }

    public static Session get(String sessionId) {
        Session session = ShortSessionCache.get(sessionId);
        if (session == null) session = LongSessionCache.get(sessionId);
        return session;
    }

    public static boolean has(String sessionId){
        boolean result = false;
        Session session = ShortSessionCache.get(sessionId);
        if (session == null) session = LongSessionCache.get(sessionId);
        if (session != null){
            result = true;
        }
        return result;
    }

    public static void remove(String sessionId){
        ShortSessionCache.remove(sessionId);
        LongSessionCache.remove(sessionId);
    }

    public static void resetExpire(String sessionId) throws SessionNotExistException {
        Session session = ShortSessionCache.get(sessionId);
        if (session != null)
            ShortSessionCache.put(sessionId, session);
        else {
            session = LongSessionCache.get(sessionId);
            if (session != null)
                LongSessionCache.put(sessionId, session);
            else
                throw new SessionNotExistException();
        }
    }
}
