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
var HierarchyUtil = new function() {
	/*
	 * Creates a map (sample identifiers as keys) for all ancestors and descendants of the specified sample.
	 * The values of the map are the children and parents of the sample specified by the key..
	 */
	this.createRelationShipsMap = function(sample) {
		var relationShipsMap = {};
		createRelationShipEntry(sample, relationShipsMap);
		traverseAncestors(sample, relationShipsMap);
		traverseDescendants(sample, relationShipsMap);
		return relationShipsMap;
	}
	
	var traverseAncestors = function(sample, relationShipsMap) {
		if (sample.parents) {
			for (var i = 0; i < sample.parents.length; i++) {
				var parent = sample.parents[i];
				addRelationShip(parent, sample, relationShipsMap);
				traverseAncestors(parent, relationShipsMap);
			}
		}
	}
	
	var traverseDescendants = function(sample, relationShipsMap) {
		if (sample.children) {
			for (var i = 0; i < sample.children.length; i++) {
				var child = sample.children[i];
				addRelationShip(sample, child, relationShipsMap);
				traverseDescendants(child, relationShipsMap);
			}
		}
	}
	
	var addRelationShip = function(parent, child, relationShipsMap) {
		getRelationShips(child, relationShipsMap).parents.push(parent);
		getRelationShips(parent, relationShipsMap).children.push(child);
	}
	
	var getRelationShips = function(sample, relationShipsMap) {
		var relationShips = relationShipsMap[sample.permId.permId];
		if (typeof relationShips === 'undefined') {
			relationShips = createRelationShipEntry(sample, relationShipsMap);
		}
		return relationShips;
	}
	
	var createRelationShipEntry = function(sample, relationShipsMap) {
		var relationShips = {parents: [], children: []};
		relationShipsMap[sample.permId.permId] = relationShips;
		return relationShips;
	}
	
}
