define([ "jquery", "components/imageviewer/AbstractView" ], function($, AbstractView) {

	//
	// MOVIE BUTTONS VIEW
	//

	function MovieButtonsView(controller) {
		this.init(controller);
	}

	$.extend(MovieButtonsView.prototype, AbstractView.prototype, {

		init : function(controller) {
			AbstractView.prototype.init.call(this, controller);
			this.panel = $("<div>").addClass("movieButtonsWidget").addClass("form-group");
		},

		render : function() {
			var thisView = this;

			var table = $("<table>").addClass("mainTable").appendTo(this.panel);
			var row = $("<tr>").appendTo(table);

			var buttonsCell = $("<td>").addClass("buttons").appendTo(row);
			var delayCell = $("<td>").addClass("delay").addClass("form-inline").appendTo(row);

			var play = $("<button>").addClass("play").addClass("btn").addClass("btn-primary");
			$("<span>").addClass("glyphicon").addClass("glyphicon-play").appendTo(play);

			play.appendTo(buttonsCell);
			play.click(function() {
				thisView.controller.play();
			});

			var stop = $("<button>").addClass("stop").addClass("btn").addClass("btn-primary");
			$("<span>").addClass("glyphicon").addClass("glyphicon-stop").appendTo(stop);

			stop.appendTo(buttonsCell);
			stop.click(function() {
				thisView.controller.stop();
			});

			var prev = $("<button>").addClass("prev").addClass("btn").addClass("btn-default");
			$("<span>").addClass("glyphicon").addClass("glyphicon-backward").appendTo(prev);

			prev.appendTo(buttonsCell);
			prev.click(function() {
				thisView.controller.prev();
			});

			var next = $("<button>").addClass("next").addClass("btn").addClass("btn-default");
			$("<span>").addClass("glyphicon").addClass("glyphicon-forward").appendTo(next);

			next.appendTo(buttonsCell);
			next.click(function() {
				thisView.controller.next();
			});

			var delayTable = $("<table>").addClass("delayTable").appendTo(delayCell);
			var delayTr = $("<tr>").appendTo(delayTable);

			$("<td>").append($("<span>").addClass("delayLabel").text("delay:").attr("for", "delayInput")).appendTo(delayTr);

			var delay = $("<input>").attr("id", "delayInput").attr("type", "text").attr("size", 4).attr("maxlength", 4).addClass("delay").addClass(
					"form-control");
			delay.change(function() {
				thisView.controller.setSelectedDelay(delay.val());
			});
			$("<td>").attr("width", "100%").append(delay).appendTo(delayTr);

			$("<td>").append($("<span>").addClass("delayUnit").text("ms")).appendTo(delayTr);

			this.refresh();

			return this.panel;
		},

		refresh : function() {
			var disabled = this.controller.getFrameCount() <= 1;

			var play = this.panel.find("button.play");
			play.prop("disabled", this.controller.isPlaying() || disabled);

			var stop = this.panel.find("button.stop");
			stop.prop("disabled", this.controller.isStopped() || disabled);

			var prev = this.panel.find("button.prev");
			prev.prop("disabled", this.controller.isFirstFrameSelected() || disabled);

			var next = this.panel.find("button.next");
			next.prop("disabled", this.controller.isLastFrameSelected() || disabled);

			var delay = this.panel.find("input.delay");
			delay.prop("disabled", disabled)
			delay.val(this.controller.getSelectedDelay());

			if (this.controller.isVisible()) {
				this.panel.show();
			} else {
				this.panel.hide();
			}
		}

	});

	return MovieButtonsView;

});