const TYPE_ROOT = 'root'
const TYPE_SPACES = 'spaces'
const TYPE_PROJECTS = 'projects'
const TYPE_COLLECTIONS = 'collections'
const TYPE_OBJECTS = 'objects'
const TYPE_OBJECT_CHILDREN = 'objectChildren'
const TYPE_DATA_SETS = 'dataSets'
const TYPE_DATA_SET_CHILDREN = 'dataSetChildren'
const TYPE_WARNING = 'warning'

const TEXT_SPACES = 'Spaces'
const TEXT_PROJECTS = 'Projects'
const TEXT_COLLECTIONS = 'Collections'
const TEXT_OBJECTS = 'Objects'
const TEXT_OBJECT_CHILDREN = 'Children'
const TEXT_DATA_SETS = 'Data Sets'
const TEXT_DATA_SET_CHILDREN = 'Children'

const SORTINGS = {
  code_asc: {
    label: 'Code ASC',
    sortBy: 'code',
    sortDirection: 'asc',
    index: 0
  },
  code_desc: {
    label: 'Code DESC',
    sortBy: 'code',
    sortDirection: 'desc',
    index: 1
  },
  registration_date_asc: {
    label: 'Registration Date ASC',
    sortBy: 'registrationDate',
    sortDirection: 'asc',
    index: 2
  },
  registration_date_desc: {
    label: 'Registration Date DESC',
    sortBy: 'registrationDate',
    sortDirection: 'desc',
    index: 3
  }
}

function nodeId(...parts) {
  return parts.join('__')
}

export default {
  nodeId,
  TYPE_ROOT,
  TYPE_SPACES,
  TYPE_PROJECTS,
  TYPE_COLLECTIONS,
  TYPE_OBJECTS,
  TYPE_OBJECT_CHILDREN,
  TYPE_DATA_SETS,
  TYPE_DATA_SET_CHILDREN,
  TYPE_WARNING,
  TEXT_SPACES,
  TEXT_PROJECTS,
  TEXT_COLLECTIONS,
  TEXT_OBJECTS,
  TEXT_OBJECT_CHILDREN,
  TEXT_DATA_SETS,
  TEXT_DATA_SET_CHILDREN,
  SORTINGS
}
