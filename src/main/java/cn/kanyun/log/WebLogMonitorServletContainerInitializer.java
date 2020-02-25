package cn.kanyun.log;

import cn.kanyun.log.common.WebLogMonitorContext;
import cn.kanyun.log.common.WebLogMonitorContextFactory;
import cn.kanyun.log.filter.AuthFilter;
import cn.kanyun.log.filter.EffectiveRequestFilter;
import cn.kanyun.log.filter.StaticFileFilter;
import cn.kanyun.log.listener.AuthSessionListener;
import cn.kanyun.log.listener.WebLogMonitorContextListener;
import cn.kanyun.log.listener.WebLogSessionListener;
import cn.kanyun.log.web.AuthServlet;
import cn.kanyun.log.web.FileServlet;
import cn.kanyun.log.web.LogServlet;
import cn.kanyun.log.web.VerificationServlet;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.DispatcherType;
import javax.servlet.FilterRegistration;
import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRegistration;
import javax.servlet.annotation.HandlesTypes;
import java.util.EnumSet;
import java.util.Set;

/**
 * ServletContainerInitializer 也是 Servlet 3.0 新增的一个接口，
 * spring对它的实现类是org.springframework.web.SpringServletContainerInitializer，
 * 这个实现类是通过ＳＰＩ机制去加载的
 * Servlet容器启动会扫描当前应用里面每一个jar包的ServletContainerInitializer实现
 * 并且容器将 WEB-INF/lib 目录下 JAR 包中的类都交给该类的 onStartup() 方法处理，
 * 每个框架要使用ServletContainerInitializer就必须在对应的jar包的META-INF/services 目录创建一个名为javax.servlet.ServletContainerInitializer的文件，
 * 文件内容指定具体的ServletContainerInitializer实现类，
 * 那么，当web容器(需要注意的是,这种方式只针对于外置容器起作用,使用Springboot的内嵌容器时无法生效的)启动时就会运行这个初始化器做一些组件内的初始化工作。
 * 我们通常需要在该实现类上使用 @HandlesTypes 注解来指定希望被处理的类，过滤掉不希望给 onStartup() 处理的类。
 * 通过HandlesTypes可以将一些感兴趣类(必须是接口或者父类才能生效，接口方式更好，Spring Web就是使用接口这种方式)注入到ServletContainerInitializer的onStartup方法作为参数传入。
 * https://www.ibm.com/developerworks/cn/java/j-lo-servlet30/index.html
 * <p>
 * 这个类主要为SpringMvc配置,如果不在这个类中添加servlet/filter/listener servlet/filter/listener 将不会生效
 * <p>
 * 嵌入式环境下，SpringBoot有意忽略javax.servlet.ServletContainerInitializer
 * 解决方案：
 * 1. 注册org.springframework.boot.context.embedded.ServletContextInitializer类型的Bean代替ServletContainerInitializer。
 * 2. 直接向容器注册Servlet和Filter。
 * 3. 向容器注册ServletRegistrationBean和FilterRegistrationBean。
 *
 * @author KANYUN
 */
@Slf4j
@HandlesTypes(value = {WebLogMonitorContextFactory.class})
public class WebLogMonitorServletContainerInitializer implements ServletContainerInitializer {

    /**
     * 应用启动的时候，会运行onStartup方法
     *
     * @param set            感兴趣的类型的所有子类型(如果未在@HandlesTypes注解中配置接口后者父类,那么set为null)
     * @param servletContext 代表当前Web应用的ServletContext；一个Web应用一个ServletContext
     * @throws ServletException
     */
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        log.info("WebLogMonitorServletContainerInitializer类 onStartup()方法被调用");
        if (set != null) {
            log.info("感兴趣类型的数量:" + set.size());
//        set集合中的值即为@HandlesTypes()注解中定义的类型
            for (Class<?> clazz : set) {

                if (clazz == WebLogMonitorContext.class) {
                    try {
                        WebLogMonitorContext webLogMonitorContext = (WebLogMonitorContext) clazz.newInstance();
                        webLogMonitorContext.initWebLogMonitorContext();
                    } catch (InstantiationException e) {
                        e.printStackTrace();
                    } catch (IllegalAccessException e) {
                        e.printStackTrace();
                    }
                }

//                todo 其他类型,或者其他子类继续初始化
            }
        }

//==========================编码形式注册三大组件============================

//        注册组件  ServletRegistration
        ServletRegistration.Dynamic servlet = servletContext.addServlet("authServlet", new AuthServlet());
        servletContext.addServlet("verificationServlet", new VerificationServlet());
        servletContext.addServlet("logServlet", new LogServlet());
        servletContext.addServlet("fileServlet", new FileServlet());
//      主要用于动态为 Servlet 增加映射信息，这等价于在 web.xml( 抑或 web-fragment.xml) 中使用 <servlet-mapping> 标签为存在的 Servlet 增加映射信息
        servletContext.getServletRegistration("authServlet").addMapping("/web/log/auth", "/web/log/login", "/web/log/*");
        servletContext.getServletRegistration("verificationServlet").addMapping("/web/log/verification");
        servletContext.getServletRegistration("logServlet").addMapping("/web/log/push");
        servletContext.getServletRegistration("fileServlet").addMapping("/web/log/file", "/web/log/file/*");

        //注册Listener
        servletContext.addListener(WebLogMonitorContextListener.class);
        servletContext.addListener(WebLogSessionListener.class);
        servletContext.addListener(AuthSessionListener.class);

        //注册Filter  FilterRegistration 注册时需要注意注册顺序,因为如果多个过滤器过滤的URL一样的话,此时过滤器执行的顺序是过滤器添加的顺序

//        权限认证过滤器
        FilterRegistration.Dynamic filter = servletContext.addFilter("authFilter", AuthFilter.class);

        //配置Filter的映射信息
        filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/web/log/push/*", "/web/log/file/*");
        filter.setAsyncSupported(true);

//        IP认证过滤器
        servletContext.addFilter("effectiveRequestFilter", EffectiveRequestFilter.class).setAsyncSupported(true);

        servletContext.getFilterRegistration("effectiveRequestFilter").addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/web/log/*");

//        静态资源文件过滤器
        servletContext.addFilter("staticFileFilter", StaticFileFilter.class).setAsyncSupported(true);

        servletContext.getFilterRegistration("staticFileFilter").addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/web/log/*");


        log.info("WebLogMonitorServletContainerInitializer类 onStartup()方法调用完成");
    }
}
