package cn.kanyun.monitor.logback.web;

import cn.kanyun.monitor.logback.util.IPAddress;
import cn.kanyun.monitor.logback.util.IPRange;
import cn.kanyun.monitor.logback.util.Utils;
import com.google.common.base.Strings;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Slf4j
public abstract class ResourceServlet extends HttpServlet {


    public static final String SESSION_USER_KEY = "web-log-user";

    /**
     * 登录时 用户名参数 key
     * 同时也是初始化参数的key
     */
    public static final String PARAM_NAME_USERNAME = "username";

    /**
     * 登录时 密码参数 key
     * 同时也是初始化参数的key
     */
    public static final String PARAM_NAME_PASSWORD = "password";

    public static final String PARAM_NAME_ALLOW = "allow";
    public static final String PARAM_NAME_DENY = "deny";
    public static final String PARAM_REMOTE_ADDR = "remoteAddress";

    protected String username = null;
    protected String password = null;
    protected String remoteAddressHeader = null;

    /**
     * 允许访问的IP段 白名单
     */
    protected List<IPRange> allowList = new ArrayList<IPRange>();

    /**
     * 拒绝访问的IP段  黑名单
     */
    protected List<IPRange> denyList = new ArrayList<IPRange>();

    /**
     * 静态资源路径
     */
    protected final String resourcePath;


    public ResourceServlet(String resourcePath) {
        this.resourcePath = resourcePath;
    }

    @Override
    public void init() throws ServletException {
        initAuthEnv();
    }

    /**
     * 初始化认证环境
     */
    private void initAuthEnv() {

//        前三段代码用来初始化登录用户名/密码,如果Servlet配置了PARAM_NAME_USERNAME常量命名的key,那么用户名/密码已配置的为准
//        否则登录的用户名/密码就是PARAM_NAME_USERNAME指定的值
        String paramUserName = getInitParameter(PARAM_NAME_USERNAME);
        if (!Strings.isNullOrEmpty(paramUserName)) {
            this.username = paramUserName;
        }

        String paramPassword = getInitParameter(PARAM_NAME_PASSWORD);
        if (!Strings.isNullOrEmpty(paramPassword)) {
            this.password = paramPassword;
        }

        String paramRemoteAddressHeader = getInitParameter(PARAM_REMOTE_ADDR);
        if (!Strings.isNullOrEmpty(paramRemoteAddressHeader)) {
            this.remoteAddressHeader = paramRemoteAddressHeader;
        }

//        初始化白名单
        String allowParam = getInitParameter(PARAM_NAME_ALLOW);
        try {
            allowList = initAuthList(allowParam);
        } catch (Exception e) {
            String msg = "initParameter config error, allow : " + allowParam;
            System.err.println(msg);
        }

//        初始化黑名单
        String denyParam = getInitParameter(PARAM_NAME_DENY);
        try {
            denyList = initAuthList(denyParam);
        } catch (Exception e) {
            String msg = "initParameter config error, deny : " + denyParam;
            System.err.println(msg);
        }
    }


    /**
     * 初始化白/黑名单
     * @param param
     * @return
     */
    public List<IPRange> initAuthList(String param) {
        List<IPRange> list = new ArrayList<>();
        if (!Strings.isNullOrEmpty(param)) {
            param = param.trim();
            String[] items = param.split(",");

            for (String item : items) {
                if (item == null || item.length() == 0) {
                    continue;
                }

                IPRange ipRange = new IPRange(item);
                list.add(ipRange);
            }
        }
        return list;
    }

    /**
     * 是否是有效请求 ip黑名单
     *
     * @param remoteAddress 远程访问地址
     * @return
     */
    public boolean isPermittedRequest(String remoteAddress) {

//        是否是ipV6
        boolean ipV6 = remoteAddress != null && remoteAddress.indexOf(':') != -1;

        if (ipV6) {
            return "0:0:0:0:0:0:0:1".equals(remoteAddress) || (denyList.size() == 0 && allowList.size() == 0);
        }

        IPAddress ipAddress = new IPAddress(remoteAddress);

//        如果在黑名单中找到匹配Ip段,返回false
        for (IPRange range : denyList) {
            if (range.isIPAddressInRange(ipAddress)) {
                return false;
            }
        }

        if (allowList.size() > 0) {
            for (IPRange range : allowList) {
                if (range.isIPAddressInRange(ipAddress)) {
                    return true;
                }
            }

            return false;
        }

        return true;
    }

    protected String getFilePath(String fileName) {
        return resourcePath + fileName;
    }

