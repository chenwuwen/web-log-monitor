package cn.kanyun.log.common;

/**
 * 主要用于 WebLogMonitorServletContainerInitializer 初始化使用
 * Servlet容器启动时会执行实现该接口的类的方法
 * @author KANYUN
 */
public interface WebLogMonitorContextFactory {

    /**
     * 初始化WebLogMonitor上下文
     */
    void initWebLogMonitorContext();
}
