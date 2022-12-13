import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import ToolBrowserCommon from '@src/js/components/tools/browser/ToolBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class ToolBrowserControllerLoadNodePath {
  async doLoadNodePath(params) {
    const { object } = params

    const rootNode = BrowserCommon.rootNode()

    if (object.type === objectType.OVERVIEW) {
      if (object.id === objectType.DYNAMIC_PROPERTY_PLUGIN) {
        return [ToolBrowserCommon.dynamicPropertyPluginsFolderNode(rootNode.id)]
      } else if (object.id === objectType.ENTITY_VALIDATION_PLUGIN) {
        return [
          ToolBrowserCommon.entityValidationPluginsFolderNode(rootNode.id)
        ]
      } else if (object.id === objectType.QUERY) {
        return [ToolBrowserCommon.queriesFolderNode(rootNode.id)]
      } else if (object.id === objectType.PERSONAL_ACCESS_TOKEN) {
        const folderNode = ToolBrowserCommon.accessFolderNode(rootNode.id)
        const personalAccessTokensNode =
          ToolBrowserCommon.personalAccessTokensNode(folderNode.id)
        return [folderNode, personalAccessTokensNode]
      }
    } else if (object.type === objectType.DYNAMIC_PROPERTY_PLUGIN) {
      const plugin = await this.searchPlugin(object.id)
      if (plugin) {
        const folderNode = ToolBrowserCommon.dynamicPropertyPluginsFolderNode(
          rootNode.id
        )
        const pluginNode = ToolBrowserCommon.dynamicPropertyPluginNode(
          folderNode.id,
          plugin.name
        )
        return [folderNode, pluginNode]
      }
    } else if (object.type === objectType.ENTITY_VALIDATION_PLUGIN) {
      const plugin = await this.searchPlugin(object.id)
      if (plugin) {
        const folderNode = ToolBrowserCommon.entityValidationPluginsFolderNode(
          rootNode.id
        )
        const pluginNode = ToolBrowserCommon.entityValidationPluginNode(
          folderNode.id,
          plugin.name
        )
        return [folderNode, pluginNode]
      }
    } else if (object.type === objectType.QUERY) {
      const query = await this.searchQuery(object.id)
      if (query) {
        const folderNode = ToolBrowserCommon.queriesFolderNode(rootNode.id)
        const queryNode = ToolBrowserCommon.queryNode(folderNode.id, query.name)
        return [folderNode, queryNode]
      }
    } else if (object.type === objectType.HISTORY) {
      const folderNode = ToolBrowserCommon.historyFolderNode(rootNode.id)
      const deletionNode = ToolBrowserCommon.historyDeletionNode(folderNode.id)
      const freezingNode = ToolBrowserCommon.historyFreezingNode(folderNode.id)

      if (object.id === deletionNode.object.id) {
        return [folderNode, deletionNode]
      } else if (object.id === freezingNode.object.id) {
        return [folderNode, freezingNode]
      }
    } else if (object.type === objectType.IMPORT) {
      const folderNode = ToolBrowserCommon.importFolderNode(rootNode.id)
      const importAllNode = ToolBrowserCommon.importAllNode(folderNode.id)
      return [folderNode, importAllNode]
    } else if (object.type === objectType.ACTIVE_USERS_REPORT) {
      const folderNode = ToolBrowserCommon.reportFolderNode(rootNode.id)
      const activeUsersReportNode = ToolBrowserCommon.activeUsersReportNode(
        folderNode.id
      )
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
