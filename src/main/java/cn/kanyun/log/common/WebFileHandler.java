package cn.kanyun.log.common;

import cn.kanyun.log.util.Utils;
import com.google.common.base.Strings;
import com.google.common.io.Files;
import lombok.extern.slf4j.Slf4j;

import javax.activation.MimetypesFileTypeMap;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.URLDecoder;
import java.nio.channels.Channels;
import java.nio.channels.FileChannel;
import java.nio.channels.WritableByteChannel;
import java.nio.file.OpenOption;
import java.nio.file.StandardOpenOption;

/**
 * Servlet处理文件
 *
 * @author KANYUN
 */
@Slf4j
public class WebFileHandler {

    File siteRoot;

    public WebFileHandler(File siteRoot) {
        this.siteRoot = siteRoot;
    }

    /**
     * 使用传统IO 处理文件 下载
     *
     * @param file
     * @param response
     * @throws IOException
     */
    @Deprecated
    public void handleFile(File file, HttpServletResponse response) throws IOException {
        String filename = file.getName().toLowerCase();
        String contentType = getContentType(filename);
        response.setContentType(contentType);

        long length = file.length();
        response.setHeader(ServletHeader.ContentLength.toString(), Long.toString(length));

        FileInputStream in;
        try {
            in = new FileInputStream(file);

            // TOD Charset conversion for text/* potentially needed?  Do I need to use InputStreamReader(in, Charset/CharsetDecoder/String charsetName) here in some cases?
            OutputStream os = response.getOutputStream();

            int c;
            while ((c = in.read()) != -1) {
                os.write(c);
            }

            in.close();
            os.close();
        } catch (FileNotFoundException ex) {
            throw new IOException("File " + file + " not found.", ex);
        }
    }

    /**
     * 使用NIO 处理文件下载
     * @param file
     * @param response
     * @throws IOException
     */
    public void nioHandleFile(File file, HttpServletResponse response) throws IOException {
        String filename = file.getName().toLowerCase();
        String contentType = getContentType(filename);
        response.setContentType(contentType);

        long length = file.length();
        response.setHeader(ServletHeader.ContentLength.toString(), Long.toString(length));

//        读文件通道
        FileChannel readFileChannel = FileChannel.open(file.toPath(), StandardOpenOption.READ);
        OutputStream os = response.getOutputStream();
//        写文件通道
        WritableByteChannel writableByteChannel = Channels.newChannel(os);
//        零拷贝将数据从读通道写入写通道
        readFileChannel.transferTo(0, readFileChannel.size(), writableByteChannel);
        try {
            readFileChannel.close();
            writableByteChannel.close();
            os.close();
        } catch (FileNotFoundException ex) {
            throw new IOException("File " + file + " not found.", ex);
        }
    }


    /**
     * 得到Content-Type类型
     *
     * @param filename
     * @return
     */
    private static String getContentType(String filename) {
        if (filename.endsWith(".js")) {
            return "application/javascript";
        } else if (filename.endsWith(".css")) {
            return "text/css";
        } else {
            return new MimetypesFileTypeMap().getContentType(filename);
        }
    }


