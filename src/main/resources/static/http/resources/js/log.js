/**
 * 下载日志
 * https://www.cnblogs.com/boluoboluo/p/6973283.html
 * https://blog.csdn.net/yangchun1213/article/details/7605497
 */
function downloadLog() {
    // 日志内容
    // let body = $("#web_log").children("div").html("");
    // 从Div中提取文字内容
    let body = document.getElementById("web_log").innerText;
    // 定义下载的文件名
    let fileName = new Date().dateFormat("yyyy-MM-dd hh:mm:ss") + ".log";

    download(fileName, body);
}


/**
 * 清空日志
 */
function clearLog() {
    $("#web_log .log_container").html("")
}