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

function SampleField(isRequired,
					 placeholder,
					 sampleTypeCode,
					 initialValue,
					 isDisabled) {
    var _this = this;
	var isRequired = isRequired;
	var placeholder = placeholder;
	var sampleTypeCode = sampleTypeCode;
	var $plainSelect = FormUtil.getPlainDropdown({}, "");

    var initialised = false;
	var storedParams = null;
	var changeListener = null;
    var initialValue = initialValue;

	//
	// Form API
	//

	this.setValue = function(v3Sample) {
		var data = {
				id : v3Sample.permId.permId,
				text : Util.getDisplayNameForEntity2(v3Sample),
				data : v3Sample
		};
		var newOption = new Option(data.text, data.id, true, true);
		newOption.data = data;
		$plainSelect.append(newOption).trigger('change');
	}


	this.val = function(samplePermId) {
	    if(!initialised) {
	        if(samplePermId) {
	            initialValue = samplePermId;
	        }
	        return;
	    } else if(samplePermId === undefined) {
	        var selected = $plainSelect.select2('data');
	        if(selected && selected[0]) {
	            return selected[0].id;
	        } else {
	            return null;
	        }
	    } else {
            require([ "as/dto/sample/id/SamplePermId", "as/dto/sample/fetchoptions/SampleFetchOptions" ],
            function(SamplePermId, SampleFetchOptions) {
                var id1 = new SamplePermId(samplePermId);
        	    var fetchOptions = new SampleFetchOptions();
        	    fetchOptions.withProperties();
        	    mainController.openbisV3.getSamples([ id1 ], fetchOptions).done(function(map) {
        	        _this.setValue(map[id1]);
        	    });
            });
	    }
	}

    $plainSelect.change = function(listener) {
        changeListener = listener;
    }

    $plainSelect.val = this.val;

    this.attr = function(attrName) {
        return $plainSelect.attr(attrName);
    }

	
	//
	// Search Entity
	//

	var searchSample = function(action) {
        var criteria = null;

        if(sampleTypeCode) {
            criteria = {
                entityKind : "SAMPLE",
                logicalOperator : "OR",
                rules : {},
                subCriteria : {
                    "1": {
                        entityKind : "SAMPLE",
                        logicalOperator : "AND",
                        rules : {
                            "1-1": { type : "Attribute", name : "SAMPLE_TYPE", value : sampleTypeCode },
                            "1-2": { type: "Property/Attribute", 	name: "ATTR.CODE", operator : "thatContains", 		value: storedParams.data.q }
                        }
                    },
                    "2": {
                        entityKind : "SAMPLE",
                        logicalOperator : "AND",
                        rules : {
                            "2-1": { type : "Attribute", name : "SAMPLE_TYPE", value : sampleTypeCode },
                            "2-2": { type: "Property/Attribute", 	name: "PROP.$NAME", operator : "thatContainsString", value: storedParams.data.q }
                        }
                    }
                }
            }
        } else {
            criteria = {
                entityKind : "SAMPLE",
                logicalOperator : "OR",
                rules : {
                    "1": { type: "Property/Attribute", 	name: "PROP.$NAME", operator : "thatContainsString", value: storedParams.data.q },
                    "2": { type: "Property/Attribute", 	name: "ATTR.CODE", operator : "thatContains", value: storedParams.data.q }
                }
            };
        }

		mainController.serverFacade.searchForSamplesAdvanced(criteria, {
			only : true,
			withType : true,
			withProperties : true,
			withExperiment : true,
			withExperimentProperties : true
		}, function(results) { results.type = "Samples"; action(results) });
	}

	//
	// Build Select
	//

    $plainSelect.attr("multiple", "multiple");
    if(isRequired) {
        $plainSelect.attr("required", "required");
    }
    if(isDisabled) {
        $plainSelect.attr("disabled", "disabled");
    }

    Util.onIsInPage($plainSelect[0], function() {
            $plainSelect.select2({
        	    width: '100%',
        	    theme: "bootstrap",
        	    maximumSelectionLength: 1,
        	    minimumInputLength: 2,
        	    placeholder : placeholder,
        	    ajax: {
        	        delay: 1000,
        		    processResults: function (data) {
                        var results = [];

                        for(var dIdx = 0; dIdx < data.length; dIdx++) {
                            var group = {
                                text: data[dIdx].type,
                                children : []
                            }

                            var entities = data[dIdx].objects;
                            for(var eIdx = 0; eIdx < entities.length; eIdx++) {
                                group.children.push({
                                id : entities[eIdx].permId.permId,
                                text : Util.getDisplayNameForEntity2(entities[eIdx]),
                                data : {
                                    id : entities[eIdx].permId.permId,
                                    text : Util.getDisplayNameForEntity2(entities[eIdx]),
                                    data : entities[eIdx]
                                    }
                                })
                            }

                            if(entities.length > 0) {
                                results.push(group);
                            }
                        }

                        return {
                            "results": results,
                            "pagination": {
                                "more": false
                            }
                        };
        			},
        		    transport: function (params, success, failure) {
        			    storedParams = params;

        				// Searches
        				var searches = [searchSample];
                        var searchesResults = [];

        				var action = null;
        				action = function(result) {
        					searchesResults.push(result);
        				    if(searches.length > 0) {
        					    var search = searches.shift();
        					    search(action);
        					} else {
        					    success(searchesResults);
        				    }
        				};

        			    var search = searches.shift();
        			    search(action);
        			    return {
        				    abort : function () { /*Not implemented*/ }
        			    }
        		    }
        	    }
            });

            $plainSelect.on('select2:select', function (e) {
                if(changeListener) {
                    changeListener(null, _this.val());
                }
            });
            $plainSelect.on('select2:unselect', function (e) {
                if(changeListener) {
                    changeListener(null, "");
                }
            });
            initialised = true;
            if(initialValue) {
                _this.val(initialValue);
            }
    });

	return $plainSelect;
}