    /**
     * 处理文件夹,如果文件夹中存在index.html文件,则直接重定向到该文件
     * 如果不存在则显示成目录形式
     *
     * @param dir
     * @param response
     * @throws IOException
     */
    public void handleDir(File dir, HttpServletRequest request, HttpServletResponse response) throws IOException {
        File indexFile = new File(dir.getAbsolutePath() + File.separator + "index.html");
        if (indexFile.exists()) {
//        是否能在dir目录下找到index.html文件,如果存在该文件,那么直接重定向到该文件
            redirect(indexFile, request, response);
        } else {
            StringBuilder builder = new StringBuilder("<!DOCTYPE html PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\"><html> \n"
                    + getWebHeader(request)
                    + "<body class=\"directory\">\n"
                    + "<div id=\"wrapper\">"
                    + getFileNavigation(request) + "\n"
                    + "<ul id=\"files\" class=\"view-tiles\">");

            String parentPath = getParentWebPath(request);
            if (!Strings.isNullOrEmpty(parentPath)) {
                String link = "<li>" +
                        "<a href=\"" + parentPath + "\""
                        + " class=\"icon icon-directory\" title=\"..\">" +
                        "<span class=\"name\">..</span>" +
                        "<span class=\"size\"></span>" +
                        "<span class=\"date\"></span>" +
                        "</a>" +
                        "</li>";
                builder.append(link);
            }
            File[] files = dir.listFiles();
            for (File file : files) {

                String link = getFileOrDirDom(request, file);

                builder.append(link);
            }
            builder.append("</ul>\n"
                    + "</div>\n"
                    + "</body>\n"
                    + "</html>");
            String content = builder.toString();
            response.setHeader(ServletHeader.ContentLength.toString(), Long.toString(content.length()));
            response.setContentType("text/html;charset=utf-8");
            OutputStream os = response.getOutputStream();
            os.write(content.getBytes("utf-8"));
            os.close();
        }
    }

    /**
     * 得到网页中记录的文件路径 相对siteRoot的路径
     *
     * @param file
     * @return
     * @throws IOException
     */
    private String getWebPath(File file, HttpServletRequest request) throws IOException {
//        String webPath = file.getCanonicalPath().replace(siteRoot.getCanonicalPath(), "");

        String pathInfo = request.getPathInfo();

        if (pathInfo.equals("/")) {
//        如果是根目录,直接返回文件名
            return file.getName();
        }


        if (pathInfo.endsWith("/")) {
//            如果pathInfo以"/"结尾,则去掉末尾的"/"
            pathInfo = pathInfo.substring(0, pathInfo.length() - 1);
        }


        int index = pathInfo.lastIndexOf("/");
//        得到当前目录
        String webPath = pathInfo.substring(index + 1);

        if (!webPath.endsWith("/")) {
            webPath += "/";
        }

        webPath = webPath + file.getName();
        return webPath;
    }


    /**
     * 得到父级目录路径
     *
     * @param request
     * @return
     * @throws IOException
     */
    private String getParentWebPath(HttpServletRequest request) throws IOException {
        String parentPath = null;
        String pathInfo = request.getPathInfo();
        if (pathInfo.equals("/")) {
//            说明已经是根目录了
            return null;
        }
        String requestURI = request.getRequestURI();
//        这一步是为了去掉URL中的特殊字符,如: %22 等等
        requestURI = URLDecoder.decode(requestURI, "UTF-8");

        if (requestURI.endsWith("/")) {
//            如果requestURI最后一个字符是"/",则去掉
            requestURI = requestURI.substring(0, requestURI.length() - 1);
        }
        int index = requestURI.lastIndexOf("/");
//        得到父级路径
        parentPath = requestURI.substring(0, index);
        return parentPath;
    }

    /**
     * 得到文件导航
     *
     * @param request
     * @return
     * @throws IOException
     */
    private String getFileNavigation(HttpServletRequest request) throws IOException {
        String servletPath = request.getServletPath();
        String requestURI = request.getRequestURI();
        String contextPath = request.getContextPath();
//        这一步是为了去掉URL中的特殊字符,如: %22 等等
        requestURI = URLDecoder.decode(requestURI, "UTF-8");
        requestURI = requestURI.replace(contextPath, "");
        String navigation = requestURI.replace(servletPath, "");
        StringBuilder sb = new StringBuilder();
        sb.append("<h1>");
        String[] navs = navigation.split("/");
        String targetNav = contextPath + servletPath;

        sb.append("<a href=\"" + targetNav + "\">" + " ~ / " + "</a>");

        for (String nav : navs) {
            if (!Strings.isNullOrEmpty(nav)) {
                targetNav = targetNav + "/" + nav;
                sb.append("<a href=\"" + targetNav + "\">" + nav + " / " + "</a>");
            }
        }
        sb.append("</h1>");
        return sb.toString();
    }

