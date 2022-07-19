// prettier-ignore
const keys = {
  ACTIONS: 'ACTIONS',
  ACTIVATE_USER: 'ACTIVATE_USER',
  ACTIVE: 'ACTIVE',
  ACTIVE_USERS_REPORT: 'ACTIVE_USERS_REPORT',
  ACTIVE_USERS_REPORT_DIALOG: 'ACTIVE_USERS_REPORT_DIALOG',
  ACTIVE_USERS_REPORT_EMAIL_SENT_CONFIRMATION: 'ACTIVE_USERS_REPORT_EMAIL_SENT_CONFIRMATION',
  ADD: 'ADD',
  ADD_GROUP: 'ADD_GROUP',
  ADD_PROPERTY: 'ADD_PROPERTY',
  ADD_ROLE: 'ADD_ROLE',
  ADD_SECTION: 'ADD_SECTION',
  ADD_TERM: 'ADD_TERM',
  ADD_USER: 'ADD_USER',
  ALL: 'ALL',
  ALL_COLUMNS: 'ALL_COLUMNS',
  ALL_PAGES: 'ALL_PAGES',
  AUTHENTICATION_SERVICE: 'AUTHENTICATION_SERVICE',
  AUTHENTICATION_SERVICE_OPENBIS: 'AUTHENTICATION_SERVICE_OPENBIS',
  AUTHENTICATION_SERVICE_SWITCH_AAI: 'AUTHENTICATION_SERVICE_SWITCH_AAI',
  CANCEL: 'CANCEL',
  CHOOSE_FILE: 'CHOOSE_FILE',
  CLEAR_SELECTION: 'CLEAR_SELECTION',
  CLOSE: 'CLOSE',
  CODE: 'CODE',
  COLLECTION_TYPE: 'COLLECTION_TYPE',
  COLLECTION_TYPES: 'COLLECTION_TYPES',
  COLUMN_FILTERS: 'COLUMN_FILTERS',
  COLUMNS: 'COLUMNS',
  CONFIRM: 'CONFIRM',
  CONFIRMATION: 'CONFIRMATION',
  CONFIRMATION_ACTIVATE_USER: 'CONFIRMATION_ACTIVATE_USER',
  CONFIRMATION_DEACTIVATE_USER: 'CONFIRMATION_DEACTIVATE_USER',
  CONFIRMATION_REMOVE: 'CONFIRMATION_REMOVE',
  CONFIRMATION_REMOVE_IT: 'CONFIRMATION_REMOVE_IT',
  CONFIRMATION_UNSAVED_CHANGES: 'CONFIRMATION_UNSAVED_CHANGES',
  CONTAINER: 'CONTAINER',
  CONTENT: 'CONTENT',
  CONVERTED: 'CONVERTED',
  CRASH: 'CRASH',
  CURRENT_PAGE: 'CURRENT_PAGE',
  DATABASE: 'DATABASE',
  DATA_SET_TYPE: 'DATA_SET_TYPE',
  DATA_SET_TYPES: 'DATA_SET_TYPES',
  DATA_TYPE: 'DATA_TYPE',
  DATA_TYPE_NOT_SELECTED_FOR_PREVIEW: 'DATA_TYPE_NOT_SELECTED_FOR_PREVIEW',
  DATA_TYPE_NOT_SUPPORTED: 'DATA_TYPE_NOT_SUPPORTED',
  DATE: 'DATE',
  DEACTIVATE_USER: 'DEACTIVATE_USER',
  DELETION: 'DELETION',
  DELETIONS: 'DELETIONS',
  DESCRIPTION: 'DESCRIPTION',
  DISALLOW_DELETION: 'DISALLOW_DELETION',
  DYNAMIC_PROPERTY_PLUGIN: 'DYNAMIC_PROPERTY_PLUGIN',
  DYNAMIC_PROPERTY_PLUGINS: 'DYNAMIC_PROPERTY_PLUGINS',
  EDIT: 'EDIT',
  EDITABLE: 'EDITABLE',
  EMAIL: 'EMAIL',
  ENTITY: 'ENTITY',
  ENTITY_IDENTIFIER: 'ENTITY_IDENTIFIER',
  ENTITY_KIND: 'ENTITY_KIND',
  ENTITY_PROJECT: 'ENTITY_PROJECT',
  ENTITY_REGISTRATION_DATE: 'ENTITY_REGISTRATION_DATE',
  ENTITY_REGISTRATOR: 'ENTITY_REGISTRATOR',
  ENTITY_SPACE: 'ENTITY_SPACE',
  ENTITY_TYPE: 'ENTITY_TYPE',
  ENTITY_TYPE_PATTERN: 'ENTITY_TYPE_PATTERN',
  ENTITY_VALIDATION_PLUGIN: 'ENTITY_VALIDATION_PLUGIN',
  ENTITY_VALIDATION_PLUGINS: 'ENTITY_VALIDATION_PLUGINS',
  ERROR: 'ERROR',
  EVALUATE: 'EVALUATE',
  EVENT_TYPE: 'EVENT_TYPE',
  EXECUTE: 'EXECUTE',
  EXPORT: 'EXPORT',
  EXPORT_PLAIN_TEXT_WARNING: 'EXPORT_PLAIN_TEXT_WARNING',
  EXPORTS: 'EXPORTS',
  FAIL_IF_EXISTS: 'FAIL_IF_EXISTS',
  FILTER: 'FILTER',
  FILTERS: 'FILTERS',
  FIRST_NAME: 'FIRST_NAME',
  FIRST_PAGE: 'FIRST_PAGE',
  FORM_PREVIEW: 'FORM_PREVIEW',
  FREEZES: 'FREEZES',
  FREEZING: 'FREEZING',
  GENERATED_CODE_PREFIX: 'GENERATED_CODE_PREFIX',
  GENERATE_CODES: 'GENERATE_CODES',
  GLOBAL_FILTER: 'GLOBAL_FILTER',
  GROUP: 'GROUP',
  GROUPS: 'GROUPS',
  HIDE: 'HIDE',
  HIDE_ALL: 'HIDE_ALL',
  HIDE_DETAILS: 'HIDE_DETAILS',
  HIDE_STACK_TRACE: 'HIDE_STACK_TRACE',
  HISTORY: 'HISTORY',
  HOME_SPACE: 'HOME_SPACE',
  IGNORE_EXISTING: 'IGNORE_EXISTING',
  IMPORT: 'IMPORT',
  IMPORT_SUCCEEDED: 'IMPORT_SUCCEEDED',
  IMPORT_FAILED: 'IMPORT_FAILED',
  INHERITED_FROM: 'INHERITED_FROM',
  INITIAL_VALUE: 'INITIAL_VALUE',
  INTERNAL: 'INTERNAL',
  IS_NEW_ENTITY: 'IS_NEW_ENTITY',
  LABEL: 'LABEL',
  LAST_NAME: 'LAST_NAME',
  LAST_PAGE: 'LAST_PAGE',
  LESS: 'LESS',
  LEVEL: 'LEVEL',
  LOGIN: 'LOGIN',
  MAIN_DATA_SET_PATH: 'MAIN_DATA_SET_PATH',
  MAIN_DATA_SET_PATTERN: 'MAIN_DATA_SET_PATTERN',
  MANDATORY: 'MANDATORY',
  MATERIAL_TYPE: 'MATERIAL_TYPE',
  MATERIAL_TYPES: 'MATERIAL_TYPES',
  META_DATA: 'META_DATA',
  MORE: 'MORE',
  NAME: 'NAME',
  NEW_COLLECTION_TYPE: 'NEW_COLLECTION_TYPE',
  NEW_DATA_SET_TYPE: 'NEW_DATA_SET_TYPE',
  NEW_DYNAMIC_PROPERTY_PLUGIN: 'NEW_DYNAMIC_PROPERTY_PLUGIN',
  NEW_ENTITY_VALIDATION_PLUGIN: 'NEW_ENTITY_VALIDATION_PLUGIN',
  NEW_GROUP: 'NEW_GROUP',
  NEW_MATERIAL_TYPE: 'NEW_MATERIAL_TYPE',
  NEW_OBJECT_TYPE: 'NEW_OBJECT_TYPE',
  NEW_QUERY: 'NEW_QUERY',
  NEW_USER: 'NEW_USER',
  NEW_VOCABULARY_TYPE: 'NEW_VOCABULARY_TYPE',
  NEXT_PAGE: 'NEXT_PAGE',
  NO_FILE_CHOSEN: 'NO_FILE_CHOSEN',
  NO_RESULTS_FOUND: 'NO_RESULTS_FOUND',
  NUMBER_OF_SELECTED_ROWS: 'NUMBER_OF_SELECTED_ROWS',
  OBJECT_DOES_NOT_EXIST: 'OBJECT_DOES_NOT_EXIST',
  OBJECT_NOT_VISIBLE_DUE_TO_FILTERING_AND_PAGING: 'OBJECT_NOT_VISIBLE_DUE_TO_FILTERING_AND_PAGING',
  OBJECT_TYPE: 'OBJECT_TYPE',
  OBJECT_TYPES: 'OBJECT_TYPES',
  OFFICIAL: 'OFFICIAL',
  OFFICIAL_TERM_HINT: 'OFFICIAL_TERM_HINT',
  ONLY_FIRST_RESULTS_SHOWN: 'ONLY_FIRST_RESULTS_SHOWN',
  OPERATOR: 'OPERATOR',
  OPERATOR_AND: 'OPERATOR_AND',
  OPERATOR_OR: 'OPERATOR_OR',
  OWNER: 'OWNER',
  OUTPUT: 'OUTPUT',
  PARAMETERS: 'PARAMETERS',
  PARENTS: 'PARENTS',
  PASSWORD: 'PASSWORD',
  PLAIN_TEXT: 'PLAIN_TEXT',
  PLUGIN: 'PLUGIN',
  PLUGIN_IS_DISABLED: 'PLUGIN_IS_DISABLED',
  PLUGIN_IS_PREDEPLOYED: 'PLUGIN_IS_PREDEPLOYED',
  PLUGIN_KIND: 'PLUGIN_KIND',
  PREVIEW: 'PREVIEW',
  PREVIOUS_PAGE: 'PREVIOUS_PAGE',
  PROJECT: 'PROJECT',
  PROPERTY: 'PROPERTY',
  PROPERTY_ASSIGNMENT_CANNOT_BE_REMOVED: 'PROPERTY_ASSIGNMENT_CANNOT_BE_REMOVED',
  PROPERTY_CONFIGURATION_IS_INCORRECT: 'PROPERTY_CONFIGURATION_IS_INCORRECT',
  PROPERTY_IS_ASSIGNED: 'PROPERTY_IS_ASSIGNED',
  PROPERTY_IS_INTERNAL: 'PROPERTY_IS_INTERNAL',
  PROPERTY_IS_NOT_USED: 'PROPERTY_IS_NOT_USED',
  PROPERTY_IS_USED: 'PROPERTY_IS_USED',
  PROPERTY_PARAMETERS_CANNOT_BE_CHANGED: 'PROPERTY_PARAMETERS_CANNOT_BE_CHANGED',
  PROPERTY_TYPES: 'PROPERTY_TYPES',
  PUBLIC: 'PUBLIC',
  QUERIES: 'QUERIES',
  QUERY: 'QUERY',
  QUERY_AUTHORIZATION_COLUMNS_DETECTED: 'QUERY_AUTHORIZATION_COLUMNS_DETECTED',
  QUERY_HINT: 'QUERY_HINT',
  QUERY_PUBLIC_WARNING: 'QUERY_PUBLIC_WARNING',
  QUERY_TYPE: 'QUERY_TYPE',
  REASON: 'REASON',
  REGISTRATOR: 'REGISTRATOR',
  REMOVE: 'REMOVE',
  REMOVE_TERM: 'REMOVE_TERM',
  RESULT: 'RESULT',
  RESULTS: 'RESULTS',
  RESULTS_RANGE: 'RESULTS_RANGE',
  RICH_TEXT: 'RICH_TEXT',
  ROLE: 'ROLE',
  ROLES: 'ROLES',
  ROLES_OF_GROUPS: 'ROLES_OF_GROUPS',
  ROLES_OF_USERS: 'ROLES_OF_USERS',
  ROLE_IS_INHERITED: 'ROLE_IS_INHERITED',
  ROLE_IS_INSTANCE_ADMIN: 'ROLE_IS_INSTANCE_ADMIN',
  ROWS: 'ROWS',
  ROWS_PER_PAGE: 'ROWS_PER_PAGE',
  SAVE: 'SAVE',
  SCOPE: 'SCOPE',
  SCRIPT: 'SCRIPT',
  SEARCH: 'SEARCH',
  SECTION: 'SECTION',
  SECTION_IS_NOT_USED: 'SECTION_IS_NOT_USED',
  SECTION_IS_USED: 'SECTION_IS_USED',
  SELECTED_ROWS: 'SELECTED_ROWS',
  SELECTED_ROWS_NOT_VISIBLE_DUE_TO_FILTERING_AND_PAGING: 'SELECTED_ROWS_NOT_VISIBLE_DUE_TO_FILTERING_AND_PAGING',
  SEND_REPORT : 'SEND_REPORT',
  SHOW: 'SHOW',
  SHOW_CONTAINER: 'SHOW_CONTAINER',
  SHOW_DETAILS: 'SHOW_DETAILS',
  SHOW_PARENTS: 'SHOW_PARENTS',
  SHOW_STACK_TRACE: 'SHOW_STACK_TRACE',
  SHOW_ALL: 'SHOW_ALL',
  SPACE: 'SPACE',
  SUPPORT: 'SUPPORT',
  SQL: 'SQL',
  SUBCODES_UNIQUE: 'SUBCODES_UNIQUE',
  TERM: 'TERM',
  TERMS: 'TERMS',
  TERM_IS_INTERNAL: 'TERM_IS_INTERNAL',
  TERM_CANNOT_BE_CHANGED_OR_REMOVED: 'TERM_CANNOT_BE_CHANGED_OR_REMOVED',
  TESTER: 'TESTER',
  TOOLS: 'TOOLS',
  TYPES: 'TYPES',
  UNSAVED_CHANGES: 'UNSAVED_CHANGES',
  UPDATE_IF_EXISTS: 'UPDATE_IF_EXISTS',
  UPDATE_MODE: "UPDATE_MODE",
  URL_TEMPLATE: 'URL_TEMPLATE',
  USAGES: 'USAGES',
  USER: 'USER',
  USERS: 'USERS',
  USERS_WHO_REGISTERED_SOME_DATA_CANNOT_BE_REMOVED: 'USERS_WHO_REGISTERED_SOME_DATA_CANNOT_BE_REMOVED',
  USER_ID: 'USER_ID',
  VALIDATION_CANNOT_BE_EMPTY: 'VALIDATION_CANNOT_BE_EMPTY',
  VALIDATION_CODE_PATTERN: 'VALIDATION_CODE_PATTERN',
  VALIDATION_INTERNAL_CODE_PATTERN: 'VALIDATION_INTERNAL_CODE_PATTERN',
  VALIDATION_PLUGIN: 'VALIDATION_PLUGIN',
  VALIDATION_TERM_CODE_PATTERN: 'VALIDATION_TERM_CODE_PATTERN',
  VALIDATION_USER_CODE_PATTERN: 'VALIDATION_USER_CODE_PATTERN',
  VALUES: 'VALUES',
  VISIBLE_COLUMNS: 'VISIBLE_COLUMNS',
  VOCABULARY_TYPE: 'VOCABULARY_TYPE',
  VOCABULARY_TYPES: 'VOCABULARY_TYPES',
  VOCABULARY_TYPE_IS_INTERNAL: 'VOCABULARY_TYPE_IS_INTERNAL',
  VOCABULARY_TYPE_CANNOT_BE_CHANGED_OR_REMOVED: 'VOCABULARY_TYPE_CANNOT_BE_CHANGED_OR_REMOVED',
  XML_SCHEMA: 'XML_SCHEMA',
  XLS_FILE: 'XLS_FILE',
  XLS_FILE_DESCRIPTION: 'XLS_FILE_DESCRIPTION',
  XSLT_SCRIPT: 'XSLT_SCRIPT'
}

