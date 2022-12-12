import _ from 'lodash'
import ImportType from '@src/js/components/tools/form/import/ImportType.js'
import ToolBrowserConsts from '@src/js/components/tools/browser2/ToolBrowserConsts.js'
import ServerInformation from '@src/js/components/common/dto/ServerInformation.js'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'
import compare from '@src/js/common/compare.js'

const LOAD_LIMIT = 100
const TOTAL_LOAD_LIMIT = 500

export default class ToolBrowserControllerLoadNodes {
  async doLoadNodes(params) {
    const { node } = params

    if (node.internalRoot) {
      return {
        nodes: [
          {
            id: ToolBrowserConsts.TYPE_ROOT,
            object: {
              type: ToolBrowserConsts.TYPE_ROOT
            },
            canHaveChildren: true
          }
        ]
      }
    } else if (node.object.type === ToolBrowserConsts.TYPE_ROOT) {
      const [dynamicPropertyPlugins, entityValidationPlugins, queries] =
        await Promise.all([
          this.searchDynamicPropertyPlugins(params),
          this.searchEntityValidationPlugins(params),
          this.searchQueries(params)
        ])

      if (params.filter) {
        const totalCount =
          dynamicPropertyPlugins.totalCount +
          entityValidationPlugins.totalCount +
          queries.totalCount

        if (totalCount > TOTAL_LOAD_LIMIT) {
          return this.tooManyResultsFound(node)
        }
      }

      let nodes = [
        this.createDynamicPropertyPluginsNode(node, dynamicPropertyPlugins),
        this.createEntityValidationPluginsNode(node, entityValidationPlugins),
        this.createQueriesNode(node, queries),
        this.createHistoryNode(node, params),
        this.createImportNode(node, params),
        this.createAccessNode(node, params),
        this.createActiveUsersReportNode(node, params)
      ]

      nodes = nodes.filter(node => !!node)

      if (params.filter) {
        nodes = nodes.filter(node => !_.isEmpty(node.children))
      }

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.OVERVIEW) {
      let types = null

      if (node.object.id === objectType.DYNAMIC_PROPERTY_PLUGIN) {
        types = await this.searchDynamicPropertyPlugins(params)
      } else if (node.object.id === objectType.ENTITY_VALIDATION_PLUGIN) {
        types = await this.searchEntityValidationPlugins(params)
      } else if (node.object.id === objectType.QUERY) {
        types = await this.searchQueries(params)
      }

      if (types) {
        return this.createNodes(node, types, node.object.id)
      } else {
        return {
          nodes: []
        }
      }
    } else {
      return null
    }
  }

  tooManyResultsFound(node) {
    return {
      nodes: [
        {
          id: ToolBrowserConsts.nodeId(node.id, ToolBrowserConsts.TYPE_WARNING),
          message: {
            type: 'warning',
            text: messages.get(messages.TOO_MANY_FILTERED_RESULTS_FOUND)
          },
          selectable: false
        }
      ]
    }
  }

  async searchDynamicPropertyPlugins(params) {
    return await this.searchPlugins(params, openbis.PluginType.DYNAMIC_PROPERTY)
  }

  async searchEntityValidationPlugins(params) {
    return await this.searchPlugins(
      params,
      openbis.PluginType.ENTITY_VALIDATION
    )
  }

  async searchPlugins(params, pluginType) {
    const { filter, offset } = params

    const criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(pluginType)
    if (filter) {
      criteria.withName().thatContains(filter)
    }
    const fetchOptions = new openbis.PluginFetchOptions()

    const result = await openbis.searchPlugins(criteria, fetchOptions)

    return {
      objects: result.getObjects().map(o => ({
        id: o.getName(),
        text: o.getName()
      })),
      totalCount: result.getTotalCount(),
      filter,
      offset
    }
  }

  async searchQueries(params) {
    const { filter, offset } = params

    const criteria = new openbis.QuerySearchCriteria()
    if (filter) {
      criteria.withName().thatContains(filter)
    }
    const fetchOptions = new openbis.QueryFetchOptions()

    const result = await openbis.searchQueries(criteria, fetchOptions)

    return {
      objects: result.getObjects().map(o => ({
        id: o.getName(),
        text: o.getName()
      })),
      totalCount: result.getTotalCount(),
      filter,
      offset
    }
  }

  createDynamicPropertyPluginsNode(parent, result) {
    return this.createFolderNode(
      parent,
      result,
      objectType.DYNAMIC_PROPERTY_PLUGIN,
      ToolBrowserConsts.TEXT_DYNAMIC_PROPERTY_PLUGINS
    )
  }

  createEntityValidationPluginsNode(parent, result) {
    return this.createFolderNode(
      parent,
      result,
      objectType.ENTITY_VALIDATION_PLUGIN,
      ToolBrowserConsts.TEXT_ENTITY_VALIDATION_PLUGINS
    )
  }

  createQueriesNode(parent, result) {
    return this.createFolderNode(
      parent,
      result,
      objectType.QUERY,
      ToolBrowserConsts.TEXT_QUERIES
    )
  }

