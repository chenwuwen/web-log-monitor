package cn.kanyun.log.appender;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import cn.kanyun.log.common.LogMessage;
import cn.kanyun.log.common.LogQueue;
import cn.kanyun.log.util.Utils;

/**
 * LogBack过滤器
 * 不是Servlet的filter
 * 弃用 使用Logback自定义的appender
 */
@Deprecated
public class WebLogLogBackFilter extends Filter<ILoggingEvent> {

    private static final LogQueue logQueue = LogQueue.INSTANCE;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        LogMessage loggerMessage = new LogMessage();
        loggerMessage.setBody(event.getMessage());
//        event.getTimeStamp()得到的时间是时间抽 将其转换为 yyyy-MM-dd HH:mm:ss 形式
        loggerMessage.setTimestamp(Utils.convertTimeToString(event.getTimeStamp()));
        loggerMessage.setLevel(event.getLevel().levelStr);
        loggerMessage.setThreadName(event.getThreadName());
//        event.getLoggerName()得到的是类的全限定名
        loggerMessage.setClassName(event.getLoggerName());
        //将LoggerMessage 推入队列中去
        logQueue.push(loggerMessage);
        return FilterReply.ACCEPT;
    }


}
