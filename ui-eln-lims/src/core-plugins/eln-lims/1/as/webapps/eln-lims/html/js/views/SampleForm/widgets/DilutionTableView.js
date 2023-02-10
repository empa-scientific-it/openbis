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

function DilutionTableView(dilutionTableController, dilutionTableModel) {
	this._dilutionTableController = dilutionTableController;
	this._dilutionTableModel = dilutionTableModel;
	
	this.repaint = function($container) {
		$container.empty();
		$container.append("Loading data for Dilution Widget.");
		var _this = this;
		//Load all proteins
		mainController.serverFacade.searchWithType("PROTEIN", null, true, function(data) {
			_this._dilutionTableModel.allProteins = data;
			
			//First repaint after all initializations
			_this._repaint($container);
			
			//Disable field or give alert
			var sampleType = profile.getSampleTypeForSampleTypeCode(_this._dilutionTableModel.sample.sampleTypeCode);
			if(!profile.isPropertyPressent(sampleType,"DILUTION_STATE" )) {
				Util.showUserError("You need a property with code DILUTION_STATE on this entity to store the state of the dilution widget.");
			} else {
				//Hide State Field
				$("#DILUTION_STATE").parent().parent().hide();
				
				//Update Values
				var stateFieldVar = _this._dilutionTableModel.sample.properties["DILUTION_STATE"];
				var stateObj = null;
				if(stateFieldVar) {
					stateObj = JSON.parse(stateFieldVar);
				} else {
					stateObj = JSON.parse("{}");
					return; //No update
				}
				var tBody = $("#" + _this._dilutionTableModel.widgetTableId).children()[1];
				for(var rowNum = 0; rowNum < (tBody.rows.length - 3); rowNum++) {
					var row = $(tBody.rows[rowNum]);
					var antibodyDropDown = $($(row.children()[_this._dilutionTableModel.antColIdx]).children()[0]);
					var antibody = stateObj[rowNum][0];
					if(antibody !== "") {
						antibodyDropDown.val(antibody);
						antibodyDropDown.change();
						var conjugatedCloneDropDown = $($(row.children()[_this._dilutionTableModel.conColIdx]).children()[0]);
						var conjugatedClone = stateObj[rowNum][1];
						if(conjugatedClone !== "") {
							conjugatedCloneDropDown.val(conjugatedClone);
							conjugatedCloneDropDown.change();
						}
					}
				}
				_this._updateCalculatedValues();
			}
		});
	}
	

	this._getProteinDropdown = function(rowNumber) {
		var $component = $("<select>");

		$component.css(
				{
					"margin-top": "1px",
					"margin-bottom": "1px",
					"border": "0px",
					"width": "100%"
				}
		);
		
		$component.attr('data-row-number', rowNumber);
		
		//Get proteins for row
		var proteins = [];
		var _this = this;
		this._dilutionTableModel.allProteins.forEach(function(protein) {
			protein.children.forEach(function(clone) {
				clone.children.forEach(function(lot) { 
					lot.children.forEach(function(conjugatedClone) {
						var metalMass = conjugatedClone.properties["METAL_MASS"];
						var predefinedMass = _this._dilutionTableModel.predefinedMass[rowNumber] + "";
						if(predefinedMass === metalMass && $.inArray(protein, proteins) === -1) {
							proteins.push(protein);
						}
					});
				});
			});
		});
		//
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < proteins.length; i++) {
			$component.append($("<option>").attr('value',proteins[i].permId).text(proteins[i].properties["PROTEIN_NAME"]));
		}
		
		var _this = this;
		$component.change(function() {
			var rowNumber = $(this).attr('data-row-number');
			var proteinPermId = $(this).val();
			
			//Clear row
			for(var clearIdx = _this._dilutionTableModel.conColIdx; clearIdx <= _this._dilutionTableModel.volColIdx; clearIdx++) {
				_this._updateCell(rowNumber,clearIdx, "");
			}
			
			_this._updateCalculatedValues();
			//Update row
			if(proteinPermId !== "") {
				_this._updateConjugatedClone(rowNumber, proteinPermId);
			}
			
			//Alert if needed
			var proteinsToRowsMap = _this._getProteinRowsMap();
			var proteinRows = proteinsToRowsMap[proteinPermId];
			if(proteinRows.length > 1) {
				Util.showInfo("You have selected " + this[this.selectedIndex].label + ": " + proteinRows.length + " times.");
			}
			//Update everything
			_this._updateCalculatedValues();
		});
		if(!_this._dilutionTableModel.isEnabled) {
			$component.attr('disabled', true)
		}
		Select2Manager.add($component);
		return $component;
	}
	
	this._updateCell = function(row, column, component) {
		var $widgetTableCell = $($("#" + this._dilutionTableModel.widgetTableId).children()[1].rows[row].cells[column]);
		$widgetTableCell.empty();
		$widgetTableCell.append(component);
	}
	
	this._updateConjugatedClone = function(rowNumber, proteinPermId) {
		//Get valid conjugated clones
		var conjugatedClones = [];
			
		this._dilutionTableModel.allProteins.forEach(function(protein) {
			if(protein.permId === proteinPermId) {
				protein.children.forEach(function(clone) {
					clone.children.forEach(function(lot) { 
						lot.children.forEach(function(conjugatedClone) { 
							conjugatedClones.push(
									{
										"clone" : clone,
										"lot" : lot,
										"conjugatedClone" : conjugatedClone
									});
							
						});
					});
				});
			}
		});
			
		//Build dropdown with conjugated clones
		var $component = $("<select>");
		$component.css(
				{
					"margin-top": "1px",
					"margin-bottom": "1px",
					"border": "0px",
					"width": "100%"
				}
		);
			
		$component.attr('data-row-number', rowNumber);
		
		$component.append($("<option>").attr('value', '').attr('selected', '').text(''));
		for(var i = 0; i < conjugatedClones.length; i++) {
			var conjugatedClone = conjugatedClones[i]["conjugatedClone"];
			var metalMass = conjugatedClone.properties["METAL_MASS"];
			var predefinedMass = this._dilutionTableModel.predefinedMass[rowNumber] + "";
			if(predefinedMass === metalMass) {
				$component.append($("<option>").attr('value',conjugatedClone.permId).text(conjugatedClone.code));
			}
		}
			
		//Add dropdown to the DOM
		this._updateCell(rowNumber,this._dilutionTableModel.conColIdx, $component);
		
		//Add change method to DOM
		var _this = this;
		var conjugatedCloneChange = function() {
			var conjugatedCloneSelected = $(this).val();
			var data = null;
			for(var i = 0; i < conjugatedClones.length; i++) {
				var conjugatedClone = conjugatedClones[i]["conjugatedClone"];
				if(conjugatedCloneSelected === conjugatedClone.permId) {
					data = conjugatedClones[i];
					break;
				}
			}
			var dilutionVolume = parseFloat(data["conjugatedClone"].properties["CYTOF_STAINING_CONC"]) / parseFloat(data["conjugatedClone"].properties["CYTOF_CONCENTRATION"]);
			
			if(conjugatedCloneSelected === "") {
				for(var clearIdx = _this._dilutionTableModel.cloColIdx; clearIdx <= _this._dilutionTableModel.volColIdx; clearIdx++) {
					_this._updateCell(rowNumber,clearIdx, "");
				}
			} else {
				// NOTE : When adding a new column please add it here
				_this._updateCell(rowNumber,_this._dilutionTableModel.cloColIdx, data["clone"].code);
				_this._updateCell(rowNumber,_this._dilutionTableModel.tbnColIdx, data["conjugatedClone"].properties["TUBE_NUMBER"]);
				_this._updateCell(rowNumber,_this._dilutionTableModel.reaColIdx, data["clone"].properties["REACTIVITY"]);
				_this._updateCell(rowNumber,_this._dilutionTableModel.supColIdx, data["lot"].properties["SUPPLIER"]);
				_this._updateCell(rowNumber,_this._dilutionTableModel.dilColIdx, dilutionVolume);
				// END NOTE
			}
			
			_this._updateCalculatedValues();
		}
		
		$component.change(conjugatedCloneChange);
		if(!this._dilutionTableModel.isEnabled) {
			$component.attr('disabled', true)
		}
		Select2Manager.add($component);
	}
	
	this._updateCalculatedValues = function() {
		var tBody = $("#" + this._dilutionTableModel.widgetTableId).children()[1];
		var totalVolumeToAdd = 0;
		
		//Row Volume to add
		for(var rowNum = 0; rowNum < (tBody.rows.length - 3); rowNum++) {
			var row = $(tBody.rows[rowNum]);
			var concentration = row.children()[this._dilutionTableModel.dilColIdx].innerHTML;
			if(concentration !== "") {
				var volumeToAdd = this._dilutionTableModel.totalVolume * parseFloat(concentration);
				totalVolumeToAdd += volumeToAdd;
				this._updateCell(rowNum,this._dilutionTableModel.volColIdx, volumeToAdd);
			}
		}
		
		//Total Volume to add
		this._updateCell(tBody.rows.length - 1,this._dilutionTableModel.volColIdx, totalVolumeToAdd);
		
		//Buffer Volume
		this._updateCell(tBody.rows.length - 2,this._dilutionTableModel.volColIdx, this._dilutionTableModel.totalVolume - totalVolumeToAdd);
		
		//Update State
		var state = {};
		for(var rowNum = 0; rowNum < (tBody.rows.length - 3); rowNum++) {
			var row = $(tBody.rows[rowNum]);
			var antibodyDropDown = $($(row.children()[this._dilutionTableModel.antColIdx]).children()[0]);
			var antibody = "";
			if(antibodyDropDown.val()) {
				antibody = antibodyDropDown.val();
			}
			var conjugatedClone = "";
				
			if(antibody !== "") {
				var conjugatedCloneDropDown = $($(row.children()[this._dilutionTableModel.conColIdx]).children()[0]);
				if(conjugatedCloneDropDown.val()) {
					conjugatedClone = conjugatedCloneDropDown.val();
				}
			}
				
			state[rowNum] = [antibody, conjugatedClone];
		}
			
		this._dilutionTableModel.sample.properties["DILUTION_STATE"] = JSON.stringify(state);
		this._updateProteinRowsByAntibody();
		
	}
	
	this._getProteinRowsMap = function() {
		var proteinsToRowsMap = {};
		var tBody = $("#" + this._dilutionTableModel.widgetTableId).children()[1];
		for(var rowNum = 0; rowNum < (tBody.rows.length - 3); rowNum++) {
			var row = $(tBody.rows[rowNum]);
			row.css({
				"background-color" : "transparent"
			});
			
			var antibodyDropDown = $($(row.children()[this._dilutionTableModel.antColIdx]).children()[0]);
			var antibodyVal = antibodyDropDown.val();
			if(antibodyVal) {
				var rows = proteinsToRowsMap[antibodyVal];
				if(!rows) {
					rows = [];
					proteinsToRowsMap[antibodyVal] = rows;
				}
				rows.push(row);
			}
		}
		return proteinsToRowsMap;
	}
	
	this._updateProteinRowsByAntibody = function() {
		var proteinsToRowsMap = this._getProteinRowsMap();
		
		var colours = ["#F0E890", "#B0F0F0", "#90F790", "#F08080", "#B0C0E0", "#00F890", "#D0C0D0", "#FFE0B0", "#90C830", "#B02020"];
		var coloursUsed = 0;
		for(var proteinKey in proteinsToRowsMap) {
			var tBody = $("#" + this._dilutionTableModel.widgetTableId).children()[1];
			var rowsWithThatAntibody = [];
			for(var rowNum = 0; rowNum < (tBody.rows.length - 3); rowNum++) {
				var $row = $(tBody.rows[rowNum]);
				var $antibodyDropDown = $($($row.children()[this._dilutionTableModel.antColIdx]).children()[0]);
				var antibodyDropDownValues = [];
				for(var opNum = 0; opNum < $antibodyDropDown[0].options.length; opNum++) {
					var valueToTest = $antibodyDropDown[0].options[opNum].value;
					if(valueToTest === proteinKey) {
						rowsWithThatAntibody.push($row);
					}
				}
			}
			
			if(rowsWithThatAntibody.length > 1 && coloursUsed < colours.length) {
				for(var rowFound = 0; rowFound < rowsWithThatAntibody.length; rowFound++) {
					rowsWithThatAntibody[rowFound].css({
						"background-color" : colours[coloursUsed]
					});
				}
				coloursUsed++;
			}			
		}
	}
	
	this._repaint = function($container) {
		var _this = this;
		//
		$container.empty();

		//Top Title
		var $legend = $("<legend>");
		$legend.append("Dilution Calculator ");
		
		var $printButton = $("<a>", { class: "btn btn-default" }).append($("<i>", { class: "glyphicon glyphicon-print" }));
		$printButton.click(function() { 
			var tableWidget = $("#" + _this._dilutionTableModel.widgetTableId);
			var clonedWidget = tableWidget.clone();
			clonedWidget.css({
				"border" : "1px solid #DDDDDD",
				"border-collapse" : "collapse",
				"border-spacing" : "0"
			});
			
			//FIX Selected values
			var tHeadClone = clonedWidget.children()[0];
			var headerRowClone = $(tHeadClone.rows[0]);
			for(var colNum = 0; colNum < headerRowClone.children().length; colNum++) {
				var col = $(headerRowClone.children()[colNum]);
				col.css({
					"border" : "1px solid #DDDDDD"
				});
			}
			
			var tBody = tableWidget.children()[1];
			var tBodyClone = clonedWidget.children()[1];
			for(var rowNum = 0; rowNum < (tBody.rows.length - 3); rowNum++) {
				var row = $(tBody.rows[rowNum]);
				var antibodyDropDown = $($(row.children()[_this._dilutionTableModel.antColIdx]).children()[0]);
				var antibody = antibodyDropDown[0][antibodyDropDown[0].selectedIndex].label;
				
				var rowClone = $(tBodyClone.rows[rowNum]);
				var antibodyDropDownClone = $($(rowClone.children()[_this._dilutionTableModel.antColIdx]).children()[0]);
				antibodyDropDownClone.remove();
				
				var conjugatedCloneDropDown = $($(row.children()[_this._dilutionTableModel.conColIdx]).children()[0]);
				var conjugatedCloneDropDownClone = $($(rowClone.children()[_this._dilutionTableModel.conColIdx]).children()[0]);
				conjugatedCloneDropDownClone.remove();
				
				if(antibody !== "") {
					$(rowClone.children()[_this._dilutionTableModel.antColIdx]).append(antibody);
					var conjugatedClone = conjugatedCloneDropDown[0][conjugatedCloneDropDown[0].selectedIndex].label;
					$(rowClone.children()[_this._dilutionTableModel.conColIdx]).append(conjugatedClone);
				}
				
				for(var colNum = 0; colNum < rowClone.children().length; colNum++) {
					var col = $(rowClone.children()[colNum]);
					col.css({
						"border" : "1px solid #DDDDDD"
					});
				}
			}
			
			var newWindow = window.open(null,"print dilution table");
			$(newWindow.document.body).html(clonedWidget);
		} );
		
		$legend.append($printButton);
		
		//Defining containers
		var $wrapper = $("<div>");
		$wrapper.append($legend);
		
		var $table = $("<table>", { "class" : "table table-bordered table-condensed table-condensed-dilution", "id" : this._dilutionTableModel.widgetTableId});
		
		$wrapper.append($table);
		var $tableHead = $("<thead>");
		var $tableBody = $("<tbody>");
		$table
			.append($tableHead)
			.append($tableBody);

		//Headers
		var $tableHeadTr = $("<tr>");
		// NOTE : Add a TH when adding a new property
		$tableHeadTr
			.append("<th><center>Index</center></th>")
			.append("<th><center>Metal Mass</center></th>")
			.append("<th><center>Antibody</center></th>")
			.append("<th><center>Conjugated Clone</center></th>")
			.append("<th><center>Clone</center></th>")
			.append("<th><center>Tube Number</center></th>")
			.append("<th><center>Reactivity</center></th>")
			.append("<th><center>Supplier</center></th>")
			.append("<th><center>Dilution Factor</center></th>")
			.append("<th><center>Volume To Add</center></th>");
		$tableHead.append($tableHeadTr);
		// END NOTE

		for(var i = 0; i < this._dilutionTableModel.predefinedMass.length; i++){
			var $tableRowTr = $("<tr>");
			
			var $proteinSelectionTD = $("<td>").append(this._getProteinDropdown(i));
			
			// NOTE : Add a TD when adding a new property
			$tableRowTr
				.append("<td>" + (i+1) + "</td>")
				.append("<td>" + this._dilutionTableModel.predefinedMass[i] +"</td>")
				.append($proteinSelectionTD)
				.append("<td></td>")
				.append("<td></td>")
				.append("<td></td>")
				.append("<td></td>")
				.append("<td></td>")
				.append("<td></td>")
				.append("<td></td>");
			// END NOTE
			
			$tableBody.append($tableRowTr);
		}
		
		
		var $tableRowTrF1TextBox = $("<input>", {"type" : "number", "step" : "any"});
		$tableRowTrF1TextBox.css(
				{
					"margin-top": "0px",
					"margin-bottom": "0px",
					"border": "0px",
					"width": "100%",
					"height": "25px",
					"padding" : "0px 0px 0px 0px",
					"background-color" : "#EEEEEE",
					"text-align" : "center",
					"border-radius" : "0px 0px 0px 0px"
				});
		var $tableRowTrF1TextBoxLastTD = $("<td>");
		$tableRowTrF1TextBoxLastTD.css(
				{
					"margin-top": "0px",
					"margin-bottom": "0px",
					"padding" : "0px 0px 0px 0px"
				});
		$tableRowTrF1TextBox.val(400);
		
		var _this = this;
		this._dilutionTableModel.totalVolume = 400;
		$tableRowTrF1TextBox.keyup(function() {
			_this._dilutionTableModel.totalVolume = $(this).val();
			_this._updateCalculatedValues();
		});
		
		$tableRowTrF1TextBoxLastTD.append($tableRowTrF1TextBox);
		
				
		var $tableRowTrF1 = $("<tr>");
		for(var i = 0; i < _this._dilutionTableModel.volColIdx - 1; i++) {
			$tableRowTrF1.append("<td></td>");
		}
		$tableRowTrF1
		.append("<td><b>Total Volume Needed</b></td>")
		.append($tableRowTrF1TextBoxLastTD);
		
		$tableBody.append($tableRowTrF1);
		var $tableRowTrF2 = $("<tr>");
		for(var i = 0; i < _this._dilutionTableModel.volColIdx - 1; i++) {
			$tableRowTrF2.append("<td></td>");
		}
		$tableRowTrF2
		.append("<td><b>Buffer Volume</b></td>")
		.append("<td style='text-align : center;'>" + this._totalVolume + "</td>");
		$tableBody.append($tableRowTrF2);
		var $tableRowTrF3 = $("<tr>");
		for(var i = 0; i < _this._dilutionTableModel.volColIdx - 1; i++) {
			$tableRowTrF3.append("<td></td>");
		}
		$tableRowTrF3
		.append("<td><b>Total Antibody</b></td>")
		.append("<td style='text-align : center;'>0</td>");
		$tableBody.append($tableRowTrF3);
		//
		$wrapper.append();
		$container.append($wrapper);
		
		//
		// Add Show / Hide Columns Filter
		//
		var	$filterColumsToShow = $('<select>', { 'id' : 'filterColumsToPrint' , class : 'multiselect' , 'multiple' : 'multiple'});
		var columns = $tableHeadTr.children();
		for(var i = 0; i < columns.length; i++) {
			$filterColumsToShow.append($('<option>', { 'value' : i , 'selected' : ''}).text(columns[i].textContent));
		}
		$filterColumsToShow.change(function(event){
			var columnsToShow = $(this).val();
			var hardCodedNumberOfColumns = 9; //TO-DO Fix this to get it from DOM
			for(var i = 0; i < hardCodedNumberOfColumns; i++) {
				var $column = $('#dilution-widget-table td:nth-child(' + (i + 1) + '), #dilution-widget-table th:nth-child(' + (i + 1) + ')');
				if($.inArray("" + i, columnsToShow) !== -1) {
					$column.show();
				} else {
					$column.hide();
				}
				
			}
		});
		
		$legend.append("&nbsp;");
		$legend.append($filterColumsToShow);
		$('#filterColumsToPrint').multiselect();
	}
}