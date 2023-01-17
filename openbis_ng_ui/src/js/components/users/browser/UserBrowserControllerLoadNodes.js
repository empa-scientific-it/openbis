import _ from 'lodash'
import BrowserCommon from '@src/js/components/common/browser/BrowserCommon.js'
import UserBrowserCommon from '@src/js/components/users/browser/UserBrowserCommon.js'
import openbis from '@src/js/services/openbis.js'
import objectType from '@src/js/common/consts/objectType.js'
import compare from '@src/js/common/compare.js'

export default class UserBrowserControllerLoadNodes {
  async doLoadNodes(params) {
    const { node } = params

    const rootNode = BrowserCommon.rootNode()

    if (node.internalRoot) {
      return {
        nodes: [rootNode]
      }
    } else if (node.object.type === rootNode.object.type) {
      if (params.filter) {
        const [users, groups] = await Promise.all([
          this.searchUsers(params),
          this.searchGroups(params)
        ])

        const totalCount = users.totalCount + groups.totalCount

        if (totalCount > UserBrowserCommon.TOTAL_LOAD_LIMIT) {
          return BrowserCommon.tooManyResultsFound(node.id)
        }

        const nodes = []

        if (!_.isEmpty(users.objects)) {
          const folderNode = UserBrowserCommon.usersFolderNode(node.id)
          const usersNodes = this.createNodes(
            folderNode,
            users,
            objectType.USER
          )
          folderNode.children = usersNodes
          folderNode.expanded = true
          nodes.push(folderNode)
        }

        if (!_.isEmpty(groups.objects)) {
          const folderNode = UserBrowserCommon.groupsFolderNode(node.id)
          const groupsNodes = this.createNodes(
            folderNode,
            groups,
            objectType.USER_GROUP
          )
          folderNode.children = groupsNodes
          folderNode.expanded = true
          nodes.push(folderNode)
        }

        return {
          nodes: nodes
        }
      } else {
        return {
          nodes: [
            UserBrowserCommon.usersFolderNode(node.id),
            UserBrowserCommon.groupsFolderNode(node.id)
          ]
        }
      }
    } else if (node.object.type === objectType.OVERVIEW) {
      let objects = null

      if (node.object.id === objectType.USER) {
        objects = await this.searchUsers(params)
      } else if (node.object.id === objectType.USER_GROUP) {
        objects = await this.searchGroups(params)
      }

      const nodes = this.createNodes(node, objects, node.object.id)

      if (!_.isEmpty(nodes)) {
        return nodes
      }
    }

    return {
      nodes: []
    }
  }

  async searchUsers(params) {
    const { filter, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.PersonSearchCriteria()
    if (!_.isNil(filter)) {
      criteria.withUserId().thatContains(filter)
    }
    if (!_.isEmpty(childrenIn)) {
      criteria.withUserIds().thatIn(childrenIn.map(child => child.object.id))
    }
    const fetchOptions = new openbis.PersonFetchOptions()

    let result = await openbis.searchPersons(criteria, fetchOptions)

    if (!_.isEmpty(childrenNotIn)) {
      const childrenNotInMap = {}
      childrenNotIn.forEach(child => {
        childrenNotInMap[child.object.id] = child
      })
      result.objects = result.objects.filter(object =>
        _.isNil(childrenNotInMap[object.getUserId()])
      )
      result.totalCount = result.objects.length
    }

    let objects = result.objects.map(o => ({
      id: o.getUserId(),
      text: o.getUserId()
    }))

    objects.sort((o1, o2) => compare(o1.text, o2.text))

    if (!_.isNil(offset) && !_.isNil(limit)) {
      objects = objects.slice(offset, offset + limit)
    }

    return {
      objects: objects,
      totalCount: result.totalCount,
      offset
    }
  }

  async searchGroups(params) {
    const { filter, offset, limit, childrenIn, childrenNotIn } = params

    const criteria = new openbis.AuthorizationGroupSearchCriteria()
    if (!_.isNil(filter)) {
      criteria.withCode().thatContains(filter)
    }
    if (!_.isEmpty(childrenIn)) {
      criteria.withCodes().thatIn(childrenIn.map(child => child.object.id))
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
      offset
    }
  }

  createNodes(parent, result, objectType) {
    const nodes = result.objects.map(object => ({
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
          limit: UserBrowserCommon.LOAD_LIMIT,
          loadedCount: result.offset + nodes.length,
          totalCount: result.totalCount,
          append: true
        }
      }
    }
  }
}
