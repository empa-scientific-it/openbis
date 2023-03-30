function attribute(name) {
  return {
    type: 'ATTRIBUTE',
    id: name
  }
}

function property(name) {
  return {
    type: 'PROPERTY',
    id: name
  }
}

export default {
  FILE_FORMAT: {
    TSV: 'TSV',
    XLS: 'XLS'
  },
  FILE_CONTENT: {
    TYPES: 'TYPES',
    VOCABULARIES: 'VOCABULARIES',
    ENTITIES: 'ENTITIES'
  },
  COLUMNS: {
    ALL: 'ALL_COLUMNS',
    VISIBLE: 'VISIBLE_COLUMNS'
  },
  ROWS: {
    ALL_PAGES: 'ALL_PAGES',
    CURRENT_PAGE: 'CURRENT_PAGE',
    SELECTED_ROWS: 'SELECTED_ROWS'
  },
  VALUES: {
    PLAIN_TEXT: 'PLAIN',
    RICH_TEXT: 'RICH'
  },
  EXPORTABLE_KIND: {
    SAMPLE_TYPE: 'SAMPLE_TYPE',
    EXPERIMENT_TYPE: 'EXPERIMENT_TYPE',
    DATASET_TYPE: 'DATASET_TYPE',
    VOCABULARY: 'VOCABULARY',
    SPACE: 'SPACE',
    PROJECT: 'PROJECT',
    SAMPLE: 'SAMPLE',
    EXPERIMENT: 'EXPERIMENT',
    DATASET: 'DATASET'
  },
  EXPORTABLE_FIELD: {
    CODE: attribute('Code'),
    IDENTIFIER: attribute('Identifier'),
    PERM_ID: attribute('PermId'),
    DESCRIPTION: attribute('Description'),
    ARCHIVING_STATUS: attribute('Archiving status'),
    PRESENT_IN_ARCHIVE: attribute('Present in archive'),
    STORAGE_CONFIRMATION: attribute('Storage confirmation'),
    URL_TEMPLATE: attribute('URL Template'),
    VALIDATION_PLUGIN: attribute('Validation Plugin'),
    GENERATED_CODE_PREFIX: attribute('Generated code prefix'),
    GENERATE_CODES: attribute('Generate Codes'),
    UNIQUE_SUBCODES: attribute('Unique Subcodes'),
    MAIN_DATA_SET_PATTERN: attribute('Main Data Set Pattern'),
    MAIN_DATA_SET_PATH: attribute('Main Data Set Path'),
    DISALLOW_DELETION: attribute('Disallow Deletion'),
    SPACE: attribute('Space'),
    PROJECT: attribute('Project'),
    EXPERIMENT: attribute('Experiment'),
    SAMPLE: attribute('Sample'),
    PARENTS: attribute('Parents'),
    CHILDREN: attribute('Children'),
    REGISTRATOR: attribute('Registrator'),
    REGISTRATION_DATE: attribute('Registration Date'),
    MODIFIER: attribute('Modifier'),
    MODIFICATION_DATE: attribute('Modification Date'),
    PROPERTY: property
  }
}
