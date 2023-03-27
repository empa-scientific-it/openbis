import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'

const TOTAL_LOAD_LIMIT = 500
const LOAD_LIMIT = 50

function groupsFolderNode() {
  return {
    text: messages.get(messages.GROUPS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.USER_GROUP
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function groupNode(groupCode) {
  return {
    text: groupCode,
    object: {
      type: objectType.USER_GROUP,
      id: groupCode
    }
  }
}

function usersFolderNode() {
  return {
    text: messages.get(messages.USERS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.USER
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function userNode(userId) {
  return {
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
