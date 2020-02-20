package cn.kanyun.log.appender;

import cn.kanyun.log.common.LogMessage;
import cn.kanyun.log.common.LogQueue;
import cn.kanyun.log.util.Utils;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.core.layout.PatternLayout;

import java.io.Serializable;

/**
 * 自定义Log4j2 appender
 * @Plugin注解：这个注解，是为了在之后配置log4j2-spring.xml时，指定Appender的 Tag
 * 这里需要注意，配置文件中,需要在configuration中，加入属性packages为自定义appender类所在包名cn.kanyun.log.appender才会被扫描生效
 * 构造函数：除了使用父类的以外，也可以增加一些自己的配置。
 * 重写append()方法：这里面需要实现具体的逻辑，日志的去向
 * @author KANYUN
 *
 */
@Plugin(name = "WebLogLog4j2Appender", category = "Core", elementType = "appender", printObject = true)
public class WebLogLog4j2Appender extends AbstractAppender {
    private String appName;
    private static final LogQueue logQueue = LogQueue.INSTANCE;

    protected WebLogLog4j2Appender(String name, String appName, Filter filter, Layout<? extends Serializable> layout,
                                   final boolean ignoreExceptions, final Property[] properties) {
//        super(name, filter, layout, ignoreExceptions, properties);
//        这里三个参数的构造方法已经过时,但是考虑到兼容性问题,还是使用这个构造方法
        super(name, filter, layout, ignoreExceptions);
        this.appName = appName;
    }

    @Override
    public void append(org.apache.logging.log4j.core.LogEvent event) {
        // 此处自定义实现输出
        LogMessage loggerMessage = new LogMessage();
        String level = event.getLevel().toString();
        String loggerName = event.getLoggerName();
        String msg = event.getMessage().getFormattedMessage();
        String threadName = event.getThreadName();
        String className = event.getSource().getClassName();
        int lineNumber = event.getSource().getLineNumber();

        Throwable throwable = event.getThrown();
        // todo 这里实现自定义的日志处理逻辑
        loggerMessage.setLevel(level);
        loggerMessage.setBody(msg);
        loggerMessage.setThreadName(threadName);
        loggerMessage.setTimestamp(Utils.convertTimeToString(event.getTimeMillis()));
        loggerMessage.setClassName(className);
        logQueue.push(loggerMessage);
    }


    /**
     * log4j2 使用 appender 插件工厂，因此传参可以直接通过 PluginAttribute 注解注入
     */
    @PluginFactory
    public static WebLogLog4j2Appender createAppender(
            @PluginAttribute("name") String name,
            @PluginAttribute("appName") String appName,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter) {
        if (layout == null) {
            layout = PatternLayout.createDefaultLayout();
        }
        return new WebLogLog4j2Appender(name, appName, filter, layout, true, null);
    }


}