function Grid(columnsFirst, columnsLast, columnsDynamicFunc, getDataList, showAllColumns, tableSettings, onChangeState, isMultiselectable, forceFullContentHeight, heightPercentage, scrollbarWidth, title) {
	this.init(columnsFirst, columnsLast, columnsDynamicFunc, getDataList, showAllColumns, tableSettings, onChangeState, isMultiselectable, forceFullContentHeight, heightPercentage, scrollbarWidth, title);
}
$.extend(Grid.prototype, {
	init : function(columnsFirst, columnsLast, columnsDynamicFunc, getDataList, showAllColumns, tableSettings, onChangeState, isMultiselectable, forceFullContentHeight, heightPercentage, scrollbarWidth, title) {
		this.columnsFirst = columnsFirst;
		this.columnsDynamicFunc = columnsDynamicFunc;
		this.columnsDynamic = [];
		this.columnsLast = columnsLast;
		this.getDataList = getDataList;
		this.showAllColumns = showAllColumns;
		this.tableSettings = tableSettings;
		this.firstLoad = true;
		this.title = title;
		
		if(!this.tableSettings) {
			this.tableSettings = {
					columns : null,
					pageSize : null,
					sort : {
							sortProperty : null,
							sortDirection : null
					}
			}
		}
		this.onChangeState = onChangeState;
		
		this.isMultiselectable = isMultiselectable;
		this.selectedItems = [];
		if(isMultiselectable) {
			this.addMultiSelect(columnsFirst);
		}
		this.lastUsedColumns = [];
		this.forceFullContentHeight = forceFullContentHeight;
		if(heightPercentage) {
			this.maxHeight = ($(window).height() - LayoutManager.secondColumnHeader.outerHeight()) * (heightPercentage/100) - 30;
		}
		this.scrollbarWidth = scrollbarWidth;
	},
	getSearchOperator : function() {
		var thisGrid = this;
		var $filterOperatorCheckbox = $(thisGrid.panel).find(".repeater-search-operator");
		var operator = "AND";
		if($filterOperatorCheckbox.length > 0) {
			var isOr = $filterOperatorCheckbox[0].checked;
			operator = (isOr)?"OR":"AND";
		}
		return operator;
	},
	addMultiSelect : function(columns) {
		var _this = this;
		columns.unshift({
				showByDefault: true,
				label : function() {
					return "";
				},
				property : '$selected',
				isExportable: false,
				sortable : false,
				render : function(data) {
					var $checkbox = $("<input>", { type : 'checkbox' , class: "repeater-checkbox multi-selectable-checkbox"});

					for(var idx = 0; idx < _this.selectedItems.length; idx++) {
                        if(_this.selectedItems[idx].permId === data.$object.permId) {
                    	    $checkbox.prop('checked', true);
                        }
                    }

					$checkbox.change(function() {
						var isChecked = $(this).is(":checked");
						if(isChecked){ //add data to selectedItems
							_this.selectedItems.push(data.$object);
						} else { //remove data from selectedItems
							var toRemoveId = null;
							if(data.$object.permId) {
								toRemoveId = data.$object.permId;
							}
							if(toRemoveId) {
								for(var idx = 0; idx < _this.selectedItems.length; idx++) {
									var foundId = null;
									if(_this.selectedItems[idx].permId) {
										foundId = _this.selectedItems[idx].permId;
									}
									if(toRemoveId === foundId) {
										_this.selectedItems.splice(idx, 1);
									}
								}
							}
						}
					});
					$checkbox.click(function(e) {
						e.stopPropagation();
					});
					return $checkbox;
				}
		});
	},
	getSelected : function() {
		return this.selectedItems;
	},
	render : function() {
		var thisGrid = this;

		thisGrid.panel = $("<div>").addClass("fuelux");

		$.get("./lib/grid/js/Grid.html", function(template) {
			//Set default pageSize on template - there is no programming API and we don't want to modify the library
			var templateToReplace = null;
			var templateToReplaceFor = null;
			if(thisGrid.onChangeState && thisGrid.tableSettings && thisGrid.tableSettings.pageSize) {
				templateToReplace = "<li data-value=\"" + thisGrid.tableSettings.pageSize + "\">";
				templateToReplaceFor = "<li data-value=\"" + thisGrid.tableSettings.pageSize + "\" data-selected=\"true\">";
			} else {
				templateToReplace = "<li data-value=\"10\">";
				templateToReplaceFor = "<li data-value=\"10\" data-selected=\"true\">";
			}
			template = template.replace(templateToReplace, templateToReplaceFor);
			
			//
			thisGrid.panel.html(template);
			thisGrid.renderDropDownOptions();
			
			if(thisGrid.rowClickListeners && thisGrid.rowClickListeners.length > 0) {
				thisGrid.panel.addClass("fuelux-selectable");
			}
			
			var repeater = thisGrid.panel.repeater({
				defaultView : "list",
				dataSource : function(options, callback) {
					//Set default sort
					if(	thisGrid.onChangeState && //If settings are stored
						thisGrid.tableSettings && //And settings are available
						thisGrid.tableSettings.sort && thisGrid.tableSettings.sort.sortProperty && thisGrid.tableSettings.sort.sortDirection && //And sort settings are available
						!options.sortProperty && !options.sortDirection //And no sort options are available by default because the table just loaded
					) { //Set stored sort options to default
						options.sortProperty = thisGrid.tableSettings.sort.sortProperty;
						options.sortDirection = thisGrid.tableSettings.sort.sortDirection;
					}
					//
					if (options.view == "list") {
						//Save default pageSize
						if(thisGrid.onChangeState && thisGrid.tableSettings) {
							thisGrid.tableSettings.pageSize = options.pageSize;
							thisGrid.onChangeState(thisGrid.tableSettings);
						}
						//
						thisGrid.list(options, callback);
					}
				},
				staticHeight : 10,
				list_selectable : false,
				list_noItemsHTML : 'No items found',
				list_rowRendered : function(helpers, callback) {
					$(helpers.item).click(function() {
						var rowIndex = helpers.item[0].rowIndex;
						thisGrid.notifyRowClickListeners({
							"index" : rowIndex,
							"data" : thisGrid.result.datas[rowIndex-1],
							"item" : thisGrid.result.items[rowIndex-1]
						});
					});

					callback();
				}
			});
			thisGrid.viewOptions = repeater.data('fu.repeater').viewOptions;
		});
		
		return thisGrid.panel;
	},

	renderColumnDropdown : function(columnsForDropdown) {
		var thisGrid = this;

		var columnList = thisGrid.panel.find(".columnDropdown").find("ul");
		columnList.empty();
		columnList.click(function(e) {
			e.stopPropagation();
		});
		
		var defaultNumColumns = 5; //Including last always
		
		var currentColumns = this.getAllColumns();
		
		columnsForDropdown.forEach(function(column, columnIndex) {
			if(!column.showByDefault && !column.hide) {

				var checkbox = $("<input>")
				.attr("type", "checkbox")
				.attr("value", column.property)
				.attr("style", "margin-left: 5px;");

				if (column.property != null) {

				    var id = column.property + "-cln";
				    if (this.title !== undefined && this.title !== null) {
				        id = this.title + "-" + id;
				    }
				    id = id.split(" ").join("-").split(".").join("-");
				    if (id[0] === '$') {
				        id = id.substring(1, id.length - 1);
				    }
				    id = id.toLowerCase();
				    checkbox.attr("id", id)
                }
			
				if(thisGrid.tableSettings && thisGrid.tableSettings.columns && Object.keys(thisGrid.tableSettings.columns).length !== 0) {
					if((thisGrid.tableSettings.columns[column.property] === true)) { //If settings are present
						checkbox.attr("checked", "checked");
					}
				} else if(thisGrid.showAllColumns || columnIndex < (defaultNumColumns - 1) || (columnIndex+1 === currentColumns.length)) { //Defaults
					checkbox.attr("checked", "checked");
				}
				if (column.canNotBeHidden) {
					checkbox.attr("checked", "checked");
					checkbox.prop("disabled", true);
				}
				checkbox.change(function(e) {
					var $checkbox = $(this);
					var propertyName = $checkbox.prop('value');
					var isChecked = $checkbox.prop('checked');
					thisGrid.tableSettings.columns[propertyName] = isChecked;
					thisGrid.panel.repeater('render');
					e.stopPropagation();
				});
				var label = $("<label>", { style : 'white-space: nowrap;' }).attr("role", "menuitem").append(checkbox).append("&nbsp;").append(column.label);
				var item = $("<li>").attr("role", "presentation").append(label);
				columnList.append(item);
			}
		});
	},

	getAllColumns : function() {
		return this.columnsFirst.concat(this.columnsDynamic).concat(this.columnsLast);
	},

	addExtraOptions : function(extraOptions) {
		this.extraOptions = extraOptions;
	},
	
	renderDropDownOptions: function() {
		var thisGrid = this;
		var columnList = thisGrid.panel.find(".optionsDropdown").find("ul");
		
		
		var options = {
				"" : false,
				"(plain text)" : true
		}
		
		var getOptionClickEvent = function(isAllRowsOrVisible, isAllColumnsOrVisible, plainText) {
			return function() {
				thisGrid.exportTSV(isAllRowsOrVisible, isAllColumnsOrVisible, plainText);
			}
		}
		// Export shown rows with shown columns
		for(option in options) {
			var labelSRSC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
							.attr("role", "menuitem")
							.append("Export visible columns with visible rows " + option);

			var itemSRSC = $("<li>")
							.attr("role", "presentation")
							.attr("style", "margin-left: 5px; margin-right: 5px;")
							.append(labelSRSC);

			itemSRSC.click(getOptionClickEvent(false, false, options[option]));

			columnList.append(itemSRSC);
		}
		
		// Export shown rows with all columns
		for(option in options) {
			var labelSRAC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
							.attr("role", "menuitem")
							.append("Export all columns with visible rows " + option);
	
			var itemSRAC = $("<li>")
							.attr("role", "presentation")
							.attr("style", "margin-left: 5px; margin-right: 5px;")
							.append(labelSRAC);
			
			itemSRAC.click(getOptionClickEvent(false, true, options[option]));
			
			columnList.append(itemSRAC);
		}
		
		// Export all rows with visible columns
		for(option in options) {
			var labelARSC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
							.attr("role", "menuitem")
							.append("Export visible columns with all rows " + option);
	
			var itemARSC = $("<li>")
							.attr("role", "presentation")
							.attr("style", "margin-left: 5px; margin-right: 5px;")
							.append(labelARSC);
			
			var plainTextVal = options[option];
			itemARSC.click(getOptionClickEvent(true, false, options[option]));
			
			columnList.append(itemARSC);
		}
		
		// Export all rows with all columns
		for (option in options) {
		    var id = "export-all-columns-and-rows";
		    if (option.length > 0) {
		        id = id + "-" + option.split('(').join("").split(')').join("").split(' ').join("-")
		    }

			var labelARAC = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
							.attr("role", "menuitem")
							.attr("id", id)
							.append("Export all columns with all rows " + option);
	
			var itemARAC = $("<li>")
							.attr("role", "presentation")
							.attr("style", "margin-left: 5px; margin-right: 5px;")
							.append(labelARAC);
			
			var plainTextVal = options[option];
			itemARAC.click(getOptionClickEvent(true, true, options[option]));
			
			columnList.append(itemARAC);
		}
		
		
		if(this.extraOptions) {
			for(var oIdx = 0; oIdx < this.extraOptions.length; oIdx++) {
				var option = this.extraOptions[oIdx];
				var extraLabel = $("<label>", { style : 'white-space: nowrap; cursor:pointer;' })
									.attr("role", "menuitem")
									.append(option.name);

				var extraItem = $("<li>")
							.attr("role", "presentation")
							.attr("style", "margin-left: 5px; margin-right: 5px;")
							.append(extraLabel);
				
				var getClick = function(option) {
					return function() {
						option.action(thisGrid.getSelected());
					}
				}
				
				extraItem.click(getClick(option));
				
				columnList.append(extraItem);
			}
		}
	},
	
	exportTSV : function(isAllRowsOrVisible, isAllColumnsOrVisible, plainText) {
		var _this = this;
		var disablePlanTextExportWarning = this.tableSettings.disablePlanTextExportWarning;
		if(plainText  && !disablePlanTextExportWarning) {
			var dontShowAnymore = "<input type='checkbox' id='disablePlanTextExportWarning'> Don't show this warning again.";
			
			Util.showWarning("<b>DO NOT USE THIS FILE FOR BATCH UPDATE!</b><br>This file does not contain rich text format. If used for Batch Update, all rich text format in the updated entries will be lost!<br><br>" + dontShowAnymore, function() {
				var isSelected = $("#disablePlanTextExportWarning")[0].checked;
				if(_this.onChangeState) {
					_this.tableSettings.disablePlanTextExportWarning = isSelected;
					_this.onChangeState(_this.tableSettings);
				}
				_this.exportTSVB(isAllRowsOrVisible, isAllColumnsOrVisible, plainText);
			});
		} else {
			_this.exportTSVB(isAllRowsOrVisible, isAllColumnsOrVisible, plainText);
		}
	},
	exportTSVB : function(isAllRowsOrVisible, isAllColumnsOrVisible, plainText) {
		var thisGrid = this;
		
		function stringToUtf16ByteArray(str)
		{
		    var bytes = [];
		    bytes.push(255, 254);
		   for (var i = 0; i < str.length; ++i)
		   {
		       var charCode = str.charCodeAt(i);
		       bytes.push(charCode & 0xFF);  //low byte
		       bytes.push((charCode & 0xFF00) >>> 8);  //high byte (might be 0)
		   }
		    return bytes;
		}
		
		var exportColumnsFromData = function(namePrefix, data, headings) {
			if(data.objects) {
				data = data.objects
			}
			var arrayOfRowArrays = [];
			arrayOfRowArrays.push(headings);
			for(var dIdx = 0; dIdx < data.length; dIdx++) {
				var rowAsArray = [];
				for(var hIdx = 0; hIdx < headings.length; hIdx++) {
					var headerKey = headings[hIdx];
					var rowValue = data[dIdx][headerKey];
					if(!rowValue) {
						rowValue = "";
					} else {
						var specialCharsRemover = document.createElement("textarea");
						specialCharsRemover.innerHTML = rowValue;
						rowValue = specialCharsRemover.value; //Removes special HTML Chars
						rowValue = String(rowValue).replace(/\r?\n|\r|\t/g, " "); //Remove carriage returns and tabs
						if(plainText === true){
							rowValue = String(rowValue).replace(/<(?:.|\n)*?>/gm, '');
						}
					}
					rowAsArray.push(rowValue);
				}
				arrayOfRowArrays.push(rowAsArray);
			}
			var tsv = $.tsv.formatRows(arrayOfRowArrays);
			var indexOfFirstLine = tsv.indexOf('\n');
			var tsvWithoutNumbers = tsv.substring(indexOfFirstLine + 1);
			
			//
			var csvContentEncoded = null;
			var out = null;
			var charType = null;
			
			var utf16bytes = stringToUtf16ByteArray(tsvWithoutNumbers);
			var utf16bytesArray = new Uint8Array( utf16bytes.length );
			utf16bytesArray.set( utf16bytes , 0 );			
			var blob = new Blob([utf16bytesArray], {type: 'text/tsv;charset=UTF-16LE;'});
			saveAs(blob,'exportedTable' + namePrefix + '.tsv');
		}
		
		var headings = [];
		var data = [];
		var prefix = "";
		
		if(isAllColumnsOrVisible) {
			var currentColumns = this.getAllColumns();
			currentColumns.forEach(function(head) {
				if(head.isExportable === true || head.isExportable === undefined) {
					headings.push(head.property);
				}
			});
			prefix += "AllColumns";
		} else {
			thisGrid.getVisibleColumns().forEach(function(head) {
				if(head.isExportable === true || head.isExportable === undefined) {
					headings.push(head.property);
				}
			});
			prefix += "VisibleColumns";
		}
		
		if(isAllRowsOrVisible) {
			var allData = thisGrid.getDataList(function(allData) {
				data = allData;
				prefix += "AllRows";
				exportColumnsFromData(prefix, data, headings);
			});
		} else {
			data = thisGrid.result.datas;
			prefix += "VisibleRows";
			exportColumnsFromData(prefix, data, headings);
		}
	},
	
	getVisibleColumns : function() {
		var _this = this;
		var columns = [];

		var columnsModel = {};
		var maxColumns = 200;
		var enabledColumns = 0;
		
		_this.getAllColumns().forEach(function(column) {
			var checkBoxForColumn = _this.panel.find(".columnDropdown").find("[value='" + column.property + "']");
			var isChecked = (checkBoxForColumn.length === 1 && checkBoxForColumn[0] && checkBoxForColumn[0].checked)?true:false;
			if(column.showByDefault || isChecked && !column.hide) {
				if(enabledColumns > maxColumns) {
					// Ignore
				} else {
					columns.push(column);
					columnsModel[column.property] = true;
					enabledColumns++;
				}
			}
			
		});

		if(enabledColumns > maxColumns) {
			Util.showError("Only the first " + maxColumns + " selected columns will be shown.", function(){}, true);
		}
		
		if(this.onChangeState) {
			if(this.tableSettings.columns) {
				for(key in columnsModel) {
					this.tableSettings.columns[key] = columnsModel[key];
				}
			} else {
				this.tableSettings.columns = columnsModel;
			}
			this.onChangeState(this.tableSettings);
		}
		
		// HACK: Add a dummy empty column (repeater does not properly handle visibility of the last column)
		columns.push({
			label : null,
			property : null,
			sortable : false
		});

		return columns;
	},

	filterData : function(dataList, filter) {
		var thisGrid = this;
		var currentColumns = this.getAllColumns();
		if (filter) {
			filterKeywords = filter.toLowerCase().split(/[ ,]+/); //Split by regular space or comma
			dataList = dataList.filter(function(data) {
				var isValid = new Array(filterKeywords.length);
				
				for(cIdx = 0; cIdx < currentColumns.length; cIdx++) {
					var column = currentColumns[cIdx];
					for(var fIdx = 0; fIdx < filterKeywords.length; fIdx++) {
						var filterKeyword = filterKeywords[fIdx];
						if (column.filter) {
							isValid[fIdx] = isValid[fIdx] || column.filter(data, filterKeyword);
						} else {
							var value = "" + data[column.property];
							isValid[fIdx] = isValid[fIdx] || (value !== null && value.toLowerCase().indexOf(filterKeyword) !== -1);
						}
					}
				}
				
				var filterOperator = thisGrid.getSearchOperator();
				var isFinallyValid;
				if(filterOperator === "AND") {
					isFinallyValid = true;
				} else if(filterOperator === "OR") {
					isFinallyValid = false;
				}
				for(var fIdx = 0; fIdx < filterKeywords.length; fIdx++) {
					if(filterOperator === "AND") {
						isFinallyValid = isFinallyValid && isValid[fIdx];
					} else if(filterOperator === "OR") {
						isFinallyValid = isFinallyValid || isValid[fIdx];
					}
				}
				
				return isFinallyValid;
			});
		}

		return dataList;
	},

	sortData : function(dataList, sortProperty, sortDirection) {
		var thisGrid = this;

		if (sortProperty && sortDirection) {
			//Save sort configuration
			if(thisGrid.onChangeState && thisGrid.tableSettings) {
				thisGrid.tableSettings.sort = {
						sortProperty : sortProperty,
						sortDirection : sortDirection
				};
				thisGrid.onChangeState(thisGrid.tableSettings);
			}
			//
			
			var sortColumn = null;
			var currentColumns = this.getAllColumns();
			currentColumns.forEach(function(column) {
				if (column.property == sortProperty) {
					sortColumn = column;
				}
			});
			if (sortColumn) {
				var sortFunction = null;
				var sortDirection = sortDirection == "asc" ? 1 : -1;

				if (sortColumn.sort) {
					sortFunction = sortColumn.sort;
				} else {
					sortFunction = function(data1, data2) {
						var value1 = data1[sortColumn.property];
						var value2 = data2[sortColumn.property];
						return naturalSort(value1, value2);
					};
				}

				dataList.sort(function(data1, data2) {
					return sortDirection * sortFunction(data1, data2);
				});
			}
		}

		return dataList;
	},

	renderData : function(dataList) {
		var thisGrid = this;
		var items = [];
		var maxLineLength = 200;
		
		var visibleColumns = thisGrid.getVisibleColumns();
		
		dataList.forEach(function(data) {
			var item = {};
			visibleColumns.forEach(function(column) {
				//1. Render
				var value = null;
				if (column.render) {
					value = column.render(data, thisGrid);
				} else {
					value = data[column.property];
				}
				
				//2. Sanitize
				var value = FormUtil.sanitizeRichHTMLText(value);
				
				//3. Shorten
				var finalValue = null;
				if(value && value.length > maxLineLength) {
					finalValue = value.substring(0, maxLineLength) + "...";
				} else {
					finalValue = value;
				}
				
				//4. Tooltip
				if(value !== finalValue) {
					finalValue = $("<div>").html(finalValue);
					finalValue.tooltipster({
		                content: $("<span>").html(value)
		            });
				}
				
				item[column.property] = finalValue;
			});
			items.push(item);
		});

		return items;
	},

	list : function(options, callback) {
		options.searchOperator = this.getSearchOperator();
		var thisGrid = this;
		thisGrid.lastUsedOptions = options;
		
		if(thisGrid.firstLoad) {
			$(thisGrid.panel).hide();
			Util.blockUI();
		}
		
		thisGrid.getDataList(function(dataListResult) {
			dataList = null;
			var isDynamic = (dataListResult.totalCount != null && dataListResult.totalCount != undefined);
			
			if(isDynamic) {
				var $and = $(thisGrid.panel).find(".repeater-search-operator-and");
				$and.empty();
				$and.append("AND");
				
				var $or = $(thisGrid.panel).find(".repeater-search-operator-or");
				$or.empty();
				$or.append("OR");
			}
			
			if(isDynamic) {
				dataList = dataListResult.objects;		
				thisGrid.lastReceivedData = dataListResult;
				if(thisGrid.onChangeState && thisGrid.tableSettings) {
					thisGrid.tableSettings.sort = {
							sortProperty : options.sortProperty,
							sortDirection : options.sortDirection
					};
					thisGrid.onChangeState(thisGrid.tableSettings);
				}
				
			} else { //Used for static tables filtering and sorting, on dynamic ones it happens on the getDataList function given the options
				dataList = dataListResult;
				dataList = thisGrid.filterData(dataList, options.search);
				dataList = thisGrid.sortData(dataList, options.sortProperty, options.sortDirection);
			}
			

			var result = {};
			
			if(isDynamic) {
				result.count = dataListResult.totalCount;
			} else {
				result.count = dataList.length;
			}
			
			result.datas = [];
			result.items = [];
			
			result.page = options.pageIndex;
			result.pages = Math.ceil(result.count / options.pageSize);
			result.start = options.pageIndex * options.pageSize;
			result.end = result.start + options.pageSize;
			result.end = (result.end <= result.count) ? result.end : result.count;
			
			if(!isDynamic) {
				dataList = dataList.slice(result.start, result.end);
			}
			
			if(thisGrid.columnsDynamicFunc) {
				thisGrid.columnsDynamic = thisGrid.columnsDynamicFunc(dataList);
			}
			thisGrid.renderColumnDropdown(thisGrid.getAllColumns());
			
			if(!thisGrid.lastUsedColumns || thisGrid.lastUsedColumns.length === 0) {
				thisGrid.lastUsedColumns = thisGrid.getVisibleColumns();
				result.columns = thisGrid.lastUsedColumns;
			} else {
				var newColumns = thisGrid.getVisibleColumns();
				if(newColumns.length === thisGrid.lastUsedColumns.length) { //No changes
					result.columns = thisGrid.lastUsedColumns;
				} else if(newColumns.length > thisGrid.lastUsedColumns.length) { //We added one column, first not matching column, we add to last used
					var newLastUsedColumns = [];
					for(var cIdx = 0; cIdx < thisGrid.lastUsedColumns.length; cIdx++) {
						newLastUsedColumns.push(thisGrid.lastUsedColumns[cIdx]);
					}
					for(var cIdx = 0; cIdx < newColumns.length; cIdx++) {
						if(newColumns[cIdx].property !== newLastUsedColumns[cIdx].property) {
							newLastUsedColumns.splice(cIdx, 0, newColumns[cIdx]);
						}
					}
					thisGrid.lastUsedColumns = newLastUsedColumns;
					result.columns = thisGrid.lastUsedColumns;
				} else { //We removed one column, first not matching column, we remove from last used
					var newLastUsedColumns = [];
					for(var cIdx = 0; cIdx < thisGrid.lastUsedColumns.length; cIdx++) {
						newLastUsedColumns.push(thisGrid.lastUsedColumns[cIdx]);
					}
					for(var cIdx = 0; cIdx < newColumns.length; cIdx++) {
						if(newColumns[cIdx].property !== newLastUsedColumns[cIdx].property) {
							newLastUsedColumns.splice(cIdx, 1);
						}
					}
					thisGrid.lastUsedColumns = newLastUsedColumns;
					result.columns = thisGrid.lastUsedColumns;
				}
			}
			
			if(dataList.length === 0) { //Special case, empty table
				result.start = 0;
			} else { //Normal Case
				result.start = result.start + 1;
			}
			
			itemList = thisGrid.renderData(dataList);
			itemList.forEach(function(item, index) {
				result.datas.push(dataList[index]);
				result.items.push(item);
			});

			// add some delay (repeater does not properly layout columns without it)
			setTimeout(function() {
				thisGrid.result = result;
				callback(result);

// TODO Remove, doesn't seem necessary anymore, probably because now tables have their own scroll and fix height, is also not compatible with CKEditor5 Tables.
//				 HACK: Fixes extra headers added on this fuelux 3.1.0 when rendering again
//				var tableHeads = $(thisGrid.panel).find('thead');
//				if(tableHeads.length > 1) {
//					for(var hIdx = 0; hIdx < tableHeads.length -1; hIdx++) {
//						var bugHeader = $(tableHeads[hIdx]);
//						bugHeader.remove();
//					}
//				}
				
				LayoutManager.isLoadingView = true; // Disable views reload by resize events
				$(window).trigger('resize'); // HACK: Fixes table rendering issues when refreshing the grid on fuelux 3.1.0 for all browsers
				LayoutManager.isLoadingView = false; // Enable views reload by resize events
				
				if(thisGrid.firstLoad) {
					Util.unblockUI();
					$(thisGrid.panel).show(0); // HACK: Fixes Chrome rendering issues when refreshing the grid on fuelux 3.1.0
					thisGrid.firstLoad = false;
				}
				
//              Fix table width since fuelux 3.1.0
//				var newWidth = $(thisGrid.panel).find(".repeater-list-wrapper > .table").width();
//				$(thisGrid.panel).find(".repeater").width(newWidth);
				thisGrid.calculateHeight();
				
				var optionsDropdowns = $(".dropdown.table-options-dropdown");
				for(var i = 0; i < optionsDropdowns.length; i++) {
					var $dropdownTD = $(optionsDropdowns[i]).parent();
					$dropdownTD.css({ "overflow" : "visible" });
				}
				
				if(thisGrid.isMultiselectable) {
					var allCheckboxes = thisGrid.panel.find(".multi-selectable-checkbox");
					for(var cIdx = 0; cIdx < allCheckboxes.length; cIdx++) {
						var $parent = $($(allCheckboxes[cIdx]).parent());
						$parent.click(function(e) {
							e.stopPropagation();
						});
						$parent.css("cursor", "initial");
					}
				}
				
			}, 100);
		}, options);
	},
	
	calculateHeight : function() {
		var thisGrid = this;
		var $panel = $(thisGrid.panel);
		var $header = $panel.find(".repeater-header");
		var headerHeight = $header.outerHeight(true);
		var listHeight = Math.max(144, $(thisGrid.panel).find(".repeater-list").outerHeight(true));
		var footerHeight = $panel.find(".repeater-footer").outerHeight(true);
		var viewport = $panel.find(".repeater-canvas")[0];
		var scrollbarHeight = viewport.scrollWidth > viewport.offsetWidth ? thisGrid.scrollbarWidth : 0;
		var totalHeight = headerHeight + listHeight + footerHeight + scrollbarHeight;
		if(thisGrid.forceFullContentHeight) {
		    thisGrid.viewOptions.staticHeight = totalHeight;
		} else {
		    totalHeight = Math.min(totalHeight, thisGrid.maxHeight);
            if (thisGrid.viewOptions.staticHeight < totalHeight) {
                thisGrid.viewOptions.staticHeight = totalHeight;
            }
		}

	},

	addRowClickListener : function(listener) {
		if (!this.rowClickListeners) {
			this.rowClickListeners = [];
		}
		this.rowClickListeners.push(listener);
	},

	notifyRowClickListeners : function(event) {
		if (this.rowClickListeners) {
			this.rowClickListeners.forEach(function(listener) {
				listener(event);
			});
		}
	}

});