  createHistoryNode(parent, params) {
    if (params.filter) {
      return null
    }

    const folderNode = {
      id: ToolBrowserConsts.nodeId(parent.id, ToolBrowserConsts.TYPE_HISTORY),
      text: ToolBrowserConsts.TEXT_HISTORY,
      object: {
        type: ToolBrowserConsts.TYPE_HISTORY,
        id: ToolBrowserConsts.TYPE_HISTORY
      },
      canHaveChildren: true,
      selectable: false,
      children: {
        nodes: [
          {
            id: ToolBrowserConsts.nodeId(
              parent.id,
              ToolBrowserConsts.TYPE_HISTORY,
              openbis.EventType.DELETION
            ),
            text: messages.get(messages.DELETION),
            object: {
              type: objectType.HISTORY,
              id: openbis.EventType.DELETION
            }
          },
          {
            id: ToolBrowserConsts.nodeId(
              parent.id,
              ToolBrowserConsts.TYPE_HISTORY,
              openbis.EventType.FREEZING
            ),
            text: messages.get(messages.FREEZING),
            object: {
              type: objectType.HISTORY,
              id: openbis.EventType.FREEZING
            }
          }
        ]
      }
    }

    return folderNode
  }

  createImportNode(parent, params) {
    if (params.filter) {
      return null
    }

    const folderNode = {
      id: ToolBrowserConsts.nodeId(parent.id, ToolBrowserConsts.TYPE_IMPORT),
      text: ToolBrowserConsts.TEXT_IMPORT,
      object: {
        type: ToolBrowserConsts.TYPE_IMPORT,
        id: ToolBrowserConsts.TYPE_IMPORT
      },
      canHaveChildren: true,
      selectable: false,
      children: {
        nodes: [
          {
            id: ToolBrowserConsts.nodeId(
              parent.id,
              ToolBrowserConsts.TYPE_IMPORT,
              ImportType.ALL
            ),
            text: messages.get(messages.ALL),
            object: {
              type: objectType.IMPORT,
              id: ImportType.ALL
            }
          }
        ]
      }
    }

    return folderNode
  }

  createAccessNode(parent, params) {
    if (params.filter) {
      return null
    }

    const personalAccessTokensEnabled =
      AppController.getInstance().getServerInformation(
        ServerInformation.PERSONAL_ACCESS_TOKENS_ENABLED
      )

    if (personalAccessTokensEnabled === 'true') {
      const folderNode = {
        id: ToolBrowserConsts.nodeId(parent.id, ToolBrowserConsts.TYPE_ACCESS),
        text: ToolBrowserConsts.TEXT_ACCESS,
        object: {
          type: ToolBrowserConsts.TYPE_ACCESS,
          id: ToolBrowserConsts.TYPE_ACCESS
        },
        canHaveChildren: true,
        selectable: false,
        children: {
          nodes: [
            {
              id: ToolBrowserConsts.nodeId(
                parent.id,
                ToolBrowserConsts.TYPE_ACCESS,
                objectType.PERSONAL_ACCESS_TOKEN
              ),
              text: messages.get(messages.PERSONAL_ACCESS_TOKENS),
              object: {
                type: objectType.OVERVIEW,
                id: objectType.PERSONAL_ACCESS_TOKEN
              }
            }
          ]
        }
      }

      return folderNode
    } else {
      return null
    }
  }

  createActiveUsersReportNode(parent, params) {
    if (params.filter) {
      return null
    }

    const node = {
      id: ToolBrowserConsts.nodeId(
        parent.id,
        ToolBrowserConsts.TYPE_ACTIVE_USERS_REPORT
      ),
      text: ToolBrowserConsts.TEXT_ACTIVE_USERS_REPORT,
      object: {
        type: objectType.ACTIVE_USERS_REPORT,
        id: objectType.ACTIVE_USERS_REPORT
      }
    }

    return node
  }

  createFolderNode(parent, result, folderObjectType, folderText) {
    const folderNode = {
      id: ToolBrowserConsts.nodeId(parent.id, folderObjectType),
      text: folderText,
      object: {
        type: objectType.OVERVIEW,
        id: folderObjectType
      },
      canHaveChildren: !!result,
      selectable: true,
      expanded: result && result.filter
    }

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        folderObjectType
      )
    }

    return folderNode
  }

  createNodes(parent, result, objectType) {
    let objects = result.objects
    objects.sort((o1, o2) => compare(o1.text, o2.text))
    objects = objects.slice(result.offset, result.offset + LOAD_LIMIT)

    let nodes = objects.map(object => ({
      id: ToolBrowserConsts.nodeId(parent.id, objectType, object.id),
      text: object.text,
      object: {
        type: objectType,
        id: object.id
      }
    }))

    if (_.isEmpty(nodes)) {
      return null
    } else {
      return {
        nodes: nodes,
        loadMore: {
          offset: result.offset + nodes.length,
          loadedCount: result.offset + nodes.length,
          totalCount: result.totalCount,
          append: true
        }
      }
    }
  }
}
