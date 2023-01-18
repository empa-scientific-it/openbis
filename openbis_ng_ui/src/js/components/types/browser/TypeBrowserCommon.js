import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'

const TOTAL_LOAD_LIMIT = 500
const LOAD_LIMIT = 5

function objectTypesFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.OBJECT_TYPE),
    text: messages.get(messages.OBJECT_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.OBJECT_TYPE
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function objectTypeNode(parentId, typeCode) {
  return {
    id: BrowserCommon.nodeId(parentId, typeCode),
    text: typeCode,
    object: {
      type: objectType.OBJECT_TYPE,
      id: typeCode
    }
  }
}

function collectionTypesFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.COLLECTION_TYPE),
    text: messages.get(messages.COLLECTION_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.COLLECTION_TYPE
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function collectionTypeNode(parentId, typeCode) {
  return {
    id: BrowserCommon.nodeId(parentId, typeCode),
    text: typeCode,
    object: {
      type: objectType.COLLECTION_TYPE,
      id: typeCode
    }
  }
}

function dataSetTypesFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.DATA_SET_TYPE),
    text: messages.get(messages.DATA_SET_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.DATA_SET_TYPE
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function dataSetTypeNode(parentId, typeCode) {
  return {
    id: BrowserCommon.nodeId(parentId, typeCode),
    text: typeCode,
    object: {
      type: objectType.DATA_SET_TYPE,
      id: typeCode
    }
  }
}

function materialTypesFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.MATERIAL_TYPE),
    text: messages.get(messages.MATERIAL_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.MATERIAL_TYPE
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function materialTypeNode(parentId, typeCode) {
  return {
    id: BrowserCommon.nodeId(parentId, typeCode),
    text: typeCode,
    object: {
      type: objectType.MATERIAL_TYPE,
      id: typeCode
    }
  }
}

function vocabularyTypesFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.VOCABULARY_TYPE),
    text: messages.get(messages.VOCABULARY_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.VOCABULARY_TYPE
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function vocabularyTypeNode(parentId, typeCode) {
  return {
    id: BrowserCommon.nodeId(parentId, typeCode),
    text: typeCode,
    object: {
      type: objectType.VOCABULARY_TYPE,
      id: typeCode
    }
  }
}

function propertyTypesFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.PROPERTY_TYPE),
    text: messages.get(messages.PROPERTY_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.PROPERTY_TYPE
    }
  }
}

export default {
  LOAD_LIMIT,
  TOTAL_LOAD_LIMIT,
  objectTypesFolderNode,
  objectTypeNode,
  collectionTypesFolderNode,
  collectionTypeNode,
  dataSetTypesFolderNode,
  dataSetTypeNode,
  materialTypesFolderNode,
  materialTypeNode,
  vocabularyTypesFolderNode,
  vocabularyTypeNode,
  propertyTypesFolderNode
}
