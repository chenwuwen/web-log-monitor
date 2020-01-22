package cn.kanyun.monitor.logback.listener;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.annotation.WebListener;

/**
 * 在 Servlet API 中有一个 ServletContextListener 接口，
 * 它能够监听 ServletContext 对象的生命周期，实际上就是监听 Web 应用的生命周期
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
