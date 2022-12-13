import _ from 'lodash'
import BrowserCommon from '@src/js/components/common/browser2/BrowserCommon.js'
import ToolBrowserCommon from '@src/js/components/tools/browser/ToolBrowserCommon.js'
import ServerInformation from '@src/js/components/common/dto/ServerInformation.js'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import compare from '@src/js/common/compare.js'

const LOAD_LIMIT = 100
const TOTAL_LOAD_LIMIT = 500

export default class ToolBrowserControllerLoadNodes {
  async doLoadNodes(params) {
    const { node } = params

    const rootNode = BrowserCommon.rootNode()

    if (node.internalRoot) {
      return {
        nodes: [rootNode]
      }
    } else if (node.object.type === rootNode.object.type) {
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
          return BrowserCommon.tooManyResultsFound(node.id)
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
    const folderNode = ToolBrowserCommon.dynamicPropertyPluginsFolderNode(
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
    const folderNode = ToolBrowserCommon.entityValidationPluginsFolderNode(
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
    const folderNode = ToolBrowserCommon.queriesFolderNode(parent.id)

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

    const folderNode = ToolBrowserCommon.historyFolderNode(parent.id)
    folderNode.children = {
      nodes: [
        ToolBrowserCommon.historyDeletionNode(folderNode.id),
        ToolBrowserCommon.historyFreezingNode(folderNode.id)
      ]
    }

    return folderNode
  }

  createImportNode(parent, params) {
    if (params.filter) {
      return null
    }

    const folderNode = ToolBrowserCommon.importFolderNode(parent.id)
    folderNode.children = {
      nodes: [ToolBrowserCommon.importAllNode(folderNode.id)]
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
      const folderNode = ToolBrowserCommon.accessFolderNode(parent.id)
      folderNode.children = {
        nodes: [ToolBrowserCommon.personalAccessTokensNode(folderNode.id)]
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

    const folderNode = ToolBrowserCommon.reportFolderNode(parent.id)
    folderNode.children = {
      nodes: [ToolBrowserCommon.activeUsersReportNode(folderNode.id)]
    }

    return folderNode
  }

  createNodes(parent, result, objectType) {
    let objects = result.objects
    objects.sort((o1, o2) => compare(o1.text, o2.text))
    objects = objects.slice(result.offset, result.offset + LOAD_LIMIT)

    let nodes = objects.map(object => ({
      id: BrowserCommon.nodeId(parent.id, objectType, object.id),
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
