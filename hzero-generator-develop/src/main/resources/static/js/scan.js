layui.use(['util', 'form', 'tree', 'upload'], function () {
    var layer = layui.layer
        , util = layui.util
        , tree = layui.tree
        , upload = layui.upload;

    var version = 'saas';
    var level = 'organization';
    var resourceDir = 'hzero-front';
    var cliVersion = 0;

    $(document).ready(function () {
        layui.use('form', function () {
            var form = layui.form;
            form.render();
            form.on('select(env)', function (data) {
                env = data.value;
            });
            form.on('select(cliVersion)', function (data) {
                cliVersion = data.value;
            });
            form.on('select(level)', function (data) {
                level = data.value;
                if (level === 'site') {
                    version = 'saas';
                } else {
                    version = 'op';
                }
                var loading = layer.load();
                $.ajax({
                    url: 'sys/ui-scans/router?level=' + level,
                    type: 'get',
                    success: function (res) {
                        layer.close(loading);
                        //实例调用
                        tree.render({
                            elem: '#routerData'
                            , showCheckbox: true
                            , id: 'routerTree'
                            , data: res
                        })
                    }
                })
            });
        })
    });

    window.onload = function (ev) {
        var loading = layer.load();
        $.ajax({
            url: 'sys/ui-scans/router?level=' + level,
            type: 'get',
            success: function (res) {
                layer.close(loading);
                //实例调用
                tree.render({
                    elem: '#routerData'
                    , showCheckbox: true
                    , id: 'routerTree'
                    , data: res
                })
            }
        })
    }

    //按钮事件
    util.event('lay-click', {
        scanButton: function (obj) {
            resourceDir = $('#resourceDir').val();
            var data = tree.getChecked('routerTree');
            var loading = layer.load();
            $.ajax({
                url: 'sys/ui-scans/button?resourceDir=' + encodeURI(resourceDir) + '&version=' + cliVersion,
                type: 'post',
                data: JSON.stringify(data),
                contentType: "application/json",
                success: function (res) {
                    if (res) {
                        layer.close(loading);
                        layer.msg("扫描成功！");
                    } else {
                        layer.close(loading);
                        layer.msg("扫描失败！未选中模块或者工程路径不正确");
                    }
                }
            })
        },
        scanLov: function (obj) {
            resourceDir = $('#resourceDir').val();
            var data = tree.getChecked('routerTree');
            var loading = layer.load();
            $.ajax({
                url: 'sys/ui-scans/lov?resourceDir=' + encodeURI(resourceDir) + '&version=' + cliVersion,
                type: 'post',
                data: JSON.stringify(data),
                contentType: "application/json",
                success: function (res) {
                    if (res) {
                        layer.close(loading);
                        layer.msg("扫描成功！");
                    } else {
                        layer.close(loading);
                        layer.msg("扫描失败！未选中模块或者工程路径不正确");
                    }
                }
            })
        },
        scanPrompt: function (obj) {
            resourceDir = $('#resourceDir').val();
            var data = tree.getChecked('routerTree');
            var loading = layer.load();
            $.ajax({
                url: 'sys/ui-scans/prompt?resourceDir=' + encodeURI(resourceDir) + '&version=' + cliVersion,
                type: 'post',
                data: JSON.stringify(data),
                contentType: "application/json",
                success: function (res) {
                    if (res) {
                        layer.close(loading);
                        layer.msg("扫描成功！");
                    } else {
                        layer.close(loading);
                        layer.msg("扫描失败！未选中模块或者工程路径不正确");
                    }
                }
            })
        },
        scanApi: function (obj) {
            resourceDir = $('#resourceDir').val();
            var data = tree.getChecked('routerTree');
            var loading = layer.load();
            $.ajax({
                url: 'sys/ui-scans/api?resourceDir=' + encodeURI(resourceDir) + '&version=' + cliVersion,
                type: 'post',
                data: JSON.stringify(data),
                contentType: "application/json",
                success: function (res) {
                    if (res) {
                        layer.close(loading);
                        layer.msg("扫描成功！");
                    } else {
                        layer.close(loading);
                        layer.msg("扫描失败！未选中模块或者工程路径不正确");
                    }
                }
            })
        },
        exportPermission: function (obj) {
            exportData('sys/ui-scans/export/permission');
        },
        exportLov: function (obj) {
            exportData('sys/ui-scans/export/lov');
        },
        exportPrompt: function (obj) {
            exportData('sys/ui-scans/export/prompt');
        },
        exportNewPrompt: function (obj) {
            exportData('sys/ui-scans/export/new/prompt');
        },
        exportAllNewPrompt: function (obj) {
            exportData('sys/ui-scans/export/all/new/prompt');
        },
        refreshPermissionSet: function (obj) {
            var loading = layer.msg("正在刷新权限集，请等待。。。", {
                icon: 16
                , shade: 0.01
                , time: 6 * 60 * 1000
            });
            var data = tree.getChecked('routerTree');
            $.ajax({
                url: 'sys/ui-scans/refresh',
                type: 'post',
                data: JSON.stringify(data),
                contentType: "application/json",
                success: function (res) {
                    if (res) {
                        layer.close(loading);
                    }
                }
            })
        }
    });

    function exportData(url) {
        var loading = layer.msg("打包中，请等待。。。", {
            icon: 16
            , shade: 0.01
            , time: 6 * 60 * 1000
        });
        var data = tree.getChecked('routerTree');
        var req = new XMLHttpRequest();
        req.open('POST', url + '?version=' + version, true);
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
            layer.close(loading);
        };
        req.send(JSON.stringify(data));
    }

});

