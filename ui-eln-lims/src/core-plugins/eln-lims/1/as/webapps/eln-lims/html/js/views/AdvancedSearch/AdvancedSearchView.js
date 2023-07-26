/*
 * Copyright 2014 ETH Zuerich, Scientific IT Services
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function AdvancedSearchView(advancedSearchController, advancedSearchModel) {
	this._advancedSearchController = advancedSearchController;
	this._advancedSearchModel = advancedSearchModel;
	this._$entityTypeDropdown = null;
	this._$andOrDropdownComponent = null;
	this._$menuPanelContainer = null;
	this._$searchCriteriaPanelContainer = null;
	this._$rulesPanelContainer = null;
	this._$tbody = null;
	this._$dataGridContainer = null;
	this._$saveLoadContainer = null;
	this._$savedSearchesDropdown = null;
	this.configKeyPrefix = "ADVANCED_SEARCH_OPENBIS_";
	this.suppressedColumns = [];
	this.hideByDefaultColumns = [];
	this.firstColumns = [];
	this.additionalColumns = [];
	this.additionalLastColumns = [];
	this.resultsTitle = "Results";
	this.beforeRenderingHook = null;
	this.extraOptions = null;

	//
	// Main Repaint Method
	//

	this.repaint = function(views) {
		var $header = views.header;
		var $container = views.content;
		var _this = this;

		//Search Menu Panel
		var $mainPanelHeader = $("<form>", {
			"class" : "form-inline",
			'role' : "form",
			'action' : 'javascript:void(0);'
		});
		$mainPanelHeader.append($("<h2>").append("Advanced Search"));
		this._$saveLoadContainer = $("<div>");
		$mainPanelHeader.append(this._$saveLoadContainer);
		this._paintSaveLoadPanel(this._$saveLoadContainer);
		$header.append($mainPanelHeader);

		//Search Criteria Panel
		var $mainPanel = $("<form>", {
			"class" : "form-inline",
			'role' : "form",
			'action' : 'javascript:void(0);'
		});

		//table to select field type, name, and value
		this._$searchCriteriaPanelContainer = $("<div>");
		this._paintCriteriaPanel(this._$searchCriteriaPanelContainer);
		$mainPanel.append(this._$searchCriteriaPanelContainer);

		//Search Results Panel
		this._$dataGridContainer = $("<div>").css("margin-left", "-10px");;
		$mainPanel.append(this._$dataGridContainer);
		//

		//Triggers Layout refresh
		$container.append($mainPanel);

		this._updateEntityTypeAndUsingDropdownValues();
	}

	this.repaintContent = function() {
		this._paintSaveLoadPanel(this._$saveLoadContainer);
		this._paintCriteriaPanel(this._$searchCriteriaPanelContainer);
		this._updateEntityTypeAndUsingDropdownValues();
	}

	this._updateEntityTypeAndUsingDropdownValues = function() {
		if(this._advancedSearchModel.forceLoadCriteria) {
			var hiddenRule = this._advancedSearchModel.getHiddenRule();
			if (hiddenRule) {
				this._$entityTypeDropdown.val('SAMPLE$' + hiddenRule.value);
			} else {
				this._$entityTypeDropdown.val(this._advancedSearchModel.criteria.entityKind);
			}
			this._$andOrDropdownComponent.val(this._advancedSearchModel.criteria.logicalOperator);
			this._advancedSearchModel.forceLoadCriteria = undefined;
		}
	}

	//
	// Repaint Panels Methods
	//

	this._save = function() {
		var _this = this;
		profile.getHomeSpace(function(HOME_SPACE) {

	    var $nameField = FormUtil.getTextInputField('Name', 'Name', true);

	    var $searchDropdownContainer = $('<div>');
	    var advancedEntitySearchDropdown = new AdvancedEntitySearchDropdown(false, true, "search entity to store query",
				true, false, false, false, false);
	    advancedEntitySearchDropdown.init($searchDropdownContainer);
			if (HOME_SPACE) {
		    advancedEntitySearchDropdown.addSelected({
		      defaultDummyExperiment: true,
					space: HOME_SPACE,
					code: 'QUERIES_COLLECTION',
					projectCode: 'QUERIES',
					projectIdentifier: IdentifierUtil.getProjectIdentifier(HOME_SPACE, 'QUERIES'),
		      identifier: { identifier: IdentifierUtil.getExperimentIdentifier(HOME_SPACE, 'QUERIES', 'QUERIES_COLLECTION') },
		      permId: { permId: 'permId' },
		    });
			}

	    var $btnSave = $('<input>', { 'type': 'submit', 'class' : 'btn btn-primary', 'value' : 'Save', 'id' : 'search-query-save-btn' });
	    var $btnCancel = $('<a>', { 'class' : 'btn btn-default' }).append('Cancel');
	    $btnCancel.click(function() {
	      Util.unblockUI();
	    });

	    // update existing sample or save new one
	    if (_this._advancedSearchModel.selcetedSavedSearchIndex > -1) {
	      Util.blockUI();
	      _this._advancedSearchController.updateSelectedSample(function() {
	        Util.unblockUI();
	      });
	    } else {
	      FormUtil.showDialog({
	        css: {'text-align': 'left'},
	        title: 'Save search query',
	        components: [$nameField, $searchDropdownContainer],
	        buttons: [$btnSave, $btnCancel],
	        callback: function() {
	          Util.unblockUI();
	          Util.blockUI();
	          _this._advancedSearchController.saveNewSample({
	            name: $nameField.val(),
	            experiment: advancedEntitySearchDropdown.getSelected()[0],
	          }, Util.unblockUI);
	        },
	      });
	    }

		});
	}

	this._delete = function() {
		var i = this._advancedSearchModel.selcetedSavedSearchIndex;
		if (i !== null && i > -1) {
			Util.blockUI();
			this._advancedSearchController.delete(i, function() {
				Util.unblockUI();
			});
		}
	}

	this._paintSaveLoadPanel = function($container) {
		var _this = this;
		$container.empty();

		if (this._advancedSearchModel.searchStoreAvailable != true) {
			return;
		}

		var savedSearchOptions = [{
			label: 'load a saved search',
			value: -1,
			disabled: true,
			selected: _this._advancedSearchModel.selcetedSavedSearchIndex == -1,
		}];
		for (var i=0; i<this._advancedSearchModel.savedSearches.length; i++) {
			var savedSearch = this._advancedSearchModel.savedSearches[i];
			savedSearchOptions.push({
				label: savedSearch.name,
				value: i,
				selected: _this._advancedSearchModel.selcetedSavedSearchIndex == i,
			});
		}
		this._$savedSearchesDropdown = FormUtil.getPlainDropdown(savedSearchOptions);
		this._$savedSearchesDropdown.attr("id", "saved-search-dropdown-id");
		this._$savedSearchesDropdown.on("select2:select", function () {
            var i = _this._$savedSearchesDropdown.val();
            _this._advancedSearchController.selectSavedSearch(i);
        });
		$container.append(this._$savedSearchesDropdown);
		this._$savedSearchesDropdown.select2({
			width: '400px',
			theme: "bootstrap"
		});

		if (_this._advancedSearchModel.selcetedSavedSearchIndex > -1) {
			var $buttonClear = FormUtil.getButtonWithIcon('glyphicon-remove', function() {
				_this._advancedSearchController.clearSelection();
			}, null, 'Clear selection');
			$container.append($buttonClear);
		}

		var $buttonSave = FormUtil.getButtonWithIcon('glyphicon-floppy-disk', function() {
			_this._save();
		}, 'Save', null, "save-btn");
		$buttonSave.css({ 'margin-left': '8px'});
		$container.append($buttonSave);

		var $buttonDelete = FormUtil.getButtonWithIcon('glyphicon-trash', function() {
			_this._delete();
		}, 'Delete');
		$buttonDelete.css({ 'margin-left': '8px'});
		var i = this._advancedSearchModel.selcetedSavedSearchIndex;
		if (i == null || i < 0) {
			$buttonDelete.attr('disabled', '');
		}
		$container.append($buttonDelete);

	}

	this._paintTypeSelectionPanel = function($menuPanelContainer) {
		this._$entityTypeDropdown = this._getEntityTypeDropdown();
		var entityTypeDropdownFormGroup = FormUtil.getFieldForComponentWithLabel(this._$entityTypeDropdown, "Search For", null, true);
		entityTypeDropdownFormGroup.css("width","50%");
		$menuPanelContainer.append(entityTypeDropdownFormGroup);

		var andOrOptions = [{value : "AND", label : "AND", selected : true}, {value : "OR", label : "OR"}];
		this._$andOrDropdownComponent = FormUtil.getDropdown(andOrOptions, "Select logical operator");
		var _this = this;

		this._$andOrDropdownComponent.change(function() {
			_this._advancedSearchModel.criteria.logicalOperator = $(this).val();
		});

		$menuPanelContainer.append(FormUtil.getFieldForComponentWithLabel(this._$andOrDropdownComponent, "Using", null, true));

		var $submitButton = FormUtil.getButtonWithIcon('glyphicon-search', function() {
			_this._advancedSearchController.search();
		}, null, null, "search-btn");

		$submitButton.css("margin-top", "22px");
		var $submitButtonGroup = FormUtil.getFieldForComponentWithLabel($submitButton, "", null, true);

		$submitButtonGroup.css("margin-left", "0px");
		$menuPanelContainer.append($submitButtonGroup);
	}

	this._paintCriteriaPanel = function($searchCriteriaPanelContainer) {
		$searchCriteriaPanelContainer.empty();
		$searchCriteriaPanelContainer.append($("<legend>").append("Criteria"));

		this._paintTypeSelectionPanel(this._$searchCriteriaPanelContainer);

		// TODO put the rest in separate panel
		this._$rulesPanelContainer = $("<div>");
        this._paintRulesPanel(this._$rulesPanelContainer, this._advancedSearchModel.criteria.entityKind.startsWith("ALL"));
		$searchCriteriaPanelContainer.append(this._$rulesPanelContainer);
	}


    this._paintRulesPanel = function($container, isGlobalSearch) {
		$container.empty();
		var _this = this;
		var $table = $("<table>", { class : "table table-bordered"});
		$table.css({ 'margin-top': '10px' });
		$thead = $("<thead>");
		this._$tbody = $("<tbody>");

		//todo there should be ONE add button at the top! (?)
		this._$addButton = FormUtil.getButtonWithIcon('glyphicon-plus', function() {
            _this._paintInputRow(isGlobalSearch);
		});

		$table
			.append($thead)
			.append(this._$tbody);

		
        $row=$("<tr>");
        if (!isGlobalSearch) {
            $row.append($("<th>").text("NOT"))
        }
        $row.append($("<th>").text("Field Type"))
            .append($("<th>").text("Field Name"))
            .append($("<th>").text("Comparator Operator"))
            .append($("<th>").text("Field Value"))
            .append($("<th>", { "style" : "width : 56px !important;" }).append(this._$addButton));
        $thead.append($row);

		if ($.isEmptyObject(this._advancedSearchModel.criteria.rules)) {
            this._paintInputRow(isGlobalSearch);
		} else {
			for(var ruleKey in this._advancedSearchModel.criteria.rules) {
				if ( ! this._advancedSearchModel.criteria.rules[ruleKey].hidden) {
                    this._paintInputRow(isGlobalSearch, ruleKey);
				}
			}
		}

		$container.append($table);
	}

	//
	// Auxiliar Components Methods
	//

	// paints a row for the given ruleKey or a new empty row if no ruleKey is given
    this._paintInputRow = function(isGlobalSearch, ruleKey) {
		var _this = this;

		var uuidValue = null;
		if (ruleKey) {
			uuidValue = ruleKey;
		} else {
			var uuidValue = Util.guid();
			this._advancedSearchModel.criteria.rules[uuidValue] = { };
		}

		var $newFieldNameContainer = $("<td>");
		var $newFieldOperatorContainer = $("<td>");
		var $newFieldValueContainer = $("<td>");
		var $newRow = $("<tr>", { id : uuidValue });
		var $fieldTypeDropdown = this._getNewFieldTypeDropdownComponent($newFieldNameContainer, $newFieldOperatorContainer, $newFieldValueContainer, this._advancedSearchModel.criteria.entityKind, uuidValue);

        if (!isGlobalSearch) {
            $newRow.append($("<td>").append(this._getNegationOperatorDropdownComponent(uuidValue)));
        }
		$newRow.append($("<td>").append($fieldTypeDropdown))
                    .append($newFieldNameContainer)
                    .append($newFieldOperatorContainer)
                    .append($newFieldValueContainer)
					.append($("<td>").append(this._getMinusButtonComponentForRow(this._$tbody, $newRow)));

		this._$tbody.append($newRow);


		if(this._advancedSearchModel.forceFreeTextSearch) {
			this._advancedSearchModel.criteria.rules[uuidValue].value = this._advancedSearchModel.forceFreeTextSearch; //Update model
			this._advancedSearchModel.forceFreeTextSearch = undefined;
		}

		if(this._advancedSearchModel.forceLoadCriteria) {
			var rule = this._advancedSearchModel.criteria.rules[uuidValue];
            $fieldTypeDropdown.val(rule.type);
            this._injectNameField($newFieldNameContainer, $newFieldOperatorContainer, $newFieldValueContainer, uuidValue);
            this._injectOperatorField($newFieldOperatorContainer, $newFieldValueContainer, uuidValue);
		}
        this._injectValueField($newFieldValueContainer, uuidValue);
	}

    this._injectNameField = function($newFieldNameContainer, $newFieldOperatorContainer, $newFieldValueContainer, uuid) {
        var _this = this;
        $newFieldNameContainer.empty();
        var fieldType = _this._advancedSearchModel.criteria.rules[uuid].type;
        switch(fieldType) {
            case "All":
                $newFieldOperatorContainer.empty();
                _this._injectValueField($newFieldValueContainer, uuid);
                break;
            default:
                $mergedDropdown = _this._getNewMergedDropdown(_this._advancedSearchModel.criteria.entityKind, 
                        fieldType, $newFieldOperatorContainer, $newFieldValueContainer, uuid);
                $mergedDropdown.val(this._advancedSearchModel.criteria.rules[uuid].name);
                $newFieldNameContainer.append($mergedDropdown);
        }
    }
    
    this._injectOperatorField = function($newFieldOperatorContainer, $newFieldValueContainer, uuid) {
        var _this = this;
        $newFieldOperatorContainer.empty();
        var dataType = _this._getDataType(uuid);
        if (dataType) {
            var operatorOptions = null;

            if (dataType === "INTEGER" || dataType === "REAL") {
                operatorOptions = [ { value : "thatEqualsNumber",                    label : "thatEquals (Number)", selected : true },
                                    { value : "thatIsLessThanNumber",                label : "thatIsLessThan (Number)" },
                                    { value : "thatIsLessThanOrEqualToNumber",       label : "thatIsLessThanOrEqualTo (Number)" },
                                    { value : "thatIsGreaterThanNumber",             label : "thatIsGreaterThan (Number)" },
                                    { value : "thatIsGreaterThanOrEqualToNumber",    label : "thatIsGreaterThanOrEqualTo (Number)" }
                                  ];
            } else if (dataType === "TIMESTAMP" || dataType === "DATE") {
                operatorOptions = [ { value : "thatEqualsDate",                      label : "thatEquals (Date)", selected : true },
                                    { value : "thatIsLaterThanOrEqualToDate",        label : "thatIsLaterThanOrEqualTo (Date)" },
                                    { value : "thatIsLaterThanDate",                 label : "thatIsLaterThan (Date)" },
                                    { value : "thatIsEarlierThanOrEqualToDate",      label : "thatIsEarlierThanOrEqualTo (Date)" },
                                    { value : "thatIsEarlierThanDate",               label : "thatIsEarlierThan (Date)" }
                                  ];
            } else if (dataType === "TYPE" || dataType === "BOOLEAN" || dataType === "ARCHIVING_STATUS") {
                operatorOptions = [];
            } else if (dataType === "PERSON") {
                operatorOptions = [ { value : "thatEqualsUserId",                    label : "thatEqualsUserId (UserId)", selected : true },
                                    { value : "thatContainsFirstName",               label : "thatContainsFirstName (First Name)" },
                                    { value : "thatContainsLastName",                label : "thatContainsLastName (Last Name)" }
                                  ];
            } else {
                operatorOptions = [ { value : "thatContainsString",                  label : "thatContains (String)", selected : true },
                                    { value : "thatEqualsString",                    label : "thatEquals (String)" },
                                    { value : "thatStartsWithString",                label : "thatStartsWith (String)" },
                                    { value : "thatEndsWithString",                  label : "thatEndsWith (String)" }
                                  ];
            }

            if (operatorOptions && operatorOptions.length > 1) {
                var comparisonDropdown = FormUtil.getDropdown(operatorOptions, "Select Comparison operator");

                comparisonDropdown.change(function() {
                    var $thisComponent = $(this);
                    var selectedValue = $thisComponent.val();
                    _this._advancedSearchModel.criteria.rules[uuid].operator = selectedValue; //Update model
                    _this._injectValueField($newFieldValueContainer, uuid);
                });
                var operator = _this._getOperator(uuid);
                if (operator) {
                    comparisonDropdown.val(operator);
                    operator = comparisonDropdown.val(); // null if operator is an unknown drop down option
                }
                if (!operator) {
                    operator = operatorOptions[0].value;
                    if (_this._getDataType(uuid) === "CONTROLLEDVOCABULARY") {
                        operator = "thatEqualsString";
                    }
                    comparisonDropdown.val(operator);
                }
                _this._advancedSearchModel.criteria.rules[uuid].operator = operator;
                $newFieldOperatorContainer.append(comparisonDropdown);
            }
        }
    }

    this._createValueField = function(uuid) {
        var $fieldValue = $("<input>", { class : "form-control", type: "text" });
        $fieldValue.css({width : "100%" });

        this._setUpKeyHandling($fieldValue, uuid);
        this._injectValue($fieldValue, uuid);
        return $fieldValue;
    }
    
    this._setUpKeyHandling = function($fieldValue, uuid) {
        var _this = this;
        $fieldValue.keyup(function() {
            var $thisComponent = $(this);
            var selectedValue = $thisComponent.val();
            _this._advancedSearchModel.criteria.rules[uuid].value = selectedValue; //Update model
        });

        $fieldValue.keypress(function (e) {
            var key = e.which;
            if (key == 13) { // the enter key code
                _this._advancedSearchController.search();
                return false;
            }
        });
    }

    this._addTimestampField = function($container, uuid, isDateOnly) {
        var _this = this;
        var $dateField = FormUtil._getDatePickerField(uuid, "", false, isDateOnly);
        var $input = $dateField.find("#" + uuid);
        this._setUpKeyHandling($input, uuid);
        $input.blur(function() {
            _this._advancedSearchModel.criteria.rules[uuid].value = $input.val();
        });
        this._injectValue($input, uuid);
        $container.append($dateField);
    }

    this._addEntityTypeDropdownField = function($container, uuid, selectedValue) {
        var _this = this;
        createDropDownField = function(result) {
            var types = [];
            result.getObjects().forEach(function(type) {
                var label = Util.getDisplayLabelFromCodeAndDescription(type);
                types.push({value:type.getCode(), label:label});
            });
            var $valueDropdown = FormUtil.getDropdown(types, "Select a type");
            $valueDropdown.change(function() {
                _this._advancedSearchModel.criteria.rules[uuid].value = $valueDropdown.val();
            });
            _this._injectValue($valueDropdown, uuid);
            $container.append($valueDropdown);
        }
        if (selectedValue === "ATTR.EXPERIMENT_TYPE") {
            require([ "as/dto/experiment/search/ExperimentTypeSearchCriteria", "as/dto/experiment/fetchoptions/ExperimentTypeFetchOptions" ],
                    function(ExperimentTypeSearchCriteria, ExperimentTypeFetchOptions) {
                mainController.openbisV3.searchExperimentTypes(new ExperimentTypeSearchCriteria(), 
                        new ExperimentTypeFetchOptions()).done(createDropDownField);
            });
        } else if (selectedValue === "ATTR.SAMPLE_TYPE") {
            require([ "as/dto/sample/search/SampleTypeSearchCriteria", "as/dto/sample/fetchoptions/SampleTypeFetchOptions" ],
                    function(SampleTypeSearchCriteria, SampleTypeFetchOptions) {
                mainController.openbisV3.searchSampleTypes(new SampleTypeSearchCriteria(), 
                        new SampleTypeFetchOptions()).done(createDropDownField);
            });
        } else if (selectedValue === "ATTR.DATA_SET_TYPE") {
            require([ "as/dto/dataset/search/DataSetTypeSearchCriteria", "as/dto/dataset/fetchoptions/DataSetTypeFetchOptions" ],
                    function(DataSetTypeSearchCriteria, DataSetTypeFetchOptions) {
                mainController.openbisV3.searchDataSetTypes(new DataSetTypeSearchCriteria(), 
                        new DataSetTypeFetchOptions()).done(createDropDownField);
            });
        }
    }

    this._addBooleanDropdownField = function($container, uuid) {
        var _this = this;
        var terms = [{value:false, label:"false"}, {value:true, label:"true"}];
        var $valueDropdown = FormUtil.getDropdown(terms, "Select a value");
        $valueDropdown.change(function() {
            _this._advancedSearchModel.criteria.rules[uuid].value = $valueDropdown.val();
            _this._advancedSearchModel.criteria.rules[uuid].operator = "thatEqualsBoolean";
        });
        _this._injectValue($valueDropdown, uuid);
        $container.append($valueDropdown);
    }

    this._addArchivingStatusDropdownField = function($container, uuid) {
        var _this = this;
        var statuses = [
            {value : "AVAILABLE", label: "AVAILABLE"},
            {value : "LOCKED", label: "LOCKED"},
            {value : "ARCHIVED", label: "ARCHIVED"},
            {value : "UNARCHIVE_PENDING", label: "UNARCHIVE_PENDING"},
            {value : "ARCHIVE_PENDING", label: "ARCHIVE_PENDING"},
            {value : "BACKUP_PENDING", label: "BACKUP_PENDING"},
        ];
        var $valueDropdown = FormUtil.getDropdown(statuses, "Select a value");
        $valueDropdown.change(function() {
            _this._advancedSearchModel.criteria.rules[uuid].type = "Attribute";
            _this._advancedSearchModel.criteria.rules[uuid].name = "PHYSICAL_STATUS";
            _this._advancedSearchModel.criteria.rules[uuid].value = $valueDropdown.val();
        });
        _this._injectValue($valueDropdown, uuid);
        $container.append($valueDropdown);
    }

    this._addVocabularyDropdownField = function($container, uuid, vocabularyCode) {
        var _this = this;
        require([ "as/dto/vocabulary/id/VocabularyPermId", "as/dto/vocabulary/fetchoptions/VocabularyFetchOptions" ],
                function(VocabularyPermId, VocabularyFetchOptions) {
            var permId = new VocabularyPermId(vocabularyCode);
            var fetchOptions = new VocabularyFetchOptions();
            fetchOptions.withTerms();
            mainController.openbisV3.getVocabularies([permId], fetchOptions).done(function (result) {
                var terms = [];
                result[permId].getTerms().forEach(function(term) {
                    terms.push({value:term.getCode(), label:term.getLabel()});
                });
                var $valueDropdown = FormUtil.getDropdown(terms, "Select a term");
                $valueDropdown.change(function() {
                    _this._advancedSearchModel.criteria.rules[uuid].value = $valueDropdown.val();
                });
                _this._injectValue($valueDropdown, uuid);
                $container.append($valueDropdown);
            });
        });
    }

    this._addUserDropdownField = function($container, uuid) {
        var _this = this;
        require([ "as/dto/person/search/PersonSearchCriteria", "as/dto/person/fetchoptions/PersonFetchOptions" ],
                function(PersonSearchCriteria, PersonFetchOptions) {
            mainController.openbisV3.searchPersons(new PersonSearchCriteria(), new PersonFetchOptions()).done(function (result) {
                var users = [];
                result.getObjects().forEach(function(user) {
                    var userId = user.getUserId();
                    var label = userId;
                    if (user.getLastName()) {
                        label = user.getLastName();
                        if (user.getFirstName()) {
                            label = user.getFirstName() + " " + user.getLastName() 
                        }
                    }
                    users.push({value:userId, label:label});
                });
                var $valueDropdown = FormUtil.getDropdown(users, "Select a person");
                $valueDropdown.change(function() {
                    _this._advancedSearchModel.criteria.rules[uuid].value = $valueDropdown.val();
                });
                _this._injectValue($valueDropdown, uuid);
                $container.append($valueDropdown);
            });
        });
    }

    this._injectValue = function($valueField, uuid) {
        var value = this._getRuleValue(uuid);
        if (value) {
            $valueField.val(value);
            $valueField.val($valueField.val()); // resets drop downs if value is invalid 
        }
    }

    this._injectValueField = function($container, uuid) {
        $container.empty();
        var ruleName = this._getRuleName(uuid);
        var dataType = this._getDataType(uuid);
        var operator = this._getOperator(uuid);
        var propertyType = this._getPropertyType(uuid);
        if (dataType === "TIMESTAMP") {
            this._addTimestampField($container, uuid, false);
        } else if (dataType === "DATE") {
            this._addTimestampField($container, uuid, true);
        } else if (dataType === "BOOLEAN") {
            this._addBooleanDropdownField($container, uuid);
        } else if (dataType === "CONTROLLEDVOCABULARY" && operator === "thatEqualsString") {
            this._addVocabularyDropdownField($container, uuid, propertyType.vocabulary.code);
        } else if (dataType === "PERSON" && operator === "thatEqualsUserId") {
            this._addUserDropdownField($container, uuid);
        } else if (dataType === "TYPE") {
            this._addEntityTypeDropdownField($container, uuid, ruleName);
        } else if (dataType === "ARCHIVING_STATUS") {
            this._addArchivingStatusDropdownField($container, uuid);
        } else {
            $container.append(this._createValueField(uuid));
        }
    }

    this._getNegationOperatorDropdownComponent = function(uuid) {
        var _this = this;
        var $checkbox = $('<input>', {'type' : 'checkbox'})
        $checkbox.change(function() {
            _this._advancedSearchModel.criteria.rules[uuid].negate = $checkbox.is(":checked");
        });
        if (this._advancedSearchModel.criteria.rules[uuid].negate) {
            $checkbox.prop('checked', true);
        }
        return $('<div>', {'class' : 'checkbox'}).append($checkbox);
    }

	//should make new objects every time. otherwise, using the same object will produce odd results!
	//how to make an on-select event??
	this._getNewFieldTypeDropdownComponent = function($newFieldNameContainer, $newFieldOperatorContainer, $newFieldValueContainer, entityKind, uuid) {
		//Update dropdown component
        var logicalOperator = "AND";
        if (this._advancedSearchModel.criteria && this._advancedSearchModel.criteria.logicalOperator) {
            logicalOperator = this._advancedSearchModel.criteria.logicalOperator;
        }
        this._$andOrDropdownComponent.val(logicalOperator).trigger('change');
        this._advancedSearchModel.criteria.logicalOperator = logicalOperator;
		this._$andOrDropdownComponent.removeAttr("disabled");
		//
		var _this = this;
		var fieldTypeOptions = null;
		switch(entityKind) {
			case "ALL":
			case "ALL_PARTIAL":
			case "ALL_PREFIX":
				fieldTypeOptions = [{value : "All", label : "All", selected : true }];
				this._$andOrDropdownComponent.val("OR").trigger('change');
				this._advancedSearchModel.criteria.logicalOperator = "OR";
				this._$andOrDropdownComponent.attr("disabled", "").trigger('change');
                this._injectValueField($newFieldValueContainer, uuid);
				break;
			case "SAMPLE":
				fieldTypeOptions = [{value : "All", label : "All", selected : true },
				                    {value : "Property/Attribute", label : "Property"},
				                    {value : "Experiment", label : ELNDictionary.getExperimentDualName() },
				                    {value : "Parent", label : "Parent"},
				                    {value : "Children", label : "Children"}];
				break;
			case "EXPERIMENT":
				fieldTypeOptions = [{value : "All", label : "All", selected : true },
				                    {value : "Property/Attribute", label : "Property"}];
				break;
			case "DATASET":
				fieldTypeOptions = [{value : "All", label : "All", selected : true },
				                    {value : "Property/Attribute", label : "Property"},
				                    {value : "Sample", label : "" + ELNDictionary.Sample + ""},
				                    {value : "Experiment", label : ELNDictionary.getExperimentDualName() },
// ELN-UI don't support this yet
//				                    {value : "Parent", label : "Parent"},
//				                    {value : "Children", label : "Children"}
				                    ];
				break;
		}

		var $fieldTypeComponent = FormUtil.getDropdown(fieldTypeOptions, "Select Field Type");
		$fieldTypeComponent.change(function() {
			var $thisComponent = $(this);

			//Get uuid and value and update model (type only)
			var uuid = $($($thisComponent.parent()).parent()).attr("id");
			var selectedValue = $thisComponent.val();
			_this._advancedSearchModel.criteria.rules[uuid].type = selectedValue; //Update model
            _this._advancedSearchModel.criteria.rules[uuid].name = null;
            _this._injectNameField($newFieldNameContainer, $newFieldOperatorContainer, $newFieldValueContainer, uuid);
		});

		if(!this._advancedSearchModel.forceLoadCriteria) {
			this._advancedSearchModel.criteria.rules[uuid].type = "All"; //Update model with defaults
		}


		return $fieldTypeComponent;
	}

	this._getNewMergedDropdown = function(entityKind, fieldType, $newFieldOperatorContainer, $newFieldValueContainer, uuid) {
		var _this = this;
		var attributesModel = null;
		if(fieldType === "Experiment") {
			attributesModel = this._getFieldNameAttributesByEntityKind("EXPERIMENT");
		} else if(fieldType === "Sample") {
			attributesModel = this._getFieldNameAttributesByEntityKind("SAMPLE");
		} else {
			attributesModel = this._getFieldNameAttributesByEntityKind(entityKind);
		}
		attributesModel.push({ value : "", label : "-------------------------", disabled : true });
		var propertiesModel = this._getFieldNameProperties();
		var model = attributesModel.concat(propertiesModel);
		var $dropdown = FormUtil.getDropdown(model, "Select a property");
		$dropdown.change(function() {
			var $thisComponent = $(this);
			//Get uuid and value and update model (type only)
			var uuid = $($($thisComponent.parent()).parent()).attr("id");
			var selectedValue = $thisComponent.val();
			_this._advancedSearchModel.criteria.rules[uuid].name = selectedValue; //Update model
            _this._advancedSearchModel.criteria.rules[uuid].operator = null;
            _this._injectOperatorField($newFieldOperatorContainer, $newFieldValueContainer, uuid);
            _this._injectValueField($newFieldValueContainer, uuid);
        });
        $newFieldOperatorContainer.empty();
        $newFieldValueContainer.empty();
		return $dropdown;
	}

    this._getDataType = function(uuid) {
        var ruleName = this._getRuleName(uuid);
        if (ruleName === "ATTR.REGISTRATION_DATE" || ruleName === "ATTR.MODIFICATION_DATE") {
            return "TIMESTAMP";
        }
        if (ruleName === "ATTR.REGISTRATOR" || ruleName === "ATTR.MODIFIER") {
            return "PERSON";
        }
        if (ruleName === "ATTR.EXPERIMENT_TYPE" ||
                ruleName === "ATTR.SAMPLE_TYPE" ||
                ruleName === "ATTR.DATA_SET_TYPE") {
            return "TYPE";
        }
        if (ruleName === "ATTR.PRESENT_IN_ARCHIVE" || ruleName === "ATTR.STORAGE_CONFIRMATION") {
            return "BOOLEAN";
        }
        if (ruleName === "ATTR.SIZE") {
            return "INTEGER";
        }
        if (ruleName === "ATTR.STATUS") {
            return "ARCHIVING_STATUS";
        }
        var propertyType = this._getPropertyType(uuid);
        return propertyType ? propertyType.dataType : null;
    }

    this._getOperator = function(uuid) {
        if (this._advancedSearchModel.criteria.rules[uuid].operator) {
            return this._advancedSearchModel.criteria.rules[uuid].operator;
        }
        return null;
    }

    this._getPropertyType = function(uuid) {
        var ruleName = this._getRuleName(uuid);
        if (ruleName && ruleName.startsWith("PROP.")) {
            return profile.getPropertyType(ruleName.substring(5));
        }
        return null;
    }

    this._getRuleName = function(uuid) {
        if (this._advancedSearchModel.criteria.rules[uuid].name) {
            return this._advancedSearchModel.criteria.rules[uuid].name;
        }
        return null;
    }

    this._getRuleValue = function(uuid) {
        if (this._advancedSearchModel.criteria.rules[uuid].value) {
            return this._advancedSearchModel.criteria.rules[uuid].value;
        }
        return null;
    }

	this._getFieldNameProperties = function() {
		var model = [];
		var allProp = profile.getPropertyTypes();
		for(var pIdx = 0; pIdx < allProp.length; pIdx++) {
			var prop = allProp[pIdx];
			model.push({ value : "PROP." + prop.code, label : prop.label + " [" + prop.code + "]" });
		}

		model.sort(function(propertyA, propertyB) {
			return propertyA.label.localeCompare(propertyB.label);
		});

		return model;
	}

	this._getFieldNameAttributesByEntityKind = function(entityKind) {
		var model = null;
		switch(entityKind) {
			case "EXPERIMENT":
				model = [{ value : "ATTR.CODE", label : "Code [ATTR.CODE]" },
				         { value : "ATTR.EXPERIMENT_TYPE", label :  ELNDictionary.getExperimentDualName() + " Type [ATTR.EXPERIMENT_TYPE]" },
				         { value : "ATTR.PERM_ID", label : "Perm Id [ATTR.PERM_ID]" },
				         { value : "ATTR.PROJECT", label : "Project [ATTR.PROJECT]" },
				         { value : "ATTR.PROJECT_PERM_ID", label : "Project Perm Id [ATTR.PROJECT_PERM_ID]" },
				         { value : "ATTR.PROJECT_SPACE", label : "Project Space [ATTR.PROJECT_SPACE]" },
//				         { value : "ATTR.METAPROJECT", label : "Tag [ATTR.METAPROJECT]" }, TO-DO Not supported by ELN yet
				         { value : "ATTR.REGISTRATOR", label : "Registrator [ATTR.REGISTRATOR]" },
				         { value : "ATTR.REGISTRATION_DATE", label : "Registration Date [ATTR.REGISTRATION_DATE]" },
				         { value : "ATTR.MODIFIER", label : "Modifier [ATTR.MODIFIER]" },
				         { value : "ATTR.MODIFICATION_DATE", label : "Modification Date [ATTR.MODIFICATION_DATE]" }
				         ];
				break;
			case "SAMPLE":
				model = [];
				model.push({ value : "ATTR.CODE", label: "Code [ATTR.CODE]" });
				if(!this._advancedSearchModel.isSampleTypeForced) {
					model.push({ value : "ATTR.SAMPLE_TYPE", label: "" + ELNDictionary.Sample + " Type [ATTR.SAMPLE_TYPE]" });
				}
				model.push({ value : "ATTR.PERM_ID", label: "Perm Id [ATTR.PERM_ID]" });
				model.push({ value : "ATTR.SPACE", label: "Space [ATTR.SPACE]" });
//				model.push({ value : "ATTR.METAPROJECT", label: "Tag [ATTR.METAPROJECT]" }); //TO-DO Not supported by ELN yet
				model.push({ value : "ATTR.REGISTRATOR", label: "Registrator [ATTR.REGISTRATOR]" });
				model.push({ value : "ATTR.REGISTRATION_DATE", label: "Registration Date [ATTR.REGISTRATION_DATE]" });
				model.push({ value : "ATTR.MODIFIER", label: "Modifier [ATTR.MODIFIER]" });
				model.push({ value : "ATTR.MODIFICATION_DATE", label: "Modification Date [ATTR.MODIFICATION_DATE]" });
				break;
			case "DATASET":
				model = [{ value : "ATTR.CODE", label : "Code [ATTR.CODE]" },
				         { value : "ATTR.DATA_SET_TYPE", label : "Data Set Type [ATTR.DATA_SET_TYPE]" },
				         { value : "ATTR.STATUS", label : "Archiving status [ATTR.STATUS]" },
				         { value : "ATTR.PRESENT_IN_ARCHIVE", label : "Present in archive [ATTR.PRESENT_IN_ARCHIVE]" },
				         { value : "ATTR.STORAGE_CONFIRMATION", label : "Storage confirmation [ATTR.STORAGE_CONFIRMATION]" },
				         { value : "ATTR.SIZE", label : "Size (bytes) [ATTR.SIZE]" },
//				         { value : "ATTR.METAPROJECT", label : "Tag [ATTR.METAPROJECT]" }, TO-DO Not supported by ELN yet
				         { value : "ATTR.REGISTRATOR", label : "Registrator [ATTR.REGISTRATOR]" },
				         { value : "ATTR.REGISTRATION_DATE", label : "Registration Date [ATTR.REGISTRATION_DATE]" },
				         { value : "ATTR.MODIFIER", label : "Modifier [ATTR.MODIFIER]" },
				         { value : "ATTR.MODIFICATION_DATE", label : "Modification Date [ATTR.MODIFICATION_DATE]" },
				         ];
				break;
		}
		return model;
	}

    this._addToEntityTypeModel = function(model, entityKindAndType, label, selectedEntityKindAndType) {
        model.push({ value : entityKindAndType, label : label, selected : entityKindAndType == selectedEntityKindAndType})
    }

	this._getEntityTypeDropdown = function() {
        var _this = this;
        var globalSearchDefault = 'ALL_PREFIX';
        if (this._advancedSearchModel.globalSearchDefault) {
            globalSearchDefault = this._advancedSearchModel.globalSearchDefault;
        }
        var model = [];
        _this._addToEntityTypeModel(model, 'ALL_PREFIX', "All (prefix match, faster)", globalSearchDefault);
        _this._addToEntityTypeModel(model, 'ALL', "All (full word match, faster)", globalSearchDefault);
        _this._addToEntityTypeModel(model, 'ALL_PARTIAL', "All (partial match, slower)", globalSearchDefault);
        _this._addToEntityTypeModel(model, 'EXPERIMENT', ELNDictionary.getExperimentDualName(), null);
        _this._addToEntityTypeModel(model, 'SAMPLE', "" + ELNDictionary.Sample + "", null);
        _this._addToEntityTypeModel(model, 'DATASET', "Dataset", null);
        model.push({ value : '', label : "--------------", disabled : true });
        var sampleTypes = profile.getAllSampleTypes();
        for(var tIdx = 0; tIdx < sampleTypes.length; tIdx++) {
            var sampleType = sampleTypes[tIdx];
            var label = Util.getDisplayNameFromCode(sampleType.code);
            _this._addToEntityTypeModel(model, 'SAMPLE$' + sampleType.code, label, null);
        }

        if(!this._advancedSearchModel.forceLoadCriteria) {
            this._advancedSearchModel.resetModel(globalSearchDefault);
        }

        var $dropdown = FormUtil.getDropdown(model, 'Select Entity Type to search for');

        $dropdown.change(function() {
            var value = $(this).val();
            var isGlobalSearch = value.startsWith('ALL');
            if (isGlobalSearch) {
                mainController.serverFacade.setSetting("GLOBAL_SEARCH_DEFAULT", value);
            }
            var kindAndType = value.split("$");
            var entityKind = kindAndType[0]
			if(_this._advancedSearchModel.isAllRules()) {
				//1. update the entity type only in the model
				_this._advancedSearchModel.setEntityKind(entityKind);
				//2. change the field type dropdowns in the view
				var rows = _this._$tbody.children();
				for(var rIdx = 0; rIdx < rows.length; rIdx++) {
					var $row = $(rows[rIdx]);
					var tds = $row.children();
                    var $newFieldTypeComponent = _this._getNewFieldTypeDropdownComponent($(tds[2]), $(tds[3]), $(tds[4]), _this._advancedSearchModel.criteria.entityKind, $row.attr("id"));
                    $(tds[1]).empty();
                    $(tds[1]).append($newFieldTypeComponent);
				}
			} else {
				_this._advancedSearchModel.resetModel(kindAndType[0]); //Restart model
			}
            if (_this._$rulesPanelContainer) {
                _this._paintRulesPanel(_this._$rulesPanelContainer, isGlobalSearch);
            }

			if(kindAndType.length === 2) {
				var uuidValue = Util.guid();
				_this._advancedSearchModel.criteria.rules[uuidValue] = { };
				_this._advancedSearchModel.criteria.rules[uuidValue].type = 'Attribute';
				_this._advancedSearchModel.criteria.rules[uuidValue].name = 'ATTR.SAMPLE_TYPE';
				_this._advancedSearchModel.criteria.rules[uuidValue].value = kindAndType[1];
				_this._advancedSearchModel.criteria.rules[uuidValue].hidden = true;
				_this._advancedSearchModel.isSampleTypeForced = true;
			} else {
				_this._advancedSearchModel.isSampleTypeForced = false;
			}
		});

		return $dropdown;
	}

	this._getMinusButtonComponentForRow = function($tbody, $row) {
		var _this = this;
		var $minusButton = FormUtil.getButtonWithIcon('glyphicon-minus', function() {
			if($tbody.children().length > 1) {
				var uuid = $row.attr("id");
				delete _this._advancedSearchModel.criteria.rules[uuid];
				$row.remove();
			} else {
				Util.showUserError("There must be at least one row of search criteria present.");
			}
		});
		return $minusButton;
	}

	this.renderResults = function(criteria) {
		if (this.beforeRenderingHook) {
			this.beforeRenderingHook();
		}
		var isGlobalSearch = this._advancedSearchModel.criteria.entityKind === "ALL"
			|| this._advancedSearchModel.criteria.entityKind === "ALL_PARTIAL"
			|| this._advancedSearchModel.criteria.entityKind === "ALL_PREFIX";

		var isMultiselectable = true; //this._advancedSearchModel.criteria.entityKind === 'SAMPLE';

        this._dataGridController = this._getGridForResults(criteria, isGlobalSearch, isMultiselectable);

        if(isMultiselectable) {
            this.extraOptions = [{
                name : "Copy Identifiers",
                action : function(selected) {
                    var identifiers = null;
                    for(var sIdx = 0; sIdx < selected.length; sIdx++) {
                        if(identifiers === null) {
                            identifiers = "";
                        } else {
                            identifiers = identifiers + " ";
                        }
                        var entityKind = selected[sIdx]["@type"].substring(selected[sIdx]["@type"].lastIndexOf(".") + 1, selected[sIdx]["@type"].length);
                        identifiers = identifiers + ((entityKind === 'DataSet') ? selected[sIdx].permId : selected[sIdx].identifier);
                    }
                    Util.showInfo("Please copy:<br><textarea style='background: transparent; border: none; width:100%;'>" + identifiers + "</textarea>", null, true, "Close");
                }
            }];
        }

        this._dataGridController.init(this._$dataGridContainer, this.extraOptions);
	}

	this._getGridForResults = function(criteria, isGlobalSearch, isMultiselectable) {
			var _this = this;

			var columns = _this.firstColumns.concat([ {
				label : 'Entity Kind',
				property : 'entityKind',
				filterable: false,
				sortable : false,
				render : function(data) {
					if(data.entityKind === "Sample") {
						return ELNDictionary.Sample;
					} else if(data.entityKind === "Experiment") {
                        return ELNDictionary.getExperimentKindName(data.entityType);
					} else {
						return data.entityKind;
					}
				}
			}, {
                label : 'Name',
                property : '$NAME',
                exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PROPERTY("$NAME"),
                filterable: !isGlobalSearch,
                sortable : !isGlobalSearch,
                render : function(data) {
                    if(data[profile.propertyReplacingCode]) {
                        return _this._getLinkOnClick(data[profile.propertyReplacingCode], data);
                    } else {
                        return "";
                    }
                }
            }, {
                label : 'Identifier',
                property : 'identifier',
                exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.IDENTIFIER,
                filterable: !isGlobalSearch,
                sortable : !isGlobalSearch,
                render : function(data, grid) {
                    var paginationInfo = null;
               	    if(!isGlobalSearch) {
               		    var indexFound = null;
               			for(var idx = 0; idx < grid.lastReceivedData.objects.length; idx++) {
               			    if(grid.lastReceivedData.objects[idx].permId === data.permId) {
               			        indexFound = idx + (grid.lastUsedOptions.pageIndex * grid.lastUsedOptions.pageSize);
               				    break;
               				}
               			}

               			if(indexFound !== null) {
               			    paginationInfo = {
               				    pagFunction : _this._advancedSearchController.searchWithPagination(_this._advancedSearchModel.criteria, false),
               				    pagOptions : grid.lastUsedOptions,
               				    currentIndex : indexFound,
               				    totalCount : grid.lastReceivedData.totalCount
               				}
               		    }
               	    }
               		return _this._getLinkOnClick(data.entityKind === 'DataSet' ? data.permId : data.identifier,
						data, paginationInfo);
                }
            }, {
				label : 'Entity Type',
				property : 'entityType',
				filterable: !isGlobalSearch,
				sortable : !isGlobalSearch
			}, {
				label : 'Code',
				property : 'code',
				exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.CODE,
				filterable: !isGlobalSearch,
				sortable : !isGlobalSearch,
				render : function(data, grid) {
					var paginationInfo = null;
					if(!isGlobalSearch) {
						var indexFound = null;
						for(var idx = 0; idx < grid.lastReceivedData.objects.length; idx++) {
							if(grid.lastReceivedData.objects[idx].permId === data.permId) {
								indexFound = idx + (grid.lastUsedOptions.pageIndex * grid.lastUsedOptions.pageSize);
								break;
							}
						}

						if(indexFound !== null) {
							paginationInfo = {
									pagFunction : _this._advancedSearchController.searchWithPagination(_this._advancedSearchModel.criteria, false),
									pagOptions : grid.lastUsedOptions,
									currentIndex : indexFound,
									totalCount : grid.lastReceivedData.totalCount
							}
						}
					}
					var id = data.code.toLowerCase() + "-id";
					return _this._getLinkOnClick(data.code, data, paginationInfo, id);
				}
			}, {
				label : ELNDictionary.getExperimentDualName(),
				property : 'experiment',
				exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.EXPERIMENT,
				filterable: !isGlobalSearch,
				sortable : false
			}, {
				label : ELNDictionary.getSampleDualName(),
				property : 'sample',
				exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.SAMPLE,
				filterable: !isGlobalSearch,
				sortable : false
			}]);

			if (criteria.entityKind === "DATASET") {
                columns.push({
                    label : 'Archiving status',
                    property : 'status',
                    exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.ARCHIVING_STATUS,
                    filterable: !isGlobalSearch,
                    sortable : false,
                    renderFilter : function(params) {
                        return FormUtil.renderArchivingStatusGridFilter(params);
                    },
                });
                columns.push({
                    label : 'Present in archive',
                    property : 'presentInArchive',
                    exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.PRESENT_IN_ARCHIVE,
                    filterable: !isGlobalSearch,
                    sortable : false,
                    renderFilter : function(params) {
                        return FormUtil.renderBooleanGridFilter(params);
                    },
                    render : function(data) {
                        return data.presentInArchive == true ? "true" : "false"
                    }
                });
                columns.push({
                    label : 'Storage confirmation',
                    property : 'storageConfirmation',
                    exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.STORAGE_CONFIRMATION,
                    filterable: !isGlobalSearch,
                    sortable : false,
                    renderFilter : function(params) {
                        return FormUtil.renderBooleanGridFilter(params);
                    },
                    render : function(data) {
                        return data.storageConfirmation == true ? "true" : "false"
                    }
                });
                columns.push({
                    label : "Size (bytes)",
                    property : "size",
									  exportableProperty: DataGridExportOptions.EXPORTABLE_FIELD.SIZE,
                    sortable : false,
                    render : function(data, grid) {
                        return data.size;
                    }
                });
								columns.push({
									label : "Size",
									property : "sizeHumanReadable",
									sortable : false,
									render : function(data, grid) {
										return PrintUtil.renderNumberOfBytes(data.size);
									}
								});
            }

			columns = columns.concat(_this.additionalColumns);

			if(isGlobalSearch) {
				columns.push({
					label : 'Matched',
					property : 'matched',
					filterable: false,
					sortable : false,
					truncate: true,
					render : function(data, grid){
						return $("<div>").html(DOMPurify.sanitize(data.matched))
					}
				});

				columns.push({
					label : 'Rank',
					property : 'rank',
					filterable: false,
					sortable : false,
                    render : function(data, grid) {
                        var indexFound = null;
                        for(var idx = 0; idx < grid.lastReceivedData.objects.length; idx++) {
							var receivedObject = grid.lastReceivedData.objects[idx];
							if(receivedObject.permId === data.permId && receivedObject.entityKind === data.entityKind) {
                                indexFound = idx + (grid.lastUsedOptions.pageIndex * grid.lastUsedOptions.pageSize);
                        		break;
                            }
                        }
                        return indexFound + 1;
					}
				});
			}

			columns.push({
				label : '---------------',
				property : null,
				filterable: false,
				sortable : false
			});

			//Add properties as columns dynamically depending on the results
			var dynamicColumnsFunc = function(entities) {
				//1. Get properties with actual data
				var foundPropertyCodes = {};
				for(var rIdx = 0; rIdx < entities.length; rIdx++) {
					var entity = entities[rIdx].$object;
					if(isGlobalSearch) {
						switch(entity.objectKind) {
							case "SAMPLE":
								entity = entity.sample;
							break;
							case "EXPERIMENT":
								entity = entity.experiment;
							break;
							case "DATA_SET":
								entity = entity.dataSet;
							break;
						}
					}

					if(!entity) {
						continue;
					}
					for(var propertyCode in entity.properties) {
						if(entity.properties[propertyCode]) {
							foundPropertyCodes[propertyCode] = true;
						}
					}
				}

				//2. Get columns
				var propertyColumnsToSort = [];
				for(var propertyCode in foundPropertyCodes) {
					var propertiesToSkip = ["$NAME", "$XMLCOMMENTS", "$ANNOTATIONS_STATE"];
					if($.inArray(propertyCode, propertiesToSkip) !== -1) {
						continue;
					}

                    var propertyType = profile.getPropertyType(propertyCode)
                    var renderValue = null;
                    var renderFilter = null;
                    var getValue = null;

                    if(propertyType.dataType === "BOOLEAN"){
                        renderFilter = function(params){
                            return FormUtil.renderBooleanGridFilter(params);
                        };
                        renderValue = function(row, params){
                            return FormUtil.renderBooleanGridValue(params)
                        };
                        getValue = (function(propertyType) {
                            return function(params) {
                                return SampleDataGridUtil.getBooleanValue(params, propertyType);
                            };
                        })(propertyType);
                    } else if(propertyType.dataType === "CONTROLLEDVOCABULARY"){
                        renderFilter = (function(propertyType){
                            return function(params){
                                return FormUtil.renderVocabularyGridFilter(params, propertyType.vocabulary);
                            }
                        })(propertyType);
                        renderValue = function(row, params){
                            return FormUtil.renderArrayGridValue(params);
                        };
                    } else if(propertyType.dataType === "SAMPLE"){
                        renderValue = function(row, params){
                            return FormUtil.renderArrayGridValue(params);
                        };
                    } else if (propertyType.dataType === "DATE" || propertyType.dataType === "TIMESTAMP") {
                        renderFilter = (function(propertyType){
                            return function(params) {
                                return FormUtil.renderDateRangeGridFilter(params, propertyType.dataType)
                            }
                        })(propertyType)
                    } else if (propertyType.dataType === "XML"){
                        renderValue = (function(propertyType){
                            return function(row, params){
                                return FormUtil.renderXmlGridValue(row, params, propertyType)
                            }
                        })(propertyType)
                    } else if (propertyType.dataType === "MULTILINE_VARCHAR"){
                        renderValue = (function(propertyType){
                            return function(row, params){
                                return FormUtil.renderMultilineVarcharGridValue(row, params, propertyType)
                            }
                        })(propertyType)
                    }

					propertyColumnsToSort.push({
						label : propertyType.label,
						property : propertyCode,
						exportableProperty : DataGridExportOptions.EXPORTABLE_FIELD.PROPERTY(propertyCode),
						filterable : !isGlobalSearch,
						render: renderValue,
						renderFilter: renderFilter,
						getValue: getValue,
						sortable : !isGlobalSearch && propertyType.dataType !== "XML" && propertyType.dataType !== "BOOLEAN",
						truncate: true,
						metadata: {
							dataType: propertyType.dataType
						}
					});
				}

				FormUtil.sortPropertyColumns(propertyColumnsToSort, entities.map(function(entity){
					return {
						entityKind: entity.entityKind,
						entityType: entity.entityType
					}
				}))

				return propertyColumnsToSort;
			}

			var columnsLast = [];
			//4. Add registration/modification date columns
			columnsLast.push({
				label : '---------------',
				property : null,
				filterable: false,
				sortable : false
			});

			columnsLast.push({
				label : 'Registrator',
				property : 'registrator',
				exportableProperty : DataGridExportOptions.EXPORTABLE_FIELD.REGISTRATOR,
				filterable : !isGlobalSearch,
				sortable : false
			});

			columnsLast.push({
				label : 'Registration Date',
				property : 'registrationDate',
				exportableProperty : DataGridExportOptions.EXPORTABLE_FIELD.REGISTRATION_DATE,
				filterable : !isGlobalSearch,
				sortable : !isGlobalSearch,
				renderFilter : function(params) {
					return FormUtil.renderDateRangeGridFilter(params, "TIMESTAMP")
				}
			});

			columnsLast.push({
				label : 'Modifier',
				property : 'modifier',
				exportableProperty : DataGridExportOptions.EXPORTABLE_FIELD.MODIFIER,
				filterable : !isGlobalSearch,
				sortable : false
			});

			columnsLast.push({
				label : 'Modification Date',
				property : 'modificationDate',
				exportableProperty : DataGridExportOptions.EXPORTABLE_FIELD.MODIFICATION_DATE,
				filterable : !isGlobalSearch,
				sortable : !isGlobalSearch,
				renderFilter : function(params) {
					return FormUtil.renderDateRangeGridFilter(params, "TIMESTAMP")
				}
			});
			columnsLast = columnsLast.concat(_this.additionalLastColumns);

            var filterModes = isGlobalSearch ? [] : null
			var getDataRows = this._advancedSearchController.searchWithPagination(criteria, isGlobalSearch);
			var dataGrid = new DataGridController(this.resultsTitle, this._filterColumns(columns), columnsLast, dynamicColumnsFunc, getDataRows, null, false, this.configKeyPrefix + this._advancedSearchModel.criteria.entityKind, isMultiselectable, {
				fileFormat: DataGridExportOptions.FILE_FORMAT.XLS,
				filePrefix: 'advanced-search',
				fileContent: DataGridExportOptions.FILE_CONTENT.ENTITIES,
			}, 70, filterModes);
			return dataGrid;
	}

	this._getLinkOnClick = function(code, data, paginationInfo, id) {
		if(data.entityKind !== "Sample") {
			paginationInfo = null;  // TODO - Only supported for samples for now
		}
		switch(data.entityKind) {
			case "Experiment":
				return FormUtil.getFormLink(code, data.entityKind, data.identifier, paginationInfo, id);
				break;
			default:
				return FormUtil.getFormLink(code, data.entityKind, data.permId, paginationInfo, id);
				break;
		}
	}

	this._filterColumns = function(columns) {
		var _this = this;
		var filteredColumns = columns.filter(column => this.suppressedColumns.includes(column.property) == false);
		filteredColumns.forEach(function(column) {
			if (_this.hideByDefaultColumns.includes(column.property)) {
				column.showByDefault = false;
			}
		});
		return filteredColumns;
	}
}
