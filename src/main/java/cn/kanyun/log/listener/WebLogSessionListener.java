package cn.kanyun.log.listener;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

/**
 * Session 监听器
 * 为什么用{@link AuthSessionListener}监听器来控制登录踢出
 * 而不是本监听器来控制：https://blog.csdn.net/joshho/article/details/79146867
 * @author KANYUN
 */
@Slf4j
@WebListener(value = "WebLogSessionListener")
public class WebLogSessionListener implements HttpSessionListener {


    @Override
    public void sessionCreated(HttpSessionEvent se) {
        log.info("WebLogSessionListener 新Session创建");
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        log.info("Session被移除");
    }
}
