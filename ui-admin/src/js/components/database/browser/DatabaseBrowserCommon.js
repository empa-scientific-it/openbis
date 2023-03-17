import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'

const TOTAL_LOAD_LIMIT = 500
const LOAD_LIMIT = 50

const TYPE_SPACES = 'spaces'
const TYPE_PROJECTS = 'projects'
const TYPE_COLLECTIONS = 'collections'
const TYPE_OBJECTS = 'objects'
const TYPE_OBJECT_CHILDREN = 'objectChildren'
const TYPE_DATA_SETS = 'dataSets'
const TYPE_DATA_SET_CHILDREN = 'dataSetChildren'

const SORTINGS = [
  {
    id: 'code_asc',
    label: messages.get(messages.CODE) + ' ' + messages.get(messages.ASCENDING),
    default: true,
    sortBy: 'code',
    sortDirection: 'asc'
  },
  {
    id: 'code_desc',
    label:
      messages.get(messages.CODE) + ' ' + messages.get(messages.DESCENDING),
    sortBy: 'code',
    sortDirection: 'desc'
  },
  {
    id: 'registration_date_asc',
    label:
      messages.get(messages.REGISTRATION_DATE) +
      ' ' +
      messages.get(messages.ASCENDING),
    sortBy: 'registrationDate',
    sortDirection: 'asc'
  },
  {
    id: 'registration_date_desc',
    label:
      messages.get(messages.REGISTRATION_DATE) +
      ' ' +
      messages.get(messages.DESCENDING),
    sortBy: 'registrationDate',
    sortDirection: 'desc'
  }
]

function spacesFolderNode() {
  return {
    text: messages.get(messages.SPACES),
    object: {
      type: TYPE_SPACES,
      id: TYPE_SPACES
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function spaceNode(spaceCode) {
  return {
    text: spaceCode,
    object: {
      type: objectType.SPACE,
      id: spaceCode
    },
    rootable: true
  }
}

function projectsFolderNode() {
  return {
    text: messages.get(messages.PROJECTS),
    object: {
      type: TYPE_PROJECTS,
      id: TYPE_PROJECTS
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function projectNode(projectPermId, projectCode) {
  return {
    text: projectCode,
    object: {
      type: objectType.PROJECT,
      id: projectPermId
    },
    rootable: true
  }
}

function collectionsFolderNode() {
  return {
    text: messages.get(messages.COLLECTIONS),
    object: {
      type: TYPE_COLLECTIONS,
      id: TYPE_COLLECTIONS
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function collectionNode(collectionPermId, collectionCode) {
  return {
    text: collectionCode,
    object: {
      type: objectType.COLLECTION,
      id: collectionPermId
    },
    rootable: true
  }
}

function objectsFolderNode() {
  return {
    text: messages.get(messages.OBJECTS),
    object: {
      type: TYPE_OBJECTS,
      id: TYPE_OBJECTS
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function objectsChildrenFolderNode() {
  return {
    text: messages.get(messages.CHILDREN),
    object: {
      type: TYPE_OBJECT_CHILDREN,
      id: TYPE_OBJECT_CHILDREN
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function objectNode(objectPermId, objectCode) {
  return {
    text: objectCode,
    object: {
      type: objectType.OBJECT,
      id: objectPermId
    }
  }
}

function dataSetsFolderNode() {
  return {
    text: messages.get(messages.DATA_SETS),
    object: {
      type: TYPE_DATA_SETS,
      id: TYPE_DATA_SETS
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function dataSetsChildrenFolderNode() {
  return {
    text: messages.get(messages.CHILDREN),
    object: {
      type: TYPE_DATA_SET_CHILDREN,
      id: TYPE_DATA_SET_CHILDREN
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT,
    selectable: false
  }
}

function dataSetNode(dataSetCode) {
  return {
    text: dataSetCode,
    object: {
      type: objectType.DATA_SET,
      id: dataSetCode
    }
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
