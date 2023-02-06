import AppController from '@src/js/components/AppController.js'
import objectType from '@src/js/common/consts/objectType.js'
import pages from '@src/js/common/consts/pages.js'

const NEW_OBJECT_TYPES = {
  [objectType.DYNAMIC_PROPERTY_PLUGIN]: objectType.NEW_DYNAMIC_PROPERTY_PLUGIN,
  [objectType.ENTITY_VALIDATION_PLUGIN]:
    objectType.NEW_ENTITY_VALIDATION_PLUGIN,
  [objectType.QUERY]: objectType.NEW_QUERY
}

export default class ToolBrowserControllerAddNode {
  canAddNode(selectedObject) {
    return (
      selectedObject &&
      selectedObject.type === objectType.OVERVIEW &&
      NEW_OBJECT_TYPES[selectedObject.id]
    )
  }

  async doAddNode(selectedObject) {
    if (!this.canAddNode(selectedObject)) {
      return
    }
    await AppController.getInstance().objectNew(
      pages.TOOLS,
      NEW_OBJECT_TYPES[selectedObject.id]
    )
  }
}
