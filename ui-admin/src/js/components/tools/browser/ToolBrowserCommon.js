import ImportType from '@src/js/components/tools/form/import/ImportType.js'
import openbis from '@src/js/services/openbis.js'
import messages from '@src/js/common/messages.js'
import objectType from '@src/js/common/consts/objectType.js'

const TOTAL_LOAD_LIMIT = 500
const LOAD_LIMIT = 50

function dynamicPropertyPluginsFolderNode() {
  return {
    text: messages.get(messages.DYNAMIC_PROPERTY_PLUGINS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.DYNAMIC_PROPERTY_PLUGIN
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function dynamicPropertyPluginNode(pluginName) {
  return {
    text: pluginName,
    object: {
      type: objectType.DYNAMIC_PROPERTY_PLUGIN,
      id: pluginName
    }
  }
}

function entityValidationPluginsFolderNode() {
  return {
    text: messages.get(messages.ENTITY_VALIDATION_PLUGINS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.ENTITY_VALIDATION_PLUGIN
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function entityValidationPluginNode(pluginName) {
  return {
    text: pluginName,
    object: {
      type: objectType.ENTITY_VALIDATION_PLUGIN,
      id: pluginName
    }
  }
}

function queriesFolderNode() {
  return {
    text: messages.get(messages.QUERIES),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.QUERY
    },
    canHaveChildren: true,
    childrenLoadLimit: LOAD_LIMIT
  }
}

function queryNode(queryName) {
  return {
    text: queryName,
    object: {
      type: objectType.QUERY,
      id: queryName
    }
  }
}

function historyFolderNode() {
  return {
    text: messages.get(messages.HISTORY),
    object: {
      type: objectType.HISTORY,
      id: objectType.HISTORY
    },
    canHaveChildren: true,
    selectable: false
  }
}

function historyDeletionNode() {
  return {
    text: messages.get(messages.DELETION),
    object: {
      type: objectType.HISTORY,
      id: openbis.EventType.DELETION
    }
  }
}

function historyFreezingNode() {
  return {
    text: messages.get(messages.FREEZING),
    object: {
      type: objectType.HISTORY,
      id: openbis.EventType.FREEZING
    }
  }
}

function importFolderNode() {
  return {
    text: messages.get(messages.IMPORT),
    object: {
      type: objectType.IMPORT,
      id: objectType.IMPORT
    },
    canHaveChildren: true,
    selectable: false
  }
}

function importAllNode() {
  return {
    text: messages.get(messages.ALL),
    object: {
      type: objectType.IMPORT,
      id: ImportType.ALL
    }
  }
}

function reportFolderNode() {
  return {
    text: messages.get(messages.REPORT),
    object: {
      type: objectType.REPORT,
      id: objectType.REPORT
    },
    canHaveChildren: true,
    selectable: false
  }
}

function activeUsersReportNode() {
  return {
    text: messages.get(messages.ACTIVE_USERS_REPORT),
    object: {
      type: objectType.ACTIVE_USERS_REPORT,
      id: objectType.ACTIVE_USERS_REPORT
    }
  }
}

function accessFolderNode() {
  return {
    text: messages.get(messages.ACCESS),
    object: {
      type: objectType.ACCESS,
      id: objectType.ACCESS
    },
    canHaveChildren: true,
    selectable: false
  }
}

function personalAccessTokensNode() {
  return {
    text: messages.get(messages.PERSONAL_ACCESS_TOKENS),
    object: {
      type: objectType.OVERVIEW,
      id: objectType.PERSONAL_ACCESS_TOKEN
    }
  }
}

export default {
  TOTAL_LOAD_LIMIT,
  LOAD_LIMIT,
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
