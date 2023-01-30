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
function FreeFormTableView(freeFormTableController, freeFormTableModel) {
	this._freeFormTableController = freeFormTableController;
	this._freeFormTableModel = freeFormTableModel;
	this._container = null;
	
	this._getDefaultSizesDropdown = function(tableData, $wrappedTable) {
		var _this = this;
		var $component = $("<select>", { class : 'form-control', 'style' : 'width:250px; height:37px; display:inline;'});
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text('Default Sizes'));
		$component.append($("<option>").attr('value', '1x1').text('1x1'));
		$component.append($("<option>").attr('value', '8x12').text('96 wells: 8x12'));
		$component.append($("<option>").attr('value', '6x8').text('48 wells: 6x8'));
		$component.append($("<option>").attr('value', '4x6').text('24 wells: 4x6'));
		$component.append($("<option>").attr('value', '16x24').text('384 wells: 16x24'));
		
		var changeEvent = function(tableData, $wrappedTable) {
			return function(){
				var newSize = $(this).val().split('x');
				if(newSize.length === 2) {
					_this._freeFormTableController.changeSize(parseInt(newSize[0]), parseInt(newSize[1]), tableData, $wrappedTable);
				}
			}
		}
		$component.change(changeEvent(tableData, $wrappedTable));
		Select2Manager.add($component);
		return $component;
	}
	
	this._getSwitchForTable = function(tableData, $wrappedTable) {
		var uniqueId = Util.guid();
		var _this = this;
		var $switch = $("<div>", {'id' : 'SwitchFreeFormTable_' + uniqueId, "class" : "switch-toggle well", "style" : "width:33%; margin-left: auto; margin-right: auto; min-height: 38px !important;"});
		var changeEvent = function(tableData, $tableContainer) {
			return function(event) {
				var isDetailed = $(this).children()[0].checked;
				var tableView = null;
				$tableContainer.empty();
				
				if(isDetailed) {
					tableView = _this._getDetailedTable(tableData);
				} else {
					tableView = _this._getMiniTable(tableData);
				}
				
				$tableContainer.append(tableView);
			}
		}
		
		$switch.change(changeEvent(tableData, $wrappedTable));
		
		$switch
			.append($("<input>", {"value" : "detailed", "id" : "tableModeDetailed_" + uniqueId,"name" : "tableMode_" + uniqueId, "type" : "radio", "checked" : ""}))
			.append($("<label>", {"for" : "tableModeDetailed_" + uniqueId, "onclick" : "", "style" : "padding-top:3px;"}).append("Detailed"))
			.append($("<input>", {"value" : "mini", "id" : "tableModeMini_" + uniqueId, "name" : "tableMode_" + uniqueId, "type" : "radio"}))
			.append($("<label>", {"for" : "tableModeMini_" + uniqueId, "onclick" : "", "style" : "padding-top:3px;"}).append("Mini"));
		
		$switch.append($("<a>", {"class" : "btn btn-primary"}));
		return $switch;
	}
	
	this._getFocusEvent = function(tableData, rIdx, cIdx) {
		var _this = this;
		return function() {
			_this._freeFormTableModel.selectedField = {
				tableData : tableData,
				rowIdx : rIdx,
				columnIdx : cIdx
			};
		};
	}

	this._getFocusEventAction = function(tableData, isActionPossible, action) {
		var _this = this;
		return function() {
			var selectedField = _this._freeFormTableModel.selectedField;
			if (selectedField && 
				tableData === selectedField.tableData &&
				isActionPossible(selectedField)) {
				action(selectedField);
			} else {
				alert('Please select a valid position to do this action on this table');
			}
		}
	}

