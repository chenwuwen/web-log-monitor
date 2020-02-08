package cn.kanyun.monitor.logback.listener;

import javax.servlet.ServletContainerInitializer;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.annotation.HandlesTypes;
import java.util.Set;

/**
 * ServletContainerInitializer 也是 Servlet 3.0 新增的一个接口，
 * Servlet容器启动会扫描当前应用里面每一个jar包的ServletContainerInitializer实现
 * 并且容器将 WEB-INF/lib 目录下 JAR 包中的类都交给该类的 onStartup() 方法处理，
 * 每个框架要使用ServletContainerInitializer就必须在对应的jar包的META-INF/services 目录创建一个名为javax.servlet.ServletContainerInitializer的文件，
 * 文件内容指定具体的ServletContainerInitializer实现类，那么，当web容器启动时就会运行这个初始化器做一些组件内的初始化工作。
 * 我们通常需要在该实现类上使用 @HandlesTypes 注解来指定希望被处理的类，过滤掉不希望给 onStartup() 处理的类。
 * 通过HandlesTypes可以将感兴趣的一些类注入到ServletContainerInitializerde的onStartup方法作为参数传入。
 * https://www.ibm.com/developerworks/cn/java/j-lo-servlet30/index.html
 */

@HandlesTypes(value = {})
public class WebLogMonitorServletContainerInitializer implements ServletContainerInitializer {

    /**
     * 应用启动的时候，会运行onStartup方法
     *
     * @param set            感兴趣的类型的所有子类型
     * @param servletContext 代表当前Web应用的ServletContext；一个Web应用一个ServletContext
     * @throws ServletException
     */
    @Override
    public void onStartup(Set<Class<?>> set, ServletContext servletContext) throws ServletException {
        System.out.println("WebLogMonitorServletContainerInitializer类 onStartup()方法被调用");
        System.out.println("感兴趣的类型：");
//        set集合中的值即为@HandlesTypes()注解中定义的类型
        for (Class<?> clazz : set) {
            System.out.println(clazz);
        }

//==========================编码形式注册三大组件============================
//        注册组件  ServletRegistration
        //ServletRegistration.Dynamic servlet = servletContext.addServlet("userServlet", new UserServlet());

//      主要用于动态为 Servlet 增加映射信息，这等价于在 web.xml( 抑或 web-fragment.xml) 中使用 <servlet-mapping> 标签为存在的 Servlet 增加映射信息
//       servletContext.getServletRegistration().addMapping()
        ////注册Listener
        //servletContext.addListener(UserListener.class);

        ////注册Filter  FilterRegistration
        //FilterRegistration.Dynamic filter = servletContext.addFilter("userFilter", UserFilter.class);
        ////配置Filter的映射信息
        //filter.addMappingForUrlPatterns(EnumSet.of(DispatcherType.REQUEST), true, "/*");
    }
}
