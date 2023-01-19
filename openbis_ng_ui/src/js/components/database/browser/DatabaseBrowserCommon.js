import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'

const TOTAL_LOAD_LIMIT = 500
const LOAD_LIMIT = 5

const TYPE_SPACES = 'spaces'
const TYPE_PROJECTS = 'projects'
const TYPE_COLLECTIONS = 'collections'
const TYPE_OBJECTS = 'objects'
const TYPE_OBJECT_CHILDREN = 'objectChildren'
const TYPE_DATA_SETS = 'dataSets'
const TYPE_DATA_SET_CHILDREN = 'dataSetChildren'

const SORT_BY_CODE_ASC = 'code_asc'
const SORT_BY_CODE_DESC = 'code_desc'
const SORT_BY_REGISTRATION_DATE_ASC = 'registration_date_asc'
const SORT_BY_REGISTRATION_DATE_DESC = 'registration_date_desc'

const SORTINGS = {
  [SORT_BY_CODE_ASC]: {
    label: messages.get(messages.CODE) + ' ' + messages.get(messages.ASCENDING),
    sortBy: 'code',
    sortDirection: 'asc',
    index: 0
  },
  [SORT_BY_CODE_DESC]: {
    label:
      messages.get(messages.CODE) + ' ' + messages.get(messages.DESCENDING),
    sortBy: 'code',
    sortDirection: 'desc',
    index: 1
  },
  [SORT_BY_REGISTRATION_DATE_ASC]: {
    label:
      messages.get(messages.REGISTRATION_DATE) +
      ' ' +
      messages.get(messages.ASCENDING),
    sortBy: 'registrationDate',
    sortDirection: 'asc',
    index: 2
  },
  [SORT_BY_REGISTRATION_DATE_DESC]: {
    label:
      messages.get(messages.REGISTRATION_DATE) +
      ' ' +
      messages.get(messages.DESCENDING),
    sortBy: 'registrationDate',
    sortDirection: 'desc',
    index: 3
  }
}

function spacesFolderNode(parent) {
  return {
    id: BrowserCommon.nodeId(parent.id, TYPE_SPACES),
    text: messages.get(messages.SPACES),
    object: {
      type: TYPE_SPACES
    },
    parent: parent,
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function spaceNode(parent, spaceCode) {
  return {
    id: BrowserCommon.nodeId(parent.id, spaceCode),
    text: spaceCode,
    object: {
      type: objectType.SPACE,
      id: spaceCode
    },
    parent: parent,
    rootable: true
  }
}

function projectsFolderNode(parent) {
  return {
    id: BrowserCommon.nodeId(parent.id, TYPE_PROJECTS),
    text: messages.get(messages.PROJECTS),
    object: {
      type: TYPE_PROJECTS
    },
    parent: parent,
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function projectNode(parent, projectPermId, projectCode) {
  return {
    id: BrowserCommon.nodeId(parent.id, projectPermId),
    text: projectCode,
    object: {
      type: objectType.PROJECT,
      id: projectPermId
    },
    parent: parent,
    rootable: true
  }
}

function collectionsFolderNode(parent) {
  return {
    id: BrowserCommon.nodeId(parent.id, TYPE_COLLECTIONS),
    text: messages.get(messages.COLLECTIONS),
    object: {
      type: TYPE_COLLECTIONS
    },
    parent: parent,
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function collectionNode(parent, collectionPermId, collectionCode) {
  return {
    id: BrowserCommon.nodeId(parent.id, collectionPermId),
    text: collectionCode,
    object: {
      type: objectType.COLLECTION,
      id: collectionPermId
    },
    parent: parent,
    rootable: true
  }
}

function objectsFolderNode(parent) {
  return {
    id: BrowserCommon.nodeId(parent.id, TYPE_OBJECTS),
    text: messages.get(messages.OBJECTS),
    object: {
      type: TYPE_OBJECTS
    },
    parent: parent,
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function objectsChildrenFolderNode(parent) {
  return {
    id: BrowserCommon.nodeId(parent.id, TYPE_OBJECT_CHILDREN),
    text: messages.get(messages.CHILDREN),
    object: {
      type: TYPE_OBJECT_CHILDREN
    },
    parent: parent,
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function objectNode(parent, objectPermId, objectCode) {
  return {
    id: BrowserCommon.nodeId(parent.id, objectPermId),
    text: objectCode,
    object: {
      type: objectType.OBJECT,
      id: objectPermId
    },
    parent: parent
  }
}

function dataSetsFolderNode(parent) {
  return {
    id: BrowserCommon.nodeId(parent.id, TYPE_DATA_SETS),
    text: messages.get(messages.DATA_SETS),
    object: {
      type: TYPE_DATA_SETS
    },
    parent: parent,
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function dataSetsChildrenFolderNode(parent) {
  return {
    id: BrowserCommon.nodeId(parent.id, TYPE_DATA_SET_CHILDREN),
    text: messages.get(messages.CHILDREN),
    object: {
      type: TYPE_DATA_SET_CHILDREN
    },
    parent: parent,
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function dataSetNode(parent, dataSetCode) {
  return {
    id: BrowserCommon.nodeId(parent.id, dataSetCode),
    text: dataSetCode,
    object: {
      type: objectType.DATA_SET,
      id: dataSetCode
    },
    parent: parent
  }
}

export default {
  TOTAL_LOAD_LIMIT,
  LOAD_LIMIT,
  TYPE_SPACES,
  TYPE_PROJECTS,
  TYPE_COLLECTIONS,
  TYPE_OBJECTS,
  TYPE_OBJECT_CHILDREN,
  TYPE_DATA_SETS,
  TYPE_DATA_SET_CHILDREN,
  SORTINGS,
  SORT_BY_CODE_ASC,
  SORT_BY_CODE_DESC,
  SORT_BY_REGISTRATION_DATE_ASC,
  SORT_BY_REGISTRATION_DATE_DESC,
  spacesFolderNode,
  spaceNode,
  projectsFolderNode,
  projectNode,
  collectionsFolderNode,
  collectionNode,
  objectsFolderNode,
  objectsChildrenFolderNode,
  objectNode,
  dataSetsFolderNode,
  dataSetsChildrenFolderNode,
  dataSetNode
}
