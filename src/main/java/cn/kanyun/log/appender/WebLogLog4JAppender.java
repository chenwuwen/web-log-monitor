package cn.kanyun.log.appender;

import cn.kanyun.log.common.LogMessage;
import cn.kanyun.log.common.LogQueue;
import cn.kanyun.log.util.Utils;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.spi.LoggingEvent;

/**
 * 自定义Log4j appender
 */
public class WebLogLog4JAppender extends AppenderSkeleton {
    private String appName;

    private static final LogQueue logQueue = LogQueue.INSTANCE;

    /**
     * 定义 setter 方法，这样在配置文件中添加类似 log4j.appender.CustomAppender.appName=test_app_name 的配置项时，配置会被注入到这个 appender 中
     */
    public void setAppName(String appName) {
        this.appName = appName;
    }

    /**
     * 这个方法负责附加日志记录事件，并在错误发生时负责调用错误处理程序
     * @param loggingEvent
     */
    @Override
    protected void append(LoggingEvent loggingEvent) {
        LogMessage loggerMessage = new LogMessage();
        loggerMessage.setLevel(loggingEvent.getLevel().toString());
        loggerMessage.setThreadName(loggingEvent.getThreadName());
        loggerMessage.setClassName(loggingEvent.getLocationInformation().getClassName());
        loggerMessage.setTimestamp(Utils.convertTimeToString(loggingEvent.getTimeStamp()));
        loggerMessage.setBody(loggingEvent.getMessage().toString());
        logQueue.push(loggerMessage);
    }

    /**
     * 关闭资源
     * 必须把 closed 字段的值设置为 true
     */
    @Override
    public void close() {
        if (this.closed) {
            return;
        }
        this.closed = true;
    }

    /**
     * 因为不需要输出
     * 所以这里不需要使用格式化器
     * 表示不需要格式化器
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }
}