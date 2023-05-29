$(function () {
   $("#uploadForm").submit(upload);
});

function upload(){
    $.ajax({
        // 存储区域
        url: "http://upload-cn-east-2.qiniup.com",
        method: "post",
        // 拒绝将表单内容转换成字符串
        processData: false,
        // 不然Jquery设置上传类型
        contentType: false,
        // 获取表单内容,得到一个表单对象
        data:new  FormData($("#uploadForm")[0]),
        // 上传成功后的回调函数
        success: function (data) {
            if (data && data.code == 0){
                // 更新头像访问路径
                $.post(
                    CONTEXT_PATH + "/user/header/url",
                    // 可新建id替换下面方法
                    {"fileName":$("input[name='key']").val()},
                    function (data){
                        data = $.parseJSON(data);
                        if (data.code == 0){
                            window.location.reload();
                        }else {
                            alert(data.msg);
                        }
                    }
                );
            }else {
                alert("上传失败");
            }
        }
    })

    return false;
}