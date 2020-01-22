package cn.kanyun.monitor.logback.listener;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import java.util.Set;

/**
 * ServletContainerInitializer 也是 Servlet 3.0 新增的一个接口，
 * 容器在启动时使用 JAR 服务 API(JAR Service API) 来发现 ServletContainerInitializer 的实现类，
 * 并且容器将 WEB-INF/lib 目录下 JAR 包中的类都交给该类的 onStartup() 方法处理，
 * 我们通常需要在该实现类上使用 @HandlesTypes 注解来指定希望被处理的类，过滤掉不希望给 onStartup() 处理的类。
 * https://www.ibm.com/developerworks/cn/java/j-lo-servlet30/index.html
 */
public class WebLogMonitorServletContainerInitializer implements ServletContainerInitializer {
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        System.out.println("WebLogMonitorServletContainerInitializer类 onStartup()方法被调用");
//        可以在此处动态添加Servlet/Filter/Listener
//        servletContext.addFilter();
//        servletContext.addListener();
//        servletContext.addServlet();

//        servletContext.createFilter();
//        servletContext.createListener();
//        servletContext.createServlet();

//        主要用于动态为 Servlet 增加映射信息，这等价于在 web.xml( 抑或 web-fragment.xml) 中使用 <servlet-mapping> 标签为存在的 Servlet 增加映射信息
//        servletContext.getServletRegistration().addMapping()
    }
}
