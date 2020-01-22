package cn.kanyun.monitor.logback.filter;

import cn.kanyun.monitor.logback.common.Constant;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * 权限过滤器
 * 拦截 uri : /log/*
 *
 * Filter的过滤顺序：
 * web.xml中是按照注册Filter的顺序来决定过滤器执行顺序
 * 注解形式的过滤器,则是根据当前类的名字的自然排序,因此这个值,应该存在规律
 */
@WebFilter(filterName = "AuthFilter", urlPatterns = "/web/log/push/*")
public class AuthFilter implements Filter {
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("AuthFilter init() 方法执行");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {

        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;
        HttpSession session = request.getSession();

        //因为登录后保存了username，所以可以先检查username判断是否登录
        if (session.getAttribute(Constant.SESSION_USER_KEY) != null) {
            //已登录，则放行
            filterChain.doFilter(request, response);
        } else {
            //未登录，重定向到登录页面
            response.sendRedirect("login.html");
        }
    }

    @Override
    public void destroy() {

    }
}
