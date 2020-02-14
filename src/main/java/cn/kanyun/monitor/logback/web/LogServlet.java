package cn.kanyun.monitor.logback.web;

import cn.kanyun.monitor.logback.common.PushService;

import javax.servlet.AsyncContext;
import javax.servlet.AsyncEvent;
import javax.servlet.AsyncListener;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * 推送日志
 */
@WebServlet(name = "log", urlPatterns = "/web/log/push", asyncSupported = true)
public class LogServlet extends HttpServlet {


    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {


//        这里把异步操作去掉了,主要还是向老项目兼容,因为servlet3.0之前不支持异步,又因为这个web日志监控没有几个人会看,因此去掉无妨

////        开启异步
//        AsyncContext ac = req.startAsync();
////        异步servlet的超时时间,异步Servlet有对应的超时时间，如果在指定的时间内没有执行完操作，response依然会走原来Servlet的结束逻辑，后续的异步操作执行完再写回的时候，可能会遇到异常。
//        ac.setTimeout(9000);
//        ac.addListener(new AsyncListener() {
//            @Override
//            public void onComplete(AsyncEvent asyncEvent) throws IOException {
//
//            }
//
//            @Override
//            public void onTimeout(AsyncEvent asyncEvent) throws IOException {
//
//            }
//
//            @Override
//            public void onError(AsyncEvent asyncEvent) throws IOException {
//
//            }
//
//            @Override
//            public void onStartAsync(AsyncEvent asyncEvent) throws IOException {
//
//            }
//        });

        PushService pushService = new PushService();
        pushService.push(req, resp);

    }
}
