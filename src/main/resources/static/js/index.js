$(function () {
    $("#publishBtn").click(publish);
});

function publish() {
    $("#publishModal").modal("hide");//发布框隐藏

    // 获取标题和内容
    var title = $("#recipient-name").val();
    var content = $("#message-text").val();
    // 发送异步请求
    $.post(
        CONTEXT_PATH + "/discuss/add",
        {"title": title, "content": content},
        function (data) {
            data = $.parseJSON(data);
            //在提示框中显示返回的消息
            $("#hintBody").text(data.msg);
            $("#hintModal").modal("show");//提示框显示
            setTimeout(function () {//提示框两秒后隐藏
                $("#hintModal").modal("hide");
                //刷新页面(显示新帖子)
                if (data.code == 0) {
                    window.location.reload();
                }
            }, 2000);
        }
    );


}