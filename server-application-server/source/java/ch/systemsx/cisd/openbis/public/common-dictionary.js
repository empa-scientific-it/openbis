// Common dictionary

var entityTypes = {}
entityTypes.sample = "Sample";
entityTypes.samples = entityTypes.sample + "s";
entityTypes.experiment = "Experiment";
entityTypes.experiments = entityTypes.experiment + "s";

var common = {
  deployedAppName: "openbis",
  springApplicationName: "openbis",
  applicationName: "openBIS",
  openbis_instance: "",
  footer: "openBIS (Version {0})",
  openbis_logo_title: "openBIS",
  
  //
  // Common Labels
  // 
  
  attachment: "Attachment",
  code: "Code",
  ordinal: "Ordinal",
  position_after: "Position After",
  section: "Section",
  file: "File",
  perm_id: "PermID",
  registrator: "Registrator",
  modifier: "Modifier",
  registration_date: "Registration Date",
  modification_date: "Modification Date",
  creation_date: "Creation Date",
  filter: "Filter",
  filters: "Filters",
  not_implemented: "Sorry, feature has not been implemented yet!",
  data_set: "Data Set",
  edit: "Edit",
  material: "Material",
  load_in_progress: "Loading...",
  load_rest_of_data: "Loading the rest of the data",
  details_title: "{0} {1}",
  edit_title: "Edit {0} {1}",
  tab_link: "Link",
  tab_link_message: "Copy this {0} and use it to access openBIS with current tab opened.",
  permlink_column_name: "Permlink",
  overview_column_name: "Overview",
  show_details_link: "Show Details Link",
  show_details_link_column_name: "Permlink",
  show_details_link_text_value: "Permlink",
  table_operations: "Table:",
  entity_operations: "Entity:",
  url: "URL",
  is_official: "Approved?",
  add_attachment: "Add attachment...",
  data_view: "Data View",
  data_set_kind: "Data Set Kind",
  deletion_disallow: "Disallow deletion",
  deletion_disallow_tooltip: "Data sets with disallow deletion flag can only be deleted if openBIS has been configured to allow this. Deletion of such data sets also requires an additional confirmation.",
  main_data_set_path: "Main Data Set Path",
  main_data_set_path_tooltip: "The path (relative to the top directory of a data set) that will be used as a starting point of 'main data set' lookup.\nExample: 'original/images/'",
  main_data_set_path_example: "for example: original/images/",
  main_data_set_pattern: "Main Data Set Pattern",
  main_data_set_pattern_tooltip: "If there is just one data set with path matching chosen 'main data set' pattern, it will be automatically displayed.\nA Regular Expression is expected. Example: '.*\.jpg'",
  main_data_set_pattern_example: "for example: .*\\.jpg",
  auto_resolve_label: "Smart View",
  data_report_label: "Report:",
  explore_label: "Explore:",
  add_unofficial_vocabulary_term_dialog_title: "Add Ad Hoc Term",
  breadcrumbs_separator: "»",
  storage_confirmation: "Storage Confirmed",
  metaproject: "Tags",
  metaprojects: "Tags",
  metaprojects_hint: "List of tag names separated by commas (\",\") or one tag name per line. If a tag does not exist, it will be created.",
  add_metaproject: "Add Tag...",
  choose_metaproject: "Choose a Tag",
  create_not_existing_metaprojects_confirmation_title: "Confirm tags creation",
  create_not_existing_metaprojects_confirmation_msg: "Tags {0} do not exist. Do you want to create them?",
  
  //
  // Form
  // 
  
  unsaved_form_changes_info: "You have unsaved changes.",
  save_unchanged_form_confirmation_title: "Confirm Save",
  save_unchanged_form_confirmation_msg: "You haven't made any changes. Do you really want to save the form?",
  reset_unsaved_form_changes_confirmation_title: "Confirmation Clear",
  revert_unsaved_form_changes_confirmation_title: "Confirmation Revert",
  lose_unsaved_form_changes_confirmation_msg: "All unsaved changes will be lost. Are you sure?",
  
  //
  // Deletion
  // 
  
  deletion_date: "Deletion Date",
  deleter: "Deleter",
  deletion: "Deletion",
  deletion_template: "{0}; {1}<br><i>reason:</i>&nbsp;{2}",
  permanent: "Permanent",
  entities: "Entities",
  reason: "Reason",
  delete_confirmation_title: "Confirm Deletion",
  delete_confirmation_message: "Are you sure you want to delete [{0}]?",
  delete_confirmation_message_with_reason_template: "You are {0} {1}(s).<br> {2} <br>Please enter a reason or cancel the operation.",
  deleting_permanently: "<b>permanently</b> deleting",
  deleting_force_section: "Force",
  deleting_force: "Force not existing locations:",
  deleting_force_tooltip: "Deletes datasets even if they cannot be found on the filesystem.",
  deleting_force_disallowed_types: "Override 'Disallow deletion' flag:",
  deleting_force_disallowed_types_tooltip: "Deletes datasets even if they have 'Disallow deletion' flag set to true in their type.",
  deleting: "Deleting",
  delete_permanently_progress_message: "Deleting {0}(s)...",
  delete_progress_message: "Moving {0}(s) to trash ...",
  cannot_modify_deleted_entity_title: "Cannot modify deleted {0}",
  cannot_modify_deleted_entity_msg: "{0} '{1}' has been moved to trash and therefore cannot be modified.",
  deletion_browser: "Trash",
  deletion_radio_group_label: "Deletions",
  button_revert_deletion: "Revert",
  button_revert_deletion_all: "Revert All",
  button_delete_permanently: "Delete Permanently",
  button_delete_permanently_all: "Delete Permanently All",
  revert_deletions_confirmation_title: "Confirm Revert",
  revert_deletions_progress: "Reverting deletion(s)...",
  revert_entity_deletion_confirmation_msg: "Are you sure you want to revert deletion of {0}?</br></br><b>NOTE:</b> This will in fact revert deletion of all entities deleted together with {0} by {1} on {2} with a following reason: <i>\"{3}\"</i>",
  revert_deletions_selected_confirmation_msg: "Are you sure you want to revert {0} selected deletion(s)?",
  revert_deletions_all_confirmation_msg: "Are you sure you want to revert <b>ALL</b> displayed deletion(s)?",
  permanent_deletions_confirmation_title: "Confirm Permanent Deletion",
  permanent_deletions_progress: "Deleting permanently...",
  permanent_deletions_selected_confirmation_msg: "Are you sure you want to <b>permanently</b> delete all entities that were moved to trash in {0} selected deletion(s)?</br></br>You cannot undo this action.",
  permanent_deletions_all_confirmation_msg: "Are you sure you want to <b>permanently</b> delete all entities that were moved to trash in <b>ALL</b> displayed deletion(s)?</br></br>You cannot undo this action.",
  use_trash_browser: "Use trash browser (click on the trash button in top menu) to delete entities permanently.",
  trash_button_tooltip: "Displays browser of deletions, allows to revert them or confirm (delete permanently).",
	 
  //
  // Table Modifications
  //
  
  confirm_save_table_modifications_dialog_title: "Save Table Modifications?",
  confirm_save_table_modifications_dialog_message: "There are modifications in the table that you didn't save. If you don't save them now they will be lost.</br></br>Would you like to save your changes?",
  table_modifications: "Table Modifications:",
  table_modifications_info_title: "Table Modification Mode On",
  table_modifications_info_text: "Use <i>Table Modifications</i> toolbar below the table to <b>save</b> or <b>cancel</b> the changes made in table cells.",
          
  //
  // Field
  //
  
  combobox_empty: "- No {0} found -",
  combobox_choose: "Choose {0}...",
  combo_box_expected_value_from_the_list: "Value from the list required",
  invalid_code_message: "Code contains invalid characters. Allowed characters are: {0}.",
  invalid_term_code_message: "{0} contains invalid characters. Allowed characters are: letters, numbers, hyphen (\"-\"), underscore (\"_\"), colon (\":\") and dot (\".\").",
  file_template_label: "Download file template",
  entity_with_file_template_label: "(change template)",
  update_existing_entities_label: "Update existing",
 
  //
  // MessageBox
  //
  
  messagebox_error: "Error",
  messagebox_warning: "Warning",
  messagebox_info: "Info",
  
	//
  // Buttons
  //
  
  button_add: "Add {0}",
  button_save: "Save",
  button_tag: "Tag",
  button_tag_tooltip: "Assign tags to selected entities",
  button_untag: "Untag",
  button_untag_tooltip: "Remove tags assignments from selected entities",
  button_upload: "Upload",
  button_save_and_upload: "Save + Upload Data Set",
  button_choose: "Choose",
  button_cancel: "Cancel",
  button_reset: "Clear",
  button_revert: "Revert",
  button_submit: "Submit",
  button_refresh: "Refresh",
  button_show: "Show",
  button_show_details: "Show Details",
  button_browse: "Browse",
  button_view: "View",
  button_delete: "Delete",
  button_deactivate: "Deactivate",
  button_delete_sample: "Delete " + entityTypes.sample,
  button_delete_experiment: "Delete " + entityTypes.experiment,
  button_delete_material: "Delete Material",
  button_delete_data_set: "Delete Data Set",
  button_delete_project: "Delete Project",
  button_delete_metaproject: "Delete Tag",
  button_configure: "Settings",  
  button_filters: "Filters",  
  button_upload_data_via_cifex: "Upload Data",
  button_process: "Process",
  button_top: "Files",
  button_evaluate: "Evaluate",
  login_buttonLabel: "Login",
  logout_buttonLabel: "Logout",
  home_buttonLabel: "Home",
  
  tooltip_refresh_disabled: "To activate select appropriate criteria first.",
  tooltip_refresh_enabled: "Load or update the table.",
  tooltip_export_enabled: "Export the table visible on the screen to an Excel file.",
  tooltip_export_disabled: "Refresh the data before exporting them.",
  tooltip_config_enabled: "Choose the columns.",
  tooltip_config_disabled: "Load the data to activate this option.",
  tooltip_view_dataset: "View data set in Data Set Download Service.",
  
  tooltip_vocabulary_managed_internally: "This operation is not available for a vocabulary that is managed internally.",
  
  export_all: "All Columns",
  export_visible: "Visible Columns",
  export_for_update: "For Batch Update",
  tooltip_export_all: "Export the table with all (visible and hidden) columns to an Excel file",
	tooltip_export_visible: "Export the table with visible columns to an Excel file",
  tooltip_export_for_update: "Export the table such that it can be used for Batch Update",
  button_export_visible: "Export",
  button_export_all: "Export All",
  button_export_for_update: "Export For Update",
  
  properties_assignments: "Properties Assignments",
  
  //
  // LoginWidget
  //
  
  login_invitation: "Please login to start your session:", 
  login_userLabel: "User",
  login_passwordLabel: "Password",
  login_buttonLabel: "Login",
  login_failed: "Sorry, you entered an invalid username or password. Please try again.",
  
  //
  // AbstractAsyncCallback
  //
  
  exception_invocationMessage: "Failed to contact the server. Please try again later or contact your administrator.", 
  exception_withoutMessage: "Unexpected error has occurred, please contact your administrator:<br>{0}",
  session_expired: "Session expired. Please login again.",
  
  //
  // Header
  //
  
  header_userWithoutHomegroup: "{0}",
  header_userWithHomegroup: "{0} ({1})",
  
  //
  // Help Info
  //

  info_button_tooltip: "Displays short help information about the application.",
  info_box_title: "OpenBIS Help",
  info_box_msg: "There is an online help under construction for tabs and dialog windows opened in openBIS.<br/><br/>To get help information about specific:<li><b>tab</b> - right click on an opened tab's header and select <i>Help</i> from its context menu,<li><b>dialog window</b> - click on <i>?</i> icon visible in window's header in the right corner.",
  
  //
  // Info Box
  //
  
  info_box_show_full_message: "show details",
  info_box_proxy_502_error: "Client lost connectivity with the server:<br/>If you are uploading data, it will be probably available in a few minutes. Wait and check with the system before uploading again.",
 
  //
  // Info Type
  //
  
  info_type_error: "Error",
  info_type_info: "Info",
  info_type_progress: "Progress",
  
  //
  // Search
  //
  
  search_button: "Search",
  use_wildcard_checkbox_text: "*?",
  use_wildcard_checkbox_text_long: "Search with Wildcards (*?)",
  use_wildcard_checkbox_tooltip: "Check to treat '*' and '?' as wildcards in searched text. Note, that only whole words will match if you don't add '*' at the beginning and at the end of the text.",
  global_search: "[{0}]: '{1}'",
  identifier: "Identifier",
  no_match: "No results found for '{0}'.<br><br><br>Note, that you are {1} using wildcard search mode. Try to turn it {2} in your user settings.",
  entity_type: "Entity Type",
  entity_kind: "Entity Kind",
  match: "Match",
  rank: "Rank",
  search_domain_type: "Search Domain",
  too_generic: "Query string '{0}' is too generic.",
  show_related_datasets: "Show Related Data Sets",
  show_related_datasets_message: "Select between showing Data Sets related to selected ({0}) entities or to all entities from the table and click on OK button.",
  show_related_datasets_radio_label: "Related entities",
  more_results_found_message: "More search results were found, but are not shown. Consider making the search more specific.",
  more_results_found_title: "More results found",
  search_criteria_dialog_title: "{0} Search Criteria",

  //
  // Sample Browser
  //
  
  subcode: "Subcode",
  sample: entityTypes.sample,
  sample_type: entityTypes.sample + " Type",
  database_instance: "Database Instance",
  sample_identifier: "Identifier",
  is_instance_sample: "Shared?",
  is_invalid: "Invalid?",
  is_deleted: "Deleted?",
  group: "Space",
  space: "Space",
  groups: "Spaces",
  project: "Project",
  project_identifier: "Project Identifier",
  experiment: entityTypes.experiment,
  experiments: entityTypes.experiments,
  experiment_identifier: entityTypes.experiment + " Identifier",
  generated_from: "Parent {0}",
  part_of: "Container",
  generatedfromparent: "Parents",
  containerparent: "Container",
  
  //
  // Experiment Browser
  //
  
  experiments_grid_header: entityTypes.experiments,
  experiment_type: entityTypes.experiment + " Type",
  project_selector_title: "Projects",
  project_selector_tooltip: "Click on a row with space/project to see list of " + entityTypes.experiments + " in that space/project.",
  project_selector_description_not_available: "(not available)",
  project_selector_code_column: "Space / Project",
  project_selector_details_link_label: "(info)",
  project_selector_details_link_tooltip: "Click on the link to see project details.",
  perform_archiving_on_all_datasets_connected_to_experiments_msg_template: "{0} {1} will be performed on those data sets connected to the " + entityTypes.experiments + " from the table that are '{2}' (see their status) after you click on a Run button.",
  perform_archiving_on_selected_or_all_datasets_connected_to_experiments_msg_template: "Select between performing {0} only on the data sets of the selected " + entityTypes.experiments + " ({1}) or on all data sets connected to an " + entityTypes.experiment + " from the table and click on a Run button. Note that only data sets that are '{2}' (see their status) will be processed.",
  
  
  //
  // DataSet Browser
  //
  
  container_datasets: "Containers",
  parent_datasets: "Parents",
  order_in_containers: "Order in Containers",
  children_datasets: "Children",
  no_datasets_selected: "No Data Sets were selected.",
  datasets_from_different_stores_selected: "Data Sets from different Data Stores were selected, so no operation can be performed on all of them.",
  perform_computation_on_all_datasets_msg_template: "{0} {1} will be performed on all Data Sets from the table that have type and data store appropriate to the service.",
  perform_computation_on_selected_or_all_datasets_msg_template: "Select between performing {0} only on selected Data Sets ({1}) or on all Data Sets of appropriate types from the table, then click on a Run button.",
  perform_archiving_on_all_datasets_msg_template: "{0} {1} will be performed on all Data Sets from the table that are '{2}' (see their status) after you click on a Run button.",
  perform_archiving_on_selected_or_all_datasets_msg_template: "Select between performing {0} only on selected Data Sets ({1}) or on all Data Sets from the table and click on a Run button. Note that only Data Sets that are '{2}' (see their status) will be processed.",
  
  //
  // Entity Type Browser
  //

  add_new_type_button: "Add",
  edit_type_button: "Edit",
  add_type_title_template: "Add a new {0} Type",
  edit_type_title_template: "Edit {0} Type {1}",
  
  //
  // Sample Type Browser
  //
  
  subcode_unique_label: "Unique Subcodes",
  auto_generate_codes_label: "Generate Codes Automatically",
  show_parent_metadata_label: "Show Parent Metadata",
  generated_code_prefix: "Generated Code Prefix",
  listable: "Listable",
  is_listable: "Listable?",
  show_container: "Show Container",
  show_parents: "Show Parents",
  is_show_container: "Show Container?",
  is_show_parents: "Show Parents?",
  is_show_parent_metadata: "Show Parent Metadata?",
  validation_script: "Validation Plugin",
  semantic_annotation_perm_id: "Annotation Perm Id",
  semantic_annotation_predicate_ontology_id: "Predicate Ontology Id",
  semantic_annotation_predicate_ontology_version: "Predicate Ontology Version",
  semantic_annotation_predicate_accession_id: "Predicate Accession Id",
  semantic_annotation_descriptor_ontology_id: "Descriptor Ontology Id",
  semantic_annotation_descriptor_ontology_version: "Descriptor Ontology Version",
  semantic_annotation_descriptor_accession_id: "Descriptor Accession Id",
  semantic_annotation_inherited: "Annotation Inherited",
  
  //
  // Property Type Browser
  //
  
  label: "Label",
  data_type: "Data Type",
  data_type_code: "Data Type Code",
  description: "Description",
  sample_types: entityTypes.sample + " Types",
  material_types: "Material Types",
  data_set_types: "Data Set Types",
  file_format_types: "File Types",
  experiment_types: entityTypes.experiment + " Types",
  mandatory: "Mandatory",
  is_mandatory: "Mandatory?",
  property_type: "Property Type",
  property_type_code: "Property Type Code",
  assigned_to: "Entity Type",
  type_of: "Entity",
  vocabulary: "Vocabulary",
  button_edit_content: "Edit Content",
  edit_content_title: "Edit Terms in Vocabulary {0}",
  vocabulary_terms: "Terms",
  vocabulary_terms_file_format: "<pre>code    [label]    [description]</pre> ([...] means that column is optional)",
  vocabulary_terms_url_template: "URL Template",
  vocabulary_terms_empty: "Space or comma separated list of terms.",
  vocabulary_term_code_empty: "The code for the term.",
  vocabulary_show_available_terms_in_choosers: "Show available terms in choosers",
  confirm_vocabulary_show_available_terms_in_chooosers_msg: "If this vocabulary has many terms (more than 100) application will slow down.<br><br>Are you sure?",
  vocabulary_show_available_terms_in_choosers: "Show available terms in choosers",
  missing_vocabulary_terms: "Missing vocabulary term.",
  section_tooltip: "The name of the section in which the assigned property should appear in entity registration and edition forms.",
  default_value: "Initial Value (for existing entities)",
  default_value_tooltip: "The value of the assigned property for all currently existing entities.",
  entity_type_assignments: "{0} Type Assignment{1}",
  xml_schema: "XML Schema",
  xml_schema_info: "XML Schema that will be used for validation of user provided XML documents.<br><br>If no schema is specified only well-formedness of the documents will be checked.",
  xslt: "XSLT Script",
  xslt_info: "XSLT Script that will be used for rendering of user provided XML documents e.g. in tables.<br><br>If no script is specified the original XML document will be displayed.",
  shown_in_edit_view: "Shown in Edit Views",
  add_property_type_button : "Add",
  
  
  //
  // Property Type Assignments Browser
  //
  dynamic: "Dynamic",
  managed: "Managed",
  is_dynamic: "Dynamic?",
  is_managed: "Managed?",
  is_shown_in_editor_view: "Shown in Editor View?",
  show_raw_value: "Show Raw Value in Forms?",
  script: "Script",
  plugin: "Plugin",
  scriptable: "Handled by Plugin",
  unassign_button_label: "Release Assignment",
  unassignment_confirmation_dialog_title: "Unassignment Confirmation",
  unassignment_confirmation_template_without_properties: "Removing assignment between {0} type {1} and property type {2}. This can be safely done because no {0} has this property filled in.<br><br>Do you want to remove the assignment?",
  unassignment_confirmation_template_with_properties: "Removing assignment between {0} type {1} and property type {2}. There are {3} {0}(s) where value for this property has been filled in.<br><br>Do you want to delete these values and remove the assignment?",
  edit_property_type_assignment_title : "Edit assignment between {0} type {1} and property type {2}",
  default_update_value: "Update Value",
  default_update_value_tooltip: "The value of the assigned property for all currently existing entities that didn't have any value for this property.",
  is_shown_in_edit_view: "Shown in Edit Views?",
  show_raw_value_in_forms: "Show Raw Value in Forms",  
  
  //
  // Entity History Browser and Tab
  //
  
  entity_history_tab: "History",
  openbis_entity_history_browser_AUTHOR: "Author",
  openbis_entity_history_browser_PROPERTY_TYPE_CODE: "Property Code",
  openbis_entity_history_browser_PROPERTY_TYPE_LABEL: "Property",
  openbis_entity_history_browser_VALID_FROM_DATE: "Valid From Date",
  openbis_entity_history_browser_VALID_UNTIL_DATE: "Valid Until Date",
  openbis_entity_history_browser_VALUE: "Value",
  openbis_entity_history_browser_RELATION_TYPE: "Relation",
  
  //
  // Wizard
  //
  wizard_page_previous_button_label: "< Previous",
  wizard_page_next_button_label: "Next >",
  wizard_page_finish_button_label: "Finish",
  
  //
  // New Menu Titles
  //
  
  menu_browse: "Browse",
  menu_new: "New",
  menu_import: "Import",
  menu_types: "Types",
  
  //
  // Menu Titles
  //
  
  menu_administration: "Admin",
  ADMINISTRATION_MENU_MANAGE_GROUPS: "Spaces",
  
  menu_authorization: "Authorization",
  AUTHORIZATION_MENU_USERS: "Users",
  AUTHORIZATION_MENU_AUTHORIZATION_GROUPS: "User Groups",
  AUTHORIZATION_MENU_ROLES: "Roles",
  TRASH: "Trash",
  LOGGING_CONSOLE: "Logging Console",

  DATA_SET_MENU_SEARCH: "Data Set Search",
  DATA_SET_MENU_TYPES: "Data Set Types",
  DATA_SET_MENU_FILE_FORMATS: "File Types",
  DATA_SET_MENU_UPLOAD: "Data Set Registration (via CIFEX)",
  DATA_SET_MENU_MASS_UPDATE: "Data Set Metadata Update",
  data_set_batch_update: "Data Set Metadata Batch Update",
  
  EXPERIMENT_MENU_BROWSE: entityTypes.experiments,
  EXPERIMENT_MENU_NEW: entityTypes.experiment,
  EXPERIMENT_MENU_IMPORT: entityTypes.experiment + " Registration",
  EXPERIMENT_MENU_MASS_UPDATE: entityTypes.experiment + " Updates",
  EXPERIMENT_MENU_TYPES: entityTypes.experiment + " Types",
  
  MATERIAL_MENU_BROWSE: "Materials",
  MATERIAL_MENU_IMPORT: "Material Registration",
  MATERIAL_MENU_MASS_UPDATE: "Material Updates",
  MATERIAL_MENU_TYPES: "Material Types",
   
  SAMPLE_MENU_SEARCH: entityTypes.sample + " Search",
  SAMPLE_MENU_BROWSE: entityTypes.samples,
  SAMPLE_MENU_NEW: entityTypes.sample,
  SAMPLE_MENU_IMPORT: entityTypes.samples + " Registration",
  SAMPLE_MENU_MASS_UPDATE: entityTypes.sample + " Updates",
  SAMPLE_MENU_TYPES: entityTypes.sample + " Types",
  
  PROJECT_MENU_BROWSE: "Projects",
  PROJECT_MENU_NEW: "Project",
  
  METAPROJECT_MENU_BROWSE: "Tags",
  
  menu_property_types: "Metadata",
  PROPERTY_TYPES_MENU_BROWSE_PROPERTY_TYPES: "Browse Property Types",
  PROPERTY_TYPES_MENU_BROWSE_ASSIGNMENTS: "Browse Assignments",
  PROPERTY_TYPES_MENU_NEW_PROPERTY_TYPES: "New Property Type",
  PROPERTY_TYPES_MENU_ASSIGN_TO_EXPERIMENT_TYPE: "Assign To " + entityTypes.experiment + " Type",
  PROPERTY_TYPES_MENU_ASSIGN_TO_SAMPLE_TYPE: "Assign To " + entityTypes.sample + " Type",
  PROPERTY_TYPES_MENU_ASSIGN_TO_MATERIAL_TYPE: "Assign To Material Type",
  PROPERTY_TYPES_MENU_ASSIGN_TO_DATA_SET_TYPE: "Assign To Data Set Type", 
  
  SCRIPT_MENU_BROWSE: "Plugins",

  ACTIVE_USERS_COUNT: "Active Users Report",
  
  VOCABULARY_MENU_BROWSE: "Vocabularies",
  VOCABULARY_MENU_NEW: "New",
  
  // menu user
  USER_MENU_CHANGE_SETTINGS: "Settings",
  USER_MENU_LOGOUT: "Logout",
  USER_MENU_LOGIN: "Login",
  USER_MENU_ABOUT_BOX: "About",
    
  menu_compute: "Perform",
  COMPUTE_MENU_QUERIES: "Query",
  COMPUTE_MENU_PROCESSING: "Processing",
  
  menu_processing: "Actions",

  menu_archiving: "Archiving",
  ARCHIVING_MENU_BACKUP: "Copy to Archive",
  ARCHIVING_MENU_ARCHIVE: "Move to Archive",
  ARCHIVING_MENU_UNARCHIVE: "Unarchive",
  ARCHIVING_MENU_LOCK: "Lock",
  ARCHIVING_MENU_UNLOCK: "Unlock",
  
  GENERAL_IMPORT_MENU: "General Batch Import",
  
  CUSTOM_IMPORT_MENU: "Custom Import",
  
  menu_modules: "Utilities",
    
  //
  // Tab Titles
  //

  data_set_upload: "Data Set Batch Registration (via CIFEX)",
  assign_data_set_property_type: "Assign Data Set Property Type",
  assign_material_property_type: "Assign Material Property Type",  
  assign_experiment_property_type: "Assign " + entityTypes.experiment + " Property Type",
  assign_sample_property_type: "Assign " + entityTypes.sample + " Property Type",
  property_type_assignments: "Property Type Assignments",
  property_type_registration: "Property Type Registration",
  property_types: "Property Types",
  experiment_browser: entityTypes.experiment + " Browser",
  experiment_import: entityTypes.experiment + " Batch Registration",
  vocabulary_registration: "Vocabulary Registration",
  sample_batch_registration: entityTypes.sample + " Batch Registration",
  sample_batch_update: entityTypes.sample + " Batch Update",
  experiment_batch_update: entityTypes.experiment + " Batch Update",
  material_batch_update: "Material Batch Update",
  sample_registration: entityTypes.sample + " Registration",
  sample_broser: entityTypes.sample + " Browser",
  list_groups: "Spaces Browser",
  confirm_title: "Confirmation",
  confirm_close_msg: "All unsaved changes will be lost. Are you sure?",
  general_import: "General Batch Import",
  custom_import: "Custom Import",
  //
  // Help Page Titles
  //
  
  HELP__EXPERIMENT__BROWSE: entityTypes.experiment + " Browser",
  HELP__EXPERIMENT__VIEW: entityTypes.experiment + " Viewer",
  HELP__EXPERIMENT__EDIT: entityTypes.experiment + " Editor",
  HELP__EXPERIMENT__REGISTER: entityTypes.experiment + " Registration",
  HELP__EXPERIMENT__EXPERIMENT_TYPE__BROWSE: entityTypes.experiment + " Type Browser",
  HELP__SAMPLE__BROWSE: entityTypes.sample + " Browser",
  HELP__SAMPLE__VIEW: entityTypes.sample + " Viewer",
  HELP__SAMPLE__EDIT: entityTypes.sample + " Editor",
  HELP__SAMPLE__SEARCH: entityTypes.sample + " Advanced Search",
  HELP__SAMPLE__REGISTER: entityTypes.sample + " Registration",
  HELP__SAMPLE__IMPORT: entityTypes.sample + " Import",
  HELP__SAMPLE__BATCH_UPDATE: entityTypes.sample + " Batch Update",
  HELP__SAMPLE__SAMPLE_TYPE__BROWSE: entityTypes.sample + " Type Browser",
  HELP__SAMPLE__SAMPLE_TYPE__REGISTER: "Add or Edit " + entityTypes.sample + " Type",
  HELP__SAMPLE__SAMPLE_TYPE__EDIT: "Add or Edit " + entityTypes.sample + " Type",
  HELP__MATERIAL__BROWSE: "Material Browser",
  HELP__MATERIAL__VIEW: "Material Viewer",
  HELP__MATERIAL__EDIT: "Material Editor",
  HELP__MATERIAL__IMPORT: entityTypes.sample + " Import",
  HELP__MATERIAL__MATERIAL_TYPE__BROWSE: "Material Type Browser",
  HELP__DATA_SET__VIEW: "Data Set Viewer",
  HELP__DATA_SET__EDIT: "Data Set Editor",
  HELP__DATA_SET__SEARCH: "Data Set Advanced Search",
  HELP__DATA_SET__REGISTER: "Data Set Upload",
  HELP__DATA_SET__REPORT: "Data Set Report",
  HELP__DATA_SET__DATA_SET_TYPE__BROWSE: "Data Set Type Browser",
  HELP__DATA_SET__DATA_SET_TYPE__REGISTER: "Add or Edit Data Set Type",
  HELP__DATA_SET__DATA_SET_TYPE__EDIT: "Add or Edit Data Set Type",
  HELP__RELATED_DATA_SETS__BROWSE: "Related Data Sets Browser",
  HELP__ADMINISTRATION__FILE_TYPE__BROWSE: "File Type Browser",
  HELP__ADMINISTRATION__GROUP__BROWSE: "Space Browser",
  HELP__ADMINISTRATION__VOCABULARY__BROWSE: "Vocabulary Browser",
  HELP__ADMINISTRATION__VOCABULARY__REGISTER: "Vocabulary Registration",
  HELP__ADMINISTRATION__VOCABULARY__TERM__BROWSE: "Vocabulary Terms Browser",
  HELP__ADMINISTRATION__VOCABULARY__TERM__REGISTER: "Adding Vocabulary Terms",
  HELP__ADMINISTRATION__VOCABULARY__TERM__DELETE: "Deletion of Vocabulary Terms with Replacements",
  HELP__ADMINISTRATION__VOCABULARY__TERM__BATCH_UPDATE: "Vocabulary Terms Batch Update",
  HELP__ADMINISTRATION__PROJECT__VIEW: "Project Viewer",
  HELP__ADMINISTRATION__PROJECT__EDIT: "Project Editor",
  HELP__ADMINISTRATION__PROJECT__BROWSE: "Project Browser",
  HELP__ADMINISTRATION__PROJECT__REGISTER: "Project Registration",
  HELP__ADMINISTRATION__PROPERTY_TYPE__BROWSE: "Property Type Browser",
  HELP__ADMINISTRATION__PROPERTY_TYPE__REGISTER: "Property Type Registration",
  HELP__ADMINISTRATION__PROPERTY_TYPE__ASSIGNMENT__BROWSE: "Property Type Assignment Browser",
  HELP__ADMINISTRATION__PROPERTY_TYPE__ASSIGNMENT__REGISTER: "Property Type Assignment",
  HELP__ADMINISTRATION__AUTHORIZATION__AUTHORIZATION_GROUPS__BROWSE: "Authorization Group Browser",
  HELP__ADMINISTRATION__AUTHORIZATION__AUTHORIZATION_GROUPS__VIEW: "Users From Authorization Group",
  HELP__ADMINISTRATION__AUTHORIZATION__ROLES__BROWSE: "Role Assignment Browser",
  HELP__ADMINISTRATION__AUTHORIZATION__ROLES__REGISTER: "Role Assignment",
  HELP__ADMINISTRATION__AUTHORIZATION__USERS__BROWSE: "Person Browser", 
  HELP__ADMINISTRATION__AUTHORIZATION__USERS__REGISTER: "Person Registration", 
  HELP__SEARCH__ACTION: "Global Search",
  HELP__ATTACHMENTS__VIEW: "Attachment Versions",
  HELP__CHANGE_USER_SETTINGS__ACTION: "Change User Settings",
  HELP__TABLE_SETTINGS__ACTION: "Table Settings",
  HELP__TABLE_SETTINGS__CUSTOM_COLUMN__REGISTER: "Add or Edit Custom Column",
  HELP__TABLE_SETTINGS__CUSTOM_COLUMN__EDIT: "Add or Edit Custom Column",
  HELP__TABLE_SETTINGS__CUSTOM_FILTER__REGISTER: "Add or Edit Custom Filter",
  HELP__TABLE_SETTINGS__CUSTOM_FILTER__EDIT: "Add or Edit Custom Filter",
  HELP__EXPORT_DATA__ACTION: "Data Exporting",
  HELP__PERFORM_COMPUTATION__ACTION: "Performing Computations on Data Sets",
  HELP__METAPROJECT__BROWSE: "Tags Browser",
    
  //
  // User Settings Dialog
  //
  
  change_user_settings_dialog_title: "Change User Settings",  
  home_group_label: "Home Space",
  reset_user_settings_button: "Restore Default",
  reset_user_settings_confirmation_msg: "Are you sure that you want to restore default settings?<br><br><b>Note that:</b><li>Every setting you changed in this application apart from home space, e.g. hiding/showing certain columns in a browser, will be lost.<li>All currently opened tabs will be closed.",
  real_number_formating_fields: "Custom Real Number Formatting in Tables",
  scientific_formating: "Scientific",
  real_number_formating_precision: "Precision",
  debugging_mode: "Enable Debugging Mode",
  debugging_mode_info: "This mode is for advanced users. When enabled information like detailed error messages useful e.g. when debugging scripts invoked by openbis will be shown.",
  reopen_last_tab_on_login_label: "Reopen Last Tab",
  reopen_last_tab_on_login_info: "Check to reopen last opened tab after login. Note, that it works only if you enter openBIS with URL to the welcome page.",
  show_last_visits_label: "Show Last Visited Places",
  show_last_visits_info: "Check to show the last visited materials, " + entityTypes.experiments + ", " + entityTypes.samples + ", and data sets on the welcome page.",
    
  // About Box
  about_box_dialog_title: "About",
  
  // Active Users Dialog
  active_users_email_sent_confirmation: "E-mail sent. You should get a copy of the e-mail in your mailbox.",
  active_users_dialog: "Number of active users ({0}) will be sent to {1}.",
  
  //
  // Role View
  //
  role: "Role",
  confirm_role_removal_msg: "Do you want to remove selected role?",
  confirm_role_removal_title: "Role removal confirmation",
  authorization_group: "User Group",
  
  //
  // Experiment Registration
  //
  experiment_registration: entityTypes.experiment + " Registration",
  experiment_import: entityTypes.experiment + " Batch Registration",
  samples: entityTypes.samples,
  samples_list: "List of " + entityTypes.sample + " (codes or identifiers) separated by commas (\",\") or one " + entityTypes.sample + " per line.",
  
  //
  // Data Set Edition
  //
  parents: "Parents",
  parents_empty: "List of parent data set codes separated by commas (\",\") or one code per line.",
  contained_data_sets: "Contained Data Sets",
  contained_data_sets_empty: "List of contained data set codes separated by commas (\",\") or one code per line.", 
  
 //
 // Vocabulary Browser
 //
 vocabulary_browser: "Vocabulary Browser",
 is_managed_internally: "Managed Internally?",
 url_template: "URL Template",
 terms: "Terms",
 term_code: "Code",
 description: "Description",
 vocabulary_term_description_empty: "The description for the term.",
 label: "Label",
 vocabulary_term_label_empty: "The label for the term.",
 VOCABULARY_TERMS_BROWSER: "Vocabulary Terms of {0}",
 TERM_FOR_SAMPLES_USAGE: "Usages for " + entityTypes.samples,
 TERM_FOR_DATA_SET_USAGE: "Usages for Data Sets",
 TERM_FOR_EXPERIMENTS_USAGE: "Usages for " + entityTypes.experiments,
 TERM_FOR_MATERIALS_USAGE: "Usages for Materials",
 TERM_TOTAL_USAGE: "Total Usages Number",
 add_vocabulary_terms_button: "Add",
 add_vocabulary_terms_title: "Add Terms",
 add_vocabulary_term_title: "Add Term",
 add_vocabulary_terms_ok_button: "OK",
 update_vocabulary_terms_button: "Batch Update",
 update_vocabulary_terms_title: "Update Vocabulary Terms of {0}",
 update_vocabulary_terms_message: "<b>Upload a TSV file</b> with all current terms of the vocabulary listed in columns:<br/><br/>{0}<br/><br/>With this file you can:<li>update label and description of current terms,<li>add new terms,<li>reorder terms (new order is taken from the order that terms are listed).<br/><br/>{1}",
 update_vocabulary_terms_message_2: "<b>To get a file for upload</b> with all current terms listed:<li>show only <i>Code</i>, <i>Label</i> and <i>Description</i> columns using table <i>Settings</i></li><li>use table <i>Export</i> functionality</li>",
 vocabulary_terms_validation_message: "Term '{0}' already exists.", 
 delete_vocabulary_terms_button: "Delete",
 delete_vocabulary_terms_invalid_title: "Invalid Deletion",
 delete_vocabulary_terms_invalid_message: "Cannot delete all terms. A vocabulary should have at least one term.",
 delete_vocabulary_terms_confirmation_title: "Deletion of Vocabulary Terms",
 delete_vocabulary_terms_confirmation_message_no_replacements_singular: "Do you want to delete the selected term?",
 delete_vocabulary_terms_confirmation_message_no_replacements: "Do you want to delete the {0} selected terms?",
 delete_vocabulary_terms_confirmation_message_for_replacements: "{0} terms will be deleted.\n\nThe terms below are used. They have to be replaced by one of the remaining terms.",
 edit_vocabulary_term_button: "Edit",
 make_official_vocabulary_term_button: "Approve Unofficial Term",
 make_official_vocabulary_terms_confirmation_title: "Approving Vocabulary Terms",
 make_official_vocabulary_terms_confirmation_message_singular: "Do you want to approve selected term?",
 make_official_vocabulary_terms_confirmation_message: "Do you want to approve {0} selected terms?",

 //
 // Person Browser
 //
 person_browser: "Person Browser",
 user_id: "User ID",
 first_name: "First Name",
 last_name: "Last Name",
 email: "Email",
 is_active: "Is Active?",
 deactivate_persons_confirmation_title: "Users Deactivation Confirmation",
 deactivate_persons_confirmation_message: "Warning! This operation cannot be undone!\n\nDo you really want to deactivate {0} users ({1})?",

 //
 // Role Browser
 //
 role_assignment_browser: "Role Assignment Browser",
 person: "Person",
 button_assign_role: "Assign Role",
 button_release_role_assignment: "Release Assignment",
 
 //
 // Space Browser
 //
 space_browser: "Space Browser",
 leader: "Head",
 add_space_title: "Add a new space",

 //
 // Project Browser
 //
 project_browser: "Project Browser",

 //
 // Metaproject Tree
 //
 
 metaproject_tree_column_header: "Tag / Entity Kind / Entity",
 metaproject_tree_info_link: "(info)",
 metaproject_tree_description_not_available: "(not available)",
 metaproject_tree_metaproject_tooltip: "Name: {0}\nDescription: {1}",
 
 //
 // Metaproject Browser
 //
 metaproject_browser: "Tag Browser",
 metaproject_browser_tree_title: "Tags",
 metaproject_browser_tree_tooltip: "Click on a row with an entity type (i.e. " + entityTypes.experiments + ", " + entityTypes.samples + ", Data Sets, Materials) to see entities of that type that belong to the given metaproject.",
 metaproject_browser_entities_title: "Entities",
 
 //
 // Metaproject Entities
 //
 
 metaproject_entities_experiments: entityTypes.experiments,
 metaproject_entities_samples: entityTypes.samples,
 metaproject_entities_data_sets: "Data Sets",
 metaproject_entities_materials: "Materials",

 //
 // Metaproject Viewer
 //

 metaproject_properties: "Tag properties",
 
 //
 // Project Registration
 //
 project_registration: "Project Registration",
  
 //
 // Detailed Search
 //
 data_set_search: "Data Set Search",
 sample_search: entityTypes.sample + " Search",
 match_all: "Match all criteria (logical AND)",
 match_any: "Match any criteria (logical OR)",
 button_change_query : "Change Search Criteria",
  
 //
 // Data Set Browser
 //
 location: "Location",
 size: "Size",
 external_data_sample_identifier: entityTypes.sample + " Identifier",
 external_data_experiment_identifier: entityTypes.experiment + " Identifier",
 source_type: "Source Type",
 is_complete: "Complete?",
 complete: "Complete",
 archiving_status: "Archiving Status",
 present_in_archive: "Present in Archive",
 data_set_type: "Data Set Type",
 parent: "Parent",
 parent_code: "Parent Code",
 file_format_type: "File Type",
 production_date: "Production Date",
 data_producer_code: "Producer",
 container_dataset: "Container",
 data_store_code: "Data Store",
 button_upload_datasets: "Export Data",
 confirm_dataset_upload_title: "Uploading Confirmation and Authentication",
 confirm_dataset_upload_msg: "This operation combines the chosen data set(s) into a single zip file, which is uploaded to CIFEX ({0}). Once in CIFEX, you may download the data or send it to colleagues.<br/><br/>Please, enter the following information:", 
 confirm_dataset_upload_file_name_field: "File name",
 confirm_dataset_upload_comment_field: "Comment",
 confirm_dataset_upload_user_field: "CIFEX user",
 confirm_dataset_upload_password_field: "CIFEX password",
 external_code: "Link External Code",
 external_dms_code: "External DMS Code",
 external_dms_label: "External DMS Label",
 external_dms_address: "External DMS Address",
 link_path: "Link Path",
 link_hash: "Link Commit Hash",
 link_repository: "Link Repository Id",
 
 //
 // Data Set Viewer
 //
 
 processing_info_title: "Processing",
 processing_info_msg: "'{0}' has been scheduled successfully. Email will be sent to you when it is finished.",
 dataset_not_available_msg: "Data Set {0} is {1}. You cannot perform any operation using its data.",
 linked_data_set: "Linked Data Set",
 linked_data_set_info: "This is a linked data set.",
 linked_data_set_link: "Link: ",
 linked_data_set_link_not_available: "Link: not available",
 
 //
 // Material Browser
 //
 material_type: "Material Type",
 material_browser: "Material Browser", 
 infibitor_of: "Inhibitor of",
 allow_any_type: "(Allow Any Type)",
 
 //
 // Import Materials
 //
 material_import: "Material Batch Registration",
ignore_unregistered_materials: "Ignore unregistered materials", 
 
 // 
 // Material Chooser
 //

 title_choose_material: "Choose a Material",
 choose_any_material: "Choose Any Material...",
 incorrect_material_syntax: "Incorrect material specification. Please provide the material code followed by the type in brackets: 'code (type)'.",
 TITLE_CHOOSE_EXPERIMENT: "Choose an " + entityTypes.experiment,
incorrect_experiment_syntax: "Incorrect " + entityTypes.experiment + " specification. Please provide the " + entityTypes.experiment + " space, project and code using the format '/space/project/code'.",
 title_choose_sample: "Choose a " + entityTypes.sample,

 //
 // Attachments
 //
 
 attachments: "Attachments",
 add_attachment_title: "Add a new attachment",
 no_attachments_found: "There are no attachments in this {0}",
 file_name: "File Name",
 title: "Title",
 version_file_name: "File Version",
 version: "Version",
 show_all_versions: "show all versions",
 button_show_all_versions: "Show All Versions",
 button_download: "Download",
 
//
// Grid Column Chooser
//

  GRID_COLUMN_CHOOSER_TITLE: "Configure table columns",
  GRID_COLUMN_NAME_HEADER: "Column",
  GRID_IS_COLUMN_VISIBLE_HEADER: "Visible?",
  GRID_COLUMN_HAS_FILTER_HEADER: "Has Filter?",
  VISIBLE_COLUMNS_LIMITED_TITLE: "Number of visible columns was limited",
  VISIBLE_COLUMNS_LIMITED_MSG: "Only {0} out of {1} requested columns are displayed. You might want to change table settings.",
  VISIBLE_COLUMNS_LIMIT_REACHED_MSG: "Limit of {0} visible columns has been reached.",
  VISIBLE_COLUMNS_LIMIT_EXCEEDED_MSG: "Limit of {0} visible columns cannot be exceeded.",
  
  message_no_external_upload_service: "External upload service (CIFEX) has not been configured. Ask the administrator for more details.",
 
 //
 // Authorization Browser
 //
 authorization_group_browser: "User Groups Browser",
 edit_persons: "Edit Users",
 add_person_to_authorization_group_title: "Add person(s) to the user group '{0}'", 
 persons_ids_label: "Users IDs",
 person_ids_list: "List of users (IDs) separated by commas (\",\") or one user per line.",
 authorization_group_users: "Persons from User Group '{0}'",
 button_show_users: "Show Users",
 radio_one_user: "One User",
 radio_many_users: "Many Users",
 remove_persons_from_authorization_group_confirmation_title: "Users Removal Confirmation",
 remove_persons_from_authorization_group_confirmation_message: "Do you really want to remove {0} users ({1}) from the user group '{2}'?",
 
all_radio: "all ({0})",
only_selected_radio: "selected ({0})",
data_sets_radio_group_label: "Data Sets",
experiments_radio_group_label: entityTypes.experiments,
samples_radio_group_label: entityTypes.samples,
materials_radio_group_label: "Materials",

//
// Import samples
//
default_group: "Default Space",

//
// Filters 
//
 name: "Name",
 is_public: "Is Public?",
 expression: "Expression",
 column: "Column",
 columns: "Columns",
 grid_settings_title: "Table Settings",
 grid_custom_filters: "Custom Filters",
 grid_custom_columns: "Custom Columns",
 apply_filter: "Apply",
 reset_filter: "Reset",
 add_new_filter: "Add a New Filter",
 add_new_column: "Add a New Column",
 how_to_address: "How To Address", 
 insert_columns: "Insert Columns", 

//
// Metaproject chooser
//
button_create_metaprojects: "Create",
button_create_new_metaprojects: "Create New Tags",
topic_create_metaprojects: "Create Tags",

//
// Script Browser
//
add_script_title: "Add a new plugin",
plugin_script: "Script",
title_choose_script: "Choose {0} Plugin",
script_browser: "Plugins",
script_type: "Plugin Type",
plugin_type: "Plugin Kind",
is_available: "Available?",
script_registration: "Plugin Registration",
evaluation_result: "Evaluation Result",
script_tester: "Script Tester",
entity_details: "Details",
show_details: "Show",
evaluation_in_progress: "Evaluation in progress...",
warning_no_script_title: "Empty script",
warning_no_script_message: "No script provided",

//
// History Widget
//
last_visits: "Last Visited Places",
clear: "Clear",

//
// Progress
//
progress_uploading: "Uploading",
progress_processing: "Processing",

//
// Unclassified
//
assign_group_existing: "Existing",
assign_group_new: "New",
assign_group_property: "Property",
vocabulary_registration_popup_warning: "WARNING: The information will be saved immediately and not with the master form.",
 // LAST LINE: KEEP IT AT THE END
  lastline: "" // we need a line without a comma
};
