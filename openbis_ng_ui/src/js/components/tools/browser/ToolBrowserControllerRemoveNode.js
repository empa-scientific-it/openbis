import AppController from '@src/js/components/AppController.js'
import objectType from '@src/js/common/consts/objectType.js'
import openbis from '@src/js/services/openbis.js'
import pages from '@src/js/common/consts/pages.js'

const REMOVABLE_OBJECT_TYPES = [
  objectType.DYNAMIC_PROPERTY_PLUGIN,
  objectType.ENTITY_VALIDATION_PLUGIN,
  objectType.QUERY
]

export default class ToolBrowserControllerRemoveNode {
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
        AppController.getInstance().objectDelete(pages.TOOLS, type, id)
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
}
