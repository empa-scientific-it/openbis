define([ "jquery", "components/common/ListenerManager",
		"components/imageviewer/LoadingWidget" ], function($, ListenerManager, LoadingWidget) {

	//
	// ABSTRACT WIDGET
	//

	function AbstractWidget(view) {
		this.init(view);
	}

	$.extend(AbstractWidget.prototype, {
		init : function(view) {
			this.setView(view);
			this.listeners = new ListenerManager();
			this.panel = $("<div>").addClass("widget");
		},

		load : function(callback) {
			callback();
		},

		render : function() {
			if (this.rendered) {
				return this.panel;
			}

			var thisWidget = this;

			var doRender = function() {
				thisWidget.panel.empty();
				if (thisWidget.getView()) {
					thisWidget.panel.append(thisWidget.getView().render());
					thisWidget.rendered = true;
					thisWidget.notifyRenderListeners();
				}
			};

			if (this.loaded) {
				doRender();
			} else {
				var loading = new LoadingWidget();
				loading.setLoading(true);
				this.panel.append(loading.render());

				this.load(function() {
					thisWidget.loaded = true;
					thisWidget.notifyLoadListeners();
					doRender();
				});
			}

			return this.panel;
		},

		refresh : function() {
			if (!this.rendered) {
				return;
			}

			if (this.getView()) {
				this.getView().refresh();
			}
		},

		getState : function() {
			if (!this.state) {
				this.state = {};
			}
			this.doGetState(this.state);
			return this.state;
		},

		setState : function(state) {
			if (state) {
				if (!this.state) {
					this.state = {};
				}
				$.extend(this.state, state);
				this.doSetState(this.state);
			}
		},

		doGetState : function(state) {
		},

		doSetState : function(state) {
		},

		getView : function() {
			return this.view;
		},

		setView : function(view) {
			this.view = view;
			if (this.rendered) {
				this.rendered = false;
				this.render();
			}
		},

		addListener : function(eventType, listener) {
			this.listeners.addListener(eventType, listener);
		},

		notifyListeners : function(eventType, event) {
			this.listeners.notifyListeners(eventType, event);
		},

		addChangeListener : function(listener) {
			this.addListener("change", listener);
		},

		notifyChangeListeners : function(event) {
			this.notifyListeners("change", event);
		},

		addLoadListener : function(listener) {
			this.addListener("load", listener);
		},

		notifyLoadListeners : function() {
			this.notifyListeners("load");
		},

		addRenderListener : function(listener) {
			this.addListener("render", listener);
		},

		notifyRenderListeners : function() {
			this.notifyListeners("render");
		}

	});

	return AbstractWidget;

});
