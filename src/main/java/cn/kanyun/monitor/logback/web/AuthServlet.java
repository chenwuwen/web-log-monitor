package cn.kanyun.monitor.logback.web;

import cn.kanyun.monitor.logback.common.Constant;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 认证端点
 */
@WebServlet(name = "auth", urlPatterns = {"/web/log/auth", "/web/log/login", "/web/log/*"})
public class AuthServlet extends HttpServlet {


    @Override
    public void init() throws ServletException {
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Get请求WebLog,将返回登录页");
        String contextPath = req.getContextPath();
        if (contextPath.equals("") || contextPath.equals("/")) {
            resp.sendRedirect("/web/log/login.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter(Constant.PARAM_NAME_USERNAME);
        String password = req.getParameter(Constant.PARAM_NAME_PASSWORD);
        System.out.printf("WebLog进行登录,用户名：%s  密码：%s \n", username, password);
        //验证输入的用户名和密码
        if ("admin".equals(username) && "admin".equals(password)) {
            //请求重定向
            req.getSession().setAttribute(Constant.SESSION_USER_KEY, username);
            resp.getWriter().write("success");
        } else {
            resp.getWriter().write("error");
        }
        resp.getWriter().flush();
    }


}
