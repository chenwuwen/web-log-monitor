package cn.kanyun.log.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 自定义线程工厂
 * 使用自定义的线程工厂ThreadFactory来进行创建，指定当时Thread的创建环境和名字，便于后续查找及发现问题。
 */
public class MonitorThreadFactory implements ThreadFactory {

    /**
     * 线程名前缀
     */
    private static final String THREAD_GROUP_NAME = "web-log-log";

    /**
     * 线程序号
     */
    private AtomicInteger nextId = new AtomicInteger(1);


    @Override
    public Thread newThread(Runnable r) {
        String threadName = THREAD_GROUP_NAME + "-" + nextId.incrementAndGet();
        Thread thread = new Thread(null, r, threadName, 0);
//        设置线程为守护线程
        thread.setDaemon(true);
        return thread;

    }
}
