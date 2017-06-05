package com.brokepal.security.boite;

import com.brokepal.boite.exception.AuthenticationException;
import com.brokepal.boite.exception.IncorrectCredentialsException;
import com.brokepal.boite.exception.UnknownAccountException;
import com.brokepal.boite.core.realm.AbstractRealm;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/22.
 */
public class TestRealm extends AbstractRealm {
    @Override
    public void authenticate(String username, String password) throws AuthenticationException {
        if(!"xiaoming".equals(username)){
            throw new UnknownAccountException();
        }
        if (!"123456".equals(password)){
            throw new IncorrectCredentialsException();
        }
    }

    @Override
    public Set<String> authorize(String username) {
        Set<String> permissions = new HashSet<String>();
        if ("xiaoming".equals(username)){
            permissions.add("visit");
        }
        return permissions;
    }
}
