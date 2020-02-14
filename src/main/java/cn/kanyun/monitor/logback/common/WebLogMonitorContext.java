package cn.kanyun.monitor.logback.common;

public final class WebLogMonitorContext implements WebLogMonitorContextFactory {

    public void initWebLogMonitorContext() {
        System.out.println("========初始化WebLogMonitorContext=========");
    }
}
