package com.brokepal.boite.web.util;

import com.brokepal.boite.core.crypto.Base64;
import com.brokepal.boite.core.crypto.MD5;
import com.brokepal.boite.core.crypto.RSA;

import java.security.interfaces.RSAPrivateKey;
import java.security.interfaces.RSAPublicKey;

/**
 * Created by Administrator on 2017/5/23.
 */
public final class SecurityUtil {
    private static String salt = " 1qfdvd";
    public static String generateToken(String username, String password){
        String temp = Base64.encode(username + "\n" + password);
        return Base64.encode(temp + salt);
    }

    public static String getUsernameFromToken(String token){
        String temp = Base64.decode(token);
        temp = temp.substring(0, temp.length() - salt.length());
        return Base64.decode(temp).split("\n")[0];
    }

    public static String getPasswordFromToken(String token){
        String temp = Base64.decode(token);
        temp = temp.substring(0, temp.length() - salt.length());
        return Base64.decode(temp).split("\n")[1];
    }

    public static String MD5EncodePassword(String password, String salt){
        return MD5.string2MD5(MD5.string2MD5(password) + salt);
    }

    public static String RSAEncode(String str_publicKey, String clearText){
        RSAPublicKey rsaPublicKey = null;
        String result = null;
        try {
            rsaPublicKey = (RSAPublicKey) RSA.getPublicKey(str_publicKey);
            result = RSA.RSAEncodeWithPublicKey(rsaPublicKey,clearText);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String RSADecode(String str_privateKey, String cryptograph) {
        RSAPrivateKey rsaPrivateKey = null;
        String result = null;
        try {
            rsaPrivateKey = (RSAPrivateKey) RSA.getPrivateKey(str_privateKey);
            result = RSA.RSADecodeWithPrivateKey(rsaPrivateKey,cryptograph);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return result;
    }

    public static String BASE64Encode(String clearText){
        return Base64.encode(clearText);
    }

    public static String BASE64Decode(String cryptograph){
        return Base64.decode(cryptograph);
    }
}