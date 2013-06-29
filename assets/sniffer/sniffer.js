
var sniffer = {

	/* 是否在android上运行 */
	onAndroid: true,

	/* 是否打印到native */
	printNative: false,

	rawUrl: "",
	
	siteName: "",

	setRawUrl: function(url) {
		sniffer.rawUrl = url;
		onStart();
	},

	/* 上报正确的结果 */
	success: function(result) {

		var src = '';
		if(sniffer.isArray(result)) {
			for(i = 0; i < result.length; ++i) {
				src += result[i] + ' ';
			}
		} else {
			src = result;
		}
		if(sniffer.onAndroid) {
			window.Sniffer.callback(sniffer.rawUrl, src);
		}
	},
	
	/* 上报错误 */
	fail: function(message) {
		if(typeof message == 'string') {
			console.log(sniffer.siteName + ' ' + message);
		} else {
			console.log(sniffer.siteName + ' unknown fail');
		}

		if(sniffer.onAndroid) {
			window.Sniffer.callback(sniffer.rawUrl, '');
		}
	},

	getUrl: function() {
		return sniffer.rawUrl;
	},
	
	getScript: function(html, callback, startStr) {

		if(!startStr) {
			startStr = '<script>';
		}
		var endStr = '</script>';
		while(true) {

			var start = html.indexOf(startStr);
			if(start == -1) {
				return null;
			}
			start += startStr.length;

			var end = html.indexOf(endStr, start);
			if(end == -1) {
				return null;
			}

			var script = html.substr(start, end - start);

			var result = callback(script);
			if(result) {
				return true;
			}

			html = html.substr(end);
		}
		return false;
	},

	/* 获得参数所指向的url的html文本 */
	getHtml: function(callback) {

		var url = sniffer.getUrl();
		if(!sniffer.checkStr(url)) {
			sniffer.fail('get html fail. invalid url');
			return;
		}
		sniffer.getText(url, 'text', callback);
	}, 

	/* 异步获取文本内容 */
	getText: function(url, type, callback) {

		sniffer.debug('get text ' + url);
		$.ajax({
			'url':url,
			'dataType':type,
			'type':'GET',
			'success': callback,
			'fail': function(xmlHttpRequest, textStatus, failThrown) {
				var message = 'status ' + xmlHttpRequest.status + 
								' state ' + xmlHttpRequest.readyState + 
								' textStatus ' + textStatus;
				sniffer.fail(message);
			}});
	},

	/* 检查字符串有效性，如果无效则上报错误 */
	checkStr: function(obj) {
		return (obj && obj != null && typeof obj == 'string' && obj != '');
	},

	checkArray: function(obj) {
		return (obj && obj != null && sniffer.isArray(obj) && obj.length != 0);
	},

	checkObj: function(obj) {
		return (obj && obj != null);
	},
	
	isArray: function(m) {

		if(typeof m === 'object') {
			if(	typeof m.length === 'number' &&
				typeof m.slice === 'function' &&
				!m.propertyIsEnumerable('length')) {
				return true;
			}
		}
		return false;
	},

	debug: function(str) {

		str = sniffer.siteName + ' ' + str;
		console.log(str);
		if((sniffer.onAndroid == true) && (sniffer.printNative == true)) {
			window.Sniffer.debug(str);
		}
	},

	error: function(str) {

		str = sniffer.siteName + ' ' + str;
		console.log(str);
		if((sniffer.onAndroid == true) && (sniffer.printNative == true)) {
			window.Sniffer.error(str);
		}
	},
	
	exception: function(e) {
		sniffer.error('name:' + e.name + ' message:' + e.message + ' descrption:' + e.descrption + ' number:' + e.number);
	}
};
