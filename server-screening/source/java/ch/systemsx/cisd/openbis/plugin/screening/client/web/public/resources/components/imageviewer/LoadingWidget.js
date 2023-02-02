define([ "jquery", "components/imageviewer/LoadingView" ], function($,
		LoadingView) {

	//
	// LOADING WIDGET
	//

	function LoadingWidget() {
		this.init();
	}

	$.extend(LoadingWidget.prototype, {

		init : function() {
			this.view = new LoadingView(this);
		},

		render : function() {
			if (this.rendered) {
				return this.panel;
			}

			this.panel = $("<div>");
			this.panel.append(this.view.render());
			this.rendered = true;

			return this.panel;
		},

		refresh : function() {
			if (!this.rendered) {
				return;
			}

			this.view.refresh();
		},

		setLoading : function(loading) {
			var thisWidget = this;

			if (this.isLoading() != loading) {
				if (!this.timeoutId) {
					this.timeoutId = setTimeout(function() {
						thisWidget.loading = loading;
						thisWidget.timeoutId = null;
						thisWidget.refresh();
					}, 100);
				}
			} else {
				if (this.timeoutId) {
					clearTimeout(this.timeoutId);
					this.timeoutId = null;
				}
			}
		},

		isLoading : function() {
			if (this.loading != undefined) {
				return this.loading;
			} else {
				return false;
			}
		}

	});

	return LoadingWidget;

});
