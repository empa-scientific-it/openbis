/*
 * Copyright 2015 ETH Zuerich, Scientific IT Services
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
function HierarchyFilterController(entity, action) {
	this._model = new HierarchyFilterModel(entity, action);
	this._view = new HierarchyFilterView(this, this._model);
	
	this.init = function(container) {
		this._view.init(container);
		$('#childrenLimit').slider().on('slideStop', function(event){
			action();
		});
		
		$('#parentsLimit').slider().on('slideStop', function(event){
			action();
		});
		
		$('#entityTypesSelector').multiselect();
		$('#entityTypesSelector').change(function(event){
			action();
		});
	}
	
	this.getParentsLimit = function() {
		return getSliderValue("parentsLimit");
	}
	
	this.getChildrenLimit = function() {
		return getSliderValue("childrenLimit");
	}
	
	this.getSelectedEntityTypes = function() {
		var selectedEntityTypes = $('#entityTypesSelector').val();
		if(selectedEntityTypes === null) {
			selectedEntityTypes = [];
		}
		return selectedEntityTypes;
	}
	
	var getSliderValue = function(id) {
		var element = $('#' + id)
		return  element.length > 0 ? element.data('slider').getValue() : 0;
	}

}