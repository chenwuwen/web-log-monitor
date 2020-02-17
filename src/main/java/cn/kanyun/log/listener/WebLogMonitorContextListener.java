package cn.kanyun.log.listener;

import uk.org.lidalia.sysoutslf4j.context.SysOutOverSLF4J;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * 在 Servlet API 中有一个 ServletContextListener 接口，
 * 它能够监听 ServletContext 对象的生命周期，实际上就是监听 Web 应用的生命周期
 * 一个模块只能有一个ServletContextListener的实例
 * 需要注意的是：ServletContextListener只能定义一个,一旦第一个ServletContextListener被调用,就不会再添加ServletContextListener了
 */
@WebListener(value = "WebLogMonitorContextListener")
public class WebLogMonitorContextListener implements ServletContextListener {


    /**
     * 当Servlet 容器启动Web 应用时调用该方法。在调用完该方法之后，容器再对Filter 初始化
     * 并且对那些在Web 应用启动时就需要被初始化的Servlet 进行初始化
     *
     * @param sce
     */
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        System.out.println("=================================================");
        System.out.println("WebLogMonitorContextListener监听器,监听到web容器启动");
        String path = sce.getServletContext().getContextPath();
        System.out.printf("访问日志URL：[%s] \n", path + "/web/log");
        System.out.println("=================================================");

        ServletContext servletContext = sce.getServletContext();

//        判断项目启动使用的是内嵌tomcat还是外置tomcat

        String serverInfo = servletContext.getServerInfo();

//        打印运行容器的名称及版本
        System.out.println("运行的容器信息:" + serverInfo);

//        Springboot如果需要使用外置tomcat部署,需要继承SpringBootServletInitializer

//        用于将System.out/System.err/e.printStackTrace() 输出到Slf4j,否则 无法获取System.out/System.err/e.printStackTrace() 输出的信息
        SysOutOverSLF4J.sendSystemOutAndErrToSLF4J();

        System.out.println("WebLogMonitorContextListener监听器,contextInitialized()方法执行完成 ");
    }

    /**
     * 当Servlet 容器终止Web 应用时调用该方法。在调用该方法之前，容器会先销毁所有的Servlet 和Filter 过滤器。
     *
     * @param sce
     */
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        System.out.println("WebLogMonitorContextListener监听器,监听到web容器关闭");
    }
}
