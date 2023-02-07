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

var TreeUtil = new function() {
	
	this.getCompleteTree = function($treeContainer) {
		var treeModel = [{ title : "/", entityType: "ROOT", key : "/", folder : true, lazy : true }];
		return this.getTreeFromModel($treeContainer, treeModel, false);
	}
	
	this.getTreeForEntity = function($treeContainer, entity) {
		var treeModel;
		switch(entity["@type"]) {
			case "SpaceWithProjectsAndRoleAssignments":
				treeModel = { title : Util.getDisplayNameForEntity(entity), entityType: "SPACE", key : entity.code, folder : true, lazy : true, hideCheckbox: true };
				break;
			case "Project":
				treeModel = { title : Util.getDisplayNameForEntity(entity), entityType: "PROJECT", key : entity.permId, folder : true, lazy : true, hideCheckbox: true };
				break;
			case "Experiment":
				treeModel = { title : Util.getDisplayNameForEntity(entity), entityType: "EXPERIMENT", key : entity.permId, folder : true, lazy : true, hideCheckbox: true };
				break;
			case "Sample":
				treeModel = { title : Util.getDisplayNameForEntity(entity), entityType: "SAMPLE", key : entity.permId, folder : true, lazy : true, hideCheckbox: true };
				break;
			case "DataSet":
				treeModel = { title : Util.getDisplayNameForEntity(entity), entityType: "DATASET", key : entity.code, folder : false, lazy : false, icon : "fa fa-database" };
				break;
		}
		
		return this.getTreeFromModel($treeContainer, [treeModel], true);
	}
	
	this.getTreeFromModel = function($treeContainer, treeModel, hideCheckboxForFolders) {
		var glyph_opts = {
        	    map: {
        	      doc: "glyphicon glyphicon-file",
        	      docOpen: "glyphicon glyphicon-file",
        	      checkbox: "glyphicon glyphicon-unchecked",
        	      checkboxSelected: "glyphicon glyphicon-check",
        	      checkboxUnknown: "glyphicon glyphicon-share",
        	      dragHelper: "glyphicon glyphicon-play",
        	      dropMarker: "glyphicon glyphicon-arrow-right",
        	      error: "glyphicon glyphicon-warning-sign",
        	      expanderClosed: "glyphicon glyphicon-plus-sign",
        	      expanderLazy: "glyphicon glyphicon-plus-sign",  // glyphicon-expand
        	      expanderOpen: "glyphicon glyphicon-minus-sign",  // glyphicon-collapse-down
        	      folder: "glyphicon glyphicon-folder-close",
        	      folderOpen: "glyphicon glyphicon-folder-open",
        	      loading: "glyphicon glyphicon-refresh"
        	    }
        };
    	
    	var onLazyLoad = function(event, data) {
    		var dfd = new $.Deferred();
    	    data.result = dfd.promise();
    	    var type = data.node.data.entityType;
    	    var permId = data.node.key;
    	    
    	    switch(type) {
    	    	case "ROOT":
    	    		var spaceRules = { entityKind : "SPACE", logicalOperator : "AND", rules : { } };
    	    		mainController.serverFacade.searchForSpacesAdvanced(spaceRules, null, function(searchResult) {
    	    			var results = [];
    	                var spaces = searchResult.objects;
    	                for (var i = 0; i < spaces.length; i++) {
    	                    var space = spaces[i];
    	                    results.push({ title : Util.getDisplayNameForEntity(space), entityType: "SPACE", key : space.code, folder : true, lazy : true, hideCheckbox: hideCheckboxForFolders });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "SPACE":
    	    		var projectRules = { "UUIDv4" : { type : "Attribute", name : "SPACE", value : permId } };
    	    		mainController.serverFacade.searchForProjectsAdvanced({ entityKind : "PROJECT", logicalOperator : "AND", rules : projectRules }, null, function(searchResult) {
    	    			var results = [];
    	                var projects = searchResult.objects;
    	                for (var i = 0; i < projects.length; i++) {
    	                    var project = projects[i];
    	                    results.push({ title : Util.getDisplayNameForEntity(project), entityType: "PROJECT", key : project.permId, folder : true, lazy : true, hideCheckbox: hideCheckboxForFolders });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "PROJECT":
    	    		var experimentRules = { "UUIDv4" : { type : "Attribute", name : "PROJECT_PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForExperimentsAdvanced({ entityKind : "EXPERIMENT", logicalOperator : "AND", rules : experimentRules }, null, function(searchResult) {
    	    			var results = [];
    	                var experiments = searchResult.objects;
    	                for (var i = 0; i < experiments.length; i++) {
    	                    var experiment = experiments[i];
    	                    results.push({ title : Util.getDisplayNameForEntity(experiment), entityType: "EXPERIMENT", key : experiment.permId, folder : true, lazy : true, hideCheckbox: hideCheckboxForFolders });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "EXPERIMENT":
    	    		var sampleRules = { "UUIDv4" : { type : "Experiment", name : "ATTR.PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForSamplesAdvanced({ entityKind : "SAMPLE", logicalOperator : "AND", rules : sampleRules }, null, function(searchResult) {
    	    			var results = [];
    	                var samples = searchResult.objects;
    	                for (var i = 0; i < samples.length; i++) {
    	                    var sample = samples[i];
    	                    results.push({ title : Util.getDisplayNameForEntity(sample), entityType: "SAMPLE", key : sample.permId, folder : true, lazy : true, hideCheckbox: hideCheckboxForFolders });
    	                }
    	                
    	                var datasetRules = { "UUIDv4" : { type : "Experiment", name : "ATTR.PERM_ID", value : permId } };
        	    		mainController.serverFacade.searchForDataSetsAdvanced({ entityKind : "DATASET", logicalOperator : "AND", rules : datasetRules }, null, function(searchResult) {
        	                var datasets = searchResult.objects;
        	                for (var i = 0; i < datasets.length; i++) {
        	                    var dataset = datasets[i];
        	                    if(!dataset.sample) {
        	                    	results.push({ title : Util.getDisplayNameForEntity(dataset), entityType: "DATASET", key : dataset.permId, folder : false, lazy : false, icon : "fa fa-database" });
        	                    }
        	                }
        	                dfd.resolve(results);
        	    		});
    	                
    	                
    	    		});
    	    		break;
    	    	case "SAMPLE":
    	    		var datasetRules = { "UUIDv4" : { type : "Sample", name : "ATTR.PERM_ID", value : permId } };
    	    		mainController.serverFacade.searchForDataSetsAdvanced({ entityKind : "DATASET", logicalOperator : "AND", rules : datasetRules }, null, function(searchResult) {
    	    			var results = [];
    	                var datasets = searchResult.objects;
    	                for (var i = 0; i < datasets.length; i++) {
    	                    var dataset = datasets[i];
    	                    results.push({ title : Util.getDisplayNameForEntity(dataset), entityType: "DATASET", key : dataset.permId, folder : false, lazy : false, icon : "fa fa-database" });
    	                }
    	                dfd.resolve(results);
    	    		});
    	    		break;
    	    	case "DATASET":
    	    		break;
    	    }
    	};
    	
    	return $treeContainer.fancytree({
        	extensions: ["dnd", "edit", "glyph"], //, "wide"
        	checkbox: true,
        	selectMode: 2, // 1:single, 2:multi, 3:multi-hier
        	glyph: glyph_opts,
        	source: treeModel,
        	lazyLoad : onLazyLoad
        });
	}
}