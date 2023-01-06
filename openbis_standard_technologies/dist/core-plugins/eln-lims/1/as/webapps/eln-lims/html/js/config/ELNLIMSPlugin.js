function ELNLIMSPlugin() {
	this.init();
}

var ViewType = {
    SAMPLE_FORM : 0,
    DATASET_FORM : 0
}

$.extend(ELNLIMSPlugin.prototype, {
	init: function() {
	
	},
	forcedDisableRTF : ["$NAME"],
	forceMonospaceFont : [],
	experimentTypeDefinitionsExtension : {

    },
	sampleTypeDefinitionsExtension : {
	
	},
	dataSetTypeDefinitionsExtension : {
	
	},
	experimentFormTop : function($container, model) {

    },
    experimentFormBottom : function($container, model) {

    },
	sampleFormTop : function($container, model) {
	
	},
	sampleFormBottom : function($container, model) {
	
	},
	dataSetFormTop : function($container, model) {
	
	},
	dataSetFormBottom : function($container, model) {

	},
	onSampleSave : function(sample, changesToDo, success, failed) {
        success();
	},
	/*
	 * Format to be used for utilities
	 * {
     * icon : "fa fa-table",
     * uniqueViewName : "VIEW_NAME_TEST",
     * label : "Label Test",
     * paintView : function($header, $content) {
     *         $header.append($("<h1>").append("Test Header"));
     *         $content.append($("<p>").append("Test Body"));
     * }
	 */
	getExtraUtilities : function() {
	    return [];
	},
	/*
	 * View events
	 */
	beforeViewPaint : function(viewType, model, $container) {

	},
	afterViewPaint : function(viewType, model, $container) {

    }
});