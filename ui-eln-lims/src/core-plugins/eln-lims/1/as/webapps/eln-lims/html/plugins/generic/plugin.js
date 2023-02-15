function GenericTechnology() {
	this.init();
}

$.extend(GenericTechnology.prototype, ELNLIMSPlugin.prototype, {
	init: function() {
	
	},
	forcedDisableRTF : [],
	forceMonospaceFont : [],
	sampleTypeDefinitionsExtension : {
		"GENERAL_ELN_SETTINGS" : {
			"SHOW" : false
		},
		"STORAGE" : {
			"SHOW" : false,
			"SAMPLE_CHILDREN_DISABLED" : true,
			"SAMPLE_PARENTS_DISABLED" : true
		},
		"STORAGE_POSITION" : {
			"SHOW" : false,
			"SHOW_ON_NAV" : false,
			"SAMPLE_CHILDREN_DISABLED" : true
		},
		"REQUEST" : {
			"SHOW" : false,
			"FORCE_TO_SHOW_PARENTS_SECTION" : true,
			"SAMPLE_PARENTS_TITLE" : "Products from Catalog",
			"SAMPLE_PARENTS_ANY_TYPE_DISABLED" : true,
			"SAMPLE_CHILDREN_DISABLED" : true,
			"SAMPLE_PARENTS_HINT" : [{
				"LABEL" : "Products",
				"TYPE": "PRODUCT",
				"MIN_COUNT" : 1,
				"MAX_COUNT" : 1,
				"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.REQUEST.QUANTITY_OF_ITEMS", "MANDATORY" : true }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
			}]
		},
		"ORDER" : {
			"SHOW" : false,
			"FORCE_TO_SHOW_PARENTS_SECTION" : true,
			"SAMPLE_PARENTS_TITLE" : "Requests",
			"SAMPLE_PARENTS_ANY_TYPE_DISABLED" : true,
			"SAMPLE_CHILDREN_DISABLED" : true,
				showParents : function(sample) { 
				var orderStatus = sample.properties["$ORDERING.ORDER_STATUS"];
				var orderSummary = sample.properties["$ORDER.ORDER_STATE"];
				return orderStatus !== "ORDERED" && orderStatus !== "DELIVERED" && orderStatus !== "PAID" && !orderSummary;
			},
			"SAMPLE_PARENTS_HINT" : [{
				"LABEL" : "Requests",
				"TYPE": "REQUEST",
				"MIN_COUNT" : 1,
				"ANNOTATION_PROPERTIES" : []
			}]
		},
		"SUPPLIER" : {
			"SHOW" : false,
			"SAMPLE_CHILDREN_DISABLED" : true,
			"SAMPLE_PARENTS_DISABLED" : true,
		},
		"PRODUCT" : {
			"SHOW" : false,
			"FORCE_TO_SHOW_PARENTS_SECTION" : true,
			"SAMPLE_CHILDREN_DISABLED" : true,
			"SAMPLE_PARENTS_TITLE" : "Suppliers",
			"SAMPLE_PARENTS_ANY_TYPE_DISABLED" : true,
			"SAMPLE_PARENTS_HINT" : [{
				"LABEL" : "Suppliers",
				"TYPE": "SUPPLIER",
				"MIN_COUNT" : 1,
				"MAX_COUNT" : 1,
				"ANNOTATION_PROPERTIES" : []
			}]
		},
		"EXPERIMENTAL_STEP" : {
			"SHOW" : true,
			"SHOW_ON_NAV" : true,
			"SAMPLE_PARENTS_HINT" : [{
				"LABEL" : "General protocol",
				"TYPE": "GENERAL_PROTOCOL",
				"MIN_COUNT" : 0,
				"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
			}],
		},
		"ENTRY" : {
        			"SHOW" : true,
        			"SHOW_ON_NAV" : true,
        			"SAMPLE_PARENTS_HINT" : [],
        },
		"GENERAL_PROTOCOL" : {
			"SHOW" : false,
			"USE_AS_PROTOCOL" : true,
			"SAMPLE_PARENTS_HINT" : [{
				"LABEL" : "General protocol",
				"TYPE": "GENERAL_PROTOCOL",
				"MIN_COUNT" : 0,
				"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
			}],
		},
		"UNKNOWN" : {
			"SHOW" : false,
			"SHOW_ON_NAV" : false,
			"SAMPLE_PARENTS_HINT" : [],
		},
	},
	dataSetTypeDefinitionsExtension : {
        "NAGIOS" : {
            "SHOW" : false,
            "SHOW_ON_NAV" : true
        },
        "UNKNOWN" : {
            "SHOW" : false,
            "SHOW_ON_NAV" : true,
        },
	},
	sampleFormTop : function($container, model) {
	
	},
	sampleFormBottom : function($container, model) {
	
	},
	dataSetFormTop : function($container, model) {
	
	},
	dataSetFormBottom : function($container, model) {
	
	}
});