package cn.kanyun.monitor.logback.common;

import lombok.Data;

/**
 * 日志消息实体
 */
@Data
public class LogMessage {
    private String body;
    private String timestamp;
    private String threadName;
    private String className;
    private String level;
}
