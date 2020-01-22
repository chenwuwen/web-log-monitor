package cn.kanyun.monitor.logback.filter;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.filter.Filter;
import ch.qos.logback.core.spi.FilterReply;
import cn.kanyun.monitor.logback.common.LogMessage;
import cn.kanyun.monitor.logback.common.LogQueue;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

/**
 * LogBack过滤器
 * 不是Servlet的filter
 */
public class WebLogFilter extends Filter<ILoggingEvent> {

    private static final LogQueue logQueue = LogQueue.INSTANCE;

    @Override
    public FilterReply decide(ILoggingEvent event) {
        LogMessage loggerMessage = new LogMessage();
        loggerMessage.setBody(event.getMessage());
//        event.getTimeStamp()得到的时间是时间抽 将其转换为 yyyy-MM-dd HH:mm:ss 形式
        loggerMessage.setTimestamp(convertTimeToString(event.getTimeStamp()));
        loggerMessage.setLevel(event.getLevel().levelStr);
        loggerMessage.setThreadName(event.getThreadName());
//        event.getLoggerName()得到的是类的全限定名
        loggerMessage.setClassName(event.getLoggerName());
        //将LoggerMessage 推入队列中去
        logQueue.push(loggerMessage);
        return FilterReply.ACCEPT;
    }


    /**
     * 时间转换
     *
     * @param time
     * @return
     */
    private String convertTimeToString(Long time) {
//        如果time不为空,则程序继续往下执行
        assert time != null;
        DateTimeFormatter ftf = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        return ftf.format(LocalDateTime.ofInstant(Instant.ofEpochMilli(time), ZoneId.systemDefault()));
    }

}
