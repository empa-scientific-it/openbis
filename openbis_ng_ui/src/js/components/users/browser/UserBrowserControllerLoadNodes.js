import _ from 'lodash'
import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import UserBrowserCommon from '@src/js/components/users/browser/UserBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import compare from '@src/js/common/compare.js'

const LOAD_LIMIT = 100
const TOTAL_LOAD_LIMIT = 500

export default class UserBrowserControllerLoadNodes {
  async doLoadNodes(params) {
    const { node } = params

    const rootNode = BrowserCommon.rootNode()

    if (node.internalRoot) {
      return {
        nodes: [rootNode]
      }
    } else if (node.object.type === rootNode.object.type) {
      const [users, groups] = await Promise.all([
        this.searchUsers(params),
        this.searchGroups(params)
      ])

      if (params.filter) {
        const totalCount = users.totalCount + groups.totalCount

        if (totalCount > TOTAL_LOAD_LIMIT) {
          return BrowserCommon.tooManyResultsFound(node.id)
        }
      }

      let nodes = [
        this.createUsersNode(node, users),
        this.createGroupsNode(node, groups)
      ]

      if (params.filter) {
        nodes = nodes.filter(node => !_.isEmpty(node.children))
        nodes.forEach(node => {
          node.expanded = true
        })
      }

      return {
        nodes: nodes
      }
    } else if (node.object.type === objectType.OVERVIEW) {
      let types = null

      if (node.object.id === objectType.USER) {
        types = await this.searchUsers(params)
      } else if (node.object.id === objectType.USER_GROUP) {
        types = await this.searchGroups(params)
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

  async searchUsers(params) {
    const { filter, offset } = params

    const criteria = new openbis.PersonSearchCriteria()
    if (filter) {
      criteria.withUserId().thatContains(filter)
    }
    const fetchOptions = new openbis.PersonFetchOptions()

    const result = await openbis.searchPersons(criteria, fetchOptions)

    return {
      objects: result.getObjects().map(o => ({
        id: o.getUserId(),
        text: o.getUserId()
      })),
      totalCount: result.getTotalCount(),
      filter,
      offset
    }
  }

  async searchGroups(params) {
    const { filter, offset } = params

    const criteria = new openbis.AuthorizationGroupSearchCriteria()
    if (filter) {
      criteria.withCode().thatContains(filter)
    }
    const fetchOptions = new openbis.AuthorizationGroupFetchOptions()

    const result = await openbis.searchAuthorizationGroups(
      criteria,
      fetchOptions
    )

    return {
      objects: result.getObjects().map(o => ({
        id: o.getCode(),
        text: o.getCode()
      })),
      totalCount: result.getTotalCount(),
      filter,
      offset
    }
  }

  createUsersNode(parent, result) {
    const folderNode = UserBrowserCommon.usersFolderNode(parent.id)

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        objectType.USER
      )
    }

    return folderNode
  }

  createGroupsNode(parent, result) {
    const folderNode = UserBrowserCommon.groupsFolderNode(parent.id)

    if (result) {
      folderNode.children = this.createNodes(
        folderNode,
        result,
        objectType.USER_GROUP
      )
    }

    return folderNode
  }

  createNodes(parent, result, objectType) {
    let objects = result.objects
    objects.sort((o1, o2) => compare(o1.text, o2.text))
    objects = objects.slice(result.offset, result.offset + LOAD_LIMIT)

    let nodes = objects.map(object => ({
      id: BrowserCommon.nodeId(parent.id, object.id),
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
