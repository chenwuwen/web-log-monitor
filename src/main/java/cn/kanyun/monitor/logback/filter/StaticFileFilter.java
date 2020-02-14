package cn.kanyun.monitor.logback.filter;

import cn.kanyun.monitor.logback.util.Utils;
import lombok.extern.slf4j.Slf4j;
import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * 静态资源文件过滤器
 * Filter的过滤顺序：
 * web.xml中是按照注册Filter的顺序来决定过滤器执行顺序
 * 注解形式的过滤器,则是根据当前类的名字的自然排序,因此这个值,应该存在规律
 */
@WebFilter(filterName = "StaticFileFilter", urlPatterns = "/web/log/*", asyncSupported = true)
public class StaticFileFilter implements Filter {

    /**
     * 静态资源文件集合
     */
    private static final List<String> STATIC_FILE = new ArrayList<>();


    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("staticFileFilter init() 方法执行");
        STATIC_FILE.add(".css");
        STATIC_FILE.add(".js");
        STATIC_FILE.add(".html");
        STATIC_FILE.add(".woff2");
        STATIC_FILE.add(".woff");
        STATIC_FILE.add(".ttf");
        STATIC_FILE.add(".eot");
        STATIC_FILE.add(".png");
        STATIC_FILE.add(".jpg");
        STATIC_FILE.add(".svg");
        STATIC_FILE.add(".gif");
        STATIC_FILE.add(".map");
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) servletRequest;
        HttpServletResponse response = (HttpServletResponse) servletResponse;

//        项目名
        String contextPath = request.getContextPath();
//        项目名到当前jsp文件的路径
        String servletPath = request.getServletPath();
//        项目名到整个文件的请求路径
        String requestURI = request.getRequestURI();
//        这一步是为了去掉URL中的特殊字符,如: %22 等等
        requestURI = URLDecoder.decode(requestURI, "UTF-8");

        int index = requestURI.lastIndexOf(".");

//        如果没有找到 . 直接进入后续的处理
        if (index == -1) {
            filterChain.doFilter(request, response);
            return;
        }

//        得到资源扩展名,是包含 . 的
        String extensionName = requestURI.substring(index);


//        如果是资源文件，则不拦截，直接返回给客户端，否则传递到下一个组件
        if (!STATIC_FILE.contains(extensionName)) {
//            转发到下一个组件，进行后续的处理（组件可以是一个过滤器，也可以是一个servlet）
            filterChain.doFilter(request, response);
            return;
        }


        if (contextPath == null) { // root context
            contextPath = "";
        }
        String uri = contextPath + servletPath;

//        得到资源文件的相对路径
        String relativeFilePath = requestURI.substring(contextPath.length() + servletPath.length());

        returnResourceFile(relativeFilePath, uri, response);

    }

    @Override
    public void destroy() {

    }

    /**
     * 返回静态资源文件
     *
     * @param relativeFilePath 相对资源文件路径
     * @param uri              请求地址
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void returnResourceFile(String relativeFilePath, String uri, HttpServletResponse response)
            throws ServletException,
            IOException {

        response.setCharacterEncoding("utf-8");

//        得到文件路径
        String filePath = Utils.getFilePath(relativeFilePath);

//        如果是图片/字体资源,返回流
        if (relativeFilePath.endsWith(".jpg")
                || relativeFilePath.endsWith(".png")
                || relativeFilePath.endsWith(".svg")
                || relativeFilePath.endsWith(".gif")
                || relativeFilePath.endsWith(".map")
                || relativeFilePath.endsWith(".ttf")
                || relativeFilePath.endsWith(".eot")
                || relativeFilePath.endsWith(".woff2")
                || relativeFilePath.endsWith(".woff")) {
            byte[] bytes = Utils.readByteArrayFromResource(filePath);
            if (bytes != null) {
                response.getOutputStream().write(bytes);
            }
            return;
        }


//        非图片资源,则将资源文件[如html/css/js]等读取成字符串
        String text = Utils.readFromResource(filePath);

        if (text == null) {
            response.sendRedirect(uri + "/login.html");
            return;
        }

//        如果请求路径结尾是html配置response的ContentType
        if (filePath.endsWith(".html")) {
            response.setContentType("text/html; charset=utf-8");
        }


        if (relativeFilePath.endsWith(".css")) {
            response.setContentType("text/css;charset=utf-8");
        } else if (relativeFilePath.endsWith(".js")) {
            response.setContentType("text/javascript;charset=utf-8");
        }
        response.getWriter().write(text);
    }


}
