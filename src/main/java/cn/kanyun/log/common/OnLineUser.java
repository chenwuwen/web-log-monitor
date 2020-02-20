package cn.kanyun.log.common;

import javax.servlet.http.HttpSession;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

/**
 * 在线用户,用于登录踢出,一个账户只能在一处登录,
 * 当然在这个模块里,只有一个用户
 * @author KANYUN
 */
public class OnLineUser {
    /**
     * 创建只有一个容量的阻塞队列,用于存放session
     * 用于登录踢出功能,web-log-monitor同时只允许一个人登录
     * 由于只有一个用户,且只能同时一个地点登录,那么其实可以使用一个变量来定义的
     * 之所以使用队列这个容器,可能是为了更直观吧,至少定义的时候我是这么想的
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
     *
     * @return
     */
    public static boolean hasLogin() {
        return blockingQueue.isEmpty();
    }


    /**
     * 检测队列中保存的用户是否失效
     */
    public static void checkOnlineUser() {
        if (!blockingQueue.isEmpty()) {
//           这里为什么不使用element()，而使用peek()?他们都是返回队列头部元素,
//           但是不删除头部元素,因为当队列为空时 peek()返回null,而element()抛出异常
            HttpSession session = blockingQueue.peek();
            if (session != null && session.getAttribute(Constant.SESSION_USER_KEY) != null) {

            }else {
//                这里为什么使用poll()而不是remove(),他们都是移除并返回队列头部的元素
//                因为poll() 如果队列为空，则返回null 而remove()如果队列为空，则抛出一个NoSuchElementException异常
                blockingQueue.poll();
            }
        }
    }
}