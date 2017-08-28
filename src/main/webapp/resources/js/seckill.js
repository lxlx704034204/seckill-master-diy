// 存放主要交换逻辑js代码
// javascript 模块化
var seckill = {
	// 封装秒杀相关ajax的url
	URL : { 		//所有后端的url 都规范集中放在这里 方便修改
		basePath : function() {
			return $('#basePath').val();
		},
		now : function() {
			return seckill.URL.basePath() + 'seckill/time/now';
		},
		exposer : function(seckillId) {
			return seckill.URL.basePath() + 'seckill/' + seckillId + '/exposer';
		},
		execution : function(seckillId, md5) {
			return seckill.URL.basePath() + 'seckill/' + seckillId + '/' + md5 + '/execution';
		}
	},
	// 处理秒杀逻辑
	handleSeckill : function(seckillId, node) {
		// 获取秒杀地址，控制显示逻辑，执行秒杀
		node.hide().html('<button class="btn btn-primary btn-lg" id="killBtn">开始秒杀</button>');
		console.log('exposerUrl=' + seckill.URL.exposer(seckillId));//TODO
		$.post(seckill.URL.exposer(seckillId), {}, function(result) {
			// 在回调函数中，执行交互流程
			if (result && result['success']) {
				var exposer = result['data'];
				if (exposer['exposed']) {// if 开启秒杀
					var md5 = exposer['md5'];
					var killUrl = seckill.URL.execution(seckillId, md5); //秒杀url
					console.log('killUrl=' + killUrl);//TODO
					$('#killBtn').one('click', function() { // .click: 永远绑定；.one:只绑定一次
						// 执行秒杀请求 ：
						$(this).addClass('disabled');			// 1.先禁用按钮 
						$.post(killUrl, {}, function(result) {	// 2.发送秒杀请求
							if (result && result['success']) {
								var killResult = result['data'];
								var state = killResult['state'];
								var stateInfo = killResult['stateInfo'];
								// 3.显示秒杀结果
								node.html('<span class="label label-success">' + stateInfo + '</span>');
							}
						});
					});
					node.show();
				} else { // if 未开启秒杀 (eg: 客户端time 与 服务器time 不一致) 
					var now = exposer['now'];
					var start = exposer['start'];
					var end = exposer['end'];
					// 重新计算计时逻辑
					seckill.countdown(seckillId, now, start, end);
				}
			} else {
				console.log('result=' + result);
			}
		});
	},
	// 验证手机号
	validatePhone : function(phone) {
		if (phone && phone.length == 11 && !isNaN(phone)) {// isNaN(phone): 数字验证
			return true;
		} else {
			return false;
		}
	},
	// 倒计时
	countdown : function(seckillId, nowTime, startTime, endTime) {
		var seckillBox = $('#seckillBox');  //取到 展示的 节点id
		if (nowTime > endTime) { // 秒杀结束 
			seckillBox.html('秒杀结束!');
		} else if (nowTime < startTime) { // 秒杀未开始，计时事件绑定
			var killTime = new Date(startTime + 1000);// 加1秒，防止计时 偏移(不加也可以)
			
			//通过在detail.jsp中引入的 ‘jquery.countdown.min.js’插件，利用.countdown()事件绑定，展示计时
			seckillBox.countdown(killTime, function(event) { //计时服务
				var format = event.strftime('秒杀倒计时：%D天 %H时 %M分 %S秒'); // 设置时间格式
				seckillBox.html(format); 
			}).on('finish.countdown', function() { //时间完成后回调事件(当前显示事件用完的时候的 事件 逻辑)
				// 处理秒杀逻辑： 获取秒杀地址，控制显示逻辑，执行秒杀
				seckill.handleSeckill(seckillId, seckillBox);
			});
		} else {
			// 秒杀开始
			seckill.handleSeckill(seckillId ,seckillBox);
		}
	},
	
	// 详情页秒杀逻辑
	detail : {
		// 详情页初始化
		init : function(params) {  //接收前端 传入的params
			// 用户手机验证和登录，计时交互
			// 规划我们的交互流程
			// 在cookie中查找手机号
			var killPhone = $.cookie('killPhone');
			var startTime = params['startTime'];
			var endTime = params['endTime'];
			var seckillId = params['seckillId'];
			// 验证手机号 
			if (!seckill.validatePhone(killPhone)) { // if phone为null或者 格式不正确 ，则弹出 警告的dialog
				//选中“killPhoneModal”节点
				var killPhoneModal = $('#killPhoneModal');// detaill.jsp中的 <div id="killPhoneModal" ... >
				killPhoneModal.modal({  //显示dialog
					show : true,		// 显示弹出层
					backdrop : 'static',// 禁止位置关闭：点击其他区域 不让dialog 关闭！
					keyboard : false	// 关闭 键盘事件
				// 关闭键盘事件
				})
				
				//id = 'killPhoneBtn' 的button的 onClinck
				$('#killPhoneBtn').click(function() {
					var inputPhone = $('#killphoneKey').val();
					console.log('inputPhone='+inputPhone);//TODO
					if (seckill.validatePhone(inputPhone)) { //点击这个buttond 时候 验证 phone输入框的内容
						// ★★★ 把电话写入cookie：网页输入‘http://localhost:8080/seckill/seckill/1001/detail’---> F12-Resources-Cookies-localhost 即可看到
						$.cookie('killPhone', inputPhone, {
							expires : 7,		//控制cookie的有效期：7天
							path : '/seckill'	//约定'/seckill'这个路劲下才有效(设置作用域)
						});
						// 刷新页面：重新进入上面的 init()方法
						window.location.reload();
					} else { //if phone不正确 则弹出提示
						$('#killphoneMessage').hide().html('<label class="label label-danger">手机号错误!</label>').show(300);
					}
				});
			}
			// 能走到这里，证明 已经登录了
			
			// 计时交互
			var startTime = params['startTime'];
			var endTime = params['endTime'];
			var seckillId = params['seckillId'];
			$.get(seckill.URL.now(), 	//diy封装url( 直接把url路径写在这里 不是很规范！)
					{}, 				//参数
					function(result) {	//回调函数
						if (result && result['success']) { // if result不为null，且 包含有 'success'字串
							var nowTime = result['data'];
							// 时间判断，计时交互
							seckill.countdown(seckillId, nowTime, startTime, endTime);
							
							
						} else {
							console.log(result['reult:'] + result);
						}
					});
		}
	}
}
