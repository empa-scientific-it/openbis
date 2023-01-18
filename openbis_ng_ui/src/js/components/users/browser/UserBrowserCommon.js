import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'

const TOTAL_LOAD_LIMIT = 500
const LOAD_LIMIT = 5

function groupsFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.USER_GROUP),
    text: messages.get(messages.GROUPS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.USER_GROUP
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function groupNode(parentId, groupCode) {
  return {
    id: BrowserCommon.nodeId(parentId, groupCode),
    text: groupCode,
    object: {
      type: objectType.USER_GROUP,
      id: groupCode
    }
  }
}

function usersFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.USER),
    text: messages.get(messages.USERS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.USER
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function userNode(parentId, userId) {
  return {
    id: BrowserCommon.nodeId(parentId, userId),
    text: userId,
    object: {
      type: objectType.USER,
      id: userId
    }
  }
}

export default {
  TOTAL_LOAD_LIMIT,
  LOAD_LIMIT,
  groupsFolderNode,
  groupNode,
  usersFolderNode,
  userNode
}