// prettier-ignore
const messages_en = {
  [keys.ACTIONS]: 'Actions',
  [keys.ACTIVATE_USER]: 'Activate user',
  [keys.ACTIVE]: 'Active',
  [keys.ACTIVE_USERS_REPORT]: 'Active Users Report',
  [keys.ACTIVE_USERS_REPORT_DIALOG]: 'Number of active users (${0}) will be sent to',
  [keys.ACTIVE_USERS_REPORT_EMAIL_SENT_CONFIRMATION]: 'E-mail sent. You should get a copy of the e-mail in your mailbox.',
  [keys.ADD]: 'Add',
  [keys.ADD_GROUP]: 'Add Group',
  [keys.ADD_PROPERTY]: 'Add Property',
  [keys.ADD_ROLE]: 'Add Role',
  [keys.ADD_SECTION]: 'Add Section',
  [keys.ADD_TERM]: 'Add Term',
  [keys.ADD_USER]: 'Add User',
  [keys.ALL]: 'All',
  [keys.ALL_COLUMNS]: 'All Columns',
  [keys.ALL_PAGES]: 'All Pages',
  [keys.AUTHENTICATION_SERVICE]: 'Authentication Service',
  [keys.AUTHENTICATION_SERVICE_OPENBIS]: 'Default Login Service',
  [keys.AUTHENTICATION_SERVICE_SWITCH_AAI]: 'SWITCHaai Single Sign On Login Service',
  [keys.CANCEL]: 'Cancel',
  [keys.CHOOSE_FILE]: 'Choose File',
  [keys.CLEAR_SELECTION]: 'Clear selection',
  [keys.CLOSE]: 'Close',
  [keys.CODE]: 'Code',
  [keys.COLLECTION_TYPES]: 'Collection Types',
  [keys.COLLECTION_TYPE]: 'Collection Type',
  [keys.COLUMN_FILTERS]: 'Filter Per Column',
  [keys.COLUMNS]: 'Columns',
  [keys.CONFIRMATION]: 'Confirmation',
  [keys.CONFIRMATION_ACTIVATE_USER]: 'Are you sure you want to activate the user?',
  [keys.CONFIRMATION_DEACTIVATE_USER]: 'Are you sure you want to deactivate the user?',
  [keys.CONFIRMATION_REMOVE]: 'Are you sure you want to remove "${0}"?',
  [keys.CONFIRMATION_REMOVE_IT]: 'Are you sure you want to remove it?',
  [keys.CONFIRMATION_UNSAVED_CHANGES]: 'Are you sure you want to lose the unsaved changes?',
  [keys.CONFIRM]: 'Confirm',
  [keys.CONTAINER]: 'Container',
  [keys.CONTENT]: 'Content',
  [keys.CONVERTED]: 'Converted',
  [keys.CRASH]: 'Something went wrong :(',
  [keys.CURRENT_PAGE]: 'Current Page',
  [keys.DATABASE]: 'Database',
  [keys.DATA_SET_TYPES]: 'Data Set Types',
  [keys.DATA_SET_TYPE]: 'Data Set Type',
  [keys.DATA_TYPE]: 'Data Type',
  [keys.DATA_TYPE_NOT_SELECTED_FOR_PREVIEW]: 'Please select a data type to display the field preview.',
  [keys.DATA_TYPE_NOT_SUPPORTED]: 'The selected data type is not supported yet.',
  [keys.DATE]: 'Date',
  [keys.DEACTIVATE_USER]: 'Deactivate user',
  [keys.DELETION]: 'Deletion',
  [keys.DELETIONS]: 'Deletions',
  [keys.DESCRIPTION]: 'Description',
  [keys.DISALLOW_DELETION]: 'Disallow Deletion',
  [keys.DYNAMIC_PROPERTY_PLUGINS]: 'Dynamic Property Plugins',
  [keys.DYNAMIC_PROPERTY_PLUGIN]: 'Dynamic Property Plugin',
  [keys.EDIT]: 'Edit',
  [keys.EDITABLE]: 'Editable',
  [keys.EMAIL]: 'Email',
  [keys.ENTITY]: 'Entity',
  [keys.ENTITY_IDENTIFIER]: 'Entity Identifier',
  [keys.ENTITY_KIND]: 'Entity Kind',
  [keys.ENTITY_PROJECT]: 'Entity Project',
  [keys.ENTITY_REGISTRATION_DATE]: 'Entity Registration Date',
  [keys.ENTITY_REGISTRATOR]: 'Entity Registrator',
  [keys.ENTITY_SPACE]: 'Entity Space',
  [keys.ENTITY_TYPE]: 'Entity Type',
  [keys.ENTITY_TYPE_PATTERN]: 'Entity Type Pattern',
  [keys.ENTITY_VALIDATION_PLUGINS]: 'Entity Validation Plugins',
  [keys.ENTITY_VALIDATION_PLUGIN]: 'Entity Validation Plugin',
  [keys.ERROR]: 'Error',
  [keys.EVALUATE]: 'Evaluate',
  [keys.EVENT_TYPE]: 'Event Type',
  [keys.EXECUTE]: 'Execute',
  [keys.EXPORT]: 'Export',
  [keys.EXPORT_PLAIN_TEXT_WARNING]: 'Do not use this file for Batch Update! This file does not contain rich text formatting. If used for Batch Update, all rich text formatting in the updated entries will be lost!',
  [keys.EXPORTS]: 'Exports',
  [keys.FAIL_IF_EXISTS]: 'Fail if exists',
  [keys.FILTER]: 'Filter',
  [keys.FILTERS]: 'Filters',
  [keys.FIRST_NAME]: 'First Name',
  [keys.FIRST_PAGE]: 'First Page',
  [keys.FORM_PREVIEW]: 'Form Preview',
  [keys.FREEZES]: 'Freezes',
  [keys.FREEZING]: 'Freezing',
  [keys.GENERATED_CODE_PREFIX]: 'Generated code prefix',
  [keys.GENERATE_CODES]: 'Generate Codes',
  [keys.GLOBAL_FILTER]: 'Global Filter',
  [keys.GROUPS]: 'Groups',
  [keys.GROUP]: 'Group',
  [keys.HIDE]: 'hide',
  [keys.HIDE_ALL]: 'Hide All',
  [keys.HIDE_DETAILS]: 'Hide details',
  [keys.HIDE_STACK_TRACE]: 'Hide stack trace',
  [keys.HISTORY]: 'History',
  [keys.HOME_SPACE]: 'Home Space',
  [keys.IGNORE_EXISTING]: 'Ignore if exists',
  [keys.IMPORT]: 'Import',
  [keys.IMPORT_SUCCEEDED]: 'Successfully imported data.',
  [keys.IMPORT_FAILED]: 'Import failed.',
  [keys.INHERITED_FROM]: 'Inherited From',
  [keys.INITIAL_VALUE]: 'Initial Value',
  [keys.INTERNAL]: 'Internal',
  [keys.IS_NEW_ENTITY]: 'Is New Entity',
  [keys.LABEL]: 'Label',
  [keys.LAST_NAME]: 'Last Name',
  [keys.LAST_PAGE]: 'Last Page',
  [keys.LESS]: 'Less',
  [keys.LEVEL]: 'Level',
  [keys.LOGIN]: 'Login',
  [keys.MAIN_DATA_SET_PATH]: 'Main Data Set Path',
  [keys.MAIN_DATA_SET_PATTERN]: 'Main Data Set Pattern',
  [keys.MANDATORY]: 'Mandatory',
  [keys.MATERIAL_TYPES]: 'Material Types',
  [keys.MATERIAL_TYPE]: 'Material Type',
  [keys.META_DATA]: 'Meta Data',
  [keys.MORE]: 'More',
  [keys.NAME]: 'Name',
  [keys.NEW_COLLECTION_TYPE]: 'New Collection Type',
  [keys.NEW_DATA_SET_TYPE]: 'New Data Set Type',
  [keys.NEW_DYNAMIC_PROPERTY_PLUGIN]: 'New Dynamic Property Plugin',
  [keys.NEW_ENTITY_VALIDATION_PLUGIN]: 'New Entity Validation Plugin',
  [keys.NEW_GROUP]: 'New Group',
  [keys.NEW_MATERIAL_TYPE]: 'New Material Type',
  [keys.NEW_OBJECT_TYPE]: 'New Object Type',
  [keys.NEW_QUERY]: 'New Query',
  [keys.NEW_USER]: 'New User',
  [keys.NEW_VOCABULARY_TYPE]: 'New Vocabulary Type',
  [keys.NEXT_PAGE]: 'Next Page',
  [keys.NO_FILE_CHOSEN]: 'No file chosen',
  [keys.NO_RESULTS_FOUND]: 'No results found',
  [keys.NUMBER_OF_SELECTED_ROWS]: '${0} selected row(s)',
  [keys.OBJECT_DOES_NOT_EXIST]: 'Object does not exist',
  [keys.OBJECT_NOT_VISIBLE_DUE_TO_FILTERING_AND_PAGING]: 'The selected object is currently not visible in the list due to the chosen filtering and paging.',
  [keys.OBJECT_TYPES]: 'Object Types',
  [keys.OBJECT_TYPE]: 'Object Type',
  [keys.OFFICIAL]: 'Official',
  [keys.OFFICIAL_TERM_HINT]: 'Unofficial (aka ad-hoc) terms can be created by regular users from the non-admin UI. Once verified a term can be made official by an admin. WARNING: Official terms cannot be made unofficial again.',
  [keys.ONLY_FIRST_RESULTS_SHOWN]: 'Showing only the first ${0} results (${1} results found)',
  [keys.OPERATOR]: 'Operator',
  [keys.OPERATOR_AND]: 'AND',
  [keys.OPERATOR_OR]: 'OR',
  [keys.OWNER]: 'Owner',
  [keys.OUTPUT]: 'Output',
  [keys.PARAMETERS]: 'Parameters',
  [keys.PARENTS]: 'Parents',
  [keys.PASSWORD]: 'Password',
  [keys.PLAIN_TEXT]: 'Plain Text',
  [keys.PLUGIN]: 'Plugin',
  [keys.PLUGIN_IS_DISABLED]: 'The plugin is disabled.',
  [keys.PLUGIN_IS_PREDEPLOYED]: 'This is a predeployed Java plugin. Its parameters and logic are defined in the plugin Java class and therefore cannot be changed from the UI.',
  [keys.PLUGIN_KIND]: 'Plugin Kind',
  [keys.PREVIEW]: 'Preview',
  [keys.PREVIOUS_PAGE]: 'Previous Page',
  [keys.PROJECT]: 'Project',
  [keys.PROPERTY]: 'Property',
  [keys.PROPERTY_ASSIGNMENT_CANNOT_BE_REMOVED]: 'The property assignment cannot be removed.',
  [keys.PROPERTY_CONFIGURATION_IS_INCORRECT]: 'The property configuration is incorrect.',
  [keys.PROPERTY_IS_ASSIGNED]: 'This property is already assigned to ${0} type(s).',
  [keys.PROPERTY_IS_INTERNAL]: 'This is a system internal property.',
  [keys.PROPERTY_IS_NOT_USED]: 'This property assignment is not yet used by any entities of "${0}" type.',
  [keys.PROPERTY_IS_USED]: 'This property assignment is already used by existing entities of "${0}" type. Removing it is also going to remove ${1} existing property value(s) - data will be lost! Are you sure you want to proceed?',
  [keys.PROPERTY_PARAMETERS_CANNOT_BE_CHANGED]: 'The property parameters cannot be changed.',
  [keys.PROPERTY_TYPES]: 'Property Types',
  [keys.PUBLIC]: 'Public',
  [keys.QUERIES]: 'Queries',
  [keys.QUERY]: 'Query',
  [keys.QUERY_AUTHORIZATION_COLUMNS_DETECTED]: 'Detected authorization column(s) that will be used for automatic results filtering: ${0}.',
  [keys.QUERY_HINT]: 'A query can contain parameters in the following format: ${parameterName}.',
  [keys.QUERY_PUBLIC_WARNING]: 'Security warning: this query is public (i.e. visible to other users) and is defined for a database that is not assigned to any space. Please make sure the query returns only data that can be seen by every user or the results contain one of the special columns (i.e. experiment_key/sample_key/data_set_key) that will be used for an automatic query results filtering.',
  [keys.QUERY_TYPE]: 'Query Type',
  [keys.REASON]: 'Reason',
  [keys.REGISTRATOR]: 'Registrator',
  [keys.REMOVE]: 'Remove',
  [keys.REMOVE_TERM]: 'Remove Term',
  [keys.RESULTS]: 'Results',
  [keys.RESULTS_RANGE]: '${0} of ${1}',
  [keys.RESULT]: 'Result',
  [keys.RICH_TEXT]: 'Rich Text',
  [keys.ROLES]: 'Roles',
  [keys.ROLES_OF_GROUPS]: 'Groups\' Roles',
  [keys.ROLES_OF_USERS]: 'Users\' Roles',
  [keys.ROLE]: 'Role',
  [keys.ROLE_IS_INHERITED]: 'This role is inherited from ${0} group.',
  [keys.ROLE_IS_INSTANCE_ADMIN]: 'This is an instance admin role. It gives an access to the user and master data management functionality.',
  [keys.ROWS]: 'Rows',
  [keys.ROWS_PER_PAGE]: 'Rows per page: ',
  [keys.SAVE]: 'Save',
  [keys.SCOPE]: 'Scope',
  [keys.SCRIPT]: 'Script',
  [keys.SEARCH]: 'Search',
  [keys.SECTION]: 'Section',
  [keys.SECTION_IS_NOT_USED]: 'This section contains only property assignments which are not yet used by any entities of "${0}" type.',
  [keys.SECTION_IS_USED]: 'This section contains property assignments which are already used by existing entities of "${0}" type. Removing it is also going to remove ${1} existing property value(s) - data will be lost! Are you sure you want to proceed?',
  [keys.SELECTED_ROWS]: 'Selected Rows',
  [keys.SELECTED_ROWS_NOT_VISIBLE_DUE_TO_FILTERING_AND_PAGING]: 'Some selected rows are not visible due to the chosen filtering and paging.',
  [keys.SEND_REPORT]: 'Send Report',
  [keys.SHOW]: 'show',
  [keys.SHOW_CONTAINER]: 'Show Container',
  [keys.SHOW_DETAILS]: 'Show details',
  [keys.SHOW_PARENTS]: 'Show Parents',
  [keys.SHOW_STACK_TRACE]: 'Show stack trace',
  [keys.SHOW_ALL]: 'Show All',
  [keys.SPACE]: 'Space',
  [keys.SUPPORT]: 'Openbis Support',
  [keys.SQL]: 'SQL',
  [keys.SUBCODES_UNIQUE]: 'Unique Subcodes',
  [keys.TERMS]: 'Terms',
  [keys.TERM]: 'Term',
  [keys.TERM_IS_INTERNAL]: 'This is a system internal term.',
  [keys.TERM_CANNOT_BE_CHANGED_OR_REMOVED]: 'The term parameters cannot be changed. The term cannot be removed.',
  [keys.TESTER]: 'Tester',
  [keys.TOOLS]: 'Tools',
  [keys.TYPES]: 'Types',
  [keys.UNSAVED_CHANGES]: 'You have unsaved changes',
  [keys.UPDATE_IF_EXISTS]: 'Update if exists',
  [keys.UPDATE_MODE]: 'Update Mode',
  [keys.URL_TEMPLATE]: 'URL Template',
  [keys.USAGES]: 'Usages',
  [keys.USERS]: 'Users',
  [keys.USERS_WHO_REGISTERED_SOME_DATA_CANNOT_BE_REMOVED]: 'Users who have already registered some data cannot be removed.',
  [keys.USER]: 'User',
  [keys.USER_ID]: 'User Id',
  [keys.VALIDATION_CANNOT_BE_EMPTY]: '${0} cannot be empty',
  [keys.VALIDATION_CODE_PATTERN]: '${0} can only contain A-Z, a-z, 0-9 and _, -, .',
  [keys.VALIDATION_INTERNAL_CODE_PATTERN]: '${0} has to start with $ and can only contain A-Z, a-z, 0-9 and _, -, .',
  [keys.VALIDATION_PLUGIN]: 'Validation Plugin',
  [keys.VALIDATION_TERM_CODE_PATTERN]: '${0} can only contain A-Z, a-z, 0-9 and _, -, ., :',
  [keys.VALIDATION_USER_CODE_PATTERN]: '${0} can only contain A-Z, a-z, 0-9 and _, -, ., @',
  [keys.VALUES]: 'Values',
  [keys.VISIBLE_COLUMNS]: 'Visible Columns',
  [keys.VOCABULARY_TYPES]: 'Vocabulary Types',
  [keys.VOCABULARY_TYPE]: 'Vocabulary Type',
  [keys.VOCABULARY_TYPE_IS_INTERNAL]: 'This is a system internal vocabulary.',
  [keys.VOCABULARY_TYPE_CANNOT_BE_CHANGED_OR_REMOVED]: 'The vocabulary parameters cannot be changed. The vocabulary cannot be removed.',
  [keys.XML_SCHEMA]: 'XML Schema',
  [keys.XLS_FILE]: 'XLS File',
  [keys.XLS_FILE_DESCRIPTION]: 'Excel file that contains the data to import. The import accepts both master data (i.e. entity types, property types and vocabulary types) as well as entities (i.e. spaces, projects, collections and objects).',
  [keys.XSLT_SCRIPT]: 'XSLT Script',
}

export default {
  ...keys,
  get: (key, ...params) => {
    const message = messages_en[key]

    if (message) {
      if (params && params.length > 0) {
        return message.replace(/\$\{(\d)\}/g, (match, index) => {
          return params[index]
        })
      } else {
        return message
      }
    } else {
      throw Error('Unknown message: ' + key)
    }
  }
}
