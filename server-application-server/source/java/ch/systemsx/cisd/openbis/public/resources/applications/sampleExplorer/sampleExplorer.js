/*
 * Widget: sampleExplorer
 */
function sampleExplorer(openbis) {
	this.init(openbis);
}

$.extend(sampleExplorer.prototype, {
	init: function(openbis){
		this.openbis = openbis;
		this.tree = new sampleExplorerTree(this);
		this.view = new sampleExplorerView(this);
	},
	render: function(){
		var $this = this;

		var widget = $("<div></div>").addClass("sampleExplorer");
		widget.append(this.renderHome());
		
		this.openbis.getSessionToken(function(getSessionTokenResponse){
			$this.openbis.useSession(getSessionTokenResponse.result);
			widget.append($this.renderTree());
			widget.append($this.renderView());
		});
		return widget;
	},
	renderHome: function(){
		return $("<a href='" + this.openbis.openbisHost + "/" + this.openbis.openbisContext + "/" + "'>Home</a>").addClass("sampleExplorerHome");
	},
	renderTree: function(){
		return this.tree.render();
	},
	renderView: function(){
		return this.view.render();
	}
});

/*
 * Widget: sampleExplorerView
 */
function sampleExplorerView(explorer){
	this.init(explorer);
}

$.extend(sampleExplorerView.prototype, {
	init: function(explorer){
		this.explorer = explorer;
	},
	render: function(){
		this.widget = $("<iframe width='100%' height='100%'></iframe>").addClass("sampleExplorerView");
		return this.widget;
	},
	open: function(entityType, entityId){
		this.widget.get(0).src = this.explorer.openbis.openbisHost + "/" + this.explorer.openbis.openbisContext + "/index.html" 
		+ "?viewMode=EMBEDDED#entity=" + entityType
		+ "&permId=" + entityId;
	}
});

/*
 * Widget: sampleExplorerTree
 */
function sampleExplorerTree(explorer){
	this.init(explorer);
}

$.extend(sampleExplorerTree.prototype, {
	init: function(explorer){
		this.root = new sampleExplorerTreeRootNode(explorer);
	},
	render: function(){
		this.widget = $("<div></div>").addClass("sampleExplorerTree");
		this.widget.append(this.root.render());
		return this.widget;
	},
	selectNode: function(node){
		if(node != this.selectedNode){
			if(this.selectedNode){
				this.selectedNode.widget.removeClass("selected");
			}
			node.widget.addClass("selected");
			this.selectedNode = node;
		}
	}
});

/*
 * Widget: sampleExplorerTreeNodeAbstract
 */
function sampleExplorerTreeNodeAbstract(explorer, entity){
	this.init(explorer, entity);
}

$.extend(sampleExplorerTreeNodeAbstract.prototype, {
	init: function(explorer, entity){
		this.explorer = explorer;
		this.entity = entity;
		this.childrenPossible = true;
		this.childrenShown = false;
		this.widget = $("<div></div>").addClass('sampleExplorerTreeNode');
	},
	render: function(){
		this.widget.empty();
		this.widget.append(this.renderShowHide());
		this.widget.append(this.renderEntity());
		this.widget.append(this.renderChildren());
		return this.widget;
	},
	renderShowHide: function(){
		var widget = this.widgetShowHide;
		if(!widget){
			widget = $("<div></div>").addClass("sampleExplorerTreeNodeShowHide");
			this.widgetShowHide = widget;
		}

		if(this.childrenPossible){
			var $this = this;
			
			var link = this.widgetShowHideLink;
			if(!link){
				link = $("<a></a>").click(function(){
					$this.toggleChildren();
				});
				widget.append(link);
				this.widgetShowHideLink = link;
			}
					
			if(this.childrenShown){
				link.removeClass("show");
				link.addClass("hide");
			}else{
				link.removeClass("hide");
				link.addClass("show");
			}
		}
		return widget;
	},
	renderEntity: function(){
	},
	renderChildren: function(){
		var widget = this.widgetChildren;
		if(!widget){
			widget = $("<div></div>").addClass("sampleExplorerTreeNodeChildren");
			this.widgetChildren = widget;
		}
		
		if(this.childrenPossible){
			var $this = this;

			if(this.childrenShown){
				if(this.childrenRendered){
					widget.show();
				}else{
					this.getChildren(function(children){
						$.each(children, function(index, child){
							widget.append(child.render());
						});
						widget.show();
					});
					$this.childrenRendered = true;
				}
			}else{
				widget.hide();
			}
		}
		return widget;
	},
	getChildren: function(action){
		action([]);
	},
	showChildren: function(){
		if(this.childrenPossible){
			this.childrenShown = true;
			this.renderShowHide();
			this.renderChildren();
		}
	},
	hideChildren: function(){
		if(this.childrenPossible){
			this.childrenShown = false;
			this.renderShowHide();
			this.renderChildren();
		}
	},
	toggleChildren: function(){
		if(this.childrenShown){
			this.hideChildren();
		}else{
			this.showChildren();
		}
	}
});

