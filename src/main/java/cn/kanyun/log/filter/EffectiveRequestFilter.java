package cn.kanyun.log.filter;

import cn.kanyun.log.common.Constant;
import cn.kanyun.log.util.IPAddress;
import cn.kanyun.log.util.IPRange;
import com.google.common.base.Strings;
import com.google.common.io.Resources;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * 有效的请求过滤器,IP 黑名单/白名单
 * Filter的过滤顺序：
 * web.xml中是按照注册Filter的顺序来决定过滤器执行顺序
 * 注解形式的过滤器,则是根据当前类的名字的自然排序,因此这个值,应该存在规律
 * 第一个执行的过滤器
 *
 * @author KANYUN
 */
@WebFilter(filterName = "EffectiveRequestFilter", urlPatterns = "/web/log/*", asyncSupported = true)
public class EffectiveRequestFilter implements Filter {

    /**
     * 允许访问的IP段 白名单
     */
    private static List<IPRange> allowList = new ArrayList();

    /**
     * 拒绝访问的IP段  黑名单
     */
    private static List<IPRange> denyList = new ArrayList();

    /**
     * 远程访问请求头的key,使用该key来通过request获得用户IP
     * 即从request中获取header,来得到真正的访问IP,众所周知的,直接使用request来获取IP是不争取的
     * 因为请求有可能经过了代理,因此从请求头获取真实IP是必要的
     * 这个值是有限的几种
     */
    private static String remoteAddressHeader;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        System.out.println("EffectiveRequestFilter init() 初始化方法执行");
        try {
            URL url = Resources.getResource(Constant.CONFIG_FILE_PATH);
            Properties properties = new Properties();
            properties.load(url.openStream());
            String allowStr = properties.get(Constant.CONFIG_ALLOW_KEY).toString();
            String denyStr = properties.get(Constant.CONFIG_DENY_KEY).toString();
            String paramRemoteAddressHeader = properties.get(Constant.CONFIG_REMOTE_ADDRESS_REQUEST_HEADER).toString();
            if (!Strings.isNullOrEmpty(paramRemoteAddressHeader)) {
                remoteAddressHeader = paramRemoteAddressHeader;
            }
            allowList = initAuthList(allowStr);
            denyList = initAuthList(denyStr);
            System.out.printf("EffectiveRequestFilter init() 初始化方法执行完成,白名单数量:%d,黑名单数量:%d \n", allowList.size(), denyList.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        if (isPermittedRequest(httpServletRequest)) {
            System.out.println("远程IP通过了EffectiveRequestFilter过滤器的IP认证,有权进行访问！");
            chain.doFilter(request, response);
            return;
        }
//      如果访问IP在黑名单中,或者不在白名单中,拒绝请求,直接返回
        System.out.println("远程IP未通过EffectiveRequestFilter过滤器的IP认证,无权进行访问！");
//      实际上如果没有通过IP认证的请求,我根本就不想给客户端任何响应,但看别人都会返回一个无权访问的页面,那我也就返回一串文字吧,其实我内心是拒绝的
        response.getWriter().write("Not Allowed!");
    }


    /**
     * 初始化白/黑名单
     *
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
     * 是否是有效请求 ip黑名单/黑名单
     * 如果同时配置了黑名单/白名单,匹配顺序是这样的
     * 先查找黑名单,如果匹配到了,返回false,如果没有再匹配白名单
     * 如果匹配到了,返回true,如果没有匹配到返回false
     * 如果只配置了黑名单,那么不在黑名单范围内的返回true,否则返回false
     * 如果只配置了白名单,那么在白名单范围内的返回true,否则返回false
     * 如果都没有配置,那么直接返回true
     *
     * @param remoteAddress 远程访问地址
     * @return
     */
    public boolean isPermittedRequest(String remoteAddress) {
//        是否是ipV6
        boolean ipV6 = remoteAddress != null && remoteAddress.indexOf(':') != -1;
        if (ipV6) {
//            0:0:0:0:0:0:0:1 对应成ip地址为127.0.0.1
            return "0:0:0:0:0:0:0:1".equals(remoteAddress) || (denyList.size() == 0 && allowList.size() == 0);
        }
//        将远程IP(字符串类型),封装成IPAddress对象
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
     * 需要注意的是获取IP的方式有很多种,使用就多的就是request.getRemoteAddr()
     * 但是这种方式获得IP不够准确,有可能请求经过了代理,因此
     * 这里获取的远程IP通过请求头来获取IP的,那么请求头header中key很多
     * 究竟从哪个key来获取IP,那么这个key就是需要配置到配置文件中的
     * 即：CONFIG_REMOTE_ADDRESS_REQUEST_HEADER
     * 需要注意的是它的取值是有限个的,不是自定义的
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
//            如果没有配置从请求头获取IP,那么直接使用request.getRemoteAddr()来获取IP地址,这种方式获取的IP可能不够准确
            remoteAddress = request.getRemoteAddr();
        }
        return remoteAddress;
    }


}