package cn.kanyun.log.common;

import lombok.Data;

/**
 * 日志消息实体
 * @author KANYUN
 */
@Data
public class LogMessage {
    private String body;
    private String timestamp;
    private String threadName;
    private String className;
    private String level;
}
