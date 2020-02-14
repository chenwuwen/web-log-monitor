package cn.kanyun.monitor.logback.listener;

import cn.kanyun.monitor.logback.common.Constant;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSession;
import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * Session 监听器
 */
@WebListener(value = "WebLogSessionListener")
public class WebLogSessionListener implements HttpSessionListener {

    /**
     * 创建只有一个容量的阻塞队列,用于存放session
     * 用于登录踢出功能,web-log-monitor同时只允许一个人登录
     */
    private static final BlockingQueue<HttpSession> blockingQueue = new ArrayBlockingQueue(1);

    @Override
    public void sessionCreated(HttpSessionEvent se) {
        System.out.println("新Session创建");
//        offer(E e)：如果队列没满，立即返回true； 如果队列满了，立即返回false-->不阻塞
        if (!blockingQueue.offer(se.getSession())) {
//            poll()：如果没有元素，直接返回null；如果有元素，出队
            HttpSession oldSession = blockingQueue.poll();
            if (oldSession != null) {
//                销毁旧Session(不使用oldSession.invalidate()的原因,是因为可能会造成主体项目Session丢失,因此通过设置session中的属性值更合适)
//                oldSession.invalidate();
                oldSession.setAttribute(Constant.SESSION_USER_KEY, null);
            }
            blockingQueue.offer(se.getSession());
        }
    }


    @Override
    public void sessionDestroyed(HttpSessionEvent se) {
        System.out.println("Session被移除");
    }
}
