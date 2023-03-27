function ELNLIMSPlugin() {
	this.init();
}

/*
 * Enum used by the Event Listeners
 */
var ViewType = {
    SPACE_FORM : 0,
    PROJECT_FORM : 1,
    EXPERIMENT_FORM : 2,
    SAMPLE_FORM : 3,
    DATASET_FORM : 4,
    SAMPLE_TABLE : 5
}

$.extend(ELNLIMSPlugin.prototype, {
	init: function() {
	
	},
	/*
	 * JSON Configuration Extensions
	 * - forcedDisableRTF (Deprecated in favour of Custom Widgets configurable from the Instance Settings on the UI)
	 * - forceMonospaceFont (Deprecated in favour of Custom Widgets configurable from the Instance Settings on the UI)
	 * - experimentTypeDefinitionsExtension
	 * - sampleTypeDefinitionsExtension
	 * - dataSetTypeDefinitionsExtension
	 */
	forcedDisableRTF : ["$NAME"], /* Deprecated */
	forceMonospaceFont : [],  /* Deprecated */
	experimentTypeDefinitionsExtension : {

    },
	sampleTypeDefinitionsExtension : {
	
	},
	dataSetTypeDefinitionsExtension : {
	
	},
	/*
	 * Template Methods:
	 * ONLY allow to add content in certain portions of the Interface.
	 * ONLY available for Experiment, Sample and DataSet form views.
	 * - experimentFormTop
	 * - experimentFormBottom
	 * - sampleFormTop
	 * - sampleFormBottom
	 * - dataSetFormTop
	 * - dataSetFormBottom
	 * These template methods are easy to use.
	 * They allow to add custom components isolating the programmer from the rest of the form.
	 */
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
	/*
	 * Reserved for internal use and discouraged to use. It is tricky to use properly.
	 */
	onSampleSave : function(sample, changesToDo, success, failed) {
        success();
	},
	/*
	 * Allows to extend the utilities menu. Format to be used for utilities
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
	 * Event Listeners
	 * Allow to listen the before/after paint events for ALL form views and list views.
	 * Allow the programmer to change the model before is displayed and any part of the view after.
	 * Provide versatility but with added complexity of dealing with the complete form.
	 * - beforeViewPaint
	 * - afterViewPaint
	 */
	beforeViewPaint : function(viewType, model, $container) {

	},
	afterViewPaint : function(viewType, model, $container) {

    }
});