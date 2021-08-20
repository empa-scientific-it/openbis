function ArchivingHelperView(archivingHelperController, archivingHelperModel) {
    this._archivingHelperController = archivingHelperController;
    this._archivingHelperModel = archivingHelperModel;
    this._archivingCheckBoxes = [];

    this.repaint = function(views) {
        var _this = this;
        var $header = views.header;
        $header.append($("<h1>").append("Archiving Helper"));
        var $archiveButton = $("<a>", { "class" : "btn btn-primary", "style" : "margin-top: 10px;"}).append("Request archiving");
        $header.append($archiveButton);

        var $container = views.content;
        $container.empty();
        $container.append(this._createStepExplanationElement("1. Search for the datasets you want to archive:"));
        
        searchController = this._advancedSearch($container, this._archivingHelperController._mainController);
        $archiveButton.click(function() {
            var dataSets = Object.values(_this._archivingHelperModel.dataSetsForArchiving);
            if (dataSets.length > 0) {
                _this._archivingHelperController.archive(dataSets, function() {
                    searchController.search();
                });
            }
        });
    }

    this._addForArchiving = function(dataSet) {
        this._archivingHelperModel.dataSetsForArchiving[dataSet.code] = dataSet;
    }
    
    this._removeForArchiving = function(dataSet) {
        delete this._archivingHelperModel.dataSetsForArchiving[dataSet.code];
    }
    
    this._advancedSearch = function($container, mainController) {
        var _this = this;
        var $explanationBox = this._createStepExplanationElement("2. Check all datasets you want to archive and click the 'Request archiving' button:");
        $explanationBox.hide();
        var searchController = new AdvancedSearchController(mainController);
        var $selectionPanel = $("<div>", { "class" : "form-inline", style : "width: 100%;" });
        $container.append($selectionPanel)
        var searchView = searchController._advancedSearchView;
        searchView._paintTypeSelectionPanel($selectionPanel);
        var $rulesPanel = $("<div>", { "class" : "form-inline", style : "width: 100%;" });
        $container.append($rulesPanel)
        searchView.resultsTitle = null;
        searchView.configKeyPrefix += "ARCHIVING_HELPER_";
//        searchView.suppressedColumns = ['entityKind', 'identifier'];
        searchView.hideByDefaultColumns = ['$NAME', 'registrator', 'modificationDate', 'modifier'];
        searchController.fetchWithSample = true;
        searchView.firstColumns = [{
            label : "Should be archived",
            property : "archive",
            value : false,
            isExportable : false,
            sortable : false,
            canNotBeHidden : true,
            render : function(data, grid) {
                var $checkbox = $("<input>", { type : "checkbox"});
                _this._archivingCheckBoxes.push($checkbox);
                $checkbox.prop("checked", _this._archivingHelperModel.dataSetsForArchiving[data.code]);
                $checkbox.change(data, function (event) {
                    if (this.checked) {
                        _this._addForArchiving(event.data);
                    } else {
                        _this._removeForArchiving(event.data);
                    }
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
            sortable : true,
            render : function(data, grid) {
                return PrintUtil.renderNumberOfBytes(data.size);
            }
        }];
        searchView._paintRulesPanel($rulesPanel);
        searchView._$entityTypeDropdown.val("DATASET");
        searchView._$entityTypeDropdown.trigger("change");
        searchView._$entityTypeDropdown.attr("disabled", "disabled");
        searchView._$andOrDropdownComponent.attr("disabled", "disabled");
        searchView._$dataGridContainer = $("<div>");
        searchView.beforeRenderingHook = function() {
            _this._archivingHelperModel.dataSetsForArchiving = {};
            $explanationBox.show();
        }
        searchView.extraOptions = [{
            name : "Select all data sets",
            action : function(selected) {
                var search = searchController.searchWithPagination(searchView._advancedSearchModel.criteria, false);
                search(function(results) {
                    results.objects.forEach(function(dataSet) {
                        _this._addForArchiving(dataSet);
                    });
                });
                _this._archivingCheckBoxes.forEach(function(checkBox) {
                    checkBox.prop("checked", true);
                });
            }
        }, {
            name : "Unselect all data sets",
            action : function(selected) {
                _this._archivingCheckBoxes.forEach(function(checkBox) {
                    checkBox.prop("checked", false);
                });
                _this._archivingHelperModel.dataSetsForArchiving = {};
            }
        }];
        searchController.additionalRules = [{
            "type" : "Attribute",
            "name" : "PHYSICAL_STATUS",
            "value" : "AVAILABLE"
        }, {
            "type" : "Attribute",
            "name" : "ARCHIVING_REQUESTED",
            "value" : "false"
        }];
        $container.append($explanationBox);
        $container.append(searchView._$dataGridContainer);
        return searchController;
    }

    this._createStepExplanationElement = function(text) {
        return $("<div>", { style : "font-weight: bold" }).text(text);
    }
}