    /**
     * 得到网页标题
     *
     * @param request
     * @return
     */
    public String getWebHeader(HttpServletRequest request) {
        StringBuilder sb = new StringBuilder();
        sb.append("<head>");
        sb.append("<meta charset='utf-8'>\n");
        sb.append("<meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0, maximum-scale=1.0, user-scalable=no\" />");
        String title = "<title>" + "listing directory " + request.getPathInfo() + "</title>";
        sb.append(title);
        sb.append("<link rel=\"stylesheet\" href=\"" + request.getContextPath() + request.getServletPath() + "/css/file.css\"" + " media=\"all\">");
        return sb.toString();
    }

    /**
     * 得到文件/文件夹的Dom片段
     *
     * @param request
     * @param file
     * @return
     */
    public String getFileOrDirDom(HttpServletRequest request, File file) throws IOException {

        String fileName = file.getName();
        String webPath = this.getWebPath(file, request);

//       使用Guava得到文件扩展名,没有扩展名：返回空字符串 只有扩展名：返回“.”之后的字符串，比如“gitIgnore”。
        String extension = Files.getFileExtension(fileName);
//        文件夹样式类名
        String dirClassName = "icon icon-directory";
//        文件样式类名(动态)
        String fileClassName = "icon icon icon-" + extension + " icon-default";
        StringBuilder sb = new StringBuilder();
        sb.append("<li>");

        if (file.isDirectory()) {
            sb.append("<a href=\"" + webPath + "\"" + "class = \"" + dirClassName + "\"" + "title = \"" + fileName + "\">");
        } else {
            sb.append("<a href=\"" + webPath + "\"" + "class = \"" + fileClassName + "\"" + "title = \"" + fileName + "\">");
        }
        sb.append("<span class=\"name\">" + fileName + "</span>");
        sb.append("<span class=\"size\">" + Utils.convertSizeToString(file.length()) + "</span>");
        sb.append("<span class=\"date\">" + Utils.convertTimeToString(file.lastModified()) + "</span>");
        sb.append("</a></li>");

        String dom = sb.toString();
        return dom;
    }

    /**
     * 重定向
     *
     * @param file
     * @param response
     * @throws IOException
     */
    private void redirect(File file, HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setStatus(302);
        response.setHeader(ServletHeader.Location.toString(), getWebPath(file, request));
    }

    /**
     * 处理异常
     *
     * @param request
     * @param response
     * @param message
     * @param ex
     * @throws IOException
     */
    public void handleException(HttpServletRequest request, HttpServletResponse response, String message, Exception ex) throws IOException {
        try {
            // If earlier code has already set a more precise HTTP error then
            // leave that, make it a generic 500 only if its still the default 200
            if (response.getStatus() == 200) {
                response.setStatus(500);
            }
            PrintWriter pw;
            response.setContentType("text/html;charset=utf-8");
            pw = response.getWriter();

            pw.println("<html><head><title>Server Error</title><meta charset='utf-8'></head><body><h1>Server Error</h1><p>");
            pw.println(message);
            pw.println("</p><pre>");
            if (ex != null) {
                ex.printStackTrace(pw);
            }
            pw.println("</pre></body></html>");
        } catch (IllegalStateException e) {
            // Oh, too late to getPrintWriter()? Well... log it but otherwise
            // ignore it; at least the setStatusCode() worked if we're here.
            System.out.println("Can't send stack trace to client because OutputStream was already open for something else: " + e.toString()); // TODO Real logging...
            System.out.println("Stack trace of where the IllegalStateException occured:");
            e.printStackTrace();
            return;
        }
    }


    public void handleError(HttpServletRequest request, HttpServletResponse response, String message) throws IOException {
        this.handleException(request, response, message, null);
    }

}
