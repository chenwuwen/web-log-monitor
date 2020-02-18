package cn.kanyun.log.common;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 在线用户,用于登录踢出,一个账户只能在一处登录,
 * 当然在这个模块里,只有一个用户
 */
public class OnLineUser {
    /**
     * 创建只有一个容量的阻塞队列,用于存放session
     * 用于登录踢出功能,web-log-monitor同时只允许一个人登录
     */
    private static final BlockingQueue<HttpSession> blockingQueue = new ArrayBlockingQueue(1);


    /**
     * 向队列中添加Session,如果队列中存在Session,就将原来的Session从队列中拿出来,然后放进
     * 新Session,对拿出来的Session,将它的属性值进行更改
     *
     * @param session
     */
    public static void addSession(HttpSession session) {
//      offer(E e)：如果队列没满，立即返回true； 如果队列满了，立即返回false-->不阻塞
        if (!blockingQueue.offer(session)) {
//          poll()：如果没有元素，直接返回null；如果有元素，出队
            HttpSession oldSession = blockingQueue.poll();
            if (oldSession != null) {
//                销毁旧Session(不使用oldSession.invalidate()的原因,是因为可能会造成主体项目Session丢失,因此通过设置session中的属性值更合适)
//                oldSession.invalidate();
                oldSession.removeAttribute(Constant.SESSION_USER_KEY);
            }
            blockingQueue.offer(session);
        }
    }

    /**
     * 判断该模块是否被登录过
     * @return
     */
    public static boolean hasLogin() {
        return blockingQueue.isEmpty();
    }
}