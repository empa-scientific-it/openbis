define([ "jquery" ], function($) {

	//
	// FACADE
	//

	function OpenbisFacade(openbis) {
		this.init(openbis);
	}

	$.extend(OpenbisFacade.prototype, {
		init : function(openbis) {
			this.openbis = openbis;
		},

		getSession : function() {
			return this.openbis.getSession();
		},

		getDataStoreBaseURL : function(dataSetCode, action) {
			this.openbis.getDataStoreBaseURLs([ dataSetCode ], function(response) {
				if (response.error) {
					alert(JSON.stringify(response.error));
				} else {
					if (response.result && response.result.length > 0) {
						var urlForDataSets = response.result[0];
						action(urlForDataSets.dataStoreURL);
					} else {
						action(null);
					}
				}
			});
		},

		getImageInfo : function(dataSetCode, action) {
			this.openbis.getImageInfo([ dataSetCode ], function(response) {
				if (response.error) {
					alert(JSON.stringify(response.error));
				} else {
					var infoMap = response.result;
					if (infoMap) {
						action(infoMap[dataSetCode]);
					} else {
						action(null);
					}
				}
			});
		},

		getImageResolutions : function(dataSetCode, action) {
			var thisFacade = this;

			this.openbis.getDatasetIdentifiers([ dataSetCode ], function(response) {
				if (response.error) {
					alert(JSON.stringify(response.error));
				} else {
					thisFacade.openbis.listAvailableImageRepresentationFormats(response.result, function(response) {
						if (response.error) {
							alert(JSON.stringify(response.error));
						} else {
							if (response.result && response.result.length > 0) {
								var formats = response.result[0].imageRepresentationFormats;

								thisFacade._maybeAddThumbnailRepresentationFormat(formats);
								thisFacade._removeDuplicatedRepresentationFormats(formats);
								thisFacade._sortRepresentationFormats(formats);

								action(formats);
							} else {
								action(null);
							}
						}
					});
				}
			});
		},

		_maybeAddThumbnailRepresentationFormat : function(formats) {
			var hasThumbnails = formats.some(function(format) {
				return false == format.original;
			});

			if (false == hasThumbnails) {
				var originals = formats.filter(function(format) {
					return format.original;
				});

				if (originals && originals.length > 0) {
					var original = originals[0];

					if (original.width && original.height) {
						formats.push({
							"original" : false,
							"width" : Math.round(original.width / 4),
							"height" : Math.round(original.height / 4)
						});
					}
				}
			}
		},

		_removeDuplicatedRepresentationFormats : function(formats) {
			var resolutions = {};
			var newFormats = [];

			formats.forEach(function(format, index) {
				var resolution = format.width + "x" + format.height;
				if (!resolutions[resolution]) {
					resolutions[resolution] = format;
					newFormats.push(format);
				}
			});

			formats.splice(0, formats.length);
			Array.prototype.push.apply(formats, newFormats);
		},

		_sortRepresentationFormats : function(formats) {
			formats.sort(function(o1, o2) {
				var compare = function(v1, v2) {
					if (v1 > v2) {
						return 1;
					} else if (v1 < v2) {
						return -1;
					} else {
						return 0;
					}
				}

				return compare(o1.width, o2.width) * 10 + compare(o1.height, o2.height);
			});
		}

	});

	return OpenbisFacade;

});