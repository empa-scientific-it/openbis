import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'

function groupsFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.USER_GROUP),
    text: messages.get(messages.GROUPS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.USER_GROUP
    },
    canHaveChildren: true
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
    canHaveChildren: true
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
  groupsFolderNode,
  groupNode,
  usersFolderNode,
  userNode
}
