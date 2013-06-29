
var sniffer = {

	run: function(url) {

		var elements = document.getElementsByTagName('video');
		if(elements.length != 0) {

			var video = elements[0];
			if(video.src && video.src != '') {
				window.Sniffer.callback(url, video.src);

			} else {
				var sources = video.getElementsByTagName('source');
				if(sources.length != 0) {
					
					var source = sources[0];
					if(source.src && source.src != '') {
						window.Sniffer.callback(url, source.src);
					}
				}
			}
		}
	}
};
sniffer.run('$currentUrl$');