////	this._getBlurEvent = function() {
////		var _this = this;
////		return function() {
////			var blurWithTimeout = function() {
////				_this._freeFormTableModel.selectedField = null;
////			}
////			setTimeout(blurWithTimeout, 1000);
////		};
////	}
//	
	this._getMiniTable = function(tableData) {
		var _this = this;
		var $colsTitle = $("<h4>").append("Columns");
		var $colsContainer = $("<div>");
		for(var i = 0; i < tableData.modelMini.columns.length; i++) {
			var fieldValue = (tableData.modelMini.columns[i])?tableData.modelMini.columns[i]:"";
			if(this._freeFormTableModel.isEnabled) {
				var $textField = FormUtil._getInputField('text', null, "Column " + (i+1), null, false);
				$textField.val(fieldValue);
				var keyUpEvent = function(columIdx, modelMini) {
					return function() {
						modelMini.columns[columIdx] = $(this).val();
						_this._freeFormTableController.save();
					};
				}
				$textField.keyup(keyUpEvent(i, tableData.modelMini));
				$textField.focus(_this._getFocusEvent(tableData, null, i));
//				$textField.blur(_this._getBlurEvent());
				$colsContainer.append(FormUtil.getFieldForComponentWithLabel($textField, "Column " + (i+1)));
			} else {
				$colsContainer.append(FormUtil.getFieldForLabelWithText("Column " + (i+1), fieldValue));
			}
		}
		var $rowsTitle = $("<h4>").append("Rows");
		var $rowsContainer = $("<div>");
		for(var i = 0; i < tableData.modelMini.rows.length; i++) {
			var fieldValue = (tableData.modelMini.rows[i])?tableData.modelMini.rows[i]:"";
			if(this._freeFormTableModel.isEnabled) {
				var $textField = FormUtil._getInputField('text', null, "Row " + (i+1), null, false);
				$textField.val(fieldValue);
				var keyUpEvent = function(rowIdx, modelMini) {
					return function() {
						modelMini.rows[rowIdx] = $(this).val();
						_this._freeFormTableController.save();
					};
				}
				$textField.keyup(keyUpEvent(i, tableData.modelMini));
				$textField.focus(_this._getFocusEvent(tableData, i, null));
//				$textField.blur(_this._getBlurEvent());
				$rowsContainer.append(FormUtil.getFieldForComponentWithLabel($textField, "Row " + (i+1)));
			} else {
				$rowsContainer.append(FormUtil.getFieldForLabelWithText("Row " + (i+1), fieldValue));
			}
		}
		var $container = $("<div>")
							.append($colsTitle)
							.append($colsContainer)
							.append($rowsTitle)
							.append($rowsContainer);
		
		return $container;
	}
	
	this._getDetailedTable = function(tableData) {
		var _this = this;
		var $table = $("<table>", { 'class' : 'table table-bordered'});
		for(var i = 0; i < tableData.modelDetailed.length; i++) {
			var $row = $("<tr>");
			$table.append($row);
			for(var j = 0; j < tableData.modelDetailed[i].length; j++) {
				var $column = $("<td>", { 'style' : 'border: 1px solid #AAAAAA; height: 34px;' });
				$row.append($column);
				
				if(this._freeFormTableModel.isEnabled) {
					var $textField = FormUtil._getInputField('text', null, "Pos (" + (i+1) + "," + (j+1) + ")", null, false);
					$textField.val(tableData.modelDetailed[i][j]);
					
					var keyUpEvent = function(rowIdx, columIdx, modelDetailed) {
						return function() {
							modelDetailed[rowIdx][columIdx] = $(this).val();
							_this._freeFormTableController.save();
						};
					}
					$textField.keyup(keyUpEvent(i, j, tableData.modelDetailed));
					$textField.focus(_this._getFocusEvent(tableData, i, j));
//					$textField.blur(_this._getBlurEvent());
					
					$column.append($textField);
				} else {
					$column.append(tableData.modelDetailed[i][j]);
				}
			}
		}
		return $table;
	}
	
	this._getTableWithContainer = function(tableData) {
		var _this = this;
		var $tableContainer = $("<div>", {"style" : "margin:5px; border-radius:4px 4px 4px 4px; overflow:auto;" });
		$tableContainer.css({
			'background-color' : '#EEEEEE',
			'padding' : '10px'
		});
		
		var $title = null;
		
		if(this._freeFormTableModel.isEnabled) {
			$title = $("<input>", { 'type' : 'text', 'style' : 'width:250px;' });
			$title.val(tableData.name);
			var keyUpEvent = function(tableData) {
				return function() {
					tableData.name = $(this).val();
					_this._freeFormTableController.save();
				};
			}
			$title.keyup(keyUpEvent(tableData));
		} else {
			$title = $("<h3>");
			$title.append(tableData.name);
		}
		
		var $wrappedTable = $("<div>", { 'style' : 'margin-top:10px;' }).append(this._getDetailedTable(tableData));
		
		var $switch = this._getSwitchForTable(tableData, $wrappedTable);
		
		var $toolBar = $("<span>", { 'style' : 'margin-left:150px;' });
		
		//
		// TXT events
		//
		var $toolBarBtnUcsv = FormUtil.getButtonWithText('Imp. TXT' ,null).attr('title', 'Import from TXT').tooltipster();
		var clickUcsvFunc = function(tableData, $wrappedTable) {
			return function() {
				_this._freeFormTableController.importCSV(tableData, $wrappedTable); 
			}
		}
		$toolBarBtnUcsv.click(clickUcsvFunc(tableData, $wrappedTable));
		
		var $toolBarBtnDcsv = FormUtil.getButtonWithText('Exp. TXT' ,null).attr('title', 'Export to TXT').tooltipster();
		var clickDcsvFunc = function(tableData, $wrappedTable) {
			return function() {
				_this._freeFormTableController.exportCSV(tableData, $wrappedTable); 
			}
		}
		$toolBarBtnDcsv.click(clickDcsvFunc(tableData, $wrappedTable));
		
		//
		// Size Modifier
		//
		var $dropDown = this._getDefaultSizesDropdown(tableData, $wrappedTable);
		
		//
		// Column events
		//
		var $toolBarBtnTACL = FormUtil.getButtonWithImage('./img/table-add-column-left.png' ,null).attr('title', 'Add Column on the left.').tooltipster();
		var focusEventTACL = this._getFocusEventAction(
				tableData,
				function(selectedField) { return selectedField.columnIdx !== null; },
				function(selectedField) { _this._freeFormTableController.addColumn(tableData, $wrappedTable, selectedField.columnIdx); }
		);
		$toolBarBtnTACL.click(focusEventTACL);
		
		var $toolBarBtnTACR = FormUtil.getButtonWithImage('./img/table-add-column-right.png' ,null).attr('title', 'Add Column on the right.').tooltipster();
		var focusEventTACR = this._getFocusEventAction(
				tableData,
				function(selectedField) { return selectedField.columnIdx !== null; },
				function(selectedField) { _this._freeFormTableController.addColumn(tableData, $wrappedTable, selectedField.columnIdx + 1); }
		);
		$toolBarBtnTACR.click(focusEventTACR);
		
		var $toolBarBtnTDC = FormUtil.getButtonWithImage('./img/table-delete-column.png' ,null).attr('title', 'Delete Column.').tooltipster();
		var focusEventTDC = this._getFocusEventAction(
				tableData,
				function(selectedField) { return selectedField.columnIdx !== null; },
				function(selectedField) { 
					_this._freeFormTableController.delColumn(tableData, $wrappedTable, selectedField.columnIdx);
					_this._freeFormTableModel.selectedField = null;
				}
		);
		$toolBarBtnTDC.click(focusEventTDC);
		
		//
		// Row events
		//
		var $toolBarBtnTARA = FormUtil.getButtonWithImage('./img/table-add-row-above.png' ,null).attr('title', 'Add Row above.').tooltipster();
		var focusEventTARA = this._getFocusEventAction(
				tableData,
				function(selectedField) { return selectedField.rowIdx !== null; },
				function(selectedField) { _this._freeFormTableController.addRow(tableData, $wrappedTable, selectedField.rowIdx); }
		);
		$toolBarBtnTARA.click(focusEventTARA);
		
		var $toolBarBtnTARB = FormUtil.getButtonWithImage('./img/table-add-row-below.png' ,null).attr('title', 'Add Row below.').tooltipster();
		var focusEventTARB = this._getFocusEventAction(
				tableData,
				function(selectedField) { return selectedField.rowIdx !== null; },
				function(selectedField) { _this._freeFormTableController.addRow(tableData, $wrappedTable, selectedField.rowIdx + 1); }
		);
		$toolBarBtnTARB.click(focusEventTARB);
		
		var $toolBarBtnTDR = FormUtil.getButtonWithImage('./img/table-delete-row.png' ,null).attr('title', 'Delete Row.').tooltipster();
		var focusEventTDR = this._getFocusEventAction(
				tableData,
				function(selectedField) { return selectedField.rowIdx !== null; },
				function(selectedField) {
					_this._freeFormTableController.delRow(tableData, $wrappedTable, selectedField.rowIdx);
					_this._freeFormTableModel.selectedField = null;
				}
		);
		$toolBarBtnTDR.click(focusEventTDR);
		
		//
		// Table events
		//
		var $toolBarBtnAT = FormUtil.getButtonWithText('+ Table' ,null).attr('title', 'Add Table.').tooltipster();
		var addTableFunc = function(tableData, $tableContainer) {
			return function() { _this._freeFormTableController.addTable(tableData, $tableContainer); };
		}
		$toolBarBtnAT.click(addTableFunc(tableData, $tableContainer));
		
		var $toolBarBtnDT = FormUtil.getButtonWithText('- Table' ,null).attr('title', 'Delete Table.').tooltipster();
		var removeTableFunc = function(tableData, $tableContainer) {
			return function() { _this._freeFormTableController.deleteTable(tableData, $tableContainer); };
		}
		$toolBarBtnDT.click(removeTableFunc(tableData, $tableContainer));
		
		if(this._freeFormTableModel.isEnabled) {
			$toolBar
				.append($toolBarBtnUcsv).append(' ')
				.append($toolBarBtnDcsv).append(' ')
				.append($dropDown).append(' ')
				.append($toolBarBtnTACL).append(' ')
				.append($toolBarBtnTACR).append(' ')
				.append($toolBarBtnTDC).append(' ')
				.append($toolBarBtnTARA).append(' ')
				.append($toolBarBtnTARB).append(' ')
				.append($toolBarBtnTDR).append(' ')
				.append($toolBarBtnAT).append(' ')
				.append($toolBarBtnDT);
		}
		
		var $titleAndToolbar = $("<div>")
							.append($switch)
							.append($title)
							.append($toolBar);
		
		$tableContainer
			.append($titleAndToolbar)
			.append($wrappedTable);
		
		return $tableContainer;
	}
	
	this.addTable = function($newTable, $tableBefore) {
		if($tableBefore) {
			$tableBefore.after($newTable);
		} else {
			this._container.append($newTable);
		}
	}
	
	this.deleteTable = function($wrappedTable) {
		$wrappedTable.remove();
	}
	
	this.repaint = function($container) {
		var _this = this;
		
		var $fieldsetOwner = $("<div>");
		var $legend = $("<legend>");
		var $fieldset = $("<div>");
		
		$fieldsetOwner.append($legend).append($fieldset);
		$container.append($fieldsetOwner);
		
		this._container = $fieldset;
		$container.attr("style", "border-radius:4px 4px 4px 4px;");
		
		var $addTableWhenEmptyBtn = "";
		if(this._freeFormTableModel.isEnabled) {
			var $addTableWhenEmptyBtn = FormUtil.getButtonWithText('+ Table' ,null).attr('title', 'Add Table.').tooltipster();
			var addTableFunc = function(tableData, $tableContainer) {
				return function() { _this._freeFormTableController.addTable(tableData, $tableContainer); };
			}
			$addTableWhenEmptyBtn.click(addTableFunc(null, null));
		}
		
		$legend.text("Free Form Tables ").append($addTableWhenEmptyBtn);
		$legend.prepend(FormUtil.getShowHideButton($fieldset, "SAMPLE-" + this._freeFormTableModel.sample.sampleTypeCode + "-freeFormTable"));
		
		var tables = this._freeFormTableModel.tables;
		var lastTable = null;
		for(var tableIdx = 0; tableIdx < tables.length; tableIdx++) {
			var tableData = tables[tableIdx];
			var $tableContainer = this._getTableWithContainer(tableData);
			this.addTable($tableContainer, lastTable);
			lastTable = $tableContainer;
		}
	}
}