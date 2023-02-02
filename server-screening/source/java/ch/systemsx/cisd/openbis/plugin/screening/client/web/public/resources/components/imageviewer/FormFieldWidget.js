define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/FormFieldView" ], function($, AbstractWidget, FormFieldView) {

	//
	// FORM FIELD WIDGET
	//

	function FormFieldWidget() {
		this.init();
	}

	$.extend(FormFieldWidget.prototype, AbstractWidget.prototype, {

		init : function() {
			AbstractWidget.prototype.init.call(this, new FormFieldView(this));
		},

		setLabel : function(label) {
			if (this.getLabel() != label) {
				this.label = label;
				this.refresh();
			}
		},

		getLabel : function() {
			if (this.label) {
				return this.label;
			} else {
				return null;
			}
		},

		setButton : function(button) {
			var existingButton = this.getButton(button.name);

			if (existingButton) {
				if (existingButton.text != button.text) {
					existingButton.text = button.text;
					changed = true;
				}
				if (existingButton.tooltip != button.tooltip) {
					existingButton.tooltip = button.tooltip;
					changed = true;
				}
				if (existingButton.action != button.action) {
					existingButton.action = button.action;
					changed = true;
				}
			} else {
				this.getButtonsMap()[button.name] = button;
				changed = true;
			}

			if (changed) {
				this.refresh();
			}
		},

		getButton : function(name) {
			var button = this.getButtonsMap()[name];
			if (button) {
				return button;
			} else {
				return null;
			}
		},

		getButtonsMap : function() {
			if (!this.buttonsMap) {
				this.buttonsMap = {};
			}
			return this.buttonsMap;
		},

		setWidget : function(widget) {
			if (this.getWidget() != widget) {
				this.widget = widget;
				this.refresh();
			}
		},

		getWidget : function() {
			if (this.widget) {
				return this.widget;
			} else {
				return null;
			}
		},

		setEnabled : function(enabled) {
			if (this.isEnabled() != enabled) {
				this.enabled = enabled;
				this.refresh();
			}
		},

		isEnabled : function() {
			if (this.enabled != undefined) {
				return this.enabled;
			} else {
				return true;
			}
		}

	});

	return FormFieldWidget;

});
