package cn.kanyun.log.listener;

import cn.kanyun.log.common.Constant;
import cn.kanyun.log.common.OnLineUser;

import javax.servlet.annotation.WebListener;
import javax.servlet.http.HttpSessionAttributeListener;
import javax.servlet.http.HttpSessionBindingEvent;

/**
 * Session属性变化监听器
 * 为什么用这个监听器来控制登录踢出
 * 而不是 {@link WebLogSessionListener}来控制：https://blog.csdn.net/joshho/article/details/79146867
 */
@WebListener(value = "AuthSessionListener")
public class AuthSessionListener implements HttpSessionAttributeListener {



    @Override
    public void attributeAdded(HttpSessionBindingEvent event) {
        String attrName = event.getName();
        if (attrName.equals(Constant.SESSION_USER_KEY)) {
            OnLineUser.addSession(event.getSession());
        }
    }

    @Override
    public void attributeRemoved(HttpSessionBindingEvent event) {

    }

    @Override
    public void attributeReplaced(HttpSessionBindingEvent event) {

    }
}