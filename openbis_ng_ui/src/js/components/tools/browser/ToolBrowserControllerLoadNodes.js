import _ from 'lodash'
import ToolBrowserConsts from '@src/js/components/tools/browser/ToolBrowserConsts.js'
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
        nodes: [ToolBrowserConsts.rootNode()]
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
    const folderNode = ToolBrowserConsts.dynamicPropertyPluginsFolderNode(
      parent.id
    )

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        objectType.DYNAMIC_PROPERTY_PLUGIN
      )
    }

    return folderNode
  }

  createEntityValidationPluginsNode(parent, result) {
    const folderNode = ToolBrowserConsts.entityValidationPluginsFolderNode(
      parent.id
    )

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        objectType.ENTITY_VALIDATION_PLUGIN
      )
    }

    return folderNode
  }

  createQueriesNode(parent, result) {
    const folderNode = ToolBrowserConsts.queriesFolderNode(parent.id)

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        objectType.QUERY
      )
    }

    return folderNode
  }

  createHistoryNode(parent, params) {
    if (params.filter) {
      return null
    }

    const folderNode = ToolBrowserConsts.historyFolderNode(parent.id)
    folderNode.children = {
      nodes: [
        ToolBrowserConsts.historyDeletionNode(folderNode.id),
        ToolBrowserConsts.historyFreezingNode(folderNode.id)
      ]
    }

    return folderNode
  }

  createImportNode(parent, params) {
    if (params.filter) {
      return null
    }

    const folderNode = ToolBrowserConsts.importFolderNode(parent.id)
    folderNode.children = {
      nodes: [ToolBrowserConsts.importAllNode(folderNode.id)]
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
      const folderNode = ToolBrowserConsts.accessFolderNode(parent.id)
      folderNode.children = {
        nodes: [ToolBrowserConsts.personalAccessTokensNode(folderNode.id)]
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

    const folderNode = ToolBrowserConsts.reportFolderNode(parent.id)
    folderNode.children = {
      nodes: [ToolBrowserConsts.activeUsersReportNode(folderNode.id)]
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
