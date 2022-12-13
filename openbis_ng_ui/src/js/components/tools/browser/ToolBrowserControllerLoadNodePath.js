import ToolBrowserConsts from '@src/js/components/tools/browser/ToolBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class ToolBrowserControllerLoadNodePath {
  async doLoadNodePath(params) {
    const { object } = params

    const rootNode = ToolBrowserConsts.rootNode()

    if (object.type === objectType.OVERVIEW) {
      if (object.id === objectType.DYNAMIC_PROPERTY_PLUGIN) {
        return [ToolBrowserConsts.dynamicPropertyPluginsFolderNode(rootNode.id)]
      } else if (object.id === objectType.ENTITY_VALIDATION_PLUGIN) {
        return [
          ToolBrowserConsts.entityValidationPluginsFolderNode(rootNode.id)
        ]
      } else if (object.id === objectType.QUERY) {
        return [ToolBrowserConsts.queriesFolderNode(rootNode.id)]
      } else if (object.id === objectType.PERSONAL_ACCESS_TOKEN) {
        const folderNode = ToolBrowserConsts.accessFolderNode(rootNode.id)
        const personalAccessTokensNode =
          ToolBrowserConsts.personalAccessTokensNode(folderNode.id)
        return [folderNode, personalAccessTokensNode]
      }
    } else if (
      object.type === objectType.DYNAMIC_PROPERTY_PLUGIN ||
      object.type === objectType.ENTITY_VALIDATION_PLUGIN
    ) {
      const id = new openbis.PluginPermId(object.id)
      const fetchOptions = new openbis.PluginFetchOptions()

      const plugins = await openbis.getPlugins([id], fetchOptions)
      const plugin = plugins[object.id]

      if (plugin) {
        if (plugin.getPluginType() === openbis.PluginType.DYNAMIC_PROPERTY) {
          return this.createNodePath(
            object,
            plugin,
            objectType.DYNAMIC_PROPERTY_PLUGIN,
            ToolBrowserConsts.TEXT_DYNAMIC_PROPERTY_PLUGINS
          )
        } else if (
          plugin.getPluginType() === openbis.PluginType.ENTITY_VALIDATION
        ) {
          return this.createNodePath(
            object,
            plugin,
            objectType.ENTITY_VALIDATION_PLUGIN,
            ToolBrowserConsts.TEXT_ENTITY_VALIDATION_PLUGINS
          )
        }
      }
    } else if (object.type === objectType.QUERY) {
      const id = new openbis.QueryName(object.id)
      const fetchOptions = new openbis.QueryFetchOptions()

      const queries = await openbis.getQueries([id], fetchOptions)
      const query = queries[object.id]

      return this.createNodePath(
        object,
        query,
        objectType.QUERY,
        ToolBrowserConsts.TEXT_QUERIES
      )
    } else if (object.type === objectType.HISTORY) {
      const folderNode = ToolBrowserConsts.historyFolderNode(rootNode.id)
      const deletionNode = ToolBrowserConsts.historyDeletionNode(folderNode.id)
      const freezingNode = ToolBrowserConsts.historyFreezingNode(folderNode.id)

      if (object.id === deletionNode.object.id) {
        return [folderNode, deletionNode]
      } else if (object.id === freezingNode.object.id) {
        return [folderNode, freezingNode]
      }
    } else if (object.type === objectType.IMPORT) {
      const folderNode = ToolBrowserConsts.importFolderNode(rootNode.id)
      const importAllNode = ToolBrowserConsts.importAllNode(folderNode.id)
      return [folderNode, importAllNode]
    } else if (object.type === objectType.ACTIVE_USERS_REPORT) {
      const folderNode = ToolBrowserConsts.reportFolderNode(rootNode.id)
      const activeUsersReportNode = ToolBrowserConsts.activeUsersReportNode(
        folderNode.id
      )
      return [folderNode, activeUsersReportNode]
    } else {
      return null
    }
  }

  createFolderPath(folderObjectType, folderText) {
    return [
      {
        id: ToolBrowserConsts.nodeId(
          ToolBrowserConsts.TYPE_ROOT,
          folderObjectType
        ),
        object: { type: objectType.OVERVIEW, id: folderObjectType },
        text: folderText
      }
    ]
  }

  createNodePath(object, loadedObject, folderObjectType, folderText) {
    if (loadedObject) {
      const folderPath = this.createFolderPath(folderObjectType, folderText)
      return [
        ...folderPath,
        {
          id: ToolBrowserConsts.nodeId(
            ToolBrowserConsts.TYPE_ROOT,
            folderObjectType,
            folderObjectType,
            object.id
          ),
          object,
          text: object.id
        }
      ]
    } else {
      return null
    }
  }
}
