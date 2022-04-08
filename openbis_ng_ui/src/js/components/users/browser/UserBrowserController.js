import openbis from '@src/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import objectType from '@src/js/common/consts/objectType.js'
import objectOperation from '@src/js/common/consts/objectOperation.js'
import BrowserController from '@src/js/components/common/browser/BrowserController.js'
import AppController from '@src/js/components/AppController.js'
import messages from '@src/js/common/messages.js'

export default class UserBrowserController extends BrowserController {
  doGetPage() {
    return pages.USERS
  }

  async doLoadNodes() {
    return Promise.all([
      openbis.searchPersons(
        new openbis.PersonSearchCriteria(),
        new openbis.PersonFetchOptions()
      ),
      openbis.searchAuthorizationGroups(
        new openbis.AuthorizationGroupSearchCriteria(),
        new openbis.AuthorizationGroupFetchOptions()
      )
    ]).then(([users, groups]) => {
      const userNodes = users.getObjects().map(user => {
        return {
          id: `users/${user.userId}`,
          text: user.userId,
          object: { type: objectType.USER, id: user.userId },
          canMatchFilter: true,
          canRemove: true
        }
      })

      const groupNodes = groups.getObjects().map(group => {
        return {
          id: `groups/${group.code}`,
          text: group.code,
          object: { type: objectType.USER_GROUP, id: group.code },
          canMatchFilter: true,
          canRemove: true
        }
      })

      let nodes = [
        {
          id: 'users',
          text: messages.get(messages.USERS),
          object: { type: objectType.OVERVIEW, id: objectType.USER },
          children: userNodes,
          childrenType: objectType.NEW_USER,
          canAdd: true
        },
        {
          id: 'groups',
          text: messages.get(messages.GROUPS),
          object: { type: objectType.OVERVIEW, id: objectType.USER_GROUP },
          children: groupNodes,
          childrenType: objectType.NEW_USER_GROUP,
          canAdd: true
        }
      ]

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
        if (
          error &&
          error.message &&
          error.message.startsWith('Could not commit Hibernate transaction')
        ) {
          AppController.getInstance().errorChange(
            messages.get(
              messages.USERS_WHO_REGISTERED_SOME_DATA_CANNOT_BE_REMOVED
            )
          )
        } else {
          AppController.getInstance().errorChange(error)
        }
      })
  }

  _prepareRemoveOperations(type, id, reason) {
    if (type === objectType.USER_GROUP) {
      return this._prepareRemoveUserGroupOperations(id, reason)
    } else if (type === objectType.USER) {
      return this._prepareRemoveUserOperations(id, reason)
    } else {
      throw new Error('Unsupported type: ' + type)
    }
  }

  _prepareRemoveUserGroupOperations(id, reason) {
    const options = new openbis.AuthorizationGroupDeletionOptions()
    options.setReason(reason)
    return Promise.resolve([
      new openbis.DeleteAuthorizationGroupsOperation(
        [new openbis.AuthorizationGroupPermId(id)],
        options
      )
    ])
  }

  _prepareRemoveUserOperations(id, reason) {
    const options = new openbis.PersonDeletionOptions()
    options.setReason(reason)
    return Promise.resolve([
      new openbis.DeletePersonsOperation(
        [new openbis.PersonPermId(id)],
        options
      )
    ])
  }

  doGetObservedModifications() {
    return {
      [objectType.USER]: [objectOperation.CREATE, objectOperation.DELETE],
      [objectType.USER_GROUP]: [objectOperation.CREATE, objectOperation.DELETE]
    }
  }
}
