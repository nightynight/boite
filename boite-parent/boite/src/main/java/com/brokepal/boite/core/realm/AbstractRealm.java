package com.brokepal.boite.core.realm;

import com.brokepal.boite.exception.AuthenticationException;

import java.util.Set;

/**
 * Created by Administrator on 2017/5/22.
 */
public abstract class AbstractRealm {

    public abstract void authenticate(String username, String clearPassword) throws AuthenticationException;

    public abstract Set<String> authorize(String username);
}
