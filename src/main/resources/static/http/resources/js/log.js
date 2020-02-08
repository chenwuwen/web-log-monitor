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
 * 清空日志,同时隐藏置顶/置底按钮,重置状态
 */
function clearLog() {
    $("#web_log .log_container").html("")
    $(".page .up_page").css('visibility', 'hidden');
    $(".page .down_page").css('visibility', 'hidden');
    handleTop = 0;
}


/**
 * 页顶
 */
function topPage() {
    $('#web_log .log_container').scrollTop(0);
}


/**
 * 页尾
 */
function downPage() {
    $('#web_log .log_container').scrollTop($('#web_log .log_container')[0].scrollHeight);
}


/**
 * 滚动条位置
 * 滚动条在最底部,返回true
 * 否则返回false
 * 这是静态形式的判断
 * @returns {boolean}
 * @constructor
 */
function scrollPosition() {
    // 容器内容高度
    let bodyHeight = $('#web_log .log_container')[0].scrollHeight;
    // console.log("滚动条所在容器的内容高度:" + height);
    let currentScrollPosition = $('#web_log .log_container').scrollTop();
    // console.log("滚动条位置:" + currentScrollPosition);
    let containerHeight = $('#web_log .log_container')[0].offsetHeight
    // console.log("可视窗口高度:" + $('#web_log .log_container')[0].offsetHeight);
    return bodyHeight - currentScrollPosition == containerHeight
}


/**
 * 当添加了新元素后,重新设置滚动条位置
 * @returns {boolean}
 * @constructor
 */
function setScrollPosition(newElement) {
    // 容器内容高度
    let bodyHeight = $('#web_log .log_container')[0].scrollHeight;
    console.log("滚动条所在容器的内容高度(如果容器中的内容未填满容器,则它的内容高度即为容器高度):" + bodyHeight);
    let currentScrollPosition = $('#web_log .log_container').scrollTop();
    console.log("滚动条位置:" + currentScrollPosition);
    let containerHeight = $('#web_log .log_container')[0].offsetHeight
    console.log("可视窗口高度:" + $('#web_log .log_container')[0].offsetHeight);
    let newElementHeight = newElement.offsetHeight
    console.log("新加元素高度:" + newElementHeight);

    // 当容器内容未填满容器时,设置滚动条在最下方,在这里handleTop是置顶标识,为0表示是初始时(当容器内没有内容,或者内容未填满容器)的置顶
    // 因此如果在某一时刻,如果内容填满了容器,由于设置了scrollTop(bodyHeight)函数,那么currentScrollPosition就肯定不是0了,也就不会再走这个逻辑了
    if (currentScrollPosition == 0 && handleTop == 0) {
        // 说明此时还没有滚动条
        $('#web_log .log_container').scrollTop(bodyHeight)
        return;
    }

    // 根据新添加的元素来判断自动滚动滚动条到最底部
    if (bodyHeight - currentScrollPosition - newElementHeight == containerHeight) {
        $('#web_log .log_container').scrollTop(bodyHeight)
        return;
    }

    // 排除了上面的情况后,滚动条位置不动

}


/**
 * 滚动条监听事件
 * $('#web_log .log_container') 中的滚动条
 *
 * 触发这个监听的条件是下面currentScrollPosition发生变化，而currentScrollPosition发生变化的条件是滚动条位置发生变化
 */
$('#web_log .log_container')[0].addEventListener("scroll", function () {
    console.log("监听到滚动条滚动")
    let containerHeight = $('#web_log .log_container')[0].offsetHeight
    console.log("可视窗口高度:" + $('#web_log .log_container')[0].offsetHeight);
    console.log("可视窗口高度:" + $('#web_log .log_container')[0].clientHeight);
    let bodyHeight = $('#web_log .log_container')[0].scrollHeight;
    console.log("滚动条所在容器的内容高度(如果容器中的内容未填满容器,则它的内容高度即为容器高度):" + bodyHeight);
    let currentScrollPosition = $('#web_log .log_container').scrollTop();
    console.log("滚动条位置:" + currentScrollPosition);

    if (currentScrollPosition == 0) {
        // 说明滚动条在最顶部,且一定是手动使滚动条滚动到顶部,但是要分两种情况,一种是手动置顶,另外一种是清空了容器,如果是清空

        if (bodyHeight == containerHeight) {
            // 这是清空容器的情况,然而当滚动条在置顶状态时被清空,则不会触发这个逻辑(这个监听也不会触发,因为置顶时currentScrollPosition为0,清空时currentScrollPosition还为0,因此监听不会触发),
            // 因此需要在清空操作时,也需要设置一次状态,所以这个逻辑可以什么都不做
            handleTop = 0;
        } else {
            handleTop = 1;
            $(".page .up_page").css('visibility', 'hidden');
            $(".page .down_page").css('visibility', 'visible');
        }
        console.log("隐藏上箭头,显示下箭头")
    } else if (bodyHeight - currentScrollPosition > containerHeight) {
        // 说明滚动条不在最顶部,也不在最底部
        $(".page .up_page").css('visibility', 'visible');
        $(".page .down_page").css('visibility', 'visible');
        console.log("显示上箭头,显示下箭头")
    } else {
        // 说明滚动条在最底部 bodyHeight - currentScrollPosition == containerHeight
        $(".page .up_page").css('visibility', 'visible');
        $(".page .down_page").css('visibility', 'hidden');
        console.log("显示上箭头,隐藏下箭头")
    }
})