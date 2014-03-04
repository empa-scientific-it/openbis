/*
 * Copyright 2013 ETH Zuerich, CISD
 *
 * Licensed under the Apache License, Version 2.0 (the 'License');
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an 'AS IS' BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

function SampleHierarchy(serverFacade, inspector, containerId, profile, sample) {
	this.nodeIdPrefix = "HIERARCHY_NODE_";
	this.serverFacade = serverFacade;
	this.inspector = inspector;
	this.containerId = containerId;
	this.profile = profile;
	this.sample = sample;
	
	this.init = function() {
		this.repaint();
	}
	
	this._getSampleTypes = function(sample) {
		var sampleTypes = {};
		
		var getSampleTypesWithQueueRecursion = function(sample, sampleTypes) {
			if(!sampleTypes[sample.sampleTypeCode]) {
				sampleTypes[sample.sampleTypeCode] = true;
			}
			
			if(sample.parents) {
				for(var i = 0; i < sample.parents.length; i++) {
					getSampleTypesWithQueueRecursion(sample.parents[i], sampleTypes);
				}
			}
			
			if(sample.children) {
				for(var i = 0; i < sample.children.length; i++) {
					getSampleTypesWithQueueRecursion(sample.children[i], sampleTypes);
				}
			}
		}
		
		getSampleTypesWithQueueRecursion(sample, sampleTypes);
		return sampleTypes;
	}
	
	this._getMaxChildrenDepth = function(sample) {
		var getMaxChildrenDepthWithQueueRecurion = function(sample, max) {
			if(sample.children) {
				var posibleNextMax = [];
				
				for(var i = 0; i < sample.children.length; i++) {
					var nextMax = getMaxChildrenDepthWithQueueRecurion(sample.children[i], (max + 1));
					posibleNextMax.push(nextMax);
				}
				
				for(var i = 0; i < posibleNextMax.length; i++) {
					if(posibleNextMax[i] > max) {
						max = posibleNextMax[i];
					}
				}
			}
			
			return max;
		}
		
		var result = getMaxChildrenDepthWithQueueRecurion(sample, 0);
		return result;
	}
	
	this._getMaxParentsDepth = function(sample) {
		var getMaxParentsDepthWithQueueRecurion = function(sample, max) {
			if(sample.parents) {
				var posibleNextMax = [];
				
				for(var i = 0; i < sample.parents.length; i++) {
					var nextMax = getMaxParentsDepthWithQueueRecurion(sample.parents[i], (max + 1));
					posibleNextMax.push(nextMax);
				}
				
				for(var i = 0; i < posibleNextMax.length; i++) {
					if(posibleNextMax[i] > max) {
						max = posibleNextMax[i];
					}
				}
			}
			
			return max;
		}
		
		var result = getMaxParentsDepthWithQueueRecurion(sample, 0);
		return result;
	}
	
	this.repaint = function() {
		var localInstance = this;
		$('#'+this.containerId).empty();
		
		var $filtersForm = $('<form>' , { class : 'form-inline'});
		$filtersForm.submit(function(event) {localInstance._filterSampleAndUpdate(); event.preventDefault();});
		
		var maxChildren = this._getMaxChildrenDepth(this.sample);
		var	$filtersFormSliderChildren = null;
		if(maxChildren > 0) {
			$filtersFormSliderChildren = $('<input>' , { 'id' : 'childrenLimit' , 'type' : 'text' , 'class' : 'span2', 'value' : '' , 'data-slider-max' : maxChildren , 'data-slider-value' : maxChildren});
		} else {
			$filtersFormSliderChildren = 'No Children';
		}
		
		var maxParents = this._getMaxParentsDepth(this.sample);
		var	$filtersFormSliderParents = null;
		if(maxParents > 0) {
			$filtersFormSliderParents = $('<input>' , { 'id' : 'parentsLimit' , 'type' : 'text' , 'class' : 'span2', 'value' : '' , 'data-slider-max' : maxParents , 'data-slider-value' : maxParents});
		} else {
			$filtersFormSliderParents = 'No Parents';
		}
		
		var sampleTypes = this._getSampleTypes(this.sample);
		var	$filtersFormSampleTypes = $('<select>', { 'id' : 'sampleTypesSelector' , class : 'multiselect' , 'multiple' : 'multiple'});
		for (var sampleType in sampleTypes) {
			$filtersFormSampleTypes.append($('<option>', { 'value' : sampleType , 'selected' : ''}).html(sampleType));
		}
		
		$filtersForm
			.append('<b>Filters</b>')
			.append("<span style='padding-right:15px;'></span>")
			.append('Children: ')
			.append($filtersFormSliderChildren)
			.append("<span style='padding-right:15px;'></span>")
			.append(' Parents: ')
			.append($filtersFormSliderParents)
			.append("<span style='padding-right:15px;'></span>")
			.append(' Show Types: ')
			.append($filtersFormSampleTypes)
			.append("<span style='position:absolute; left:30px; top:80px;'><svg height='100' width='100'><g id='svgControls'/></svg></span>");
		
		$('#'+this.containerId).append($filtersForm);
		$('#'+this.containerId).append($('<div>', { 'id' : 'graphContainer' }));
		$('#graphContainer').append("<svg id='svgMapContainer'><g id='svgMap' transform='translate(20,20) scale(1)'/></svg>");
		
		$('#childrenLimit').slider();
		$('#childrenLimit').slider().on('slideStop', function(event){
			localInstance._filterSampleAndUpdate();
		});
		
		$('#parentsLimit').slider();
		$('#parentsLimit').slider().on('slideStop', function(event){
			localInstance._filterSampleAndUpdate();
		});
		
		$('#sampleTypesSelector').multiselect();
		$('#sampleTypesSelector').change(function(event){
			localInstance._filterSampleAndUpdate();
		});
		
		this._filterSampleAndUpdate();
		
		//Add SVG Map Controls
		var path1 = this._makeSVG('path', {'class' : 'svgButton', 'stroke-linecap':'round', 'stroke-miterlimit':'6', 'onclick':'javascript:mainController.currentView.pan( 0, 50);', 'd':'M50 10 l12 20 a40, 70 0 0,0 -24, 0z', 'stroke-width':'1.5', 'fill':'#0088CC', 'stroke': '#0088CC' });
		var path2 = this._makeSVG('path', {'class' : 'svgButton', 'stroke-linecap':'round', 'stroke-miterlimit':'6', 'onclick':'javascript:mainController.currentView.pan( 50, 0);', 'd':'M10 50 l20 -12 a70, 40 0 0,0 0, 24z', 'stroke-width':'1.5', 'fill':'#0088CC', 'stroke': '#0088CC' });
		var path3 = this._makeSVG('path', {'class' : 'svgButton', 'stroke-linecap':'round', 'stroke-miterlimit':'6', 'onclick':'javascript:mainController.currentView.pan( 0,-50);', 'd':'M50 90 l12  -20 a40, 70 0 0,1 -24,  0z', 'stroke-width':'1.5', 'fill':'#0088CC', 'stroke': '#0088CC' });
		var path4 = this._makeSVG('path', {'class' : 'svgButton', 'stroke-linecap':'round', 'stroke-miterlimit':'6', 'onclick':'javascript:mainController.currentView.pan(-50, 0);', 'd':'M90 50 l-20 -12 a70, 40 0 0,1 0, 24z', 'stroke-width':'1.5', 'fill':'#0088CC', 'stroke': '#0088CC' });
		var circle2 = this._makeSVG('circle', {'cx':'50', 'cy':'50', 'r':'20', 'stroke':'#000', 'stroke-width':'1.5', 'fill':'#fff', 'opacity':'0.75'});
		var rect1 = this._makeSVG('rect', {'x':'46', 'y':'39.5', 'width':'8', 'height':'3' , 'fill':'#fff', 'style' : 'pointer-events: none;'});
		var rect2 = this._makeSVG('rect', {'x':'46', 'y':'57.5', 'width':'8', 'height':'3' , 'fill':'#fff', 'style' : 'pointer-events: none;'});
		var rect3 = this._makeSVG('rect', {'x':'48.5', 'y':'55', 'width':'3', 'height':'8' , 'fill':'#fff', 'style' : 'pointer-events: none;'});
		var circle3 = this._makeSVG('circle', {'class' : 'svgButton', 'stroke-linecap':'round', 'stroke-miterlimit':'6', 'onclick':'javascript:mainController.currentView.zoom(0.8)', 'cx':'50', 'cy':'41', 'r':'8', 'stroke-width':'1.5', 'fill':'#0088CC', 'stroke': '#0088CC' });
		var circle4 = this._makeSVG('circle', {'class' : 'svgButton', 'stroke-linecap':'round', 'stroke-miterlimit':'6', 'onclick':'javascript:mainController.currentView.zoom(1.25)', 'cx':'50', 'cy':'59', 'r':'8', 'stroke-width':'1.5', 'fill':'#0088CC', 'stroke': '#0088CC' });
		var svgControls = $("#svgControls")
			.append(path1)
			.append(path2)
			.append(path3)
			.append(path4)
			.append(circle2)
			.append(circle3)
			.append(circle4)
			.append(rect1)
			.append(rect2)
			.append(rect3);
	}
	
	this._filterSampleAndUpdate = function() {
		var newSample = jQuery.extend(true, {}, this.sample);
		
		//
		// Used to remove the type label when rendering
		//
		var selectedSampleTypes = $('#sampleTypesSelector').val();
		if(selectedSampleTypes === null) {
			selectedSampleTypes = [];
		}
		var inArray = function(value, array) {
			for(var i = 0; i < array.length; i++) {
				if(array[i] === value) {
					return true;
				}
			}
			return false;
		}
		
		var selectedSampleTypesFilter = function(sample, selectedSampleTypes) {
			if(sample.parents) {
				for(var i = 0; i < sample.parents.length; i++) {
					sample.parents[i].showLabel = inArray(sample.parents[i].sampleTypeCode, selectedSampleTypes);
					selectedSampleTypesFilter(sample.parents[i], selectedSampleTypes);
				}
				
			}
			if(sample.children) {
				for(var i = 0; i < sample.children.length; i++) {
					sample.children[i].showLabel = inArray(sample.children[i].sampleTypeCode, selectedSampleTypes);
					selectedSampleTypesFilter(sample.children[i], selectedSampleTypes);
				}
			}
		}
		selectedSampleTypesFilter(newSample, selectedSampleTypes);
		
		//
		// Used to cut the tree
		//
		var parentsLimit =  ($('#parentsLimit').length > 0)?$('#parentsLimit').data('slider').getValue():0;
		var parentsLimitFilter = function(sample, depthLimit) {
			if(sample.parents) {
				if(depthLimit === 0) {
					sample.parents = null;
				} else {
					for(var i = 0; i < sample.parents.length; i++) {
						parentsLimitFilter(sample.parents[i], (depthLimit - 1));
					}
				}
			}
		}
		parentsLimitFilter(newSample, parentsLimit);
		
		//
		// Used to cut the tree
		//
		var childrenLimit = ($('#childrenLimit').length > 0)?$('#childrenLimit').data('slider').getValue():0;
		var childrenLimitFilter = function(sample, depthLimit) {
			if(sample.children) {
				if(depthLimit === 0) {
					sample.children = null;
				} else {
					for(var i = 0; i < sample.children.length; i++) {
						childrenLimitFilter(sample.children[i], (depthLimit - 1));
					}
				}
			}
		}
		childrenLimitFilter(newSample, childrenLimit);
		
		this._repaintGraph(newSample);
	}
	
	this._addChildFor = function(permId) {
		var sampleTypes = this.profile.getAllSampleTypes();
		
		var component = "<select id='sampleTypeSelector' required>";
		component += "<option disabled=\"disabled\" selected></option>";
		for(var i = 0; i < sampleTypes.length; i++) {
			var sampleType = sampleTypes[i];
			var label = Util.getEmptyIfNull(sampleType.description);
			if(label === "") {
				label = sampleType.code;
			}
			
			component += "<option value='" + sampleType.code + "'>" + label + "</option>";
		}
		component += "</select>";
		
		Util.blockUI("Select the type for the Child: <br><br>" + component + "<br> or <a class='btn' id='sampleTypeSelectorCancel'>Cancel</a>");
		
		$("#sampleTypeSelectorCancel").on("click", function(event) { 
			Util.unblockUI();
		});
		
		var _this = this;
		$("#sampleTypeSelector").on("change", function(event) {
			var sampleTypeCode = $("#sampleTypeSelector")[0].value;
			mainController.changeView('showCreateSamplePage', sampleTypeCode);
			_this.serverFacade.searchWithUniqueId(permId, function(data) {
				var parentGroup = _this.profile.getGroupTypeCodeForTypeCode(data[0].sampleTypeCode);
				mainController.currentView.sampleTypesLinksTables["sampleParents_" + parentGroup].addSample(data[0]);
			});
		});
	}
	
	this._updateDataFor = function(show, permId) {
		var searchAndUpdateData = function(show, permId, sample) {
			if(sample.permId === permId) {
				sample.showDataOnGraph = show;
			} else {
				if(sample.parents) {
					for(var i = 0; i < sample.parents.length; i++) {
						searchAndUpdateData(show, permId, sample.parents[i]);
					}
				}
				
				if(sample.children) {
					for(var i = 0; i < sample.children.length; i++) {
						searchAndUpdateData(show, permId, sample.children[i]);
					}
				}
			}
		}
		
		searchAndUpdateData(show, permId, this.sample);
		this._filterSampleAndUpdate();
		this._glowNode(this.nodeIdPrefix + permId);
	}
	
	this._updateDisplayabilityFor = function(hide, permId) {
		var searchAndUpdateDisplayability = function(hide, permId, sample) {
			if(sample.permId === permId) {
				sample.hideGraphConnections = hide;
			} else {
				if(sample.parents) {
					for(var i = 0; i < sample.parents.length; i++) {
						searchAndUpdateDisplayability(hide, permId, sample.parents[i]);
					}
				}
				
				if(sample.children) {
					for(var i = 0; i < sample.children.length; i++) {
						searchAndUpdateDisplayability(hide, permId, sample.children[i]);
					}
				}
			}
		}
		
		searchAndUpdateDisplayability(hide, permId, this.sample);
		this._filterSampleAndUpdate();
		this._glowNode(this.nodeIdPrefix + permId);
	}
	
	this._glowNode = function(nodeId) {
		var _this = this;
		$("#"+nodeId).removeClass("glow");
		var glow = function() {
			$("#" + nodeId).addClass("glow"); //Make it Glow
			
			//TO-DO: Fix this hack
			//In Webkit browsers is needed to force rendering moving DOM objects during the animation.
			//On this case we move them 0 pixels during the animation during 3 seconds, every 100 millis
			for(var i=0; i < 30; i++) {
				var webKitFixAnimFix = function() {
					_this.pan(0,0);
				};
				setTimeout(webKitFixAnimFix, 100*i);
			}
			//
		}
		setTimeout(glow, 500);
	}
	
	this._repaintGraph = function(sample) {
		var g = new dagreD3.Digraph();
		
		//Fill graph
		var NODES = {};
		
		var _this = this;
		function addSampleNodes(sample, rootPermId) {
			if(!NODES[sample.permId]) {
				var nodeId =  _this.nodeIdPrefix + sample.permId;
				var $nodeContent = $('<div>');
				$nodeContent.css({
					'white-space' :'nowrap',
					'padding' : '10px',
					'margin' : '10px',
					'background-color' : (sample.permId === rootPermId)?'lightgreen':'transparent',
					'border-radius' : '90px'
				});
				
				var $hideLink = $('<a>', {
					'href' : "javascript:mainController.currentView._updateDisplayabilityFor(" + ((sample.hideGraphConnections)?false:true) + ",'" + sample.permId + "');"
				}).append(
					$('<img>', { 
						'src' : (sample.hideGraphConnections)?'./img/eye-close-icon.png':'./img/eye-open-icon.png',
						'style' : 'cursor:pointer; width:18px; height:18px;',
					}));

				var $dataLink = $('<a>', {
					'href' : "javascript:mainController.currentView._updateDataFor(" + ((sample.showDataOnGraph)?false:true) + ",'" + sample.permId + "');"
				}).append(
					$('<i>', {
						'class' : (sample.showDataOnGraph)?'icon-chevron-up':'icon-chevron-down',
						'style' : 'cursor:pointer',
					}));
				
				var $addChildLink = $('<a>', {
					'href' : "javascript:mainController.currentView._addChildFor('" + sample.permId + "');"
				}).append(
					$('<i>', {
						'class' : 'icon-plus-sign',
						'style' : 'cursor:pointer',
					}));
				
				var $sampleLink = $('<a>', { 'href' : "javascript:mainController.changeView('showViewSamplePageFromPermId', '" + sample.permId + "')"}).html(sample.code);
				
				
				
				if(sample.showLabel || (sample.permId === rootPermId)) {
					$nodeContent
						.append($hideLink)
						.append($dataLink)
						.append($addChildLink)
						.append(sample.sampleTypeCode + ':')
						.append($sampleLink);
				
				if(sample.showDataOnGraph) {
					var optionalInspectorTitle = $nodeContent[0].outerHTML;
					var $inspector = _this.inspector.getInspectorTable(sample, false, true, false, optionalInspectorTitle, true);
					
					$nodeContent.empty();
					$nodeContent.css({
						'background-color' : 'transparent'
					});
					$nodeContent.append($inspector);
				}
					
				} else {
					$nodeContent.append('---');
				}
				
				$nodeContent.attr('id', nodeId);
				if(sample.showDataOnGraph) {
					$nodeContent.css({
						'padding' : '0px'
					});
				}
				g.addNode(sample.permId, { label: $nodeContent[0].outerHTML});
				
				NODES[sample.permId] = true;
			}
			
			if(sample.parents && !sample.hideGraphConnections) {
				sample.parents.forEach(addSampleNodes, rootPermId);
			}
			if(sample.children && !sample.hideGraphConnections) {
				sample.children.forEach(addSampleNodes, rootPermId);
			}
		}
		
		var EDGES = {};
		
		function addSampleEdges(sample) {
			if(sample.parents && !sample.hideGraphConnections) {
				for(var i=0; i < sample.parents.length; i++) {
					if(!EDGES[sample.parents[i].permId + ' -> ' + sample.permId]) {
						g.addEdge(null, sample.parents[i].permId, sample.permId);
						EDGES[sample.parents[i].permId + ' -> ' + sample.permId] = true;
					}
				}
				sample.parents.forEach(addSampleEdges);
			}
			if(sample.children && !sample.hideGraphConnections) {
				for(var i=0; i < sample.children.length; i++) {
					if(!EDGES[sample.permId + ' -> ' + sample.children[i].permId]) {
						g.addEdge(null, sample.permId, sample.children[i].permId);
						EDGES[sample.permId + ' -> ' + sample.children[i].permId] = true;
					}
				}
				
				sample.children.forEach(addSampleEdges);
			}
		}
		
		addSampleNodes(sample, sample.permId);
		addSampleEdges(sample);
		
		// Render the directed graph
		var svgG = d3.select('#svgMap');
		var renderer = new dagreD3.Renderer();
		
		// Custom transition function
		function transition(selection) {
			return selection.transition().duration(500);
		}
		
		renderer.transition(transition);
		
		var layout = dagreD3.layout()
							.nodeSep(20)
							.rankDir("TB");
		
		//Render Layout
		renderer.layout(layout).run(g, svgG);
		transition(d3.select('#svgMapContainer'))
			.attr('width', $(document).width() - 30)
			.attr('height', $(document).height() - 120)
		
		d3.select('#svgMapContainer').call(zoomFunc);
	}
	
	this._makeSVG = function(tag, attrs) {
        var el= document.createElementNS('http://www.w3.org/2000/svg', tag);
        for (var k in attrs)
            el.setAttribute(k, attrs[k]);
        return el;
    }
	
	//
	// Zoom and Pan API (Can be used externaly)
	//
	
    this.pan = function(dx, dy)
    {
		var newTranslate = [projection.translate[0] + dx, projection.translate[1] + dy];
		zoomFunc.translate(newTranslate);
		projection.translate = newTranslate;
		d3.select('#svgMap').attr('transform', 'translate(' + newTranslate + ') scale(' + projection.scale + ')');
    }
    
	this.zoom = function(scale)
	{
		var newScale = projection.scale * scale;
		zoomFunc.scale(newScale);
		projection.scale = newScale;
		d3.select('#svgMap').attr('transform', 'translate(' + projection.translate + ') scale(' + newScale + ')');
    }
	
	//
	// Zoom and Pan Internals (To keep in sync internal D3 projection of the translate and scale properties with the DOM ones)
	//
	var projection = {
			scale : 1,
			translate : [20, 20]
	};
	
	var zoomFunc = d3.behavior.zoom()
		.translate(projection.translate)
		.scale(projection.scale)
		.on("zoom", move);
	
	function move() {
		  var t = d3.event.translate,
		      s = d3.event.scale;
		  
		  zoomFunc.translate(t);
		  zoomFunc.scale(s);
		  
		  projection.translate = t; 
		  projection.scale = s;
		  
		  d3.select('#svgMap').attr('transform', 'translate(' + t + ') scale(' + s + ')');
	}
}
