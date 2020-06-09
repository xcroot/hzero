var vm = new Vue({
	el:'#app',
	data:{
        info: {},
        sourceDBList: [],
        targetDBList: [],
        updateDBList: []
	},
	methods: {
		changeSourceEnv: function(){
			$.ajax({
				type: "GET",
			    url: "sys/db/database?env="+vm.info.sourceEnv,
                contentType: "application/json",
			    success: function(data){
			    	vm.sourceDBList = data.database;
				}
			});
		},
		changeTargetEnv: function(){
			$.ajax({
				type: "GET",
			    url: "sys/db/database?env="+vm.info.targetEnv,
                contentType: "application/json",
			    success: function(data){
			    	vm.targetDBList = data.database;
				}
			});
		},
		changeUpdateEnv: function(){
			$.ajax({
				type: "GET",
			    url: "sys/db/database?env="+vm.info.updateEnv,
                contentType: "application/json",
			    success: function(data){
			    	vm.updateDBList = data.database;
				}
			});
		},
		compare: function() {
			if(vm.validator()){
                return ;
            }
			location.href = "sys/db/diff?sourceEnv="+vm.info.sourceEnv+"&sourceDB="+vm.info.sourceDB+"&targetEnv="+vm.info.targetEnv+"&targetDB="+vm.info.targetDB;
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
		dbUpdate: function() {
			if(vm.updateValidator()){
                return ;
            }
			var formData = new FormData($("#uploadForm")[0]);  
			$.ajax({
				type: "POST",
			    url: "sys/db/update",
			    cache: false,
			    data: formData,
			    processData:false,
			    contentType:false,
		        success: function (data) {  
		        	alert("更新数据库成功");
		        },  
		        error: function (data) {  
		        	alert("更新数据库失败");
		        }  
			});
		},
		validator: function () {
            if(isBlank(vm.info.sourceEnv)){
                alert("来源环境不能为空");
                return true;
            }
            if(isBlank(vm.info.sourceDB)){
                alert("来源数据库不能为空");
                return true;
            }
            if(isBlank(vm.info.targetEnv)){
                alert("目标环境不能为空");
                return true;
            }
            if(isBlank(vm.info.targetDB)){
                alert("目标数据库不能为空");
                return true;
            }
        },
        updateValidator: function () {
            if(isBlank(vm.info.updateEnv)){
                alert("更新环境不能为空");
                return true;
            }
            if(isBlank(vm.info.updateDB)){
                alert("更新数据库不能为空");
                return true;
            }
            if(isBlank(vm.info.file)){
                alert("更新文件不能为空");
                return true;
            }
        }
	}
});

