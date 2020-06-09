layui.use(['tree', 'util', 'upload', 'form'], function () {
    var tree = layui.tree
        , layer = layui.layer
        , util = layui.util
        , upload = layui.upload
        , form = layui.form

    var env = 'dev';
    var version = 'saas'
    var dataDir = '';
    var groovyDir = '';

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
            var serviceDTO = tree.getChecked('demoId'); //获取选中节点的数据
            if (serviceDTO.length == 0) {
                layer.msg('请先选择需要导出的数据', {
                    time: 2000, //2s后自动关闭
                });
                return true;
            }

            var loading = layer.msg("打包中，请等待。。。", {
                icon: 16
                , shade: 0.01
                , time: 6 * 60 * 1000
            })
            var req = new XMLHttpRequest();
            req.open('POST', 'sys/export-datas/export?env=' + env, true);
            req.responseType = 'blob';
            req.setRequestHeader('Content-Type', 'application/json');
            req.onload = function () {
                var data = req.response;
                var a = document.createElement('a');
                var blob = new Blob([data]);
                var blobUrl = window.URL.createObjectURL(blob);
                a.style.display = 'none';
                a.download = 'ExportData' + new Date().getTime() + '.zip';
                a.href = blobUrl;
                a.click();
                layer.close(loading)
            };
            req.send(JSON.stringify(serviceDTO));
        },
        virtualData: function () {
            var serviceDTO = tree.getChecked('demoId'); //获取选中节点的数据
            if (serviceDTO.length == 0) {
                layer.msg('请先选择需要导出的数据', {
                    time: 2000, //2s后自动关闭
                });
                return true;
            }

            var loading = layer.msg("打包中，请等待。。。", {
                icon: 16
                , shade: 0.01
                , time: 6 * 60 * 1000
            })
            var req = new XMLHttpRequest();
            req.open('POST', 'sys/export-datas/virtual-export?env=' + env, true);
            req.responseType = 'blob';
            req.setRequestHeader('Content-Type', 'application/json');
            req.onload = function () {
                var data = req.response;
                var a = document.createElement('a');
                var blob = new Blob([data]);
                var blobUrl = window.URL.createObjectURL(blob);
                a.style.display = 'none';
                a.download = 'ExportData' + new Date().getTime() + '.zip';
                a.href = blobUrl;
                a.click();
                layer.close(loading)
            };
            req.send(JSON.stringify(serviceDTO));
        }
    });
});

