import ToolBrowserCommon from '@src/js/components/tools/browser/ToolBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class ToolBrowserControllerLoadNodePath {
  async doLoadNodePath(params) {
    const { object } = params

    if (object.type === objectType.OVERVIEW) {
      if (object.id === objectType.DYNAMIC_PROPERTY_PLUGIN) {
        return [ToolBrowserCommon.dynamicPropertyPluginsFolderNode()]
      } else if (object.id === objectType.ENTITY_VALIDATION_PLUGIN) {
        return [ToolBrowserCommon.entityValidationPluginsFolderNode()]
      } else if (object.id === objectType.QUERY) {
        return [ToolBrowserCommon.queriesFolderNode()]
      } else if (object.id === objectType.PERSONAL_ACCESS_TOKEN) {
        const folderNode = ToolBrowserCommon.accessFolderNode()
        const personalAccessTokensNode =
          ToolBrowserCommon.personalAccessTokensNode()
        return [folderNode, personalAccessTokensNode]
      }
    } else if (object.type === objectType.DYNAMIC_PROPERTY_PLUGIN) {
      const plugin = await this.searchPlugin(object.id)
      if (plugin) {
        const folderNode = ToolBrowserCommon.dynamicPropertyPluginsFolderNode()
        const pluginNode = ToolBrowserCommon.dynamicPropertyPluginNode(
          object.id
        )
        return [folderNode, pluginNode]
      }
    } else if (object.type === objectType.ENTITY_VALIDATION_PLUGIN) {
      const plugin = await this.searchPlugin(object.id)
      if (plugin) {
        const folderNode = ToolBrowserCommon.entityValidationPluginsFolderNode()
        const pluginNode = ToolBrowserCommon.entityValidationPluginNode(
          object.id
        )
        return [folderNode, pluginNode]
      }
    } else if (object.type === objectType.QUERY) {
      const query = await this.searchQuery(object.id)
      if (query) {
        const folderNode = ToolBrowserCommon.queriesFolderNode()
        const queryNode = ToolBrowserCommon.queryNode(object.id)
        return [folderNode, queryNode]
      }
    } else if (object.type === objectType.HISTORY) {
      const folderNode = ToolBrowserCommon.historyFolderNode()
      const deletionNode = ToolBrowserCommon.historyDeletionNode()
      const freezingNode = ToolBrowserCommon.historyFreezingNode()

      if (object.id === deletionNode.object.id) {
        return [folderNode, deletionNode]
      } else if (object.id === freezingNode.object.id) {
        return [folderNode, freezingNode]
      }
    } else if (object.type === objectType.IMPORT) {
      const folderNode = ToolBrowserCommon.importFolderNode()
      const importAllNode = ToolBrowserCommon.importAllNode()
      return [folderNode, importAllNode]
    } else if (object.type === objectType.ACTIVE_USERS_REPORT) {
      const folderNode = ToolBrowserCommon.reportFolderNode()
      const activeUsersReportNode = ToolBrowserCommon.activeUsersReportNode()
      return [folderNode, activeUsersReportNode]
    }

    return null
  }

  async searchPlugin(pluginId) {
    const id = new openbis.PluginPermId(pluginId)
    const fetchOptions = new openbis.PluginFetchOptions()
    const plugins = await openbis.getPlugins([id], fetchOptions)
    return plugins[pluginId]
  }

  async searchQuery(queryName) {
    const id = new openbis.QueryName(queryName)
    const fetchOptions = new openbis.QueryFetchOptions()
    const queries = await openbis.getQueries([id], fetchOptions)
    return queries[queryName]
  }
}
