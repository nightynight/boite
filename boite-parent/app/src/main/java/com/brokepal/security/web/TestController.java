package com.brokepal.security.web;

import com.brokepal.boite.annotation.Logical;
import com.brokepal.boite.annotation.RequiresPermissions;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Created by Administrator on 2017/5/22.
 */
@Controller
public class TestController {

    @RequestMapping(value = "/hello")
    @ResponseBody
    public String hello() {
        return "{\"data\":\"hello\"}";
    }

    @RequiresPermissions(values = {"visit","write"}, logical = Logical.OR)
    @RequestMapping(value = "static/hello")
    @ResponseBody
    public String hello2(@RequestParam("name") String name, @RequestParam("sessionId") String sId, String tt) {
        return "{\"data\":\"hello\"}";
    }
}
