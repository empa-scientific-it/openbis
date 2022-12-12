import ToolBrowserConsts from '@src/js/components/tools/browser/ToolBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'

export default class ToolBrowserControllerLoadNodePath {
  async doLoadNodePath(params) {
    const { object } = params

    if (object.type === objectType.OVERVIEW) {
      if (object.id === objectType.DYNAMIC_PROPERTY_PLUGIN) {
        return this.createFolderPath(
          objectType.DYNAMIC_PROPERTY_PLUGIN,
          ToolBrowserConsts.TEXT_DYNAMIC_PROPERTY_PLUGINS
        )
      } else if (object.id === objectType.ENTITY_VALIDATION_PLUGIN) {
        return this.createFolderPath(
          objectType.ENTITY_VALIDATION_PLUGIN,
          ToolBrowserConsts.TEXT_ENTITY_VALIDATION_PLUGINS
        )
      } else if (object.id === objectType.QUERY) {
        return this.createFolderPath(
          objectType.QUERY,
          ToolBrowserConsts.TEXT_QUERIES
        )
      } else if (object.id === objectType.PERSONAL_ACCESS_TOKEN) {
        return [
          {
            id: ToolBrowserConsts.nodeId(
              ToolBrowserConsts.TYPE_ROOT,
              ToolBrowserConsts.TYPE_ACCESS
            ),
            object: {
              type: ToolBrowserConsts.TYPE_ACCESS,
              id: ToolBrowserConsts.TYPE_ACCESS
            },
            text: ToolBrowserConsts.TEXT_ACCESS
          },
          {
            id: ToolBrowserConsts.nodeId(
              ToolBrowserConsts.TYPE_ROOT,
              ToolBrowserConsts.TYPE_ACCESS,
              object.id
            ),
            object,
            text: object.id
          }
        ]
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
      return [
        {
          id: ToolBrowserConsts.nodeId(
            ToolBrowserConsts.TYPE_ROOT,
            ToolBrowserConsts.TYPE_HISTORY
          ),
          object: {
            type: ToolBrowserConsts.TYPE_HISTORY,
            id: ToolBrowserConsts.TYPE_HISTORY
          },
          text: ToolBrowserConsts.TEXT_HISTORY
        },
        {
          id: ToolBrowserConsts.nodeId(
            ToolBrowserConsts.TYPE_ROOT,
            ToolBrowserConsts.TYPE_HISTORY,
            object.id
          ),
          object,
          text: object.id
        }
      ]
    } else if (object.type === objectType.IMPORT) {
      return [
        {
          id: ToolBrowserConsts.nodeId(
            ToolBrowserConsts.TYPE_ROOT,
            ToolBrowserConsts.TYPE_IMPORT
          ),
          object: {
            type: ToolBrowserConsts.TYPE_IMPORT,
            id: ToolBrowserConsts.TYPE_IMPORT
          },
          text: ToolBrowserConsts.TEXT_IMPORT
        },
        {
          id: ToolBrowserConsts.nodeId(
            ToolBrowserConsts.TYPE_ROOT,
            ToolBrowserConsts.TYPE_IMPORT,
            object.id
          ),
          object,
          text: object.id
        }
      ]
    } else if (object.type === objectType.ACTIVE_USERS_REPORT) {
      return [
        {
          id: ToolBrowserConsts.nodeId(
            ToolBrowserConsts.TYPE_ROOT,
            ToolBrowserConsts.TYPE_ACTIVE_USERS_REPORT
          ),
          object: {
            type: objectType.ACTIVE_USERS_REPORT,
            id: objectType.ACTIVE_USERS_REPORT
          },
          text: ToolBrowserConsts.TEXT_ACTIVE_USERS_REPORT
        }
      ]
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
