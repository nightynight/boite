package com.brokepal.boite.web.conteoller;

import com.brokepal.boite.cache.ShakeHandCache;
import com.brokepal.boite.core.crypto.RSA;
import com.brokepal.boite.web.constant.BoiteConst;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.security.NoSuchAlgorithmException;

/**
 * Created by Administrator on 2017/5/23.
 */

@Controller
public class ConnectController {
    @RequestMapping(value=BoiteConst.CONNECT_URI)
    @ResponseBody
    public String shakeHand(@RequestParam(BoiteConst.KEY_SESSION_ID) String sessionId) throws NoSuchAlgorithmException {
        RSA.KeyPairOfString keyPairOfString = RSA.makeBothKeyOfString();
        String str_publicKey = keyPairOfString.getPublicKey();
        String str_privateKey = keyPairOfString.getPrivateKey();
        //将私钥加入缓存，处理登录请求时取出;登录前sessionId中存的是私钥
        ShakeHandCache.put(sessionId, str_privateKey);
        return str_publicKey;
    }
}
