package cn.kanyun.monitor.logback.web;

import cn.kanyun.monitor.logback.common.Constant;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * 登录成功后验证
 */
@WebServlet(name = "VerificationServlet", urlPatterns = {"/web/log/verification"}, asyncSupported = true)
public class VerificationServlet extends HttpServlet {

    @Override
    public void init() throws ServletException {
        System.out.println("VerificationServlet init()方法执行");
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        if (req.getSession().getAttribute(Constant.SESSION_USER_KEY) != null) {
            resp.sendRedirect("monitor.html");
            return;
        }

        resp.sendRedirect("login.html");

    }
}
