package cn.kanyun.log.web;

import cn.kanyun.log.common.Constant;
import cn.kanyun.log.common.WebFileHandler;
import com.google.common.base.Strings;
import com.google.common.io.Resources;
import lombok.extern.slf4j.Slf4j;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Properties;

/**
 * 简易的web文件目录,可以下载文件
 *
 * @author KANYUN
 */
@Slf4j
@WebServlet(name = "file", urlPatterns = {"/web/log/file", "/web/log/file/*"}, asyncSupported = true)
public class FileServlet extends HttpServlet {

    private static String rootPath;

    File siteRoot;

    @Override
    public void init() throws ServletException {
        try {
            URL url = Resources.getResource(Constant.CONFIG_FILE_PATH);
            Properties properties = new Properties();
            properties.load(url.openStream());
            String path = properties.get(Constant.CONFIG_LOG_FILE_PATH).toString();
            if (!Strings.isNullOrEmpty(path)) {
                rootPath = path;
                siteRoot = new File(rootPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        URI uri;
        WebFileHandler handler = new WebFileHandler(siteRoot);

        if (siteRoot == null) {
            handler.handleError(req, resp, "未设置日志文件存储目录！");
            return;
        }

        try {
            String pathInfo = req.getPathInfo();
//            如果pathInfo ="/"  说明访问的是根目录,即rootPath
            if (Strings.isNullOrEmpty(pathInfo)) {
//                说明访问的 web/log/file ,重定向到 web/log/file/ 需要注意路径问题,使用相对路径或者绝对路径(如果使用绝对路径需要注意得加上contextPath)
                resp.sendRedirect("file/");
                return;
            }
            uri = new URI(pathInfo);
        } catch (URISyntaxException e) {
            // 400 请求时错误的
            resp.setStatus(400);
            handler.handleException(req, resp, "URISyntaxException", e);
            return;
        }
        // This wouldn't handle %20-like encoding/decoding:  String uri = req.getURI();
        File file = new File(siteRoot, uri.getPath());

        if (!file.exists()) {
            // 404 如果路径[目录或文件]不存在就是404
            resp.setStatus(404);
            handler.handleError(req, resp, "File Not Found for request URI '" + uri + "' ");
            return;
        }
        if (!file.canRead()) {
            // 403 如果文件不可读,那么返回403 意味着权限不足
            resp.setStatus(403);
            handler.handleError(req, resp, "Local file matched by reqed URI is not readable");
            // SECURITY Note: It's better not to show the full local path to the client, let's just log it on the server to help debugging
            return;
        }

        // TODO Security: Check that no req can read "outside" (above) the siteRoot... using getCanonicalPath() ?
        // (E.g. of the form http://localhost/../java/ch/vorburger/simplewebserver/reqHandlerStaticSite.java if siteroot is src/htdocs-test)

        // TODO Implement modified-since stuff handling... something like: always send Last-Modified in resp, and if req has a If-Modified-Since then check file with file.lastModified() and answer with code 304 if match (and Expires? Also not sure how exactly to handle If-Unmodified-Since req header)

        if (file.isFile()) {
//            如果是文件,就处理文件(下载、浏览等)
            handler.handleFile(file, resp);
        } else if (file.isDirectory()) {
//            如果是文件夹.就返回文件夹中的内容
            handler.handleDir(file, req, resp);
        } else {
            handler.handleError(req, resp, "Content not file, not directory. We don't know how to handle it.");
        }
    }

}


