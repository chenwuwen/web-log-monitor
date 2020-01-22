package cn.kanyun.monitor.logback.web;

import cn.kanyun.monitor.logback.common.LogMessage;
import cn.kanyun.monitor.logback.common.LogQueue;
import com.google.gson.Gson;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * 推送日志
 */
@WebServlet(name = "log", urlPatterns = "/web/log/push")
public class LogServlet extends HttpServlet {


    /**
     * 创建定时循环线程池
     */
    ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);

    /**
     * 得到日志队列
     */
    LogQueue logQueue = LogQueue.INSTANCE;

    Gson gson = new Gson();

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");

        PrintWriter pw = resp.getWriter();
//        延迟500毫秒启动,每次间隔500毫秒
        scheduledExecutorService.scheduleAtFixedRate(() -> {
            LogMessage logMessage = logQueue.poll();
            if (logMessage != null) {
                String body = gson.toJson(logMessage);
//                  固定格式
                pw.write("data: " + body + "\n\n");

//               如果长连接过程中客户端被关闭了，服务端没有感知却还在执行这个循环，这种情况显然是不允许的。
//               关闭浏览器后，服务器下一次要推送时就会抛出异常，这个异常已经在PrintWriter的flush()中被捕捉了，
//               我们只需它的调用checkError()，有错误的话return即可停止执行
                if (pw.checkError()) {
//                  checkError()方法中已经调用了flush()方法,因此不必再调用flush()方法
                    System.out.println("WebLog客户端断开连接");
                    return;
                }

            }
        }, 500, 500, TimeUnit.MILLISECONDS);
    }
}
