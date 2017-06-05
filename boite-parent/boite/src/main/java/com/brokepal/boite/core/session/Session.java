package com.brokepal.boite.core.session;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by Administrator on 2017/5/22.
 */
public class Session implements Serializable {
    private Map<String,Object> map = new HashMap<String, Object>();

    public void put(String key, Object value){
        map.put(key, value);
    }

    public <V> V get(String key){
        return (V)map.get(key);
    }

    public void remove(String key){
        map.remove(key);
    }

}
