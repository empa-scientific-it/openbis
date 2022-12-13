import AppController from '@src/js/components/AppController.js'
import objectType from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'
import messages from '@src/js/common/messages.js'

const REMOVABLE_OBJECT_TYPES = [objectType.USER, objectType.USER_GROUP]

export default class UserBrowserControllerRemoveNode {
  canRemoveNode(selectedObject) {
    return (
      selectedObject && REMOVABLE_OBJECT_TYPES.includes(selectedObject.type)
    )
  }

  async doRemoveNode(selectedObject) {
    if (!this.canRemoveNode(selectedObject)) {
      return
    }

    const { type, id } = selectedObject
    const reason = 'deleted via ng_ui'

    return this._prepareRemoveOperations(type, id, reason)
      .then(operations => {
        const options = new openbis.SynchronousOperationExecutionOptions()
        options.setExecuteInOrder(true)
        return openbis.executeOperations(operations, options)
      })
      .then(() => {
        AppController.getInstance().objectDelete(pages.USERS, type, id)
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
}
