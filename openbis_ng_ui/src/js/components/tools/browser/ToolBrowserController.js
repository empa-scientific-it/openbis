import openbis from '@src/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'
import ImportType from '@src/js/components/tools/form/import/ImportType.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'
import ServerInformation from '@src/js/components/common/dto/ServerInformation.js'
import AppController from '@src/js/components/AppController.js'
import messages from '@src/js/common/messages.js'

export default class ToolBrowserController extends BrowserController {
  doGetPage() {
    return pages.TOOLS
  }

  async doLoadNodes() {
    return Promise.all([
      openbis.searchPlugins(
        new openbis.PluginSearchCriteria(),
        new openbis.PluginFetchOptions()
      ),
      openbis.searchQueries(
        new openbis.QuerySearchCriteria(),
        new openbis.QueryFetchOptions()
      )
    ]).then(([plugins, queries]) => {
      const dynamicPropertyPluginNodes = plugins
        .getObjects()
        .filter(
          plugin => plugin.pluginType === openbis.PluginType.DYNAMIC_PROPERTY
        )
        .map(plugin => {
          return {
            id: `dynamicPropertyPlugin/${plugin.name}`,
            text: plugin.name,
            object: {
              type: objectType.DYNAMIC_PROPERTY_PLUGIN,
              id: plugin.name
            },
            canMatchFilter: true,
            canRemove: true
          }
        })

      const entityValidationPluginNodes = plugins
        .getObjects()
        .filter(
          plugin => plugin.pluginType === openbis.PluginType.ENTITY_VALIDATION
        )
        .map(plugin => {
          return {
            id: `entityValidationPlugin/${plugin.name}`,
            text: plugin.name,
            object: {
              type: objectType.ENTITY_VALIDATION_PLUGIN,
              id: plugin.name
            },
            canMatchFilter: true,
            canRemove: true
          }
        })

      const queryNodes = queries.getObjects().map(query => {
        return {
          id: `query/${query.name}`,
          text: query.name,
          object: {
            type: objectType.QUERY,
            id: query.name
          },
          canMatchFilter: true,
          canRemove: true
        }
      })

      const historyNodes = [
        {
          id: `history/deletion`,
          text: messages.get(messages.DELETION),
          object: {
            type: objectType.HISTORY,
            id: openbis.EventType.DELETION
          },
          canMatchFilter: true,
          canRemove: false
        },
        {
          id: `history/freeze`,
          text: messages.get(messages.FREEZING),
          object: {
            type: objectType.HISTORY,
            id: openbis.EventType.FREEZING
          },
          canMatchFilter: true,
          canRemove: false
        }
      ]

      const importNodes = [
        {
          id: `import/all`,
          text: messages.get(messages.ALL),
          object: {
            type: objectType.IMPORT,
            id: ImportType.ALL
          },
          canMatchFilter: true,
          canRemove: false
        }
      ]

      let nodes = [
        {
          id: 'dynamicPropertyPlugins',
          text: messages.get(messages.DYNAMIC_PROPERTY_PLUGINS),
          object: {
            type: objectType.OVERVIEW,
            id: objectType.DYNAMIC_PROPERTY_PLUGIN
          },
          children: dynamicPropertyPluginNodes,
          childrenType: objectType.NEW_DYNAMIC_PROPERTY_PLUGIN,
          canAdd: true
        },
        {
          id: 'entityValidationPlugins',
          text: messages.get(messages.ENTITY_VALIDATION_PLUGINS),
          object: {
            type: objectType.OVERVIEW,
            id: objectType.ENTITY_VALIDATION_PLUGIN
          },
          children: entityValidationPluginNodes,
          childrenType: objectType.NEW_ENTITY_VALIDATION_PLUGIN,
          canAdd: true
        },
        {
          id: 'queries',
          text: messages.get(messages.QUERIES),
          object: {
            type: objectType.OVERVIEW,
            id: objectType.QUERY
          },
          children: queryNodes,
          childrenType: objectType.NEW_QUERY,
          canAdd: true
        },
        {
          id: 'history',
          text: messages.get(messages.HISTORY),
          children: historyNodes,
          canAdd: false
        },
        {
          id: 'import',
          text: messages.get(messages.IMPORT),
          children: importNodes,
          canAdd: false
        }
      ]

      const personalAccessTokensEnabled =
        AppController.getInstance().getServerInformation(
          ServerInformation.PERSONAL_ACCESS_TOKENS_ENABLED
        )

      if (personalAccessTokensEnabled === 'true') {
        nodes.push({
          id: 'access',
          text: messages.get(messages.ACCESS),
          children: [
            {
              id: 'access/personalAccessTokens',
              text: messages.get(messages.PERSONAL_ACCESS_TOKENS),
              object: {
                type: objectType.OVERVIEW,
                id: objectType.PERSONAL_ACCESS_TOKEN
              },
              canMatchFilter: true,
              canRemove: false
            }
          ],
          canAdd: false
        })
      }

      nodes.push({
        id: 'activeUsersReport',
        text: messages.get(messages.ACTIVE_USERS_REPORT),
        object: {
          type: objectType.ACTIVE_USERS_REPORT,
          id: objectType.ACTIVE_USERS_REPORT
        },
        canAdd: false
      })

      return nodes
    })
  }

  doNodeAdd(node) {
    if (node && node.childrenType) {
      AppController.getInstance().objectNew(this.getPage(), node.childrenType)
    }
  }

  doNodeRemove(node) {
    if (!node.object) {
      return Promise.resolve()
    }

    const { type, id } = node.object
    const reason = 'deleted via ng_ui'

    return this._prepareRemoveOperations(type, id, reason)
      .then(operations => {
        const options = new openbis.SynchronousOperationExecutionOptions()
        options.setExecuteInOrder(true)
        return openbis.executeOperations(operations, options)
      })
      .then(() => {
        AppController.getInstance().objectDelete(this.getPage(), type, id)
      })
      .catch(error => {
        AppController.getInstance().errorChange(error)
      })
  }

  _prepareRemoveOperations(type, id, reason) {
    if (
      type === objectType.DYNAMIC_PROPERTY_PLUGIN ||
      type === objectType.ENTITY_VALIDATION_PLUGIN
    ) {
      return this._prepareRemovePluginOperations(id, reason)
    } else if (type === objectType.QUERY) {
      return this._prepareRemoveQueryOperations(id, reason)
    } else {
      throw new Error('Unsupported type: ' + type)
    }
  }

  _prepareRemovePluginOperations(id, reason) {
    const options = new openbis.PluginDeletionOptions()
    options.setReason(reason)
    return Promise.resolve([
      new openbis.DeletePluginsOperation(
        [new openbis.PluginPermId(id)],
        options
      )
    ])
  }

  _prepareRemoveQueryOperations(id, reason) {
    const options = new openbis.QueryDeletionOptions()
    options.setReason(reason)
    return Promise.resolve([
      new openbis.DeleteQueriesOperation([new openbis.QueryName(id)], options)
    ])
  }

  doGetObservedModifications() {
    return {
      [objectType.DYNAMIC_PROPERTY_PLUGIN]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.ENTITY_VALIDATION_PLUGIN]: [
        objectOperation.CREATE,
        objectOperation.DELETE
      ],
      [objectType.QUERY]: [objectOperation.CREATE, objectOperation.DELETE]
    }
  }
}
