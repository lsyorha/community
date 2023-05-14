// 用于点赞功能
// 代码中的btn是点赞按钮，entityType是点赞的类型，entityId是点赞的id，entityUserId是点赞的用户id
// 异步刷新是通过代码中的$.post()实现的，$.post()是jquery中的ajax请求，第一个参数是请求的路径，第二个参数是请求的参数，第三个参数是请求成功后的回调函数
function like(btn, entityType, entityId, entityUserId,postId) {

    $.post(
        CONTEXT_PATH + "/like",
        {"entityType":entityType,"entityId":entityId,"entityUserId":entityUserId,"postId":postId},
        function(data) {
            data = $.parseJSON(data);
            if(data.code == 0) {
                $(btn).children("b").text(data.likeStatus==1?'已赞':"赞");
                $(btn).children("i").text(data.likeCount);
            } else {
                alert(data.msg);
            }
        }
    );
}