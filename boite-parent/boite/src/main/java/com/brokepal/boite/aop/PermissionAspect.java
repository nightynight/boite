package com.brokepal.boite.aop;

import com.brokepal.boite.exception.NoPermissionException;
import com.brokepal.boite.core.session.Session;
import com.brokepal.boite.annotation.Logical;
import com.brokepal.boite.annotation.RequiresPermissions;
import com.brokepal.boite.web.constant.BoiteConst;
import com.brokepal.boite.core.session.SessionUtil;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.RequestParam;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by Administrator on 2017/5/23.
 */

@Aspect
@Component
public class PermissionAspect {
    /**
     * 为所有有@RequiresPermissions注解的方法设置切面
     */
    @Pointcut("execution(public * *..*(..)) && " +
            "@annotation(com.brokepal.boite.annotation.RequiresPermissions)")
    public void declareJointPointExpression(){}

    @Around("declareJointPointExpression()")
    public Object around(ProceedingJoinPoint joinPoint) throws Throwable{
        Object result = null;
        //获取需要的权限
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Method method = signature.getMethod();
        RequiresPermissions annotation = method.getAnnotation(RequiresPermissions.class);
        String[] needPermissions = annotation.values();
        Logical logical = annotation.logical();

        //获取参数列表
        Object[] args = joinPoint.getArgs();
        //获取sessionId的值
        Annotation[][] annotations = method.getParameterAnnotations();
        int indexOfSessionId = 0;
        for (int i = 0; i < annotations.length; i++){
            if (annotations[i].length == 1
                    && annotations[i][0].annotationType() == RequestParam.class
                    && BoiteConst.KEY_SESSION_ID.equals(((RequestParam)annotations[i][0]).value())){
                indexOfSessionId = i;
                break;
            }
        }
        String sessionId = (String) args[indexOfSessionId];

        //根据sessionId到缓存中查对应用户的权限
        Set<String> userPermissions = new HashSet<String>();
        Session session = SessionUtil.getSession(sessionId);
        do {
            if (session == null)
                break;
            userPermissions = session.get(BoiteConst.KEY_PERMISSION);
            if (userPermissions == null) userPermissions = new HashSet<String>();
        } while (false);

        //判断是否有权限
        boolean hasPermission = hasAllPermission(userPermissions, needPermissions, logical);
        if(hasPermission){
            //执行方法
            result = joinPoint.proceed();
        }
        else {
            throw new NoPermissionException();
        }
        return result;
    }

    private boolean hasAllPermission(Set<String> userPermissions, String[] needPermissions, Logical logical){
        if (logical == Logical.AND){
            for (String needPermission : needPermissions) {
                boolean flag = false;
                for (String userPermission : userPermissions) {
                    if (userPermission.equals(needPermission)){
                        flag = true;
                        break;
                    }
                }
                if (!flag)
                    return false;
            }
            return true;
        }
        else {
            for (String needPermission : needPermissions) {
                for (String userPermission : userPermissions) {
                    if (userPermission.equals(needPermission)){
                        return true; //有一个权限，就返回true
                    }
                }
            }
            return false;
        }
    }
}
