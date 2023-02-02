// screening dictionary for DynamiX
var screening = {
	
	// If a dictionary contains 'is_default_dictionary' key and the value is 'true', 
	// that dictionary will be treated as a 'default' one. This means that values 
	// from that dictionary will override entries with the same keys defined in other dictionaries. 
	// There should be at most one default dictionary, otherwise the behavior is undefined.  
	is_default_dictionary: "true",


  GENE_LIBRARY_URL: "http://www.yeastgenome.org/cgi-bin/locus.fpl?locus={0}",
  GENE_LIBRARY_SEARCH_URL: "http://www.yeastgenome.org/cgi-bin/search/quickSearch.fpl?query={0}&Submit=Search",
 
  // 
  // General
  // 
  PLATE: "Chip",
  WELL_ROW: "Chamber Row",
  WELL_COLUMN: "Chamber Column",
  WELL: "Chamber",
  WELL_IMAGES: "Chamber Images",
  PREVIEW: "Preview",
  PLATE_VIEWER_TITLE: "Chip {0}",
  
  //
  // Experiment Viewer
  //
  experiment_samples_selction_title: "Chips",
  no_samples_found: "There are no chips in this experiment",
  
  //
  // Sample Viewer
  //

  sample_properties_panel_sample_identifier: "ID",  
  sample: "Sample",
  sample_type: "Sample Type",
  generated_samples: "Children Samples",
  openbis_plate_metadata_browser_CODE: "Code",
  openbis_plate_metadata_browser_TYPE: "Type",
  openbis_plate_metadata_browser_THUMBNAIL: "Thumbnail",
  
  
  sample_properties_heading: "Properties",
  part_of_heading: "Contained",
  derived_samples_heading: "Children",
  parent_samples_heading: "Parents",
  derived_sample: "Child",
  derived_samples: "Children",
  external_data_heading: "Data Sets",
  show_only_directly_connected: "directly connected",

	//
	// Sample import
	//
  import_scheduled: "Import has started successfully. Notification will be sent to '{0}' upon completion.",
	register: "Register",
	
	//
	// Gene Viewer
	//
	plate_locations: "Plate Locations",
  
	//
	// Plate Material Reviewer 
	//    
    SCREENING_MODULE_TITLE: "Screening",
    
    WELL_CONTENT_MATERIAL: "Content",
    WELL_CONTENT_MATERIAL_TYPE: "Content Type",
    WELL_CONTENT_PROPERTIES: "Content Properties",
    WELL_CONTENT_FEATURE_VECTORS: "Feature Vector",
    IMAGE_ANALYSIS_DATA_SET: "Image Analysis Dataset",
    IMAGE_DATA_SET: "Image Dataset",
    
    PLATE_MATERIAL_REVIEWER_TITLE: "Chambers Search",
    PLATE_MATERIAL_REVIEWER_SPECIFY_METERIAL_ITEMS: "E.g. gene symbols, gene ids, gene descriptions, control names or compound names. Separate items with commas (\",\") or specify one item per line.",

    EXPERIMENT_PLATE_MATERIAL_REVIEWER_SECTION: "Chambers Search",
    EXPERIMENT_PLATE_MATERIAL_BROWSER_SECTION: "Library Index",
		EXACT_MATCH_ONLY: "Exact Matches Only",
	
  
  // LAST LINE: KEEP IT AT THE END
  lastline: "" // we need a line without a comma
};