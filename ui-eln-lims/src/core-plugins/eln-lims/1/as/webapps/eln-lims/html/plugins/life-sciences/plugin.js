function LifeSciencesTechnology() {
	this.init();
}

$.extend(LifeSciencesTechnology.prototype, ELNLIMSPlugin.prototype, {
	init: function() {
				
	},
	forcedDisableRTF : [],
	forceMonospaceFont : [],
	sampleTypeDefinitionsExtension : {
				"ANTIBODY" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
				},
				"CHEMICAL" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
					"SHOW_ON_NAV": true,
				    "SHOW_ON_NAV_FOR_PARENT_TYPES": ["EXPERIMENTAL_STEP"]
				},
				"ENZYME" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
				},
				"OLIGO" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
				},
				"RNA" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
				},
				"MEDIA" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													}																								
												],
				},
				"SOLUTION_BUFFER" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Media",
														"TYPE": "MEDIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													}																								
												],
				},
				"PCR_PROTOCOL" : {
					"SHOW" : false,
					"USE_AS_PROTOCOL" : true,
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Enzyme",
														"TYPE": "ENZYME",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},																								
												],
				},
				"WESTERN_BLOTTING_PROTOCOL" : {
					"SHOW" : false,
					"USE_AS_PROTOCOL" : true,
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Chemical",
														"TYPE": "CHEMICAL",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Solution/Buffer",
														"TYPE": "SOLUTION_BUFFER",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Antibody",
														"TYPE": "ANTIBODY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.QUANTITY", "MANDATORY" : false }, {"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},																								
												],
				},
				"PLASMID" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													}																		
												],
				},
				"BACTERIA" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Bacteria parents",
														"TYPE": "BACTERIA",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "ANNOTATION.SYSTEM.PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													}																				
												],
				},
				"YEAST" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Yeast parents",
														"TYPE": "YEAST",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "ANNOTATION.SYSTEM.PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													}																					
												],
				},
				"CELL_LINE" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Parental cell line",
														"TYPE": "CELL_LINE",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "ANNOTATION.SYSTEM.PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},		
					                             	{
														"LABEL" : "Parental fly",
														"TYPE": "FLY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													}																									
												],
				},
				"FLY" : {
					"SHOW" : false,
					"ENABLE_STORAGE" : true,
					"SAMPLE_PARENTS_HINT" : [
					                             	{
														"LABEL" : "Fly parents",
														"TYPE": "FLY",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													},
					                             	{
														"LABEL" : "Plasmid",
														"TYPE": "PLASMID",
														"MIN_COUNT" : 0,
														"ANNOTATION_PROPERTIES" : [{"TYPE" : "ANNOTATION.SYSTEM.PLASMID_RELATIONSHIP", "MANDATORY" : false },{"TYPE" : "ANNOTATION.SYSTEM.PLASMID_ANNOTATION", "MANDATORY" : false },{"TYPE" : "ANNOTATION.SYSTEM.COMMENTS", "MANDATORY" : false }]
													}																					
												],
				}
	},
	dataSetTypeDefinitionsExtension : {

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

profile.plugins.push( new LifeSciencesTechnology());