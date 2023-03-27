import _ from 'lodash'
import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import ToolBrowserCommon from '@src/js/components/tools/browser/ToolBrowserCommon.js'
import ServerInformation from '@src/js/components/common/dto/ServerInformation.js'
import AppController from '@src/js/components/AppController.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import compare from '@src/js/common/compare.js'

export default class ToolBrowserControllerLoadNodes {
  async doLoadNodes(params) {
    const { node } = params

    const rootNode = BrowserCommon.rootNode()

    if (node.internalRoot) {
      return {
        nodes: [rootNode]
      }
    } else if (node.object.type === rootNode.object.type) {
      if (!_.isNil(params.filter)) {
        const [dynamicPropertyPlugins, entityValidationPlugins, queries] =
          await Promise.all([
            this.searchDynamicPropertyPlugins({
              ...params,
              limit: ToolBrowserCommon.LOAD_LIMIT
            }),
            this.searchEntityValidationPlugins({
              ...params,
              limit: ToolBrowserCommon.LOAD_LIMIT
            }),
            this.searchQueries({
              ...params,
              limit: ToolBrowserCommon.LOAD_LIMIT
            })
          ])

        const totalCount =
          dynamicPropertyPlugins.totalCount +
          entityValidationPlugins.totalCount +
          queries.totalCount

        if (totalCount > ToolBrowserCommon.TOTAL_LOAD_LIMIT) {
          return {
            nodes: [BrowserCommon.tooManyResultsFound()]
          }
        }

        const nodes = []

        if (!_.isEmpty(dynamicPropertyPlugins.objects)) {
          const folderNode =
            ToolBrowserCommon.dynamicPropertyPluginsFolderNode()
          const pluginsNodes = this.createNodes(
            dynamicPropertyPlugins,
            objectType.DYNAMIC_PROPERTY_PLUGIN
          )
          folderNode.children = pluginsNodes
          folderNode.expanded = true
          nodes.push(folderNode)
        }

        if (!_.isEmpty(entityValidationPlugins.objects)) {
          const folderNode =
            ToolBrowserCommon.entityValidationPluginsFolderNode()
          const pluginsNodes = this.createNodes(
            entityValidationPlugins,
            objectType.ENTITY_VALIDATION_PLUGIN
          )
          folderNode.children = pluginsNodes
          folderNode.expanded = true
          nodes.push(folderNode)
        }

        if (!_.isEmpty(queries.objects)) {
          const folderNode = ToolBrowserCommon.queriesFolderNode()
          const queriesNodes = this.createNodes(queries, objectType.QUERY)
          folderNode.children = queriesNodes
          folderNode.expanded = true
          nodes.push(folderNode)
        }

        return {
          nodes: nodes
        }
      } else {
        const nodes = []
        nodes.push(ToolBrowserCommon.dynamicPropertyPluginsFolderNode())
        nodes.push(ToolBrowserCommon.entityValidationPluginsFolderNode())
        nodes.push(ToolBrowserCommon.queriesFolderNode())
        nodes.push(ToolBrowserCommon.historyFolderNode())
        nodes.push(ToolBrowserCommon.importFolderNode())

        const personalAccessTokensEnabled =
          AppController.getInstance().getServerInformation(
            ServerInformation.PERSONAL_ACCESS_TOKENS_ENABLED
          )

        if ('true'.equalsIgnoreCase(personalAccessTokensEnabled)) {
          nodes.push(ToolBrowserCommon.accessFolderNode())
        }

        nodes.push(ToolBrowserCommon.reportFolderNode())

        return {
          nodes: nodes
        }
      }
    } else if (node.object.type === objectType.OVERVIEW) {
      let objects = null

      if (node.object.id === objectType.DYNAMIC_PROPERTY_PLUGIN) {
        objects = await this.searchDynamicPropertyPlugins(params)
      } else if (node.object.id === objectType.ENTITY_VALIDATION_PLUGIN) {
        objects = await this.searchEntityValidationPlugins(params)
      } else if (node.object.id === objectType.QUERY) {
        objects = await this.searchQueries(params)
      }

      if (objects) {
        return this.createNodes(objects, node.object.id)
      }
    } else if (node.object.type === objectType.HISTORY) {
      return {
        nodes: [
          ToolBrowserCommon.historyDeletionNode(),
          ToolBrowserCommon.historyFreezingNode()
        ]
      }
    } else if (node.object.type === objectType.IMPORT) {
      return {
        nodes: [ToolBrowserCommon.importAllNode()]
      }
    } else if (node.object.type === objectType.ACCESS) {
      return {
        nodes: [ToolBrowserCommon.personalAccessTokensNode()]
      }
    } else if (node.object.type === objectType.REPORT) {
      return {
        nodes: [ToolBrowserCommon.activeUsersReportNode()]
      }
    }

    return {
      nodes: []
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
    const { filter, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.PluginSearchCriteria()
    criteria.withPluginType().thatEquals(pluginType)
    if (!_.isNil(filter)) {
      criteria.withName().thatContains(filter)
    }
    const fetchOptions = new openbis.PluginFetchOptions()

    const result = await openbis.searchPlugins(criteria, fetchOptions)

    if (!_.isEmpty(childrenIn)) {
      const childrenInMap = {}
      childrenIn.forEach(child => {
        childrenInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(
        object => !_.isNil(childrenInMap[object.getName()])
      )
      result.totalCount = result.objects.length
    }

    if (!_.isEmpty(childrenNotIn)) {
      const childrenNotInMap = {}
      childrenNotIn.forEach(child => {
        childrenNotInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(object =>
        _.isNil(childrenNotInMap[object.getName()])
      )
      result.totalCount = result.objects.length
    }

    let objects = result.objects.map(o => ({
      id: o.getName(),
      text: o.getName()
    }))

    objects.sort((o1, o2) => compare(o1.text, o2.text))

    if (!_.isNil(offset) && !_.isNil(limit)) {
      objects = objects.slice(offset, offset + limit)
    }

    return {
      objects: objects,
      totalCount: result.totalCount
    }
  }

  async searchQueries(params) {
    const { filter, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.QuerySearchCriteria()
    if (!_.isNil(filter)) {
      criteria.withName().thatContains(filter)
    }
    const fetchOptions = new openbis.QueryFetchOptions()

    const result = await openbis.searchQueries(criteria, fetchOptions)

    if (!_.isEmpty(childrenIn)) {
      const childrenInMap = {}
      childrenIn.forEach(child => {
        childrenInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(
        object => !_.isNil(childrenInMap[object.getName()])
      )
      result.totalCount = result.objects.length
    }

    if (!_.isEmpty(childrenNotIn)) {
      const childrenNotInMap = {}
      childrenNotIn.forEach(child => {
        childrenNotInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(object =>
        _.isNil(childrenNotInMap[object.getName()])
      )
      result.totalCount = result.objects.length
    }

    let objects = result.objects.map(o => ({
      id: o.getName(),
      text: o.getName()
    }))

    objects.sort((o1, o2) => compare(o1.text, o2.text))

    if (!_.isNil(offset) && !_.isNil(limit)) {
      objects = objects.slice(offset, offset + limit)
    }

    return {
      objects: objects,
      totalCount: result.totalCount
    }
  }

  createNodes(result, objectType) {
    const nodes = result.objects.map(object => ({
      text: object.text,
      object: {
        type: objectType,
        id: object.id
      }
    }))

    return {
      nodes: nodes,
      totalCount: result.totalCount
    }
  }
}
