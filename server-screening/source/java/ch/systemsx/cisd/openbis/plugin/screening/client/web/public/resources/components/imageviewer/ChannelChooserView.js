define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// CHANNEL CHOOSER VIEW
	//

	function ChannelChooserView(controller) {
		this.init(controller);
	}

	$.extend(ChannelChooserView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>");
		},

		render : function() {
			this.panel.append(this.createChannelWidget());
			this.panel.append(this.createMergedChannelsWidget());

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var thisView = this;

			var select = this.panel.find("select");
			var mergedChannels = this.panel.find(".mergedChannelsWidget");

			if (this.controller.getSelectedChannel() != null) {
				select.val(this.controller.getSelectedChannel());
				mergedChannels.hide();
			} else {
				select.val("");
				mergedChannels.find("input").each(function() {
					var checkbox = $(this);
					checkbox.prop("checked", thisView.controller.isMergedChannelSelected(checkbox.val()));
					checkbox.prop("disabled", !thisView.controller.isMergedChannelEnabled(checkbox.val()));
				});
				mergedChannels.show();
			}
		},

		createChannelWidget : function() {
			var thisView = this;
			var widget = $("<div>").addClass("channelWidget").addClass("form-group");

			$("<label>").text("Channel").attr("for", "channelChooserSelect").appendTo(widget);

			var select = $("<select>").attr("id", "channelChooserSelect").addClass("form-control").appendTo(widget);
			$("<option>").attr("value", "").text("Merged Channels").appendTo(select);

			this.controller.getChannels().forEach(function(channel) {
				$("<option>").attr("value", channel.code).text(channel.label).appendTo(select);
			});

			select.change(function() {
				if (select.val() == "") {
					thisView.controller.setSelectedChannel(null);
				} else {
					thisView.controller.setSelectedChannel(select.val());
				}
			});

			return widget;
		},

		createMergedChannelsWidget : function() {
			var thisView = this;
			var widget = $("<div>").addClass("mergedChannelsWidget").addClass("form-group");

			var table = $("<table>").appendTo(widget);
			var row = $("<tr>").appendTo(table);

			this.controller.getChannels().forEach(function(channel) {
				var cell = $("<td>").appendTo(row);
				var checkbox = $("<label>").addClass("checkbox-inline").appendTo(cell);
				$("<input>").attr("type", "checkbox").attr("value", channel.code).appendTo(checkbox);
				checkbox.append(channel.label);
			});

			widget.find("input").change(function() {
				var channels = []
				widget.find("input:checked").each(function() {
					channels.push($(this).val());
				});
				thisView.controller.setSelectedMergedChannels(channels);
			});

			return widget;
		}

	});

	return ChannelChooserView;

});