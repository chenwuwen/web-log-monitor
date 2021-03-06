package cn.kanyun.log.common;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 推送服务
 */
@Slf4j
public class PushService {


    /**
     * 创建定时循环线程池
     */
    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1, new MonitorThreadFactory());

    /**
     * 得到日志队列
     */
    LogQueue logQueue = LogQueue.INSTANCE;

    /**
     * 浏览器默认的是，如果服务器端三秒内没有发送任何信息，则开始重连。服务器端可以用retry头信息，指定通信的最大间隔时间。
     */
    private static final String RETRY = "retry:" + 30 * 1000 + "\n";


    /**
     * 实例化Gson实例,需要注意的是gson会默认将特殊字符转化成unicode编码
     * 因此需要禁用Gson将特殊字符转换为unicode编码
     */
    private static final Gson gson = new GsonBuilder()
//            .excludeFieldsWithoutExposeAnnotation() //不对没有用@Expose注解的属性进行操作
//            .enableComplexMapKeySerialization() //当Map的key为复杂对象时,需要开启该方法
            .serializeNulls() //当字段值为空或null时，依然对该字段进行转换
            .setDateFormat("yyyy-MM-dd HH:mm:ss") //时间转化为特定格式
//            .setPrettyPrinting() //对结果进行格式化，增加了换行,在这里不启用是因为,SSE对换行符敏感
            .disableHtmlEscaping() //防止特殊字符出现乱码
//            .registerTypeAdapter(LogMessage.class, new LogMessageAdapter()) //为某特定对象设置固定的序列或反序列方式，自定义Adapter需实现JsonSerializer或者JsonDeserializer接口
            .create();


    /**
     * 推送消息
     * @param req
     * @param resp
     * @throws IOException
     */
    public void push(HttpServletRequest req, HttpServletResponse resp) throws IOException {

        resp.setContentType("text/event-stream");
        resp.setCharacterEncoding("UTF-8");
        resp.setHeader("Cache-Control", "no-cache,no-store");
        resp.setHeader("Connection", "keep-alive");

        PrintWriter pw = resp.getWriter();

//        一次请求的标志
        boolean request = true;

//        重置连接关闭标志(该变量有两种情况会变为true,一种是推送出错,一种是session失效)
        AtomicBoolean CLOSE_FLAG = new AtomicBoolean(false);


//        如果是第一次请求,或者是sse的断开重连,那么死循环将日志队列中的数据取出并发送到前端,当队列为空时,再定时从队列中取数据
        while (request) {
            try {
//                解决死循环CPU占用100%问题
                Thread.sleep(1);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            LogMessage logMessage = logQueue.poll();
            if (null == logMessage) {
//                如果队列数据取完了,则跳出死循环,定时从队列中取数据
                request = false;
                continue;
            }
            String data = gson.toJson(logMessage);
//         EventSource接收数据的固定格式：retry:${毫秒数}\ndata:${返回数据}\n\n
//         retry是指每隔多久请求一次服务器，data是指要接收的数据。注意这个retry参数不是必须的，如果不填写，对应的浏览器会有一个默认间隔时间，谷歌默认是3000毫秒，也就是3秒钟
            pw.write(RETRY);
//          自定义事件类型(日志推送事件)
            pw.write("event: " + "WebLogMonitor\n");
            pw.write("data:" + data + "\n\n");
            if (pw.checkError()) {
                log.info("首次连接WebLog客户端断开连接");
                return;
            }
        }

//       初始化延时0毫秒启动,每次间隔500毫秒
        scheduledExecutorService.scheduleAtFixedRate(() -> {

//            log.info("定时器执行");
            LogMessage logMessage = logQueue.poll();
            if (logMessage != null) {
                String data = gson.toJson(logMessage);
//              EventSource接收数据的固定格式：retry:${毫秒数}\ndata:${返回数据}\n\n
//              retry是指每隔多久请求一次服务器，data是指要接收的数据。注意这个retry参数不是必须的，如果不填写，对应的浏览器会有一个默认间隔时间，谷歌默认是3000毫秒，也就是3秒钟
                pw.write(RETRY);
//              自定义事件类型(日志推送事件)
                pw.write("event: " + "WebLogMonitor\n");
//              固定格式
                pw.write("data:" + data + "\n\n");
            }

//            如果长连接过程中客户端被关闭了，服务端没有感知却还在执行这个循环，这种情况显然是不允许的。
//            关闭浏览器后，服务器下一次要推送时就会抛出异常，这个异常已经在PrintWriter的flush()中被捕捉了，
//            我们只需它的调用checkError()，有错误的话return即可停止执行(如果打印流遇到了一个错误该方法返回true，无论是在基础输出流或在格式转换过程。)
            if (pw.checkError()) {
//            checkError()方法中已经调用了flush()方法,因此不必再调用flush()方法
                log.info("定时取出数据WebLog客户端断开连接");
                CLOSE_FLAG.set(true);
                try {
//                  关闭线程池
                    scheduledExecutorService.shutdown();
                    if (scheduledExecutorService.awaitTermination(1, TimeUnit.SECONDS)) {
                        log.info("=========PushService 线程池已关闭========");
                    }
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                return;
            }

        }, 0, 500, TimeUnit.MILLISECONDS);


//        这里放置一个死循环会为了防止response关闭
        while (!CLOSE_FLAG.get()) {
            try {
                Thread.sleep(250);
                verificationSession(req, CLOSE_FLAG);
//                log.info("正在进行死循环,为了防止response输出流关闭");
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (!verificationSession(req, CLOSE_FLAG)) {
//            如果session失效,将会重定向到登陆页,进行重新登录
            log.info("SESSION失效,重定向到登录页");
//            这里不用resp.sendRedirect()是因为SSE是异步请求,因此直接使用,无法达到效果,所以可以通过推送自定义事件,让前端进行重定向
//            resp.sendRedirect("/web/log/login.html");
            redirect("/web/log/", pw);
        }
//        如果session有效,但是又进行到了这里,说明是推送出错,此时前端将会进行重试
    }


    /**
     * 验证session是否有效
     * 无效Session将会重定向
     *
     * @param req
     */
    public boolean verificationSession(HttpServletRequest req, AtomicBoolean CLOSE_FLAG) {
        if (req.getSession() == null) {
            CLOSE_FLAG.set(true);
            return false;
        }

        if (req.getSession().getAttribute(Constant.SESSION_USER_KEY) == null) {
            CLOSE_FLAG.set(true);
            return false;
        }

        return true;
    }


    /**
     * 重定向
     *
     * @param url
     * @param pw
     */
    public void redirect(String url, PrintWriter pw) {
        log.info("发送重定向消息 ");
//        自定义事件(日志推送事件)
        pw.write("event: " + "Redirect\n");
        pw.write("data:" + url + "\n\n");
        pw.flush();
    }
}

