股票撮合交易

委托挂单：url：http://192.168.2.100:7073/tran/add
	参数：tran对象  id唯一标识  name:股票名称  userid:用户id  username:用户名称  salary:单价   num:数量  status: 订单状态  put:买入or卖出
	          dealnum:成交手数    uncompleted:未成交手数    time:生成时间
	请求方式 post请求
	返回值JsonRes对象 

	Integer code 状态码
	String  msg  状态信息
	Object data  数据
	String  token token

撤单请求：url：http://192.168.2.100:7073/tran/revoke
	参数：tran对象  id唯一标识  name:股票名称  userid:用户id  username:用户名称  salary:单价   num:数量  status: 订单状态  put:买入or卖出
	          dealnum:成交手数    uncompleted:未成交手数    time:生成时间
	请求方式 post请求
	返回值JsonRes对象 

	Integer code 状态码
	String  msg  状态信息
	Object data  数据
	String  token token
