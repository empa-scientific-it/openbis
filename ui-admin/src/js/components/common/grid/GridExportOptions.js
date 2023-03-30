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
    VOCABULARY_TYPE: 'VOCABULARY_TYPE',
    SPACE: 'SPACE',
    PROJECT: 'PROJECT',
    SAMPLE: 'SAMPLE',
    EXPERIMENT: 'EXPERIMENT',
    DATASET: 'DATASET'
  },
  EXPORTABLE_FIELD: {
    ARCHIVING_STATUS: attribute('ARCHIVING_STATUS'),
    CHILDREN: attribute('CHILDREN'),
    CODE: attribute('CODE'),
    DESCRIPTION: attribute('DESCRIPTION'),
    DISALLOW_DELETION: attribute('DISALLOW_DELETION'),
    EXPERIMENT: attribute('EXPERIMENT'),
    GENERATED_CODE_PREFIX: attribute('GENERATED_CODE_PREFIX'),
    GENERATE_CODES: attribute('AUTO_GENERATE_CODES'),
    IDENTIFIER: attribute('IDENTIFIER'),
    MAIN_DATA_SET_PATH: attribute('MAIN_DATA_SET_PATH'),
    MAIN_DATA_SET_PATTERN: attribute('MAIN_DATA_SET_PATTERN'),
    MODIFICATION_DATE: attribute('MODIFICATION_DATE'),
    MODIFIER: attribute('MODIFIER'),
    PARENTS: attribute('PARENTS'),
    PERM_ID: attribute('PERM_ID'),
    PRESENT_IN_ARCHIVE: attribute('PRESENT_IN_ARCHIVE'),
    PROJECT: attribute('PROJECT'),
    PROPERTY: property,
    REGISTRATION_DATE: attribute('REGISTRATION_DATE'),
    REGISTRATOR: attribute('REGISTRATOR'),
    SAMPLE: attribute('SAMPLE'),
    SPACE: attribute('SPACE'),
    STORAGE_CONFIRMATION: attribute('STORAGE_CONFIRMATION'),
    UNIQUE_SUBCODES: attribute('UNIQUE_SUBCODES'),
    URL_TEMPLATE: attribute('URL_TEMPLATE'),
    VALIDATION_PLUGIN: attribute('VALIDATION_SCRIPT')
  }
}
