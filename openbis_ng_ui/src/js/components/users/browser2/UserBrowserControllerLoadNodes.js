import _ from 'lodash'
import UserBrowserConsts from '@src/js/components/users/browser2/UserBrowserConsts.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import messages from '@src/js/common/messages.js'
import compare from '@src/js/common/compare.js'

const LOAD_LIMIT = 100
const TOTAL_LOAD_LIMIT = 500

export default class UserBrowserConstsLoadNodesUnfiltered {
  async doLoadNodes(params) {
    const { node } = params

    if (node.internalRoot) {
      return {
        nodes: [
          {
            id: UserBrowserConsts.TYPE_ROOT,
            object: {
              type: UserBrowserConsts.TYPE_ROOT
            },
            canHaveChildren: true
          }
        ]
      }
    } else if (node.object.type === UserBrowserConsts.TYPE_ROOT) {
      const [users, groups] = await Promise.all([
        this.searchUsers(params),
        this.searchGroups(params)
      ])

      if (params.filter) {
        const totalCount = users.totalCount + groups.totalCount

        if (totalCount > TOTAL_LOAD_LIMIT) {
          return this.tooManyResultsFound(node)
        }
      }

      let nodes = [
        this.createUsersNode(node, users),
        this.createGroupsNode(node, groups)
      ]

      if (params.filter) {
        nodes = nodes.filter(node => !_.isEmpty(node.children))
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

  tooManyResultsFound(node) {
    return {
      nodes: [
        {
          id: UserBrowserConsts.nodeId(node.id, UserBrowserConsts.TYPE_WARNING),
          message: {
            type: 'warning',
            text: messages.get(messages.TOO_MANY_FILTERED_RESULTS_FOUND)
          },
          selectable: false
        }
      ]
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
    return this.createFolderNode(
      parent,
      result,
      objectType.USER,
      UserBrowserConsts.TEXT_USERS
    )
  }

  createGroupsNode(parent, result) {
    return this.createFolderNode(
      parent,
      result,
      objectType.USER_GROUP,
      UserBrowserConsts.TEXT_GROUPS
    )
  }

  createFolderNode(parent, result, folderObjectType, folderText) {
    const folderNode = {
      id: UserBrowserConsts.nodeId(parent.id, folderObjectType),
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
      id: UserBrowserConsts.nodeId(parent.id, objectType, object.id),
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
