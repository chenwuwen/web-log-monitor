package cn.kanyun.log.common;

/**
 * 定义返回的头信息的key
 * @author KANYUN
 */

public enum ServletHeader {

    /**
     * Header中的age属性
     */
    Age("Age"),

    /**
     * Header中的etag属性
     */
    ETag("ETag"),

    /**
     * Header中的location属性
     */
    Location("Location"),

    /**
     * Header中的proxy-authenticate属性
     */
    ProxyAuthenticate("Proxy-Authenticate"),

    RetryAfter("Retry-After"),

    Server("Server"),

    Vary("Vary"),

    WWWAuthenticate("WWW-Authenticate"),

    /**
     * Header中的connection属性
     */
    Connection("Connection"),

    /**
     * Header中的content-length属性(文件大小)
     */
    ContentLength("Content-Length"),

    /**
     * Header中的Content-Type属性(请求/返回类型)
     */
    ContentType("Content-Type"),

    Date("Date");


    /**
     * header 属性key
     */
    private final String header;

    ServletHeader(String header) {
        this.header = header;
    }

    @Override
    public String toString() {
        return header;
    }
}
