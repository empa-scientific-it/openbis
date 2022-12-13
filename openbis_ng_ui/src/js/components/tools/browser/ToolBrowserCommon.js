import BrowserCommon from '@src/js/components/common/browser2/BrowserCommon.js'
import ImportType from '@src/js/components/tools/form/import/ImportType.js'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import objectType from '@src/js/common/consts/objectType.js'

function dynamicPropertyPluginsFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.DYNAMIC_PROPERTY_PLUGIN),
    text: messages.get(messages.DYNAMIC_PROPERTY_PLUGINS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.DYNAMIC_PROPERTY_PLUGIN
    },
    canHaveChildren: true
  }
}

function dynamicPropertyPluginNode(parentId, pluginName) {
  return {
    id: BrowserCommon.nodeId(parentId, pluginName),
    text: pluginName,
    object: {
      type: objectType.DYNAMIC_PROPERTY_PLUGIN,
      id: pluginName
    }
  }
}

function entityValidationPluginsFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.ENTITY_VALIDATION_PLUGIN),
    text: messages.get(messages.ENTITY_VALIDATION_PLUGINS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.ENTITY_VALIDATION_PLUGIN
    },
    canHaveChildren: true
  }
}

function entityValidationPluginNode(parentId, pluginName) {
  return {
    id: BrowserCommon.nodeId(parentId, pluginName),
    text: pluginName,
    object: {
      type: objectType.ENTITY_VALIDATION_PLUGIN,
      id: pluginName
    }
  }
}

function queriesFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.QUERY),
    text: messages.get(messages.QUERIES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.QUERY
    },
    canHaveChildren: true
  }
}

function queryNode(parentId, queryName) {
  return {
    id: BrowserCommon.nodeId(parentId, queryName),
    text: queryName,
    object: {
      type: objectType.QUERY,
      id: queryName
    }
  }
}

function historyFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.HISTORY),
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
    id: BrowserCommon.nodeId(parentId, openbis.EventType.DELETION),
    text: messages.get(messages.DELETION),
    object: {
      type: objectType.HISTORY,
      id: openbis.EventType.DELETION
    }
  }
}

function historyFreezingNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, openbis.EventType.FREEZING),
    text: messages.get(messages.FREEZING),
    object: {
      type: objectType.HISTORY,
      id: openbis.EventType.FREEZING
    }
  }
}

function importFolderNode(parentId) {
  return {
    id: BrowserCommon.nodeId(parentId, objectType.IMPORT),
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
    id: BrowserCommon.nodeId(parentId, ImportType.ALL),
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
    id: BrowserCommon.nodeId(parentId, TYPE_REPORT),
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
    id: BrowserCommon.nodeId(parentId, objectType.ACTIVE_USERS_REPORT),
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
    id: BrowserCommon.nodeId(parentId, TYPE_ACCESS),
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
    id: BrowserCommon.nodeId(parentId, objectType.PERSONAL_ACCESS_TOKEN),
    text: messages.get(messages.PERSONAL_ACCESS_TOKENS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.PERSONAL_ACCESS_TOKEN
    }
  }
}

export default {
  dynamicPropertyPluginsFolderNode,
  dynamicPropertyPluginNode,
  entityValidationPluginsFolderNode,
  entityValidationPluginNode,
  queriesFolderNode,
  queryNode,
  historyFolderNode,
  historyDeletionNode,
  historyFreezingNode,
  importFolderNode,
  importAllNode,
  reportFolderNode,
  activeUsersReportNode,
  accessFolderNode,
  personalAccessTokensNode
}
