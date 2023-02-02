define(
		[ "jquery", "components/imageviewer/AbstractView", "components/imageviewer/FormFieldWidget" ],
		function($, AbstractView, FormFieldWidget) {

			//
			// TRANSFORMATION CHOOSER VIEW
			//

			function TransformationChooserView(controller) {
				this.init(controller);
			}

			$
					.extend(
							TransformationChooserView.prototype,
							AbstractView.prototype,
							{

								init : function(controller) {
									AbstractView.prototype.init.call(this, controller);
									this.panel = $("<div>").addClass("transformationChooserWidget").addClass("form-group");
								},

								render : function() {
									var thisView = this;

									var selectContainer = $("<div>").addClass("transformationSelectContainer").appendTo(this.panel);
									var parametersContainer = $("<div>").addClass("transformationParametersContainer").appendTo(this.panel);

									this.renderOrRefreshSelect(selectContainer);
									this.renderOrRefreshParameters(parametersContainer);

									return this.panel;
								},

								refresh : function() {
									this.renderOrRefreshSelect(this.panel.find(".transformationSelectContainer"));
									this.renderOrRefreshParameters(this.panel.find(".transformationParametersContainer"));
								},

								renderOrRefreshSelect : function(container) {
									var transformations = this.controller.getTransformations().map(function(transformation) {
										return transformation.code;
									});

									if (this.currentTransformations != undefined) {
										if (this.currentTransformations.toString() != transformations.toString()) {
											container.empty().append(this.renderSelect());
										} else {
											this.refreshSelect();
										}
									} else {
										container.append(this.renderSelect());
									}

									this.currentTransformations = transformations;
								},

								renderSelect : function(container) {
									var thisView = this;
									var widget = $("<div>").addClass("transformationSelect").addClass("form-group");

									$("<label>").text("Filter").attr("for", "transformationSelect").appendTo(widget);

									var select = $("<select>").attr("id", "transformationSelect").addClass("form-control");

									$("<div>").append(select).appendTo(widget);

									this.controller.getTransformations().forEach(function(transformation) {
										$("<option>").attr("value", transformation.code).text(transformation.label).appendTo(select);
									});

									select.val(this.controller.getSelectedTransformation());

									select.change(function() {
										thisView.controller.setSelectedTransformation(select.val());
									});
									return widget;
								},

								refreshSelect : function() {
									var select = this.panel.find(".transformationSelect select");

									if (select.val() != this.controller.getSelectedTransformation()) {
										select.val(this.controller.getSelectedTransformation());
									}
								},

								renderOrRefreshParameters : function(container) {
									var thisView = this;

									if (this.controller.isUserDefinedTransformation()) {
										var channels = this.controller.getSelectedChannels();
										var scales = [];

										channels.forEach(function(channel) {
											var parameters = thisView.controller.getUserDefinedTransformationParameters(channel);
											scales.push(parameters.min + "_" + parameters.max);
										});

										var channelsChanned = !this.currentChannels || this.currentChannels.toString() != channels.toString();
										var scalesChanned = !this.currentScales || this.currentScales.toString() != scales.toString();

										if (channelsChanned || scalesChanned) {
											container.empty().append(this.renderParameters());
											this.currentChannels = channels;
											this.currentScales = scales;
										} else {
											this.refreshParameters();
										}
									} else {
										container.empty();
										this.currentChannels = null;
										this.currentScales = null;
									}
								},

								renderParameters : function() {
									var thisView = this;

									var panel = $("<div>").addClass("transformationParameters");

									this.controller.getSelectedChannels().forEach(function(channel) {
										var parameters = thisView.controller.getUserDefinedTransformationParameters(channel);
										panel.append(thisView.renderChannelParameters(channel, parameters));
									});

									return panel;
								},

								renderChannelParameters : function(channel, parameters) {
									var thisView = this;

									var channelObject = this.controller.getChannelsMap()[channel];
									var input = $("<input>").attr("type", "text").addClass("form-control");

									var formField = new FormFieldWidget();
									formField.setWidget(input);
									formField.setLabel(channelObject.label + " (" + parameters.blackpoint + ", " + parameters.whitepoint + ")");
									formField
											.setButton({
												"name" : "rescale",
												"text" : "Rescale",
												"tooltip" : "Changes a scale of the slider basing on the currenly selected black and white points. The black point value becomes the minimum of the scale. The white point value becomes the maximum of the scale.",
												"action" : function() {
													var value = input.slider("getValue");

													thisView.controller.setUserDefinedTransformationParameters(channel, {
														"min" : value[0],
														"max" : value[1],
														"blackpoint" : value[0],
														"whitepoint" : value[1]
													});
												}
											});
									formField.setButton({
										"name" : "reset",
										"text" : "Reset",
										"tooltip" : "Changes a scale of the slider back to the default value of (0, 255).",
										"action" : function() {
											var value = input.slider("getValue");

											thisView.controller.setUserDefinedTransformationParameters(channel, {
												"min" : 0,
												"max" : 255,
												"blackpoint" : 0,
												"whitepoint" : 255,
											});
										}
									});

									var widget = $("<div>").addClass("transformationParameter").addClass("form-group");
									widget.data("channel", channel);
									widget.data("formField", formField);
									widget.append(formField.render());

									input.slider({
										"min" : parameters.min,
										"max" : parameters.max,
										"step" : 1,
										"tooltip" : "hide",
										"value" : [ parameters.blackpoint, parameters.whitepoint ]
									}).on("slide", function(event) {
										var value = input.slider("getValue");

										thisView.controller.setUserDefinedTransformationParameters(channel, {
											"min" : parameters.min,
											"max" : parameters.max,
											"blackpoint" : value[0],
											"whitepoint" : value[1],
										}, true);
									});

									return widget;
								},

								refreshParameters : function() {
									var thisView = this;

									this.panel.find(".transformationParameter").each(
											function() {
												var widget = $(this);
												var channel = widget.data("channel");
												var channelObject = thisView.controller.getChannelsMap()[channel];
												var channelParameters = thisView.controller.getUserDefinedTransformationParameters(channel);

												var formField = $(this).data("formField");
												formField.setLabel(channelObject.label + " (" + channelParameters.blackpoint + ", "
														+ channelParameters.whitepoint + ")");
												formField.getWidget().slider("setValue",
														[ channelParameters.blackpoint, channelParameters.whitepoint ]);
											});
								}

							});

			return TransformationChooserView;

		});