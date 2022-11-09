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

function VocabularyManagerView(vocabularyManagerController, vocabularyManagerModel) {
	this._vocabularyManagerController = vocabularyManagerController;
	this._vocabularyManagerModel = vocabularyManagerModel;
	this._dataGridContainer = $("<div>").css("margin-top", "-10px").css("margin-left", "-10px");
	this._subtitle = $("<h4>", { "style" : "font-weight:normal;" });
    this._toolbox = $("<div>", { 'id' : 'toolBoxContainer', class : 'toolBox'});
	
	this.repaint = function(views) {
		
		//
		// Form template and title
		//
		var $containerColumn = $("<form>", {
			'role' : "form", 
			"action" : "javascript:void(0);", 
			"onsubmit" : ""
		});
		
		var $containerHeader = $("<div>");
		$containerHeader.append($("<h2>", {"id" : "vocabulary-browser-title-id"}).append("Vocabulary Browser"));
		$containerHeader.append(this._subtitle);
		$containerHeader.append(this._toolbox);
		views.header.append($containerHeader);
		views.content.append($containerColumn);
		
		this._showVocabularies();
		$containerColumn.append(this._dataGridContainer);
	}
	
	this._showVocabularies = function() {
		var _this = this;
		
		var columns = [ {
			label : 'Code',
			property : 'code',
			render: function(data){
				return $("<div>").html(data.code)
			},
			sortable : true
		} , {
			label : 'Description',
			property : 'description',
			sortable : true
		}];
		
		var getDataList = function(callback) {
			var dataList = [];
			for(var idx = 0; idx < _this._vocabularyManagerModel.vocabularies.length; idx++) {
				var vocabulary =  _this._vocabularyManagerModel.vocabularies[idx];
				var id = FormUtil.prepareId(vocabulary.code).toLowerCase() + "_id";
				var codeDiv = "<div id= " + id +">" + vocabulary.code + "</did>";
				dataList.push({
					id: vocabulary.code,
					exportableId : {
						exportable_kind: 'VOCABULARY',
						perm_id: vocabulary.code
					},
					code : codeDiv,
					description : vocabulary.description,
					object : vocabulary
				});
			}
			callback(dataList);
		}
		
		var rowClick = function(e) {
			_this._showVocabulary(e.data.object)
		}
		
		var dataGrid = new DataGridController(null, columns, [], null, getDataList, rowClick, true, "VOCABULARY_TABLE", false, {
			fileFormat: 'XLS',
			filePrefix: 'vocabularies'
		}, 90);
		dataGrid.setId("vocabulary-grid")
		dataGrid.init(this._dataGridContainer);
		
		this._subtitle.empty();
		this._subtitle.append("Vocabularies List");

		this._toolbox.empty();
	}
	
	this._showVocabulary = function(vocabulary) {
	    var _this = this;

		var columns = [ {
			label : 'Code',
			property : 'code',
			sortable : true
		} , {
			label : 'Label',
			property : 'label',
			sortable : true
		} , {
			label : 'Description',
			property : 'description',
			sortable : true
		}];
		
		var getDataList = function(callback) {
			var dataList = [];
			for(var idx = 0; idx < vocabulary.terms.length; idx++) {
				var term =  vocabulary.terms[idx];
				dataList.push({
					id: term.code,
					code : term.code,
					label : term.label,
					description : term.description,
					object : term
				});
			}
			callback(dataList);
		}
		
		var dataGrid = new DataGridController(null, columns, [], null, getDataList, null, true, "VOCABULARY_TERMS_TABLE", false, {
			fileFormat: 'TSV',
			filePrefix: 'vocabulary-terms-grid'
		}, 90);
		dataGrid.setId("vocabulary-terms-table")
		dataGrid.init(this._dataGridContainer);
		
		this._subtitle.empty();
		this._subtitle.append("Terms from vocabulary " + vocabulary.code);

		var $back = $("<a>", { "class" : "btn btn-default", "style" : "margin-top: 10px;", "id" : "back-to-vocabularies"}).append("⇦ Back");
        $back.click(function() {
            _this._showVocabularies();
        });
        this._toolbox.empty();
        this._toolbox.append($back);
	}
}