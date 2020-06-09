var vm = new Vue({
    el:'#app',
    data:{
        array: [],
        selected: ''
    },
    methods: {
        upgrade: function () {
            if(isBlank(vm.selected)){
                alert("升级版本不能为空");
                return;
            }
            $.ajax({
                type: "POST",
                url: "/sys/service-upgrade/upgrade?version=" + vm.selected,
                contentType: "application/json",
                async: false,
                success: function(){
                    alert("升级完成：" + vm.selected);
                },
                error: function (e) {
                    alert(e.getData());
                }
            });
        }
    }
});

$(document).ready(function(){
    $.ajax({
        type: "GET",
        url: "/sys/service-upgrade/list",
        contentType: "application/json",
        success: function(data){
            vm.array = data;
        },
        error: function (e) {
            alert(e.getData());
        }
    });
});