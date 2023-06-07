function UnarchivingHelperView(unarchivingHelperController, unarchivingHelperModel) {
	this._unarchivingHelperController = unarchivingHelperController;
	this._unarchivingHelperModel = unarchivingHelperModel;
	this._unarchivingCheckBoxes = [];
	
	this.repaint = function(views) {
		var _this = this;
		var $header = views.header;
		$header.append($("<h1>").append("Unarchiving Helper"));
		var $unarchiveButton = $("<a>", { "class" : "btn btn-primary", "style" : "margin-top: 10px;"}).append("Unarchive");
		var $infoSummary = $("<span>", { style : "width: 100%; vertical-align: middle;margin-left: 10px; margin-top: 10px;" });
		$header.append($unarchiveButton);
		$header.append($infoSummary);
		
		var $container = views.content;
		$container.empty();
		$explanationBox = FormUtil.getInfoBox("Data sets are usually archived together in bundles. "
				+ "Unarchiving one means that all data sets of the bundle are unarchived, too. " 
				+ "Note, that all these data sets are unarchived on a scratch disk. " 
				+ "They can be deleted without further notice. "
				+ "If they are needed again they have to be unarchived again.", []);
		$container.append($explanationBox);
		$explanationBox.css("border", "none");
		$container.append(this._createStepExplanationElement("1. Search for the datasets you want to unarchive:"));
		
		searchController = this._advancedSearch($container, $infoSummary, this._unarchivingHelperController._mainController);
		$unarchiveButton.click(function() {
			var dataSetCodes = Object.keys(_this._unarchivingHelperModel.dataSetsForUnarchiving);
			if (dataSetCodes.length > 0) {
				_this._unarchivingHelperController.unarchive(dataSetCodes, function(success) {
					if (success) {
						Util.showSuccess("Unarchiving has been triggered.", function() {
                            searchController.search();
						});
					}
				});
			}
		});
	}
	
	this._updateInfoSummary = function($infoSummary) {
		var dataSetsForUnarchiving = this._unarchivingHelperModel.dataSetsForUnarchiving;
		var numberOfDataSets = 0;
		var bundleSizes = {}
		for (var dataSetCode in dataSetsForUnarchiving) {
			var dataSet = dataSetsForUnarchiving[dataSetCode];
			bundleSizes[dataSet.bundleId] = dataSet.bundleSize
			numberOfDataSets++;
		}
		var totalSize = 0.0;
		for (var key in bundleSizes) {
			totalSize += bundleSizes[key];
		}
		if (totalSize > 0) {
			$infoSummary.text("Unarchiving all selected data sets (" + numberOfDataSets + " in total) needs " 
					+ PrintUtil.renderNumberOfBytes(totalSize) + " of free disk space.");
		} else {
			$infoSummary.text("");
		}
	}
	
	this._addForUnarchiving = function(dataSet) {
		this._unarchivingHelperModel.dataSetsForUnarchiving[dataSet.code] = {
				bundleId : dataSet.bundleId, 
				bundleSize : dataSet.bundleSize
			};
	}
	
	this._removeForUnarchiving = function(dataSet) {
		delete this._unarchivingHelperModel.dataSetsForUnarchiving[dataSet.code];
	}
	
	this._advancedSearch = function($container, $infoSummary, mainController) {
		var _this = this;
		var $explanationBox = this._createStepExplanationElement("2. Check all datasets you want to unarchive and click the 'Unarchive' button:");
		$explanationBox.hide();
		var searchController = new AdvancedSearchController(mainController);
		var $selectionPanel = $("<div>", { "class" : "form-inline", style : "width: 100%;" });
		$container.append($selectionPanel)
		var searchView = searchController._advancedSearchView;
		searchView._paintTypeSelectionPanel($selectionPanel);
		var $rulesPanel = $("<div>", { "class" : "form-inline", style : "width: 100%;" });
		$container.append($rulesPanel)
		searchView.resultsTitle = null;
		searchView.configKeyPrefix += "UNARCHIVING_HELPER_";
		searchView.suppressedColumns = ['entityKind', 'identifier'];
		searchView.hideByDefaultColumns = ['$NAME', 'bundle', 'registrator', 'modificationDate', 'modifier'];
		searchView.firstColumns = [{
			label : "Should be unarchived",
			property : "unarchive",
			value : false,
			isExportable : false,
			sortable : false,
			canNotBeHidden : true,
			render : function(data, grid) {
				var $checkbox = $("<input>", { type : "checkbox"});
				_this._unarchivingCheckBoxes.push($checkbox);
				$checkbox.prop("checked", _this._unarchivingHelperModel.dataSetsForUnarchiving[data.code]);
				$checkbox.change(data, function (event) {
					if (this.checked) {
						_this._addForUnarchiving(event.data);
					} else {
						_this._removeForUnarchiving(event.data);
					}
					_this._updateInfoSummary($infoSummary);
				});
				return $checkbox;
			}
		}];
		searchView.additionalColumns = [{
			label : ELNDictionary.Sample,
			property : 'sample',
			isExportable: false,
			sortable : false
		}];
		searchView.additionalLastColumns = [{
			label : "Size",
			property : "size",
			isExportable : false,
			sortable : false,
			render : function(data, grid) {
				return PrintUtil.renderNumberOfBytes(data.size);
			}
		},
		{
			label : "Datasets in Bundle",
			property : "bundle",
			isExportable : false,
			sortable : false,
			render : function(data, grid) {
				return data.bundle;
			}
		},
		{
			label : "Bundle Size",
			property : "bundleSize",
			isExportable : false,
			sortable : false,
			render : function(data, grid) {
				return PrintUtil.renderNumberOfBytes(data.bundleSize);
			}
		}];
		searchView._paintRulesPanel($rulesPanel);
		searchView._$entityTypeDropdown.val("DATASET");
		searchView._$entityTypeDropdown.trigger("change");
		searchView._$entityTypeDropdown.attr("disabled", "disabled");
        searchView._$andOrDropdownComponent.val("AND").trigger("change");
		searchView._$andOrDropdownComponent.attr("disabled", "disabled");
		searchView._$dataGridContainer = $("<div>");
		searchView.beforeRenderingHook = function() {
			_this._unarchivingHelperModel.dataSetsForUnarchiving = {};
			$explanationBox.show();
			$infoSummary.text("");
		}
		searchView.extraOptions = [{
			name : "Select all data sets",
			action : function(selected) {
				var search = searchController.searchWithPagination(searchView._advancedSearchModel.criteria, false);
				search(function(results) {
					results.objects.forEach(function(dataSet) {
						_this._addForUnarchiving(dataSet);
					});
					_this._updateInfoSummary($infoSummary);
				});
				_this._unarchivingCheckBoxes.forEach(function(checkBox) {
					checkBox.prop("checked", true);
				});
			}
		}, {
			name : "Unselect all data sets",
			action : function(selected) {
				_this._unarchivingCheckBoxes.forEach(function(checkBox) {
					checkBox.prop("checked", false);
				});
				_this._unarchivingHelperModel.dataSetsForUnarchiving = {};
				_this._updateInfoSummary($infoSummary);
			}
		}];
		searchController.additionalRules = [{
			"type" : "Attribute",
			"name" : "PHYSICAL_STATUS",
			"value" : "ARCHIVED"
		}];
		searchController.enrichResultsFunction = function(results, callback) {
			var codes = results.map(row => row.code);
			unarchivingHelperController.getInfo(codes, function(infos) {
				_this._unarchivingCheckBoxes = [];
				results.forEach(function(row) {
					info = infos[row.code];
					row.size = info.size;
					row.bundle = info.numberOfDataSets;
					row.bundleId = info.bundleId;
					row.bundleSize = info.bundleSize;
				});
				callback(results);
			})
		}
		$container.append($explanationBox);
		$container.append(searchView._$dataGridContainer);
		return searchController;
	}
	
	this._createStepExplanationElement = function(text) {
		return $("<div>", { style : "font-weight: bold" }).text(text);
	}
}