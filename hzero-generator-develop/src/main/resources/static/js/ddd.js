$(function () {
    $("#jqGrid").jqGrid({
        url: 'sys/generator/list',
        datatype: "json",
        colModel: [	
			{ label: '表名', name: 'tableName', width: 100, key: true },
			{ label: 'Engine', name: 'engine', width: 70},
			{ label: '表备注', name: 'tableComment', width: 100 },
			{ label: '创建时间', name: 'createTime', width: 100 }
        ],
		viewrecords: true,
        height: 325,
        rowNum: 10,
		rowList : [10,30,50,100,200],
        rownumbers: true, 
        rownumWidth: 25, 
        autowidth:true,
        multiselect: true,
        pager: "#jqGridPager",
        jsonReader : {
            root: "page.list",
            page: "page.currPage",
            total: "page.totalPage",
            records: "page.totalCount"
        },
        prmNames : {
            page:"page", 
            rows:"limit", 
            order: "order"
        },
        gridComplete:function(){
        	//隐藏grid底部滚动条
        	$("#jqGrid").closest(".ui-jqgrid-bdiv").css({ "overflow-x" : "hidden" }); 
        }
    });
});

var vm = new Vue({
	el:'#app',
	data:{
		q:{
			tableName: null
		},
        info: {}
	},
	methods: {
		query: function () {
			$("#jqGrid").jqGrid('setGridParam',{ 
                postData:{'tableName': vm.q.tableName},
                page:1 
            }).trigger("reloadGrid");
		},
		generator: function() {
			if(vm.validator()){
                return ;
            }
			var tableNames = getSelectedRows();
			if(tableNames == null){
				return ;
			}
			location.href = "sys/generator/ddd/code?tablePrefix="+vm.info.tablePrefix+"&pkg="+vm.info.pkg+"&author="+vm.info.author+"&level="+vm.info.level+"&tables=" + JSON.stringify(tableNames);
			/*
			$.ajax({
				type: "POST",
			    url: "sys/generator/code",
                contentType: "application/json",
			    data: JSON.stringify(vm.info),
			    success: function(response, status, request){
			    	
				}
			});
			*/
		},
		validator: function () {
			if(isBlank(vm.info.tablePrefix)){
                alert("表过滤前缀不能为空");
                return true;
            }
            if(isBlank(vm.info.pkg)){
                alert("包前缀不能为空");
                return true;
            }
            if(isBlank(vm.info.author)){
                alert("代码作者不能为空");
                return true;
            }
            if(isBlank(vm.info.level)){
                alert("层级不能为空");
                return true;
            }
        }
	}
});