    /**
     * 返回资源文件
     *
     * @param fileName
     * @param uri
     * @param response
     * @throws ServletException
     * @throws IOException
     */
    protected void returnResourceFile(String fileName, String uri, HttpServletResponse response)
            throws ServletException,
            IOException {

//        得到文件路径
        String filePath = getFilePath(fileName);

//        如果是图片资源,返回流
        if (fileName.endsWith(".jpg")) {
            byte[] bytes = Utils.readByteArrayFromResource(filePath);
            if (bytes != null) {
                response.getOutputStream().write(bytes);
            }
            return;
        }


//        非图片资源,则将资源文件读取成字符串
        String text = Utils.readFromResource(filePath);

        if (text == null) {
            response.sendRedirect(uri + "/login.html");
            return;
        }

//        如果请求路径结尾是html配置response的ContentType
        if (filePath.endsWith(".html")) {
            response.setContentType("text/html; charset=utf-8");
        }



        if (fileName.endsWith(".css")) {
            response.setContentType("text/css;charset=utf-8");
        } else if (fileName.endsWith(".js")) {
            response.setContentType("text/javascript;charset=utf-8");
        }
        response.getWriter().write(text);
    }

    @Override
    public void service(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
//        项目名
        String contextPath = request.getContextPath();
//        项目名到当前jsp文件的路径
        String servletPath = request.getServletPath();
//        项目名到整个文件的请求路径
        String requestURI = request.getRequestURI();

        response.setCharacterEncoding("utf-8");

        if (contextPath == null) { // root context
            contextPath = "";
        }
        String uri = contextPath + servletPath;

//        得到资源在服务端的位置
        String path = requestURI.substring(contextPath.length() + servletPath.length());

        if (!isPermittedRequest(request)) {
            path = "/nopermit.html";
            returnResourceFile(path, uri, response);
            return;
        }

//        认证请求
        if ("/auth".equals(path)) {
            String usernameParam = request.getParameter(PARAM_NAME_USERNAME);
            String passwordParam = request.getParameter(PARAM_NAME_PASSWORD);
            if (username.equals(usernameParam) && password.equals(passwordParam)) {
                request.getSession().setAttribute(SESSION_USER_KEY, username);
                response.getWriter().print("success");
            } else {
                response.getWriter().print("error");
            }
            return;
        }

        if (isRequireAuth() //
                && !ContainsUser(request)//
                && !checkLoginParam(request)//
                && !("/login.html".equals(path) //
                || path.startsWith("/css")//
                || path.startsWith("/js") //
                || path.startsWith("/img"))) {
            if (contextPath.equals("") || contextPath.equals("/")) {
                response.sendRedirect("/web/log/login.html");
            } else {
                if ("".equals(path)) {
                    response.sendRedirect("web/log/login.html");
                } else {
                    response.sendRedirect("login.html");
                }
            }
            return;
        }

        if ("".equals(path)) {
            if (contextPath.equals("") || contextPath.equals("/")) {
                response.sendRedirect("/druid/login.html");
            } else {
                response.sendRedirect("druid/login.html");
            }
            return;
        }

        if ("/".equals(path)) {
            response.sendRedirect("login.html");
            return;
        }

//        如果是.do请求,说明需要做一定的业务处理
        if (path.contains(".do")) {
            String fullUrl = path;
            if (request.getQueryString() != null && request.getQueryString().length() > 0) {
                fullUrl += "?" + request.getQueryString();
            }
            response.getWriter().print(process(fullUrl));
            return;
        }

        // find file in resources path
        returnResourceFile(path, uri, response);
    }

    /**
     * Session中是否包含用户
     *
     * @param request
     * @return
     */
    public boolean ContainsUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        return session != null && session.getAttribute(SESSION_USER_KEY) != null;
    }

    /**
     * 检测登录参数
     *
     * @param request
     * @return
     */
    public boolean checkLoginParam(HttpServletRequest request) {
        String usernameParam = request.getParameter(PARAM_NAME_USERNAME);
        String passwordParam = request.getParameter(PARAM_NAME_PASSWORD);
        if (null == username || null == password) {
            return false;
        } else if (username.equals(usernameParam) && password.equals(passwordParam)) {
            return true;
        }
        return false;
    }

    public boolean isRequireAuth() {
        return this.username != null;
    }

    /**
     * 请求是否合法,主要是看请求的IP地址否是在黑名单内
     *
     * @param request
     * @return
     */
    public boolean isPermittedRequest(HttpServletRequest request) {
        String remoteAddress = getRemoteAddress(request);
        return isPermittedRequest(remoteAddress);
    }

    /**
     * 获得远程访问IP
     *
     * @param request
     * @return
     */
    protected String getRemoteAddress(HttpServletRequest request) {
        String remoteAddress = null;

        if (remoteAddressHeader != null) {
            remoteAddress = request.getHeader(remoteAddressHeader);
        }

        if (remoteAddress == null) {
            remoteAddress = request.getRemoteAddr();
        }

        return remoteAddress;
    }

    protected abstract String process(String url);
}

