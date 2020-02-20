package cn.kanyun.log.common;

/**
 * 常量类
 * @author KANYUN
 */
public class Constant {

    /**
     * Session key
     */
    public static final String SESSION_USER_KEY = "web-log-user";

    /**
     * 登录时 用户名参数 key
     * 同时也是初始化参数的key
     */
    public static final String PARAM_NAME_USERNAME = "username";

    /**
     * 登录时 密码参数 key
     * 同时也是初始化参数的key
     */
    public static final String PARAM_NAME_PASSWORD = "password";

    /**
     * 静态资源文件路径前缀
     */
    public static final String STATIC_FILE_PREFIX = "static/http/resources";

    /**
     * 配置文件名称,默认在ClassPath下
     */
    public static final String CONFIG_FILE_PATH = "web_log_monitor.properties";

    /**
     * properties配置文件用户名key
     */
    public static final String CONFIG_USERNAME_KEY = "web_log_monitor.username";

    /**
     * properties配置文件用户名key
     */
    public static final String CONFIG_PASSWORD_KEY = "web_log_monitor.password";

    /**
     * properties配置文件白名单key
     */
    public static final String CONFIG_ALLOW_KEY = "web_log_monitor.allow";


    /**
     * properties配置文件黑名单key
     */
    public static final String CONFIG_DENY_KEY = "web_log_monitor.deny";

    /**
     * properties配置文件获取请求IP的请求头key
     * 这个key对应的值是有限个的
     * 参考：https://blog.csdn.net/jek123456/article/details/90716316
     * https://www.cnblogs.com/zwdx/p/8989663.html
     */
    public static final String CONFIG_REMOTE_ADDRESS_REQUEST_HEADER = "web_log_monitor.remote_address_request_header";

}
