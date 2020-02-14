package cn.kanyun.monitor.logback.web;

import cn.kanyun.monitor.logback.common.Constant;
import com.google.common.io.Resources;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

/**
 * 认证端点
 */
@WebServlet(name = "auth", urlPatterns = {"/web/log/auth", "/web/log/login", "/web/log/*"}, asyncSupported = true)
public class AuthServlet extends HttpServlet {

    private static String USER_NAME = "admin";
    private static String PASSWORD = "admin";

    @Override
    public void init() throws ServletException {
        try {
            URL url = Resources.getResource(Constant.CONFIG_FILE_PATH);
            Properties properties = new Properties();
            properties.load(url.openStream());
            USER_NAME = properties.get(Constant.CONFIG_USERNAME_KEY).toString();
            PASSWORD = properties.get(Constant.CONFIG_PASSWORD_KEY).toString();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        System.out.println("Get请求WebLog,将返回登录页");
        String contextPath = req.getContextPath();
        String servletPath = req.getServletPath();
//        判断顺序不能乱
        if (contextPath.equals("") || contextPath.equals("/")) {
            resp.sendRedirect("/web/log/login.html");
        } else if (!servletPath.endsWith("/")) {
            resp.sendRedirect(contextPath + "/web/log/login.html");
        } else {
            resp.sendRedirect("login.html");
        }
    }

    @Override
    protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        String username = req.getParameter(Constant.PARAM_NAME_USERNAME);
        String password = req.getParameter(Constant.PARAM_NAME_PASSWORD);
        System.out.printf("WebLog进行登录,用户名：%s  密码：%s \n", username, password);
        //验证输入的用户名和密码
        if (USER_NAME.equals(username) && PASSWORD.equals(password)) {
            //请求重定向
            req.getSession().setAttribute(Constant.SESSION_USER_KEY, username);
            resp.getWriter().write("success");
        } else {
            resp.getWriter().write("error");
        }
        resp.getWriter().flush();
    }


}
