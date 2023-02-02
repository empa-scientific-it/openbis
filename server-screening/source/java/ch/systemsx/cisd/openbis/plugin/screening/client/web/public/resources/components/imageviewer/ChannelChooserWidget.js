define([ "jquery", "components/imageviewer/AbstractWidget", "components/imageviewer/ChannelChooserView" ], function($, AbstractWidget,
		ChannelChooserView) {

	//
	// CHANNEL CHOOSER WIDGET
	//

	function ChannelChooserWidget(channels) {
		this.init(channels);
	}

	$.extend(ChannelChooserWidget.prototype, AbstractWidget.prototype, {

		init : function(channels) {
			AbstractWidget.prototype.init.call(this, new ChannelChooserView(this));
			this.setChannels(channels);
		},

		doGetState : function(state) {
			state.selectedChannel = this.getSelectedChannel();
			state.selectedMergedChannels = this.getSelectedMergedChannels();
		},

		doSetState : function(state) {
			this.setSelectedChannel(state.selectedChannel);
			this.setSelectedMergedChannels(state.selectedMergedChannels);
		},

		getSelectedChannel : function() {
			if (this.selectedChannel) {
				return this.selectedChannel;
			} else {
				return null;
			}
		},

		getSelectedChannels : function() {
			if (this.getSelectedChannel() != null) {
				return [ this.getSelectedChannel() ];
			} else {
				return this.getSelectedMergedChannels();
			}
		},

		setSelectedChannel : function(channel) {
			if (channel != null && $.inArray(channel, this.getChannelsCodes()) == -1) {
				channel = null;
			}

			if (this.getSelectedChannel() != channel) {
				this.selectedChannel = channel;
				this.refresh();
				this.notifyChangeListeners();
			}
		},

		getSelectedMergedChannels : function() {
			if (this.selectedMergedChannels) {
				return this.selectedMergedChannels;
			} else {
				return [];
			}
		},

		setSelectedMergedChannels : function(channels) {
			var thisWidget = this;

			if (!channels) {
				channels = [];
			}

			channels = channels.filter(function(channel) {
				return $.inArray(channel, thisWidget.getChannelsCodes()) != -1;
			});

			if (channels.length == 0) {
				channels = this.getChannelsCodes();
			}

			if (this.getSelectedMergedChannels().toString() != channels.toString()) {
				this.selectedMergedChannels = channels;
				this.refresh();
				this.notifyChangeListeners();
			}
		},

		isMergedChannelSelected : function(channel) {
			return $.inArray(channel, this.getSelectedMergedChannels()) != -1;
		},

		isMergedChannelEnabled : function(channel) {
			if (this.getSelectedMergedChannels().length == 1) {
				return !this.isMergedChannelSelected(channel);
			} else {
				return true;
			}
		},

		getChannelsCodes : function() {
			return this.getChannels().map(function(channel) {
				return channel.code;
			});
		},

		getChannels : function() {
			if (this.channels) {
				return this.channels;
			} else {
				return [];
			}
		},

		setChannels : function(channels) {
			if (!channels) {
				channels = [];
			}

			if (this.getChannels().toString() != channels.toString()) {
				this.channels = channels;
				this.setSelectedChannel(null);
				this.setSelectedMergedChannels(channels.map(function(channel) {
					return channel.code;
				}));
				this.refresh();
			}
		}

	});

	return ChannelChooserWidget;

});