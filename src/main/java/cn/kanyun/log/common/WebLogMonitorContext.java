package cn.kanyun.log.common;

import lombok.extern.slf4j.Slf4j;

/**
 * 该类 在SSM项目启动时通过加载{@link cn.kanyun.log.WebLogMonitorServletContainerInitializer} 类
 * 来调用该类的方法
 * 那么为什么会调用WebLogMonitorServletContainerInitializer这个类呢
 * 因为 在 META-INF/services下 定义了javax.servlet.ServletContainerInitializer文件
 * 这个文件中的实现类就是WebLogMonitorServletContainerInitializer这个类
 * 这个文件在servlet容器启动时加载并调用其中的实现类,因此可以做一些初始化操作
 * 需要注意的是Springboot启动时不加载这个文件
 * @author KANYUN
 */
@Slf4j
public final class WebLogMonitorContext implements WebLogMonitorContextFactory {

    @Override
    public void initWebLogMonitorContext() {
        log.info("========初始化WebLogMonitorContext=========");
    }
}
