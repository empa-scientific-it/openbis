/************************************************************************
*************************************************************************
@Name :       	jNotify - jQuery Plugin
@Revison :    	2.1
@Date : 		01/2011
@Author:     	ALPIXEL - (www.myjqueryplugins.com - www.alpixel.fr)
@Support:    	FF, IE7, IE8, MAC Firefox, MAC Safari
@License :		Open Source - MIT License : http://www.opensource.org/licenses/mit-license.php

**************************************************************************
*************************************************************************/
(function($){

	$.jNotify = {
	    sequence : 0,

		defaults: {
			/** VARS - OPTIONS **/
			autoHide : true,				// Notify box auto-close after 'TimeShown' ms ?
			clickOverlay : false,			// if 'clickOverlay' = false, close the notice box on the overlay click ?
			MinWidth : 200,					// min-width CSS property
			TimeShown : 1500, 				// Box shown during 'TimeShown' ms
			ShowTimeEffect : 200, 			// duration of the Show Effect
			HideTimeEffect : 200, 			// duration of the Hide effect
			LongTrip : 15,					// in pixel, length of the move effect when show and hide
			HorizontalPosition : 'right', 	// left, center, right
			VerticalPosition : 'bottom',	 // top, center, bottom
			ShowOverlay : true,				// show overlay behind the notice ?
			ColorOverlay : '#000',			// color of the overlay
			OpacityOverlay : 0.3,			// opacity of the overlay
			
			/** METHODS - OPTIONS **/
			onClosed : null,
			onCompleted : null
		},

        overlay : [], // Empty overlay
        div : null, //Empty div

		/*****************/
		/** Init Method **/
		/*****************/
		init:function(msg, options, id) {
			this.opts = $.extend({}, this.defaults, options);

			/** Box **/
			if($("#"+id).length == 0)
				this.div = this._construct(id, msg);

			// Width of the Brower
			WidthDoc = parseInt($(window).width());
			HeightDoc = parseInt($(window).height());

			// Scroll Position
			ScrollTop = parseInt($(window).scrollTop());
			ScrollLeft = parseInt($(window).scrollLeft());

			// Position of the jNotify Box
			posTop = this.vPos(this.opts.VerticalPosition);
			posLeft = this.hPos(this.opts.HorizontalPosition);

			// Show the jNotify Box
			if(this.opts.ShowOverlay && this.overlay.length == 0)
				this._showOverlay(this.div);

			this._show(msg);
			return this;
		},

		/*******************/
		/** Construct DOM **/
		/*******************/
		_construct:function(id, msg) {
			this.div =
			$('<div id="'+id+'"/>')
			.css({opacity : 0,minWidth : this.opts.MinWidth})
			.html(msg)
			.appendTo('body');
			return this.div;
		},

		/**********************/
		/** Postions Methods **/
		/**********************/
		vPos:function(pos) {
			switch(pos) {
				case 'top':
					var vPos = ScrollTop + parseInt(this.div.outerHeight(true)/2);
					break;
				case 'center':
					var vPos = ScrollTop + (HeightDoc/2) - (parseInt(this.div.outerHeight(true))/2);
					break;
				case 'bottom':
					var vPos = ScrollTop + HeightDoc - parseInt(this.div.outerHeight(true));
					break;
			}
			return vPos;
		},

		hPos:function(pos) {
			switch(pos) {
				case 'left':
					var hPos = ScrollLeft;
					break;
				case 'center':
					var hPos = ScrollLeft + (WidthDoc/2) - (parseInt(this.div.outerWidth(true))/2);
					break;
				case 'right':
					var hPos = ScrollLeft + WidthDoc - parseInt(this.div.outerWidth(true));
					break;
			}
			return hPos;
		},

		/*********************/
		/** Show Div Method **/
		/*********************/
		_show:function(msg) {
		    var opts = this.opts;
			this.div
			.css({
				top: posTop,
				left : posLeft
			});
			switch (opts.VerticalPosition) {
				case 'top':
					this.div.animate({
						top: posTop + opts.LongTrip,
						opacity:1
					},opts.ShowTimeEffect,function(){
						if(opts.onCompleted) opts.onCompleted();
					});
					if(opts.autoHide)
						this._close();
					else
						this.div.css('cursor','pointer').click(function(e){
							// $.jNotify._close();
						});
					break;
				case 'center':
					this.div.animate({
						opacity:1
					},opts.ShowTimeEffect,function(){
						if(opts.onCompleted) opts.onCompleted();
					});
					if(opts.autoHide)
						this._close();
					else
						this.div.css('cursor','pointer').click(function(e){
							// $.jNotify._close();
						});
					break;
				case 'bottom' :
					this.div.animate({
						top: posTop - opts.LongTrip,
						opacity:1
					},opts.ShowTimeEffect,function(){
						if(opts.onCompleted) opts.onCompleted();
					});
					if(opts.autoHide)
						this._close();
					else
						this.div.css('cursor','pointer').click(function(e){
							// $.jNotify._close();
						});
					break;
			}
		},

		_showOverlay:function(el){
		    var opts = this.opts;
			this.overlay =
			$('<div>')
			.css({
				backgroundColor : opts.ColorOverlay,
				opacity: opts.OpacityOverlay
			})
			.appendTo('body')
			.show();

			if(opts.clickOverlay)
			this.overlay.click(function(e){
				e.preventDefault();
				opts.TimeShown = 0;
				// $.jNotify._close();
			});
		},


		_close:function(){
	    var opts = this.opts;
		var overlay = this.overlay;
				switch (opts.VerticalPosition) {
					case 'top':
						if(!opts.autoHide)
							opts.TimeShown = 0;
						this.div.stop(true, true).delay(opts.TimeShown).animate({
							top: posTop-opts.LongTrip,
							opacity:0
						},opts.HideTimeEffect,function(){
							$(this).remove();
							if(opts.ShowOverlay && overlay.length > 0)
								overlay.remove();
								if(opts.onClosed) opts.onClosed();
						});
						break;
					case 'center':
						if(!opts.autoHide)
							opts.TimeShown = 0;
						this.div.stop(true, true).delay(opts.TimeShown).animate({
							opacity:0
						},opts.HideTimeEffect,function(){
							$(this).remove();
							if(opts.ShowOverlay && overlay.length > 0)
								overlay.remove();
								if(opts.onClosed) opts.onClosed();
						});
						break;
					case 'bottom' :
						if(!opts.autoHide)
							opts.TimeShown = 0;
						this.div.stop(true, true).delay(opts.TimeShown).animate({
							top: posTop+opts.LongTrip,
							opacity:0
						},opts.HideTimeEffect,function(){
							$(this).remove();
							if(opts.ShowOverlay && overlay.length > 0)
								overlay.remove();
								if(opts.onClosed) opts.onClosed();
						});
						break;
				}
		}
	};

	/** Init method **/
	jNotify = function(msg,options) {
	    var jnotify = $.extend({}, $.jNotify);
		return jnotify.init(msg,options,'jNotify');
	};

	jNotifyImage = function(msg,options) {
	    var jnotify = $.extend({}, $.jNotify);
		return jnotify.init(msg,options,'jNotifyImage');
	};
	
	jSuccess = function(msg,options) {
	    var jnotify = $.extend({}, $.jNotify);
		return jnotify.init(msg,options,'jSuccess');
	};

	jError = function(msg,options) {
	    var jnotify = $.extend({}, $.jNotify);
		return jnotify.init(msg,options,'jError');
	};
})(jQuery);