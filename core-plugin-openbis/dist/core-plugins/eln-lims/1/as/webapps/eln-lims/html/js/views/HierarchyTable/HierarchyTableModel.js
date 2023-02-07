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

function HierarchyTableModel(entity) {
	this.entity = entity;
	this.ancestorPermIds = null;
	this.descendentPermIds = null;
    this.dataList = null;
	if(this.entity["@type"] === "as.dto.sample.Sample") {  // V3 Sample
		profile.deleteSampleConnectionsByType(this.entity);
	}
	
	this.relationShipsMap = HierarchyUtil.createRelationShipsMap(entity);
	
	this.getData = function(dataList) {
		if(this.dataList !== null) {
			return this.dataList;
		}
		this.dataList = [];
		this.ancestorPermIds = {};
		this.descendentPermIds = {};
		this._addRow(this.dataList, this.entity, 0, "");
		this._addAncestorData(this.dataList, this.entity, 0, "");
		this._addDescendentData(this.dataList, this.entity, 0, "");
		this.dataList.sort(function (e1, e2) {
			var l1 = e1.level;
			var p1 = e1.identifier + e1.path;
			var l2 = e2.level;
			var p2 = e2.identifier + e2.path;
			if (l1 !== l2) {
				return l1 - l2;
			}
			return p1 < p2 ? -1 : (p1 > p2 ? 1 : 0);
		});
		return this.dataList;
	}

	this._addAncestorData = function(dataList, entity, level, path) {
		if (entity.parents) {
			for (var i = 0; i < entity.parents.length; i++) {
				var parent = entity.parents[i];
				var parentPermId = parent.getPermId().getPermId();
				var isCycle = this.ancestorPermIds[parentPermId] === true;
				var cyclePrefix = (isCycle)?"<REPEATED>":"";
				var newPath = cyclePrefix + " → " + entity.code + path;
				var newLevel = level - 1;
				this._addRow(dataList, parent, newLevel, newPath);
				if(!isCycle) {
					this.ancestorPermIds[parentPermId] = true;
					this._addAncestorData(dataList, parent, newLevel, newPath);
				}
			}
		}
	}



	this._addDescendentData = function(dataList, entity, level, path) {
		if (entity.children) {
			for (var i = 0; i < entity.children.length; i++) {
				var child = entity.children[i];
				var childPermId = child.getPermId().getPermId();
				var isCycle = this.descendentPermIds[childPermId] === true;
				var cyclePrefix = (isCycle)?"<REPEATED>":"";
				var newPath = cyclePrefix + " ← " + entity.code + path;
				var newLevel = level + 1;
				this._addRow(dataList, child, newLevel, newPath);
				if(!isCycle) {
					this.descendentPermIds[childPermId] = true;
					this._addDescendentData(dataList, child, newLevel, newPath);
				}
			}
		}
	}
	
	this._addRow = function(dataList, entity, level, path) {
        var annotations = {};
	    if(entity["@type"] === "as.dto.sample.Sample") {
	        annotations = FormUtil.getAnnotationsFromSample(entity);
	    }
		var relationShips = this.relationShipsMap[entity.permId.permId];
		
		var repositoryId = null;
		if(entity.linkedData && entity.linkedData.contentCopies && entity.linkedData.contentCopies[0]) {
			repositoryId = entity.linkedData.contentCopies[0].gitRepositoryId;
		}
		
		var historyId = null;
		if(entity.properties && entity.properties["HISTORY_ID"]) {
			historyId = entity.properties["HISTORY_ID"];
		}
		
		dataList.push({
			id: entity.permId.permId + "-" + dataList.length,
			level : level,
			registrationDate : Util.getFormatedDate(new Date(entity.registrationDate)),
			type : entity.type.code,
			identifier : (entity.identifier)?entity.identifier.identifier:undefined,
			code : entity.code,
			repositoryId : repositoryId,
			historyId : historyId,
			permId : entity.permId.permId,
			path: path,
			name : entity.properties["$NAME"],
			sampleCode : (entity.sample)?entity.sample.code:null,
			experimentCode : (entity.experiment)?entity.experiment.code:null,
			parentAnnotations : this._createAnnotations(annotations, relationShips.parents),
			childrenAnnotations : this._createAnnotations(annotations, relationShips.children),
			entity : entity
		});
		
	}
	
    /*
	 * Only samples have annotations, if they are not samples, they will simply not be found
	 */
	this._createAnnotations = function(annotations, entities) {
		var content = "";
		var rowStarted = false;
		AnnotationUtil.buildAnnotations(annotations, entities, {
			startRow : function() {
				if (content !== "") {
					content += "\n";
				}
				rowStarted = true;
			},
			addKeyValue : function(key, value) {
				if (rowStarted === false) {
					content += ", ";
				}
				content += key + ":" + value;
				rowStarted = false;
			}
		})
		return content;
	}
	

}