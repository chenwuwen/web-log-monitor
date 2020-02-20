package cn.kanyun.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.UnsynchronizedAppenderBase;
import cn.kanyun.log.common.LogMessage;
import cn.kanyun.log.common.LogQueue;
import cn.kanyun.log.util.Utils;
import lombok.extern.slf4j.Slf4j;

/**
 * Logback自定义appender
 * @author KANYUN
 */
@Slf4j
public class WebLogLogBackAppender extends UnsynchronizedAppenderBase<ILoggingEvent> {

    private static final LogQueue logQueue = LogQueue.INSTANCE;

    @Override
    public void start() {
        //或者写入数据库 或者redis时 初始化连接等等
        super.start();
    }

    @Override
    protected void append(ILoggingEvent iLoggingEvent) {
        LogMessage loggerMessage = new LogMessage();
//        iLoggingEvent.getMessage() 得到的是拼接前的日志 比如 使用log.info("Hello {}","World)
//        得到的是"Hello {}" getFormattedMessage()得到的是拼接后的数据
        loggerMessage.setBody(iLoggingEvent.getFormattedMessage());
//        event.getTimeStamp()得到的时间是时间抽 将其转换为 yyyy-MM-dd HH:mm:ss 形式
        loggerMessage.setTimestamp(Utils.convertTimeToString(iLoggingEvent.getTimeStamp()));
        loggerMessage.setLevel(iLoggingEvent.getLevel().levelStr);
        loggerMessage.setThreadName(iLoggingEvent.getThreadName());
//        event.getLoggerName()得到的是类的全限定名
        loggerMessage.setClassName(iLoggingEvent.getLoggerName());
        //将LoggerMessage 推入队列中去
        logQueue.push(loggerMessage);
    }

    /**
     * stop方法可能需要注册一个钩子才能正常运行
     * 使用RunTime.getRunTime().addShutdownHook()
     */
    @Override
    public void stop() {
        //释放相关资源，如数据库连接，redis线程池等等
        log.info("logback-stop方法被调用");
        if (!isStarted()) {
            return;
        }
        super.stop();
    }
}