/*
 * Widget: sampleExplorerTreeNodeWithStaticChildrenListAbstract
 */
function sampleExplorerTreeNodeWithStaticChildrenListAbstract(explorer, entity){
	this.init(explorer, entity);
}

$.extend(sampleExplorerTreeNodeWithStaticChildrenListAbstract.prototype, sampleExplorerTreeNodeAbstract.prototype, {
	init: function(explorer, entity){
		sampleExplorerTreeNodeAbstract.prototype.init.call(this, explorer, entity);
		this.children = [];
	},
	addChild: function(child){
		this.children.push(child);	
	},
	getChildren: function(action){
		action(this.children);
	}
});

/*
 * Widget: sampleExplorerTreeNodeWithDynamicChildrenListAbstract
 */
function sampleExplorerTreeNodeWithDynamicChildrenListAbstract(explorer, entity){
	this.init(explorer, entity);
}

$.extend(sampleExplorerTreeNodeWithDynamicChildrenListAbstract.prototype, sampleExplorerTreeNodeAbstract.prototype, {
	init: function(explorer, entity){
		sampleExplorerTreeNodeAbstract.prototype.init.call(this, explorer, entity);
		this.children = [];
		this.childrenLoaded = false;
		this.childrenCached = true;
	},
	getChildren: function(action){
		var $this = this;
		if(this.childrenLoaded && this.childrenCached){
			action(this.children);
		}else{
			this.loadChildren(function(children){
				$this.childrenLoaded = true;
				$this.children = children;
				action(children);
			});
		}
	},
	loadChildren: function(action){
		action([]);
	}
});

/*
 * Widget: sampleExplorerTreeRootNode
 */
function sampleExplorerTreeRootNode(explorer){
	this.init(explorer);
}

$.extend(sampleExplorerTreeRootNode.prototype, sampleExplorerTreeNodeWithDynamicChildrenListAbstract.prototype, {
	init: function(explorer){
		sampleExplorerTreeNodeWithDynamicChildrenListAbstract.prototype.init.call(this, explorer, null);
		this.widget.addClass("sampleExplorerTreeRootNode");
		this.childrenShown = true;
	},
	renderShowHide: function(){},
	renderEntity: function(){},
	loadChildren: function(action){
		var $this = this;
		this.explorer.openbis.listSpacesWithProjectsAndRoleAssignments(null, function(listSpacesResponse){
			var children = [];
			if(listSpacesResponse.result && listSpacesResponse.result.length > 0){
				$.each(listSpacesResponse.result, function(spaceIndex, space){
					var spaceNode = new sampleExplorerTreeSpaceNode($this.explorer, space);
					if(space.projects && space.projects.length > 0){
						$.each(space.projects, function(projectIndex, project){
							spaceNode.addChild(new sampleExplorerTreeProjectNode($this.explorer, project));
						});
					}else{
						spaceNode.addChild(new sampleExplorerTreeMessageNode("No projects found"));
					}
					children.push(spaceNode);
				});
			}else{
				children.push(new sampleExplorerTreeMessageNode("No spaces found"));
			}
			action(children);
		});
	}
});

/*
 * Widget: sampleExplorerTreeSpaceNode
 */
function sampleExplorerTreeSpaceNode(explorer, space){
	this.init(explorer, space);
}

$.extend(sampleExplorerTreeSpaceNode.prototype, sampleExplorerTreeNodeWithStaticChildrenListAbstract.prototype, {
	init: function(explorer, space){
		sampleExplorerTreeNodeWithStaticChildrenListAbstract.prototype.init.call(this, explorer, space);
		this.widget.addClass("sampleExplorerTreeSpaceNode");
		this.childrenShown = true;
	},
	renderEntity: function(){
		return new sampleExplorerTreeNodeEntityPanel(this).render();
	}
});

/*
 * Widget: sampleExplorerTreeProjectNode
 */
function sampleExplorerTreeProjectNode(explorer, project){
	this.init(explorer, project);
}

$.extend(sampleExplorerTreeProjectNode.prototype, sampleExplorerTreeNodeWithDynamicChildrenListAbstract.prototype, {
	init: function(explorer, project){
		sampleExplorerTreeNodeWithDynamicChildrenListAbstract.prototype.init.call(this, explorer, project);
		this.widget.addClass("sampleExplorerTreeProjectNode");
	},
	renderEntity: function(){
		return new sampleExplorerTreeNodeEntityPanel(this).render();
	},
	loadChildren: function(action){
		var $this = this;
		this.explorer.openbis.listExperiments([this.entity], null, function(listExperimentsResponse){
			var children = [];
			if(listExperimentsResponse.result && listExperimentsResponse.result.length > 0){
				$.each(listExperimentsResponse.result, function(experimentIndex, experiment){
					children.push(new sampleExplorerTreeExperimentNode($this.explorer, experiment));
				});
			}else{
				children.push(new sampleExplorerTreeMessageNode("No experiments found"));				
			}
			action(children);
		});
	}	
});

