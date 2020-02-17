package cn.kanyun.log.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public enum LogQueue {

    INSTANCE;

    /**
     * 队列大小
     * 这个队列大小不宜太大,否则当在前端连接SSE时,想要查看当前的日志,需要等待比较久的时间
     */
    public static final int QUEUE_MAX_SIZE = 100;

    /**
     * 阻塞队列
     */
    private BlockingQueue<LogMessage> blockingQueue;


    /**
     * 自定义构造方法,用于构造blockingQueue象
     * 枚举的构造方法都是私有的
     */
    LogQueue() {
        blockingQueue = new LinkedBlockingQueue<>(QUEUE_MAX_SIZE);
    }

    /**
     * 消息入队
     * 如果入队失败,则移除队头元素,再入队
     *
     * @param logMessage
     * @return
     */
    public boolean push(LogMessage logMessage) {
//        offer: 将指定元素插入此队列中（如果立即可行且不会违反容量限制）,成功时返回 true，如果当前没有可用的空间，则返回 false，不会抛异常
        if (!blockingQueue.offer(logMessage)) {
            blockingQueue.poll();
            return blockingQueue.offer(logMessage);
        }
        return true;
    }


    /**
     * 消息出队
     * 取出消息并移除
     *
     * @return
     */
    public LogMessage poll() {
//            peek 返回队列头部的元素 如果队列为空，则返回null
//            take():移除并返回BlockingQueue里排在首位的对象,若BlockingQueue为空,阻断进入等待状态直到Blocking有新的对象被加入为止
//            message = this.blockingQueue.take();
//            poll(time):移除并返回BlockingQueue里排在首位的对象,若不能立即取出,则可以等time参数规定的时间,取不到时返回null
        LogMessage message = blockingQueue.poll();

        return message;
    }

}
