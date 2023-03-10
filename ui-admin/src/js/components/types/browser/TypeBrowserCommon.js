import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'

const TOTAL_LOAD_LIMIT = 500
const LOAD_LIMIT = 50

function objectTypesFolderNode() {
  return {
    text: messages.get(messages.OBJECT_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.OBJECT_TYPE
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function objectTypeNode(typeCode) {
  return {
    text: typeCode,
    object: {
      type: objectType.OBJECT_TYPE,
      id: typeCode
    }
  }
}

function collectionTypesFolderNode() {
  return {
    text: messages.get(messages.COLLECTION_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.COLLECTION_TYPE
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function collectionTypeNode(typeCode) {
  return {
    text: typeCode,
    object: {
      type: objectType.COLLECTION_TYPE,
      id: typeCode
    }
  }
}

function dataSetTypesFolderNode() {
  return {
    text: messages.get(messages.DATA_SET_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.DATA_SET_TYPE
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function dataSetTypeNode(typeCode) {
  return {
    text: typeCode,
    object: {
      type: objectType.DATA_SET_TYPE,
      id: typeCode
    }
  }
}

function materialTypesFolderNode() {
  return {
    text: messages.get(messages.MATERIAL_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.MATERIAL_TYPE
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function materialTypeNode(typeCode) {
  return {
    text: typeCode,
    object: {
      type: objectType.MATERIAL_TYPE,
      id: typeCode
    }
  }
}

function vocabularyTypesFolderNode() {
  return {
    text: messages.get(messages.VOCABULARY_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.VOCABULARY_TYPE
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function vocabularyTypeNode(typeCode) {
  return {
    text: typeCode,
    object: {
      type: objectType.VOCABULARY_TYPE,
      id: typeCode
    }
  }
}

function propertyTypesFolderNode() {
  return {
    text: messages.get(messages.PROPERTY_TYPES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.PROPERTY_TYPE
    }
  }
}

export default {
  TOTAL_LOAD_LIMIT,
  LOAD_LIMIT,
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
