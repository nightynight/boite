package com.brokepal.security.web;

import com.brokepal.boite.exception.*;
import com.brokepal.boite.core.handle.SubjectHandle;
import com.brokepal.boite.core.handle.SubjectHandleUtil;
import com.brokepal.security.dto.OperationResult;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by chenchao on 17/3/28.
 */
@Controller
@RequestMapping(value = "static")
public class LoginController {

    @RequestMapping(value = "/login", method = RequestMethod.POST)
    @ResponseBody
    public ResponseEntity login(@RequestParam("sessionId") String sessionId,
                                @RequestParam("username") String username,
                                @RequestParam("password") String password,
                                boolean keepPassword) {
        SubjectHandle subjectHandle = SubjectHandleUtil.createHandle(sessionId);

        if (keepPassword)
            subjectHandle.setKeepPassword(true);
        subjectHandle.openSingleDeviceOn();

        OperationResult result;

        try {
            String srcPassword = subjectHandle.decodePassword(password);//解密密码
            String token = subjectHandle.login(username, srcPassword);
            if (subjectHandle.isAuthenticated()) {
                result = OperationResult.buildSuccessResult(token, subjectHandle.getPermissions());
            } else
                result = OperationResult.buildFailureResult("身份认证失败");
        } catch (ConnectTimeOutException e) {
            e.printStackTrace();
            result = OperationResult.buildFailureResult("建立连接超时，请刷新页面，重新登录");
        } catch (UnknownAccountException e) {
            e.printStackTrace();
            result = OperationResult.buildFailureResult("用户名不存在");
        } catch (IncorrectCredentialsException e) {
            e.printStackTrace();
            result = OperationResult.buildFailureResult("密码错误");
        } catch (LockedAccountException e) {
            e.printStackTrace();
            result = OperationResult.buildFailureResult("账号锁定");
        }

        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/isLogin", method = RequestMethod.GET)
    @ResponseBody
    public ResponseEntity isLogin(@RequestParam("sessionId") String sessionId, @RequestParam("token") String token) throws NoSubjectHandleException {
        SubjectHandle subjectHandle = SubjectHandleUtil.getSubjectHandle(sessionId);
        boolean isLogin = subjectHandle.isLogin(token);
        OperationResult result;
        if (isLogin)
            result = OperationResult.buildSuccessResult("true");
        else
            result = OperationResult.buildFailureResult("false");
        return new ResponseEntity(result, HttpStatus.OK);
    }

    @RequestMapping(value = "/logout")
    @ResponseBody
    public ResponseEntity logout(@RequestParam("sessionId") String sessionId) throws NoSubjectHandleException {
        SubjectHandle subjectHandle = SubjectHandleUtil.getSubjectHandle(sessionId);
        subjectHandle.logout();
        return new ResponseEntity(OperationResult.buildSuccessResult(), HttpStatus.OK);
    }
}
