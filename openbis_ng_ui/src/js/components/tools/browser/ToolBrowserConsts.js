import ImportType from '@src/js/components/tools/form/import/ImportType.js'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import objectType from '@src/js/common/consts/objectType.js'

const TYPE_ROOT = 'root'
const TYPE_WARNING = 'warning'

const TEXT_DYNAMIC_PROPERTY_PLUGINS = messages.get(
  messages.DYNAMIC_PROPERTY_PLUGINS
)
const TEXT_ENTITY_VALIDATION_PLUGINS = messages.get(
  messages.ENTITY_VALIDATION_PLUGINS
)
const TEXT_QUERIES = messages.get(messages.QUERIES)

function nodeId(...parts) {
  return parts.join('__')
}

function rootNode() {
  return {
    id: TYPE_ROOT,
    object: {
      type: TYPE_ROOT
    },
    canHaveChildren: true
  }
}

function historyFolderNode(parentId) {
  return {
    id: nodeId(parentId, objectType.HISTORY),
    text: messages.get(messages.HISTORY),
    object: {
      type: objectType.HISTORY,
      id: objectType.HISTORY
    },
    canHaveChildren: true,
    selectable: false
  }
}

function historyDeletionNode(parentId) {
  return {
    id: nodeId(parentId, openbis.EventType.DELETION),
    text: messages.get(messages.DELETION),
    object: {
      type: objectType.HISTORY,
      id: openbis.EventType.DELETION
    }
  }
}

function historyFreezingNode(parentId) {
  return {
    id: nodeId(parentId, openbis.EventType.FREEZING),
    text: messages.get(messages.FREEZING),
    object: {
      type: objectType.HISTORY,
      id: openbis.EventType.FREEZING
    }
  }
}

function importFolderNode(parentId) {
  return {
    id: nodeId(parentId, objectType.IMPORT),
    text: messages.get(messages.IMPORT),
    object: {
      type: objectType.IMPORT,
      id: objectType.IMPORT
    },
    canHaveChildren: true,
    selectable: false
  }
}

function importAllNode(parentId) {
  return {
    id: nodeId(parentId, ImportType.ALL),
    text: messages.get(messages.ALL),
    object: {
      type: objectType.IMPORT,
      id: ImportType.ALL
    }
  }
}

function reportFolderNode(parentId) {
  const TYPE_REPORT = 'report'
  return {
    id: nodeId(parentId, TYPE_REPORT),
    text: messages.get(messages.REPORT),
    object: {
      type: TYPE_REPORT,
      id: TYPE_REPORT
    },
    canHaveChildren: true,
    selectable: false
  }
}

function activeUsersReportNode(parentId) {
  return {
    id: nodeId(parentId, objectType.ACTIVE_USERS_REPORT),
    text: messages.get(messages.ACTIVE_USERS_REPORT),
    object: {
      type: objectType.ACTIVE_USERS_REPORT,
      id: objectType.ACTIVE_USERS_REPORT
    }
  }
}

function accessFolderNode(parentId) {
  const TYPE_ACCESS = 'access'
  return {
    id: nodeId(parentId, TYPE_ACCESS),
    text: messages.get(messages.ACCESS),
    object: {
      type: TYPE_ACCESS,
      id: TYPE_ACCESS
    },
    canHaveChildren: true,
    selectable: false
  }
}

function personalAccessTokensNode(parentId) {
  return {
    id: nodeId(parentId, objectType.PERSONAL_ACCESS_TOKEN),
    text: messages.get(messages.PERSONAL_ACCESS_TOKENS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.PERSONAL_ACCESS_TOKEN
    }
  }
}

export default {
  nodeId,
  rootNode,
  historyFolderNode,
  historyDeletionNode,
  historyFreezingNode,
  importFolderNode,
  importAllNode,
  reportFolderNode,
  activeUsersReportNode,
  accessFolderNode,
  personalAccessTokensNode,
  TYPE_ROOT,
  TYPE_WARNING,
  TEXT_DYNAMIC_PROPERTY_PLUGINS,
  TEXT_ENTITY_VALIDATION_PLUGINS,
  TEXT_QUERIES
}
