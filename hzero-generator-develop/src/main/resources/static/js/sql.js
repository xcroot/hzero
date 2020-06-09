var vm = new Vue({
	el:'#app',
	data:{
        info: {}
	},
	methods: {
		executeSQL: function() {
			if(vm.validator()){
                return ;
            }
			$.ajax({
				type: "POST",
			    url: "sys/generator/execute/sql?sql="+vm.info.sql,
                contentType: "application/json",
			    data: JSON.stringify(vm.info),
			    success: function(r){
			    	if(r.code==500){
			    		alert(r.msg);
			    	} else {
			    		alert("SQL执行成功，请执行后续操作!!!");
			    	}
				}
			});
		},
		validator: function () {
			if(isBlank(vm.info.sql)){
                alert("执行SQL不能为空");
                return true;
            }
        }
	}
});

