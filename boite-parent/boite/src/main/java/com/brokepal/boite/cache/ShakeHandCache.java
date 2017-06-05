package com.brokepal.boite.cache;

import com.brokepal.boite.exception.ConnectTimeOutException;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/23.
 */
public class ShakeHandCache implements Serializable {
    private static CacheManager manager = CacheManager.create();
    private static net.sf.ehcache.Cache sessionCache = manager.getCache("boiteShakeHandCache");

    private ShakeHandCache() {}

    public static void put(String sessionId, String privateKey){
        Element element = new Element(sessionId, privateKey);
        sessionCache.put(element);
    }

    public static String get(String sessionId) throws ConnectTimeOutException {
        Element element;
        String privateKey = null;
        element = sessionCache.get(sessionId);
        if (element != null){
            privateKey = (String) element.getValue();
        }
        if (privateKey == null) // 私钥是在ConnectController中的shakeHand加到缓存的，如果为null，说明已经过期
            throw new ConnectTimeOutException();
        return privateKey;
    }

//    public static boolean has(String sessionId){
//        Element element = sessionCache.get(sessionId);
//        boolean result = false;
//        element = sessionCache.get(sessionId);
//        if (element != null){
//            result = true;
//        }
//        return result;
//    }

    public static void remove(String sessionId){
        sessionCache.remove(sessionId);
    }

    public static void resetExpire(String sessionId) throws ConnectTimeOutException {
        put(sessionId, get(sessionId));
    }

}
