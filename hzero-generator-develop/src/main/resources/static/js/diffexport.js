layui.use(['tree', 'util', 'upload', 'form'], function () {
    var tree = layui.tree
        , layer = layui.layer
        , util = layui.util
        , upload = layui.upload
        , form = layui.form

    var env = 'dev';
    var version = 'saas'
    var dataDir = '';

    getServiceList = function () {
        $.ajax({
            type: "GET",
            url: "sys/export-datas/services?env=" + env + "&version=" + version,
            contentType: "application/json",
            success: function (data) {
                //实例调用
                tree.render({
                    elem: '#data'
                    , showCheckbox: true
                    , id: 'demoId'
                    , data: data
                })
            }
        });
    };
    window.onload = function () {
        getServiceList()
    };
    $(document).ready(function () {
        layui.use('form', function () {
            var form = layui.form;
            form.render();
            form.on('select(env)', function (data) {
                env = data.value;
                getServiceList();
            });
            form.on('select(version)', function (data) {
                version = data.value;
                getServiceList();
            });
        })
    });
    //按钮事件
    util.event('lay-demo', {
        exportInitData: function () {
            const serviceDTO = tree.getChecked('demoId'); //获取选中节点的数据
            dataDir = document.getElementById("initData").value;
            if (dataDir.length === 0) {
                layer.msg('请先选择需要导出的数据', {
                    time: 2000, //2s后自动关闭
                });
                return true;
            }
            if (serviceDTO.length === 0) {
                layer.msg('请先选择需要导出的数据', {
                    time: 2000, //2s后自动关闭
                });
                return true;
            }

            const loading = layer.msg("打包中，请等待。。。", {
                icon: 16
                , shade: 0.01
                , time: 6 * 60 * 1000
            });
            const req = new XMLHttpRequest();
            req.open('POST', 'sys/export-datas/diff-export?env=' + env + '&dir=' + encodeURI(dataDir), true);
            req.responseType = 'blob';
            req.setRequestHeader('Content-Type', 'application/json');
            req.onload = function () {
                const data = req.response;
                const a = document.createElement('a');
                const blob = new Blob([data]);
                const blobUrl = window.URL.createObjectURL(blob);
                a.style.display = 'none';
                a.download = 'DiffExportData' + new Date().getTime() + '.zip';
                a.href = blobUrl;
                a.click();
                layer.close(loading)
            };
            req.send(JSON.stringify(serviceDTO));
        }
    });
});

