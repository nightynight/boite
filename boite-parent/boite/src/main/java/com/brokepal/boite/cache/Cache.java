package com.brokepal.boite.cache;

import net.sf.ehcache.CacheManager;
import net.sf.ehcache.Element;

import java.io.Serializable;

/**
 * Created by Administrator on 2017/5/23.
 */
public class Cache {
    private CacheManager manager = CacheManager.create();
    private net.sf.ehcache.Cache cache;

    public Cache(String cacheName) {
        cache = manager.getCache(cacheName);
    }

    public <K extends Serializable,V extends Serializable> void put(K k, V v){
        Element element = new Element(k,v);
        cache.put(element);
    }

    public <K extends Serializable, V> V get(K k){
        Element element;
        V result = null;
        element = cache.get(k);
        if (element != null){
            result = (V) element.getValue();
        }
        return result;
    }

    public <K extends Serializable> boolean has(K k){
        Element element = cache.get(k);
        boolean result = false;
        element = cache.get(k);
        if (element != null){
            result = true;
        }
        return result;
    }

    public <K extends Serializable> void remove(K k){
        cache.remove(k);
    }

    public <K extends Serializable> void resetExpire(K k){
        put(k, (Serializable) get(k));
    }
}