/*
 * Widget: sampleExplorerTreeExperimentNode
 */
function sampleExplorerTreeExperimentNode(explorer, experiment){
	this.init(explorer, experiment);
}

$.extend(sampleExplorerTreeExperimentNode.prototype, sampleExplorerTreeNodeWithDynamicChildrenListAbstract.prototype, {
	init: function(explorer, experiment){
		sampleExplorerTreeNodeWithDynamicChildrenListAbstract.prototype.init.call(this, explorer, experiment);
		this.widget.addClass("sampleExplorerTreeExperimentNode");
	},
	renderEntity: function(){
		return new sampleExplorerTreeNodeEntityPanelWithDetails(this, "EXPERIMENT").render();
	},
	loadChildren: function(action){
		var $this = this;
		this.explorer.openbis.listSamplesForExperiment(this.entity.identifier, function(listSamplesResponse){
			var children = [];
			if(listSamplesResponse.result && listSamplesResponse.result.length > 0){
				$.each(listSamplesResponse.result, function(sampleIndex, sample){
					children.push(new sampleExplorerTreeSampleNode($this.explorer, sample));
				});
			}else{
				children.push(new sampleExplorerTreeMessageNode("No samples found"));
			}
			action(children);
		});
	}	
});

/*
 * Widget: sampleExplorerTreeSampleNode
 */
function sampleExplorerTreeSampleNode(explorer, sample){
	this.init(explorer, sample);
}

$.extend(sampleExplorerTreeSampleNode.prototype, sampleExplorerTreeNodeAbstract.prototype, {
	init: function(explorer, sample){
		sampleExplorerTreeNodeAbstract.prototype.init.call(this, explorer, sample);
		this.widget.addClass("sampleExplorerTreeSampleNode");
		this.childrenPossible = false;
	},
	renderEntity: function(){
		var panel = new sampleExplorerTreeNodeEntityPanelWithDetails(this, "SAMPLE");
		var widget = panel.render();
		widget.click(function(){
			panel.showDetails();
		});
		return widget;
	}
});

/*
 * Widget: sampleExplorerTreeMessageNode
 */
function sampleExplorerTreeMessageNode(message){
	this.init(message);
}

$.extend(sampleExplorerTreeMessageNode.prototype, sampleExplorerTreeNodeAbstract.prototype, {
	init: function(message){
		sampleExplorerTreeNodeAbstract.prototype.init.call(this, null, message);
		this.widget.addClass("sampleExplorerTreeMessageNode");
		this.childrenPossible = false;
	},
	renderEntity: function(){
		return $("<div>" + this.entity + "</div>").addClass("sampleExplorerTreeNodeEntity");
	}
});

/*
 * Widget: sampleExplorerTreeEntityPanel
 */
function sampleExplorerTreeNodeEntityPanel(node){
	this.init(node);
}

$.extend(sampleExplorerTreeNodeEntityPanel.prototype, {
	init: function(node){
		this.node = node;
	},
	render: function(){
		var $this = this;
		var widget = $("<div>" + this.node.entity.code + "</div>").addClass("sampleExplorerTreeNodeEntity");
		widget.click(function(){
			$this.node.toggleChildren();
		});
		return widget;
	}
});

/*
 * Widget: sampleExplorerTreeEntityPanelWithDetails
 */
function sampleExplorerTreeNodeEntityPanelWithDetails(node, nodeType){
	this.init(node, nodeType);
}

$.extend(sampleExplorerTreeNodeEntityPanelWithDetails.prototype, {
	init: function(node, nodeType){
		this.node = node;
		this.nodeType = nodeType;
	},
	render: function(){
		var $this = this;

		var code = $("<span>" + this.node.entity.code + "</span>").addClass("sampleExplorerTreeNodeEntityCode");
		var details = $("<span>(show details)</span>").addClass("sampleExplorerTreeNodeEntityDetails").hide();
		
		details.click(function(event){
			event.stopPropagation();
			$this.showDetails();
		});

		var widget = $("<div></div>").addClass("sampleExplorerTreeNodeEntity");
		widget.click(function(){
			$this.node.toggleChildren();
		});
		widget.hover(function(){
			details.css('display', 'inline');
		}, function(){
			details.css('display', 'none');
		});
		
		widget.append(code);
		widget.append(details);
		return widget;
	},
	showDetails: function(){
		this.node.explorer.view.open(this.nodeType, this.node.entity.permId);
		this.node.explorer.tree.selectNode(this.node);
	}
});