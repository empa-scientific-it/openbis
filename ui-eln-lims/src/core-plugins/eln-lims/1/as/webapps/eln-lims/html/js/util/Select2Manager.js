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

// Don't bind events when the UI is block or Select2 will not work correctly.
$.blockUI.defaults.bindEvents = false;

var Select2Manager = new function() {
	var toInitialize = [];
	var isPolling = false;
	
	this.add = function(element, properties) {
		toInitialize.push({
			element : element,
			properties : properties,
		});
		if (isPolling === false) {
			isPolling = true;
			setTimeout(_polling, 50);
		}
	}
	
	var _polling = function() {
		try {
			var toReschedule = [];
			while(toInitialize.length > 0) {
				var next = toInitialize.shift();
				if (_isInDom(next.element)) {
					next.element.select2(next.element.properties ? next.element.properties : { width: '100%', theme: "bootstrap" });
				} else {
					toReschedule.push(next);
				}	
			}
			
			toInitialize = toReschedule;
			
			if (toInitialize.length === 0) {
				isPolling = false;
			} else {
				setTimeout(_polling, 50);
			}
			
		} catch(err) {
			isPolling = false;
			toInitialize = [];
		}
	}
	
	var _isInDom = function(element) {
		var el = element[0];
		while (el = el.parentNode) {
			if (el === document) {
				return true;
			}
		}
		return false;
	}
}