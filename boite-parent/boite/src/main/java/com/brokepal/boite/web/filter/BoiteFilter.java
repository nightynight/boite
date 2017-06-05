package com.brokepal.boite.web.filter;

import com.brokepal.boite.cache.SessionCache;
import com.brokepal.boite.exception.NoSubjectHandleException;
import com.brokepal.boite.exception.SessionNotExistException;
import com.brokepal.boite.core.handle.SubjectHandle;
import com.brokepal.boite.web.constant.BoiteConst;
import com.brokepal.boite.core.handle.SubjectHandleUtil;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Created by Administrator on 2017/5/23.
 */
public class BoiteFilter implements Filter {

    private FilterConfig config;

    public void destroy() {

    }

    public void doFilter(ServletRequest arg0, ServletResponse arg1, FilterChain arg2)
            throws IOException, ServletException {
        HttpServletRequest req=(HttpServletRequest) arg0;
        HttpServletResponse resp=(HttpServletResponse) arg1;
        resp.setHeader("Access-Control-Allow-Origin","*");

        //设置编码
        String charset=config.getInitParameter("charset");
        if (charset==null) {
            charset="UTF-8";//设置默认值
        }
        req.setCharacterEncoding(charset);

        //建立连接不需要拦截
        if (BoiteConst.CONNECT_URI.equals(req.getRequestURI())){
            arg2.doFilter(arg0, arg1);
            return;
        }

        //排除在配置文件中配置的不需要过滤的请求
        String noLoginPaths= config.getInitParameter("nonFilterPaths");
        noLoginPaths.replace("\n","");
        if (noLoginPaths!=null) {
            String[] strArray=noLoginPaths.split(",");
            for (int i = 0; i < strArray.length; i++) {
                if (strArray[i]==null || "".equals(strArray[i].trim())) continue;
                if (req.getRequestURI().indexOf(strArray[i].trim())!=-1) {
                    arg2.doFilter(arg0, arg1);
                    return;
                }
            }
        }
        boolean isLogin = false;
        String sessionId = req.getParameter(BoiteConst.KEY_SESSION_ID);
        String token = req.getParameter(BoiteConst.KEY_TOKEN);
        do {
            if (sessionId == null || token == null)
                break;
            try {
                SubjectHandle handle = SubjectHandleUtil.getSubjectHandle(sessionId);
                if (handle.isLogin(token)){
                    isLogin = true;
                    SessionCache.resetExpire(sessionId);
                }
            } catch (NoSubjectHandleException e) {
                e.printStackTrace();
            } catch (SessionNotExistException e) {
                e.printStackTrace();
            }
        } while (false);
        if (isLogin){
            arg2.doFilter(arg0, arg1);
        }
    }

    public void init(FilterConfig arg0) throws ServletException {
        // 获取在配置文件中设置的init-param
        this.config=arg0;
    }
}
